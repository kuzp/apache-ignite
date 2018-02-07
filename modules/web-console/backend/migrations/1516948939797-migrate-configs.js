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

const createClusterForMigration = require('./migration-utils').createClusterForMigration;
const getClusterForMigration = require('./migration-utils').getClusterForMigration;
const createCacheForMigration = require('./migration-utils').createCacheForMigration;
const getCacheForMigration = require('./migration-utils').getCacheForMigration;

function linkCacheToCluster(clustersModel, cluster, cachesModel, cache) {
    return clustersModel.update({_id: cluster._id}, {$addToSet: {caches: cache._id}}).exec()
        .then(() => cachesModel.update({_id: cache._id}, {clusters: [cluster._id]}).exec())
        .catch((err) => log(`Failed link cache to cluster [cache=${cache.name}, cluster=${cluster.name}, err=${err}]`));
}

function cloneCache(clustersModel, cachesModel, domainsModel, cache) {
    const cacheId = cache._id;
    const clusters = cache.clusters;

    delete cache._id;
    cache.clusters = [];

    if (cache.cacheStoreFactory && cache.cacheStoreFactory.kind === null)
        delete cache.cacheStoreFactory.kind;

    return Promise.all(_.map(clusters, (cluster, idx) => {
        cache.clusters = [cluster];

        if (idx > 0) {
            return clustersModel.update({_id: {$in: cache.clusters}}, {$pull: {caches: cacheId}}, {multi: true}).exec()
                .then(() => cachesModel.create(cache))
                .then((clone) => clustersModel.update({_id: {$in: cache.clusters}}, {$addToSet: {caches: clone._id}}, {multi: true}).exec())
                .then(() => {
                    const domainIds = cache.domains;

                    if (_.isEmpty(domainIds))
                        return Promise.resolve(true);

                    return Promise.all(_.map(domainIds, (domainId) => {
                        return domainsModel.findOne({_id: domainId})
                            .then((domain) => {
                                log(`Domain to duplicate: ${domain._id}`);
                                return Promise.resolve(true);
                            });
                    }));
                })
                .catch((err) => log(`Failed to clone cache: id=${cacheId}, name=${cache.name}, err=${err}`));
        }

        return cachesModel.update({_id: cacheId}, {clusters: [cluster]}).exec();
    }));
}

function migrateCache(clustersModel, cachesModel, domainsModel, cache) {
    const len = _.size(cache.clusters);

    if (len < 1) {
        log(`Found cache not linked to cluster [cache=${cache.name}]`);

        return getClusterForMigration(clustersModel)
            .then((clusterLostFound) => linkCacheToCluster(clustersModel, clusterLostFound, cachesModel, cache));
    }

    if (len > 1) {
        log(`Found cache linked to many clusters [cache=${cache.name}, cnt=${len}]`);

        return cloneCache(clustersModel, cachesModel, domainsModel, cache);
    }

    // Nothing to migrate, cache linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateCaches(clustersModel, cachesModel, domainsModel) {
    log('Caches migration started...');

    return cachesModel.find({}).lean().exec()
        .then((caches) => {
            log(`Caches to migrate: ${_.size(caches)}`);

            _.reduce(caches, (start, cache) => start.then(() => migrateCache(clustersModel, cachesModel, domainsModel, cache)), Promise.resolve(true))
                .then(() => log('Caches migration finished.'));
        })
        .catch((err) => log('Caches migration failed: ' + err));
}

function linkIgfsToCluster(clustersModel, cluster, igfsModel, igfs) {
    return clustersModel.update({_id: cluster._id}, {$addToSet: {igfss: igfs._id}}).exec()
        .then(() => igfsModel.update({_id: igfs._id}, {clusters: [cluster._id]}).exec())
        .catch((err) => log(`Failed link IGFS to cluster [IGFS=${igfs.name}, cluster=${cluster.name}, err=${err}]`));
}

