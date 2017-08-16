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

import cloneDeep from 'lodash/cloneDeep';

import {
    isNewItem
} from './reducer';

import {
    basicCachesActionTypes,
    clustersActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    cachesActionTypes
} from '../page-configure/reducer';

import {uniqueName} from 'app/utils/uniqueName';
import get from 'lodash/get';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/take';
import 'rxjs/add/operator/takeUntil';
import 'rxjs/add/observable/combineLatest';

const ofType = (type) => (action) => action.type === type;

export default class PageConfigureBasic {
    isNewItem = isNewItem;

    static $inject = [
        '$q',
        'IgniteMessages',
        'Clusters',
        'Caches',
        'ConfigureState',
        'PageConfigure',
        'IgniteVersion',
        'ConfigurationDownload'
    ];

    constructor($q, messages, clusters, caches, ConfigureState, pageConfigure, IgniteVersion, ConfigurationDownload) {
        Object.assign(this, {$q, messages, clusters, caches, ConfigureState, pageConfigure, IgniteVersion, ConfigurationDownload});

        this.saveClusterAndCaches$ = this.ConfigureState.actions$
            .filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES'))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([action, state]) => {
                // Updates
                const cluster = {
                    ...action.cluster,
                    caches: [...action.caches.ids.values()]
                };
                const shortCluster = clusters.toShortCluster(cluster);
                const caches = [...action.caches.changedItems.values()]
                    .filter((shortCache) => state.caches.has(shortCache._id))
                    .map((shortCache) => ({...state.caches.get(shortCache._id), ...shortCache}));
                const shortCaches = [...action.caches.changedItems.values()].map((cache) => ({
                    ...cache, clusters: [action.cluster._id]
                }));

                // Backups
                const clustersBak = state.clusters;
                const shortClustersBak = state.shortClusters;
                const cachesBak = state.caches;
                const shortCachesBak = state.shortCaches;
                const basicCachesBak = state.basicCaches;

                return Observable.of({
                    type: clustersActionTypes.UPSERT,
                    items: [cluster]
                }, {
                    type: shortClustersActionTypes.UPSERT,
                    items: [shortCluster]
                }, {
                    type: cachesActionTypes.UPSERT,
                    items: caches
                }, {
                    type: shortCachesActionTypes.UPSERT,
                    items: shortCaches
                })
                .merge(
                    Observable.fromPromise(this.clusters.saveBasic(cluster, shortCaches))
                    .switchMap((res) => {
                        return Observable.of({
                            type: 'BASIC_SAVE_CLUSTER_AND_CACHES_OK',
                            cluster: {name: cluster.name, _id: cluster._id}
                        });
                    })
                    .catch((res) => {
                        return Observable.of({
                            type: 'BASIC_SAVE_CLUSTER_AND_CACHES_ERR',
                            cluster: {name: cluster.name, _id: cluster._id},
                            error: res
                        }, {
                            type: clustersActionTypes.SET,
                            state: clustersBak
                        }, {
                            type: shortClustersActionTypes.SET,
                            state: shortClustersBak
                        }, {
                            type: cachesActionTypes.SET,
                            state: cachesBak
                        }, {
                            type: shortCachesActionTypes.SET,
                            state: shortCachesBak
                        }, {
                            type: basicCachesActionTypes.SET,
                            state: basicCachesBak
                        });
                    })
                );
            });

        this.basicSaveOKMessages$ = this.ConfigureState.actions$
            .filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES_OK'))
            .do((action) => this.messages.showInfo(`Cluster ${action.cluster.name} saved.`));

        this.basicSaveErrMessages$ = this.ConfigureState.actions$
            .filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES_ERR'))
            .do((action) => this.messages.showError(`Failed to save cluster ${action.cluster.name}.`));

        Observable.merge(this.basicSaveOKMessages$, this.basicSaveErrMessages$).subscribe();
        Observable.merge(this.saveClusterAndCaches$).subscribe((a) => ConfigureState.dispatchAction(a));

        this.clusterDiscoveries = clusters.discoveries;
        this.minMemoryPolicySize = clusters.minMemoryPolicySize;
    }

    saveClusterAndCaches(cluster, caches) {
        // TODO IGNITE-5476 Implement single backend API method with transactions and use that instead
        const stripFakeID = (item) => Object.assign({}, item, {_id: isNewItem(item) ? void 0 : item._id});
        const noFakeIDCaches = caches.map(stripFakeID);
        cluster = cloneDeep(stripFakeID(cluster));
        return this.$q.all(noFakeIDCaches.map((cache) => (
            this.caches.saveCache(cache)
                .then(
                    ({data}) => data,
                    (e) => {
                        this.messages.showError(e);
                        return this.$q.resolve(null);
                    }
                )
        )))
        .then((cacheIDs) => {
            // Make sure we don't loose new IDs even if some requests fail
            this.pageConfigure.upsertCaches(
                cacheIDs.map((_id, i) => {
                    if (!_id) return;
                    const cache = caches[i];
                    return Object.assign({}, cache, {
                        _id,
                        clusters: cluster._id ? [...cache.clusters, cluster._id] : cache.clusters
                    });
                }).filter((v) => v)
            );

            cluster.caches = cacheIDs.map((_id, i) => _id || noFakeIDCaches[i]._id).filter((v) => v);
            this.setSelectedCaches(cluster.caches);
            caches.forEach((cache, i) => {
                if (isNewItem(cache) && cacheIDs[i]) this.removeCache(cache);
            });
            return cacheIDs;
        })
        .then((cacheIDs) => {
            if (cacheIDs.indexOf(null) !== -1) return this.$q.reject([cluster._id, cacheIDs]);
            return this.clusters.saveCluster(cluster)
            .catch((e) => {
                this.messages.showError(e);
                return this.$q.reject(e);
            })
            .then(({data: clusterID}) => {
                this.messages.showInfo(`Cluster ${cluster.name} was saved.`);
                // cache.clusters has to be updated again since cluster._id might have not existed
                // after caches were saved

                this.pageConfigure.upsertCaches(
                    cacheIDs.map((_id, i) => {
                        if (!_id) return;
                        const cache = caches[i];
                        return Object.assign({}, cache, {
                            _id,
                            clusters: cache.clusters.indexOf(clusterID) !== -1 ? cache.clusters : cache.clusters.concat(clusterID)
                        });
                    }).filter((v) => v)
                );
                this.pageConfigure.upsertClusters([
                    Object.assign(cluster, {
                        _id: clusterID
                    })
                ]);
                this.setCluster(clusterID);
                return [clusterID, cacheIDs];
            });
        });
    }

    save(cluster) {
        const caches = this.ConfigureState.state$.value.basicCaches;
        const clusterToSend = {...cluster, caches: caches.ids};
        const changedCaches = [...caches.changedItems.values()].map((cache) => ({...cache, clusters: [cluster._id]}));

        this.ConfigureState.dispatchAction({
            type: 'BASIC_SAVE_CLUSTER_AND_CACHES',
            cluster,
            caches
        });
    }

    saveAndDownload(cluster) {
        const actions = this.ConfigureState.actions$;
        const oks = actions.filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES_OK'));
        const errs = actions.filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES_ERR'));

        this.save(cluster);

        oks.take(1).takeUntil(errs.take(1))
        .do(() => this.ConfigurationDownload.downloadClusterConfiguration(cluster))
        .subscribe();
    }

    addCache(caches) {
        this.ConfigureState.dispatchAction({
            type: basicCachesActionTypes.UPSERT,
            items: [{...this.caches.getBlankCache(), name: uniqueName('New cache', caches)}]
        });
    }

    removeCache(item) {
        this.ConfigureState.dispatchAction({
            type: basicCachesActionTypes.REMOVE,
            ids: [item._id]
        });
    }

    updateCache(item) {
        this.ConfigureState.dispatchAction({
            type: basicCachesActionTypes.UPSERT,
            items: [item]
        });
    }

    getObservable() {
        const {state$} = this.ConfigureState;

        const cluster = state$
            .pluck('clusterConfiguration', 'originalCluster')
            .distinctUntilChanged()
            .map((cluster) => {
                const clonedCluster = cloneDeep(cluster);
                return {
                    originalCluster: cluster,
                    clonedCluster,
                    defaultMemoryPolicy: this.clusters.getDefaultClusterMemoryPolicy(clonedCluster)
                };
            });

        const caches = Observable.combineLatest(
            state$.pluck('basicCaches', 'ids').distinctUntilChanged().map((ids) => [...ids.values()]),
            state$.pluck('basicCaches', 'changedItems').distinctUntilChanged(),
            state$.pluck('shortCaches').distinctUntilChanged(),
            (ids, changedCaches, oldCaches) => ({
                allClusterCaches: ids.map((id) => changedCaches.get(id) || oldCaches.get(id)).filter((v) => v)
            })
        );

        const memorySizeInput = this.IgniteVersion.currentSbj.map((version) => ({
            memorySizeInputVisible: this.IgniteVersion.since(version.ignite, '2.0.0')
        }));

        return Observable.combineLatest(cluster, caches, memorySizeInput, (...values) => Object.assign({}, ...values));
    }
}
