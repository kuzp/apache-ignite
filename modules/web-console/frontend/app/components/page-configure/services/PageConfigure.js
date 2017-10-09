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

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/take';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/merge';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/observable/empty';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/forkJoin';
import get from 'lodash/fp/get';
import propEq from 'lodash/fp/propEq';
import cloneDeep from 'lodash/cloneDeep';

import {
    clustersActionTypes,
    shortClustersActionTypes,
    ADD_CLUSTER,
    ADD_CLUSTERS,
    UPDATE_CLUSTER,
    UPSERT_CLUSTERS,
    REMOVE_CLUSTERS,
    UPSERT_CACHES,
    uniqueName
} from '../reducer';

export const REMOVE_CLUSTERS_LOCAL_REMOTE = Symbol('REMOVE_CLUSTERS_LOCAL_REMOTE');
export const CLONE_CLUSTERS = Symbol('CLONE_CLUSTERS');

export default class PageConfigure {
    static $inject = ['IgniteConfigurationResource', '$state', 'ConfigureState', 'Clusters', 'ConfigSelectors'];

    constructor(configuration, $state, ConfigureState, Clusters, ConfigSelectors) {
        Object.assign(this, {configuration, $state, ConfigureState, Clusters, ConfigSelectors});

        this.removeClusters$ = this.ConfigureState.actions$
            .filter(propEq('type', REMOVE_CLUSTERS_LOCAL_REMOTE))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([{clusters}, state]) => {
                const backup = {
                    clusters: state.clusters,
                    shortClusters: state.shortClusters
                };
                return Observable.of({
                    type: shortClustersActionTypes.REMOVE,
                    ids: clusters.map((c) => c._id)
                }).merge(...clusters.map((cluster) => {
                    return this.Clusters.removeCluster$(cluster)
                    .switchMap(() => Observable.empty())
                    .catch(() => Observable.of({
                        type: shortClustersActionTypes.UPSERT,
                        items: [backup.shortClusters.get(cluster._id)]
                    }));
                }));
            });

        this.cloneClusters$ = this.ConfigureState.actions$
            .filter(propEq('type', CLONE_CLUSTERS))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([{clusters}, state]) => {
                const sendRequest = (c) => this.Clusters.saveCluster$(Object.assign({}, c, {_id: void 0}));
                const toAdd = clusters.map((c, i) => {
                    return Object.assign(cloneDeep(state.list.clusters.get(c._id)), {
                        _id: (i + 1) * -1,
                        name: uniqueName(`${c.name} (clone)`, [...state.list.clusters.values()])
                    });
                });
                return Observable.of({
                    type: ADD_CLUSTERS,
                    clusters: toAdd
                })
                .merge(...toAdd.map((c) => sendRequest(c)
                    .map(({data}) => ({
                        type: UPDATE_CLUSTER,
                        _id: c._id,
                        cluster: {_id: data}
                    }))
                    .catch((e) => {
                        return Observable.of({
                            type: REMOVE_CLUSTERS,
                            clusterIDs: [c._id]
                        });
                    })
                ));
            });

        this.removeClusters$.merge(this.cloneClusters$).subscribe((a) => ConfigureState.dispatchAction(a));
    }

    cloneClusters(clusters) {
        this.ConfigureState.dispatchAction({type: CLONE_CLUSTERS, clusters});
    }

    addCluster(cluster) {
        this.ConfigureState.dispatchAction({type: ADD_CLUSTER, cluster});
    }

    updateCluster(cluster) {
        this.ConfigureState.dispatchAction({type: UPDATE_CLUSTER, cluster});
    }

    upsertCaches(caches) {
        this.ConfigureState.dispatchAction({type: UPSERT_CACHES, caches});
    }

    upsertClusters(clusters) {
        this.ConfigureState.dispatchAction({type: UPSERT_CLUSTERS, clusters});
    }

    removeClusters(clusterIDs) {
        this.ConfigureState.dispatchAction({type: REMOVE_CLUSTERS, clusterIDs});
    }

    removeClustersLocalRemote(clusters) {
        this.ConfigureState.dispatchAction({type: REMOVE_CLUSTERS_LOCAL_REMOTE, clusters});
    }

    editCluster(clusterID) {
        return this.ConfigureState.state$
        .take(1)
        .switchMap((state) => {
            const toClusterCachesAmount = get('caches.length', state.list.clusters.get(clusterID)) || 0;
            return this.$state.go(
                (state.list.clusters.size <= 1 && toClusterCachesAmount < 10)
                    ? 'base.configuration.edit.basic'
                    : 'base.configuration.edit.advanced.clusters',
                {clusterID}
            );
        })
        .subscribe();
    }

    getClusterConfiguration({clusterID, isDemo}) {
        return Observable.merge(
            Observable
                .timer(1)
                .take(1)
                .do(() => this.ConfigureState.dispatchAction({type: 'LOAD_COMPLETE_CONFIGURATION', clusterID, isDemo}))
                .ignoreElements(),
            this.ConfigureState.state$
                .let(this.ConfigSelectors.selectCompleteClusterConfiguration({clusterID, isDemo}))
                .filter((c) => c.__isComplete)
                .take(1)
                .map((data) => ({...data, clusters: [cloneDeep(data.cluster)]}))
        )
        .take(1)
        .toPromise();
    }
}