function cloneIgfs(clustersModel, igfsModel, igfs) {
    const igfsId = igfs._id;
    const clusters = igfs.clusters;

    delete igfs._id;
    igfs.clusters = [];

    return Promise.all(_.map(clusters, (cluster, idx) => {
        igfs.clusters = [cluster];

        if (idx > 0) {
            return clustersModel.update({_id: {$in: igfs.clusters}}, {$pull: {igfss: igfsId}}, {multi: true}).exec()
                .then(() => igfsModel.create(igfs))
                .then((clone) => clustersModel.update({_id: {$in: igfs.clusters}}, {$addToSet: {igfss: clone._id}}, {multi: true}).exec())
                .catch((err) => log(`Failed to clone IGFS: id=${igfsId}, name=${igfs.name}, err=${err}`));
        }

        return igfsModel.update({_id: igfsId}, {clusters: [cluster]}).exec();
    }));
}

function migrateIgfs(clustersModel, igfsModel, igfs) {
    const len = _.size(igfs.clusters);

    if (len < 1) {
        log(`Found IGFS not linked to cluster [IGFS=${igfs.name}]`);

        return getClusterForMigration(clustersModel)
            .then((clusterLostFound) => linkIgfsToCluster(clustersModel, clusterLostFound, igfsModel, igfs));
    }

    if (len > 1) {
        log(`Found IGFS linked to many clusters [IGFS=${igfs.name}, cnt=${len}]`);

        return cloneIgfs(clustersModel, igfsModel, igfs);
    }

    // Nothing to migrate, IGFS linked to cluster 1-to-1.
    return Promise.resolve(true);
}

function migrateIgfss(clustersModel, igfsModel) {
    log('IGFS migration started...');

    return igfsModel.find({}).lean().exec()
        .then((igfss) => {
            log(`IGFS to migrate: ${_.size(igfss)}`);

            return _.reduce(igfss, (start, igfs) => migrateIgfs(clustersModel, igfsModel, igfs), Promise.resolve(true))
                .then(() => log('IGFS migration finished.'));
        })
        .catch((err) => log('IGFS migration failed: ' + err));
}

function linkDomainToCache(cachesModel, cache, domainsModel, domain) {
    log(`Cache: ${cache.name}, domain: ${domain._id}`);

    return Promise.resolve(true);
}

function migrateDomain(clustersModel, cachesModel, domainsModel, domain) {
    const len = _.size(domain.caches);

    if (len < 1) {
        log(`Found domain model not linked to cache [domain=${domain._id}]`);

        return getCacheForMigration(cachesModel)
            .then((cacheLostFound) => {
                log(cacheLostFound);

                linkDomainToCache(cachesModel, cacheLostFound, domainsModel, domain)
            });
    }

    // Nothing to migrate, other domains will be migrated with caches.
    return Promise.resolve(true);
}

function migrateDomains(clustersModel, cachesModel, domainsModel) {
    log('Domain models migration started...');

    return domainsModel.find({}).lean().exec()
        .then((domains) => {
            log(`Domain models to migrate: ${_.size(domains)}`);

            return _.reduce(domains, (start, domain) => migrateDomain(clustersModel, cachesModel, domainsModel, domain), Promise.resolve(true))
                .then(() => log('Domain models migration finished.'));
        })
        .catch((err) => log('Domain models migration failed: ' + err));
}

exports.up = function up(done) {
    const clustersModel = this('Cluster');
    const cachesModel = this('Cache');
    const domainsModel = this('DomainModel');
    const igfsModel = this('Igfs');

    createClusterForMigration(clustersModel, cachesModel)
        .then(() => createCacheForMigration(clustersModel, cachesModel, domainsModel))
        .then(() => migrateCaches(clustersModel, cachesModel, domainsModel))
        .then(() => migrateDomains(clustersModel, cachesModel, domainsModel))
        .then(() => migrateIgfss(clustersModel, igfsModel))
        .then(() => done())
        .catch(done);
};

exports.down = function down(done) {
    log('Model migration can not be reverted');

    done();
};
