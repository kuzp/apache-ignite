/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const _ = require('lodash');

const LOST_FOUND = 'LOST+FOUND';

function _log(msg) {
    console.log(`[${new Date().toISOString()}] ${msg}`);
}

function getClusterForMigration(clusterModel, space) {
    return clusterModel.findOne({name: LOST_FOUND}).lean().exec()
        .then((cluster) => {
            if (!cluster) {
                return clusterModel.create({
                    space,
                    name: LOST_FOUND,
                    connector: {noDelay: true},
                    communication: {tcpNoDelay: true},
                    igfss: [],
                    caches: [],
                    binaryConfiguration: {
                        compactFooter: true,
                        typeConfigurations: []
                    },
                    discovery: {
                        kind: 'Multicast',
                        Multicast: {addresses: ['127.0.0.1:47500..47510']},
                        Vm: {addresses: ['127.0.0.1:47500..47510']}
                    }
                })
                    .catch((err) => {
                        if (err.code === 11000)
                            return clusterModel.findOne({name: LOST_FOUND}).lean().exec();

                        throw err;
                    });
            }

            return cluster;
        });
}

function linkCacheToCluster(clusterModel, clusterId, cachesModel, cacheId) {
    return clusterModel.update({_id: clusterId}, {$addToSet: {caches: cacheId}}).exec()
        .then(() => cachesModel.update({_id: cacheId}, {clusters: [clusterId]}).exec());
}

function cloneCache(clusterModel, cachesModel, cache) {
    const cacheId = cache._id;
    const clusters = cache.clusters;

    delete cache._id;
    cache.clusters = [];

    return Promise.all(_.map(clusters, (cluster, idx) => {
        cache.clusters = [cluster];

        if (idx > 0) {
            return cachesModel.create(cache)
                .then((clone) => clusterModel.update({_id: {$in: cache.clusters}}, {$addToSet: {caches: clone._id}}, {multi: true}).exec())
                .then(() => clusterModel.update({_id: {$in: cache.clusters}}, {$pull: {caches: cacheId}}, {multi: true}).exec())
                .catch((err) => {
                    _log(`Failed to clone cache: id=${cacheId}, name=${cache.name}, err=${err}`);
                });
        }

        return cachesModel.update({_id: cacheId}, {clusters: [cluster]}).exec();

        // TODO IGNITE-5737 domainsModel.update({caches: {$in: ids}}, {$pull: {caches: {$in: ids}}}, {multi: true}).exec())
    }));
}

function migrateCache(clusterModel, cachesModel, cache) {
    const len = _.size(cache.clusters);

    if (len < 1) {
        _log(`${LOST_FOUND}: cache=${cache.name}`);

        return getClusterForMigration(clusterModel, cache.space)
            .then((clusterLostFound) => linkCacheToCluster(clusterModel, clusterLostFound._id, cachesModel, cache._id));
    }

    if (len > 1) {
        _log(`One-to-many links found: n=${len}, cache=${cache.name}`);

        return cloneCache(clusterModel, cachesModel, cache);
    }

    // Nothing to migrate, cache linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateCaches(clusterModel, cachesModel) {
    _log('Caches migration started...');

    return cachesModel.find({}).lean().exec()
        .then((caches) => {
            _log(`Caches to migrate: ${_.size(caches)}`);

            return Promise.all(_.map(caches, (cache) => migrateCache(clusterModel, cachesModel, cache)))
                .then(() => _log('Caches migration finished.'));
        })
        .catch((err) => _log('Caches migration failed: ' + err));
}

function linkIgfsToCluster(clusterModel, clusterId, igfsModel, igfsId) {
    return clusterModel.update({_id: clusterId}, {$addToSet: {igfss: igfsId}}).exec()
        .then(() => igfsModel.update({_id: igfsId}, {clusters: [clusterId]}).exec());
}

function cloneIgfs(clusterModel, igfsModel, igfs) {
    const igfsId = igfs._id;
    const clusters = igfs.clusters;

    delete igfs._id;
    igfs.clusters = [];

    return Promise.all(clusters, (cluster, idx) => {
        igfs.clusters = [cluster];

        if (idx > 0) {
            const clone = igfsModel.create(igfs);

            return clusterModel.update({_id: {$in: igfs.clusters}}, {$addToSet: {igfss: clone._id}}, {multi: true}).exec()
                .then(() => clusterModel.update({_id: {$in: igfs.clusters}}, {$pull: {igfs: igfsId}}, {multi: true}).exec());
        }

        return igfsModel.update({_id: igfsId}, {clusters: [cluster]}).exec();
    });
}

function migrateIgfs(clusterModel, igfsModel, igfs) {
    const len = _.size(igfs.clusters);

    if (len < 1) {
        _log(`${LOST_FOUND}: IGFS=${igfs.name}`);

        return getClusterForMigration(clusterModel, igfs.space)
            .then((clusterLostFound) => linkIgfsToCluster(clusterModel, clusterLostFound._id, igfsModel, igfs._id));
    }

    if (len > 1) {
        _log(`One-to-many links found: n=${len}, IGFS=${igfs.name}`);

        return cloneIgfs(clusterModel, igfsModel, igfs);
    }

    // Nothing to migrate, IGFS linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateIgfss(clusterModel, igfsModel) {
    _log('IGFS migration started...');

    return igfsModel.find({}).lean().exec()
        .then((igfss) => {
            _log(`IGFS to migrate: ${_.size(igfss)}`);

            return Promise.all(_.map(igfss, (igfs) => migrateIgfs(clusterModel, igfsModel, igfs)))
                .then(() => _log('IGFS migration finished.'));
        })
        .catch((err) => _log('IGFS migration failed: ' + err));
}

exports.up = function up(done) {
    const clustersModel = this('Cluster');
    const cachesModel = this('Cache');
    const igfsModel = this('Igfs');

    migrateCaches(clustersModel, cachesModel)
        .then(() => migrateIgfss(clustersModel, igfsModel))
        .then(() => done())
        .catch(done);
};

exports.down = function down(done) {
    _log('Model migration can not be reverted');

    done();
};
