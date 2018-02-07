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

const log = require('./migration-utils').log;
const getClusterForMigration = require('./migration-utils').getClusterForMigration;

function linkCacheToCluster(clusterModel, cluster, cachesModel, cache) {
    return clusterModel.update({_id: cluster._id}, {$addToSet: {caches: cache._id}}).exec()
        .then(() => cachesModel.update({_id: cache._id}, {clusters: [cluster._id]}).exec())
        .catch((err) => log(`Failed link cache to cluster [cache=${cache.name}, cluster=${cluster.name}, err=${err}]`));
}

function cloneCache(clusterModel, cachesModel, cache) {
    const cacheId = cache._id;
    const clusters = cache.clusters;

    delete cache._id;
    cache.clusters = [];

    if (cache.cacheStoreFactory && cache.cacheStoreFactory.kind === null)
        delete cache.cacheStoreFactory.kind;

    let dup = 1;

    return Promise.all(_.map(clusters, (cluster, idx) => {
        cache.clusters = [cluster];

        if (idx > 0) {
            return cachesModel.create(cache)
                .catch((err) => {
                    if (err.code === 11000) {
                        cache.name += dup.toString();

                        dup++;

                        return cachesModel.create(cache);
                    }

                    throw err;
                })
                .then((clone) => clusterModel.update({_id: {$in: cache.clusters}}, {$addToSet: {caches: clone._id}}, {multi: true}).exec())
                .then(() => clusterModel.update({_id: {$in: cache.clusters}}, {$pull: {caches: cacheId}}, {multi: true}).exec())
                .catch((err) => log(`Failed to clone cache: id=${cacheId}, name=${cache.name}, err=${err}`));
        }

        return cachesModel.update({_id: cacheId}, {clusters: [cluster]}).exec();

        // TODO IGNITE-5737 domainsModel.update({caches: {$in: ids}}, {$pull: {caches: {$in: ids}}}, {multi: true}).exec())
    }));
}

function migrateCache(clusterModel, cachesModel, cache) {
    const len = _.size(cache.clusters);

    if (len < 1) {
        log(`Found cache not linked to cluster [cache=${cache.name}]`);

        return getClusterForMigration(clusterModel, cache.space)
            .then((clusterLostFound) => linkCacheToCluster(clusterModel, clusterLostFound, cachesModel, cache));
    }

    if (len > 1) {
        log(`Found cache linked to many clusters [cache=${cache.name}, cnt=${len}]`);

        return cloneCache(clusterModel, cachesModel, cache);
    }

    // Nothing to migrate, cache linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateCaches(clusterModel, cachesModel) {
    log('Caches migration started...');

    return cachesModel.find({}).lean().exec()
        .then((caches) => {
            log(`Caches to migrate: ${_.size(caches)}`);

            return Promise.all(_.map(caches, (cache) => migrateCache(clusterModel, cachesModel, cache)))
                .then(() => log('Caches migration finished.'));
        })
        .catch((err) => log('Caches migration failed: ' + err));
}

function linkIgfsToCluster(clusterModel, cluster, igfsModel, igfs) {
    return clusterModel.update({_id: cluster._id}, {$addToSet: {igfss: igfs._id}}).exec()
        .then(() => igfsModel.update({_id: igfs._id}, {clusters: [cluster._id]}).exec())
        .catch((err) => log(`Failed link IGFS to cluster [IGFS=${igfs.name}, cluster=${cluster.name}, err=${err}]`));
}

function cloneIgfs(clusterModel, igfsModel, igfs) {
    const igfsId = igfs._id;
    const clusters = igfs.clusters;

    delete igfs._id;
    igfs.clusters = [];

    return Promise.all(_.map(clusters, (cluster, idx) => {
        igfs.clusters = [cluster];

        if (idx > 0) {
            return igfsModel.create(igfs)
                .then((clone) => clusterModel.update({_id: {$in: igfs.clusters}}, {$addToSet: {igfss: clone._id}}, {multi: true}).exec())
                .then(() => clusterModel.update({_id: {$in: igfs.clusters}}, {$pull: {igfss: igfsId}}, {multi: true}).exec())
                .catch((err) => log(`Failed to clone IGFS: id=${igfsId}, name=${igfs.name}, err=${err}`));
        }

        return igfsModel.update({_id: igfsId}, {clusters: [cluster]}).exec();
    }));
}

function migrateIgfs(clusterModel, igfsModel, igfs) {
    const len = _.size(igfs.clusters);

    if (len < 1) {
        log(`Found IGFS not linked to cluster [IGFS=${igfs.name}]`);

        return getClusterForMigration(clusterModel, igfs.space)
            .then((clusterLostFound) => linkIgfsToCluster(clusterModel, clusterLostFound, igfsModel, igfs));
    }

    if (len > 1) {
        log(`Found IGFS linked to many clusters [IGFS=${igfs.name}, cnt=${len}]`);

        return cloneIgfs(clusterModel, igfsModel, igfs);
    }

    // Nothing to migrate, IGFS linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateIgfss(clusterModel, igfsModel) {
    log('IGFS migration started...');

    return igfsModel.find({}).lean().exec()
        .then((igfss) => {
            log(`IGFS to migrate: ${_.size(igfss)}`);

            return Promise.all(_.map(igfss, (igfs) => migrateIgfs(clusterModel, igfsModel, igfs)))
                .then(() => log('IGFS migration finished.'));
        })
        .catch((err) => log('IGFS migration failed: ' + err));
}

function migrateModels(cachesModel, domainsModel) {
    return Promise.resolve(true); // TODO
}

exports.up = function up(done) {
    const clustersModel = this('Cluster');
    const cachesModel = this('Cache');
    const domainsModel = this('DomainModel');
    const igfsModel = this('Igfs');

    migrateCaches(clustersModel, cachesModel)
        .then(() => migrateModels(cachesModel, domainsModel))
        .then(() => migrateIgfss(clustersModel, igfsModel))
        .then(() => done())
        .catch(done);
};

exports.down = function down(done) {
    log('Model migration can not be reverted');

    done();
};
