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

function getClusterForMigration(clusterModel, spaceIds) {
    return clusterModel.findOne({space: spaceIds, name: 'LOST+FOUND'}).lean().exec()
        .then((cluster) => {
            if (!cluster) {
                return clusterModel.create({
                    space: spaceIds,
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
                        Multicast: { addresses: ['127.0.0.1:47500..47510'] },
                        Vm: { addresses: ['127.0.0.1:47500..47510'] }
                    }
                });
            }

            return cluster;
        });
}

function linkCacheToCluster(clusterModel, clusterId, cachesModel, cacheId) {
    return clusterModel.update({_id: clusterId}, {$addToSet: {caches: cacheId}}).exec()
        .then(() => cachesModel.update({_id: cacheId}, {clusters: [clusterId]}).exec());
}

async function cloneCache(clusterModel, cachesModel, cache) {
    const cacheId = cache._id;
    const clusters = cache.clusters;

    delete cache._id;
    cache.clusters = [];

    _.forEach(clusters, async(cluster, idx) => {
        cache.clusters = [cluster];

        if (idx > 0) {
            const clone = await cachesModel.create(cache);

            await clusterModel.update({_id: {$in: cache.clusters}}, {$addToSet: {caches: clone._id}}, {multi: true}).exec();
            await clusterModel.update({_id: {$in: cache.clusters}}, {$pull: {caches: cacheId}}, {multi: true}).exec();
        }
        else
            await cachesModel.update({_id: cacheId}, {clusters: [cluster]}).exec();

    });

    // TODO await domainsModel.update({caches: {$in: ids}}, {$pull: {caches: {$in: ids}}}, {multi: true}).exec())
}

async function migrateCaches(clusterModel, cachesModel) {
    console.log('Caches migration started...');

    try {
        const caches = await cachesModel.find({}).lean().exec();

        let clusterLostFound = null;

        _.forEach(caches, async(cache) => {
            const len = _.size(cache.clusters);

            switch (len) {
                case 0:
                    console.log(`${LOST_FOUND}: ${cache.name}`);

                    if (_.isNil(clusterLostFound))
                        clusterLostFound = await getClusterForMigration(clusterModel, cache.space);

                    await linkCacheToCluster(clusterModel, clusterLostFound._id, cachesModel, cache._id);

                    break;

                case 1:
                    console.log(`Nothing to migrate: ${cache.name}`);
                    break;

                default:
                    console.log(`One-to-many links found: n= ${len}, name= ${cache.name}`);

                    await cloneCache(clusterModel, cachesModel, cache);
            }
        });

        console.log('Caches migration finished.');
    }
    catch (err) {
        console.log('Caches migration failed!', err);
    }
}

function linkIgfsToCluster(clusterModel, clusterId, igfsModel, igfsId) {
    return clusterModel.update({_id: clusterId}, {$addToSet: {igfss: igfsId}}).exec()
        .then(() => igfsModel.update({_id: igfsId}, {clusters: [clusterId]}).exec());
}

async function cloneIgfs(clusterModel, igfsModel, igfs) {
    const igfsId = igfs._id;
    const clusters = igfs.clusters;

    delete igfs._id;
    igfs.clusters = [];

    _.forEach(clusters, async(cluster, idx) => {
        igfs.clusters = [cluster];

        if (idx > 0) {
            const clone = await igfsModel.create(igfs);

            await clusterModel.update({_id: {$in: igfs.clusters}}, {$addToSet: {igfss: clone._id}}, {multi: true}).exec();
            await clusterModel.update({_id: {$in: igfs.clusters}}, {$pull: {igfs: igfsId}}, {multi: true}).exec();
        }
        else
            await igfsModel.update({_id: igfsId}, {clusters: [cluster]}).exec();

    });
}

async function migrateIgfs(clusterModel, igfsModel) {
    console.log('IGFS migration started...');

    try {
        const igfss = await igfsModel.find({}).lean().exec();

        let clusterLostFound = null;

        _.forEach(igfss, async(igfs) => {
            const len = _.size(igfs.clusters);

            switch (len) {
                case 0:
                    console.log(`${LOST_FOUND}: ${igfs.name}`);

                    if (_.isNil(clusterLostFound))
                        clusterLostFound = await getClusterForMigration(clusterModel, igfs.space);

                    await linkIgfsToCluster(clusterModel, clusterLostFound._id, igfsModel, igfs._id);

                    break;

                case 1:
                    console.log(`Nothing to migrate: ${igfs.name}`);
                    break;

                default:
                    console.log(`One-to-many links found: n= ${len}, name= ${igfs.name}`);

                    await cloneIgfs(clusterModel, igfsModel, igfs);
            }
        });

        console.log('IGFS migration finished.');
    }
    catch (err) {
        console.log('IGFS migration failed!', err);
    }
}


exports.up = function up(done) {
    const clustersModel = this('Cluster');
    const cachesModel = this('Cache');
    const igfsModel = this('Igfs');

    migrateCaches(clustersModel, cachesModel);
    migrateIgfs(clustersModel, igfsModel);

    done();
};

exports.down = function down(done) {
    console.log('Model migration can not be reverted');

    done();
};
