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
    modelsActionTypes,
    shortModelsActionTypes,
    igfssActionTypes,
    shortIGFSsActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    cachesActionTypes
} from '../page-configure/reducer';
const ofType = (type) => (action) => action.type === type;
import {Observable} from 'rxjs/Observable';

export default class PageConfigureAdvanced {
    static $inject = ['ConfigureState', 'Clusters', 'Caches', 'IgniteMessages'];

    constructor(ConfigureState, Clusters, Caches, messages) {
        Object.assign(this, {ConfigureState, Clusters, Caches, messages});

        this.saveCompleteConfiguration$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION'))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([action, state]) => {
                const actions = [
                    {
                        type: modelsActionTypes.UPSERT,
                        items: action.changedItems.models
                    },
                    {
                        type: shortModelsActionTypes.UPSERT,
                        items: action.changedItems.models
                    },
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
                        items: action.changedItems.caches.map(Caches.toShortCache)
                    },
                    {
                        type: clustersActionTypes.UPSERT,
                        items: [action.changedItems.cluster]
                    },
                    {
                        type: shortClustersActionTypes.UPSERT,
                        items: [Clusters.toShortCluster(action.changedItems.cluster)]
                    }
                ];

                return Observable.of(...actions)
                .merge(
                    Observable.fromPromise(Clusters.saveAdvanced(action.changedItems))
                    .switchMap((res) => {
                        return Observable.of(
                            {type: 'EDIT_CLUSTER', cluster: action.changedItems.cluster},
                            // {type: 'RESET_EDIT_CHANGES'},
                            {type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK'}
                        );
                    })
                    .catch((res) => {
                        return Observable.of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR',
                            changedItems: action.changedItems,
                            error: res
                        }, {
                            type: 'UNDO_ACTIONS',
                            // actions
                            actions: action.prevActions.concat(actions)
                        });
                    })
                );
            });

        // this.saveOKMessages$ = this.ConfigureState.actions$
        //     .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK'))
        //     .do((action) => this.messages.showInfo(`Cluster ${action.changedItems.cluster.name} saved.`));

        this.saveErrMessages$ = this.ConfigureState.actions$
            .filter(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR'))
            .do((action) => this.messages.showError(`Failed to save cluster ${action.changedItems.cluster.name}.`));

        Observable.merge(this.saveErrMessages$).subscribe();
        this.saveCompleteConfiguration$.subscribe((a) => ConfigureState.dispatchAction(a));
    }
}
