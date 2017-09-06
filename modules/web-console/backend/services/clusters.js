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

'use strict';

// Fire me up!

module.exports = {
    implements: 'services/clusters',
    inject: ['require(lodash)', 'mongo', 'services/spaces', 'services/caches', 'services/igfss', 'errors']
};

/**
 * @param _
 * @param mongo
 * @param {SpacesService} spacesService
 * @param {CachesService} cachesService
 * @param {IgfssService} igfssService
 * @param errors
 * @returns {ClustersService}
 */
module.exports.factory = (_, mongo, spacesService, cachesService, igfssService, errors) => {
    /**
     * Convert remove status operation to own presentation.
     *
     * @param {RemoveResult} result - The results of remove operation.
     */
    const convertRemoveStatus = ({result}) => ({rowsAffected: result.n});

    /**
     * Update existing cluster.
     *
     * @param {Object} cluster - The cluster for updating.
     * @returns {Promise.<mongo.ObjectId>} that resolves cluster id.
     */
    const update = (cluster) => {
        const clusterId = cluster._id;

        return mongo.Cluster.update({_id: clusterId}, cluster, {upsert: true}).exec()
            .then(() => mongo.Cache.update({_id: {$in: cluster.caches}}, {$addToSet: {clusters: clusterId}}, {multi: true}).exec())
            .then(() => mongo.Cache.update({_id: {$nin: cluster.caches}}, {$pull: {clusters: clusterId}}, {multi: true}).exec())
            .then(() => mongo.Igfs.update({_id: {$in: cluster.igfss}}, {$addToSet: {clusters: clusterId}}, {multi: true}).exec())
            .then(() => mongo.Igfs.update({_id: {$nin: cluster.igfss}}, {$pull: {clusters: clusterId}}, {multi: true}).exec())
            .then(() => cluster)
            .catch((err) => {
                if (err.code === mongo.errCodes.DUPLICATE_KEY_UPDATE_ERROR || err.code === mongo.errCodes.DUPLICATE_KEY_ERROR)
                    throw new errors.DuplicateKeyException('Cluster with name: "' + cluster.name + '" already exist.');
            });
    };

    /**
     * Create new cluster.
     *
     * @param {Object} cluster - The cluster for creation.
     * @returns {Promise.<mongo.ObjectId>} that resolves cluster id.
     */
    const create = (cluster) => {
        return mongo.Cluster.create(cluster)
            .catch((err) => {
                if (err.code === mongo.errCodes.DUPLICATE_KEY_ERROR)
                    throw new errors.DuplicateKeyException(`Cluster with name: "${cluster.name}" already exist.`);
            })
            .then((savedCluster) =>
                mongo.Cache.update({_id: {$in: savedCluster.caches}}, {$addToSet: {clusters: savedCluster._id}}, {multi: true}).exec()
                    .then(() => mongo.Igfs.update({_id: {$in: savedCluster.igfss}}, {$addToSet: {clusters: savedCluster._id}}, {multi: true}).exec())
                    .then(() => savedCluster)
            );
    };

    /**
     * Remove all caches by space ids.
     *
     * @param {Number[]} spaceIds - The space ids for cache deletion.
     * @returns {Promise.<RemoveResult>} - that resolves results of remove operation.
     */
    const removeAllBySpaces = (spaceIds) => {
        return mongo.Cache.update({space: {$in: spaceIds}}, {clusters: []}, {multi: true}).exec()
            .then(() => mongo.Igfs.update({space: {$in: spaceIds}}, {clusters: []}, {multi: true}).exec())
            .then(() => mongo.Cluster.remove({space: {$in: spaceIds}}).exec());
    };

    class ClustersService {
        static shortList(userId, demo) {
            return spacesService.spaceIds(userId, demo)
                .then((spaceIds) => Promise.all([
                    mongo.Cluster.find({space: {$in: spaceIds}}).select('name discovery.kind caches igfss').sort('name').lean().exec(),
                    mongo.Cache.find({space: {$in: spaceIds}}).select('domains').lean().exec()
                ])
                .then(([clusters, caches]) => {
                    const cmap = _.keyBy(caches, '_id');

                    return _.map(clusters, (cluster) => {
                        const models = _.reduce(cluster.caches, (acc, cacheId) => {
                            _.forEach(_.get(cmap[cacheId], 'domains'), (modelId) => acc.add(modelId.toString()));

                            return acc;
                        }, new Set());

                        return {
                            _id: cluster._id,
                            name: cluster.name,
                            discovery: cluster.discovery.kind,
                            cachesCount: _.size(cluster.caches),
                            modelsCount: models.size,
                            igfsCount: _.size(cluster.igfss)
                        };
                    });
                }));
        }

        static get(userId, demo, _id) {
            return spacesService.spaceIds(userId, demo)
                .then((spaceIds) => mongo.Cluster.findOne({space: {$in: spaceIds}, _id}).lean().exec());
        }

        static normalize(spaceId, cluster, ...models) {
            cluster.space = spaceId;

            _.forEach(models, (model) => {
                _.forEach(model, (item) => {
                    item.space = spaceId;
                    item.clusters = [cluster._id];
                });
            });
        }

        static removedInCluster(oldCluster, newCluster, field) {
            return _.difference(_.invokeMap(_.get(oldCluster, field), 'toString'), _.get(newCluster, field));
        }

        static upsertBasic(userId, demo, {cluster, caches}) {
            if (_.isNil(cluster._id))
                return Promise.reject(new errors.IllegalArgumentException('Cluster id can not be undefined or null'));

            return spacesService.spaceIds(userId, demo)
                .then((spaceIds) => {
                    this.normalize(_.head(spaceIds), cluster, caches);

                    const query = _.pick(cluster, ['space', '_id']);
                    const basicCluster = _.pick(cluster, ['space', '_id', 'name', 'discovery', 'caches', 'memoryConfiguration.memoryPolicies']);

                    return mongo.Cluster.findOneAndUpdate(query, {$set: basicCluster}, {projection: {caches: 1, igfss: 1}, upsert: true}).lean().exec()
                        .catch((err) => {
                            if (err.code === mongo.errCodes.DUPLICATE_KEY_ERROR)
                                throw new errors.DuplicateKeyException(`Cluster with name: "${cluster.name}" already exist.`);

                            throw err;
                        })
                        .then((oldCluster) => {
                            if (oldCluster) {
                                const ids = this.removedInCluster(oldCluster, cluster, 'caches');

                                return cachesService.remove(ids);
                            }

                            cluster.caches = _.map(caches, '_id');

                            return mongo.Cluster.update(query, {$set: cluster, new: true}, {upsert: true}).exec();
                        });
                })
                .then(() => _.map(caches, cachesService.upsertBasic))
                .then(() => ({n: 1}));
        }

        static upsert(userId, demo, {cluster, caches, igfss}) {
            if (_.isNil(cluster._id))
                return Promise.reject(new errors.IllegalArgumentException('Cluster id can not be undefined or null'));

            return spacesService.spaceIds(userId, demo)
                .then((spaceIds) => {
                    this.normalize(_.head(spaceIds), cluster, caches, igfss);

                    const query = _.pick(cluster, ['space', '_id']);

                    return mongo.Cluster.findOneAndUpdate(query, {$set: cluster}, {projection: 'caches', upsert: true}).lean().exec()
                        .catch((err) => {
                            if (err.code === mongo.errCodes.DUPLICATE_KEY_ERROR)
                                throw new errors.DuplicateKeyException(`Cluster with name: "${cluster.name}" already exist.`);

                            throw err;
                        })
                        .then((oldCluster) => {
                            const cacheIds = this.removedInCluster(oldCluster, cluster, 'caches');
                            const igfsIds = this.removedInCluster(oldCluster, cluster, 'igfss');

                            return Promise.all([cachesService.remove(cacheIds), igfssService.remove(igfsIds)]);
                        });
                })
                .then(() => Promise.all(_.concat(_.map(caches, cachesService.upsert), _.map(igfss, igfssService.upsert))))
                .then(() => ({n: 1}));
        }

        /**
         * Create or update cluster.
         *
         * @param {Object} cluster - The cluster.
         * @returns {Promise.<mongo.ObjectId>} that resolves cluster id of merge operation.
         */
        static merge(cluster) {
            if (cluster._id)
                return update(cluster);

            return create(cluster);
        }

        /**
         * Get clusters and linked objects by space.
         *
         * @param {mongo.ObjectId|String} spaceIds The spaces ids that own clusters.
         * @returns {Promise.<Array<mongo.Cluster>>} Requested clusters.
         */
        static listBySpaces(spaceIds) {
            return mongo.Cluster.find({space: {$in: spaceIds}}).sort('name').lean().exec();
        }

        /**
         * Remove clusters.
         *
         * @param {Array.<String>|String} ids - The cluster ids for remove.
         * @returns {Promise.<{rowsAffected}>} - The number of affected rows.
         */
        static remove(ids) {
            if (_.isNil(ids))
                return Promise.reject(new errors.IllegalArgumentException('Cluster id can not be undefined or null'));

            if (_.isEmpty(ids))
                return Promise.resolve({rowsAffected: 0});

            ids = _.castArray(ids);

            return mongo.Cache.remove({clusters: {$in: ids}}).exec()
                .then(() => mongo.Igfs.remove({clusters: {$in: ids}}).exec())
                .then(() => mongo.Cluster.remove({_id: {$in: ids}}).exec())
                .then(convertRemoveStatus);
        }

        /**
         * Remove all clusters by user.
         * @param {mongo.ObjectId|String} userId - The user id that own cluster.
         * @param {Boolean} demo - The flag indicates that need lookup in demo space.
         * @returns {Promise.<{rowsAffected}>} - The number of affected rows.
         */
        static removeAll(userId, demo) {
            return spacesService.spaceIds(userId, demo)
                .then(removeAllBySpaces)
                .then(convertRemoveStatus);
        }
    }

    return ClustersService;
};
