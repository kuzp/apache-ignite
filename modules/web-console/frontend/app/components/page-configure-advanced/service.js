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

import {
    basicCachesActionTypes,
    clustersActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    cachesActionTypes
} from '../page-configure/reducer';
const ofType = (type) => (action) => action.type === type;
import {Observable} from 'rxjs/Observable';

export default class PageConfigureAdvanced {
    static $inject = ['ConfigureState', 'Clusters'];

    constructor(ConfigureState, Clusters) {
        Object.assign(this, {ConfigureState, Clusters});

        this.saveCompleteConfiguration$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION'))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([action, state]) => {
                // Updates
                const cluster = {
                    ...action.cluster,
                    caches: [...action.caches.ids.values()]
                };
                const shortCluster = Clusters.toShortCluster(cluster);
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
                    Observable.fromPromise(Clusters.saveAdvanced(cluster, shortCaches))
                    .switchMap((res) => {
                        return Observable.of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK',
                            cluster: {name: cluster.name, _id: cluster._id}
                        });
                    })
                    .catch((res) => {
                        return Observable.of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR',
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

        this.saveCompleteConfiguration$.subscribe((a) => ConfigureState.dispatchAction(a));
    }
}
