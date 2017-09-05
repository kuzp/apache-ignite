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
    igfssActionTypes,
    shortIGFSsActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    cachesActionTypes
} from '../page-configure/reducer';
const ofType = (type) => (action) => action.type === type;
import {Observable} from 'rxjs/Observable';

export default class PageConfigureAdvanced {
    static $inject = ['ConfigureState', 'Clusters', 'IgniteMessages'];

    constructor(ConfigureState, Clusters, messages) {
        Object.assign(this, {ConfigureState, Clusters, messages});

        this.saveCompleteConfiguration$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION'))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([action, state]) => {
                // Backups
                const clustersBak = state.clusters;
                const shortClustersBak = state.shortClusters;
                const cachesBak = state.caches;
                const shortCachesBak = state.shortCaches;
                const basicCachesBak = state.basicCaches;

                return Observable.of(
                    {
                        type: igfssActionTypes.UPSERT,
                        items: action.changedItems.igfss
                    },
                    {
                        type: shortIGFSsActionTypes.UPSERT,
                        items: action.changedItems.igfss
                    },
                    {
                        type: cachesActionTypes.UPSERT,
                        items: action.changedItems.caches
                    },
                    {
                        type: shortCachesActionTypes.UPSERT,
                        items: action.changedItems.caches
                    },
                    {
                        type: clustersActionTypes.UPSERT,
                        items: [action.changedItems.cluster]
                    },
                    {
                        type: shortClustersActionTypes.UPSERT,
                        items: [Clusters.toShortCluster(action.changedItems.cluster)]
                    }
                )
                .merge(
                    Observable.fromPromise(Clusters.saveAdvanced(action.changedItems))
                    .switchMap((res) => {
                        return Observable.of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK',
                            changedItems: action.changedItems
                        });
                    })
                    .catch((res) => {
                        return Observable.of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR',
                            changedItems: action.changedItems,
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

        this.saveOKMessages$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK'))
            .do((action) => this.messages.showInfo(`Cluster ${action.changedItems.cluster.name} saved.`));

        this.saveErrMessages$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR'))
            .do((action) => this.messages.showError(`Failed to save cluster ${action.changedItems.cluster.name}.`));

        Observable.merge(this.saveOKMessages$, this.saveErrMessages$).subscribe();
        this.saveCompleteConfiguration$.subscribe((a) => ConfigureState.dispatchAction(a));
    }
}
