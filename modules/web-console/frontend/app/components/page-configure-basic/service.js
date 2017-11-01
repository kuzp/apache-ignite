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
                const actions = [{
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
                    items: [clusters.toShortCluster(action.changedItems.cluster)]
                },
                {
                    type: 'EDIT_CLUSTER',
                    cluster: action.changedItems.cluster
                }
                ];


                return Observable.of(...actions)
                .merge(
                    Observable.fromPromise(this.clusters.saveBasic(action.changedItems))
                    .switchMap((res) => {
                        return Observable.of(
                            {type: 'EDIT_CLUSTER', cluster: action.changedItems.cluster},
                            {type: 'BASIC_SAVE_CLUSTER_AND_CACHES_OK', changedItems: action.changedItems}
                        );
                    })
                    .catch((res) => {
                        return Observable.of({
                            type: 'BASIC_SAVE_CLUSTER_AND_CACHES_ERR',
                            changedItems: action.changedItems,
                            error: {
                                message: `Failed to save cluster "${action.changedItems.cluster.name}": ${res.data}.`
                            }
                        }, {
                            type: 'UNDO_ACTIONS',
                            actions: action.prevActions.concat(actions)
                        });
                    })
                );
            });

        this.basicSaveOKMessages$ = this.ConfigureState.actions$
            .filter(ofType('BASIC_SAVE_CLUSTER_AND_CACHES_OK'))
            .do((action) => this.messages.showInfo(`Cluster ${action.changedItems.cluster.name} saved.`));

        Observable.merge(this.basicSaveOKMessages$).subscribe();
        Observable.merge(this.saveClusterAndCaches$).subscribe((a) => ConfigureState.dispatchAction(a));

        this.clusterDiscoveries = clusters.discoveries;
        this.minMemoryPolicySize = clusters.minMemoryPolicySize;
    }
}
