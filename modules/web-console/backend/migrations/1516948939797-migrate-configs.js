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

function getClusterForMigration(clusterModel, spaceIds) {
    return clusterModel.findOne({space: spaceIds, name: 'LOST+FOUND'}).lean().exec()
        .then((cluster) => {
            if (!cluster) {
                return clusterModel.create({
                    space: spaceIds,
                    name: 'LOST+FOUND',
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

function getCaches(cachesModel) {
    return cachesModel.find({}).lean().exec();
}

function linkCacheToCluster(clusterModel, clusterId, cachesModel, cacheId) {
    return clusterModel.update({_id: clusterId}, {$addToSet: {caches: cacheId}}).exec()
        .then(() => cachesModel.update({_id: cacheId}, {clusters: [clusterId]}).exec());
}

function cloneCache(clusterModel, cachesModel, cache) {
    return true;
}

async function migrateCaches(clusterModel, cachesModel) {
    console.log('Caches migration started...');

    try {
        const caches = await getCaches(cachesModel);

        let clusterLostFound = null;

        _.forEach(caches, async (cache) => {
            const len = _.size(cache.clusters);

            switch (len) {
                case 0:
                    console.log('LOST+FOUND: ' + cache.name);

                    if (_.isNil(clusterLostFound))
                        clusterLostFound = await getClusterForMigration(clusterModel, cache.space);

                    await linkCacheToCluster(clusterModel, clusterLostFound._id, cachesModel, cache._id);

                    break;

                case 1:
                    console.log('Nothing to migrate: ' + cache.name);
                    break;

                default:
                    console.log('One-to-many links found: n=' + len + ', name=' + cache.name);

                    cloneCache(clusterModel, cachesModel, cache);
            }
        });

        console.log('Caches migration finished.');
    }
    catch (err) {
        console.log('Caches migration failed!', err);
    }
}

exports.up = function up(done) {
    const clustersModel = this('Cluster');
    const cachesModel = this('Cache');

    migrateCaches(clustersModel, cachesModel);

    done();
};

exports.down = function down(done) {
    console.log('Model migration can not be reverted');

    done();
};
