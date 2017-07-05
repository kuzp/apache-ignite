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
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/toPromise';
// import 'rxjs/add/operator/flatMap';
import 'rxjs/add/operator/concat';
import 'rxjs/add/operator/repeat';
import 'rxjs/add/operator/merge';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/observable/empty';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/observable/merge';
import 'rxjs/add/observable/throw';
import 'rxjs/add/observable/forkJoin';
import get from 'lodash/fp/get';
import propEq from 'lodash/fp/propEq';
import cloneDeep from 'lodash/cloneDeep';

import {
    ADD_CLUSTER,
    ADD_CLUSTERS,
    UPDATE_CLUSTER,
    UPSERT_CLUSTERS,
    REMOVE_CLUSTERS,
    LOAD_LIST,
    UPSERT_CACHES,
    uniqueName
} from '../reducer';

export const REMOVE_CLUSTERS_LOCAL_REMOTE = Symbol('REMOVE_CLUSTERS_LOCAL_REMOTE');
export const CLONE_CLUSTERS = Symbol('CLONE_CLUSTERS');

export default class PageConfigure {
    static $inject = ['IgniteConfigurationResource', '$state', 'ConfigureState', 'Clusters'];

    constructor(configuration, $state, ConfigureState, Clusters) {
        Object.assign(this, {configuration, $state, ConfigureState, Clusters});

        this.removeClusters$ = this.ConfigureState.actions$
            .filter(propEq('type', REMOVE_CLUSTERS_LOCAL_REMOTE))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([{clusters}, state]) => {
                const updateServer = () => Observable.forkJoin(
                    clusters.map(
                        (cluster) => this.Clusters.removeCluster$(cluster)
                            .map(() => ({success: true, cluster}))
                            .catch((e) => Observable.of({success: false, reason: e, cluster}))
                    )
                );

                return Observable.of({
                    type: REMOVE_CLUSTERS,
                    clusterIDs: clusters.map(get('_id'))
                })
                .merge(
                    updateServer().switchMap((results) => {
                        return results.every(get('success'))
                            ? Observable.empty()
                            : Observable.from([
                                {
                                    type: LOAD_LIST,
                                    list: state.list
                                },
                                {
                                    type: REMOVE_CLUSTERS,
                                    clusterIDs: results.filter(get('success')).map(get('cluster._id'))
                                }
                            ]);
                    })
                );
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
                .merge(
                    ...toAdd.map((c) => sendRequest(c)
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
                    )
                );
            });

        Observable.merge(this.removeClusters$, this.cloneClusters$).subscribe((a) => ConfigureState.dispatchAction(a));
    }

    onStateEnterRedirect(toState) {
        if (toState.name !== 'base.configuration.tabs')
            return this.$q.resolve();

        return this.configuration.read()
            .then((data) => {
                this.loadList(data);

                return this.$q.resolve(data.clusters.length
                    ? 'base.configuration.tabs.advanced'
                    : 'base.configuration.tabs.basic');
            });
    }

    cloneClusters(clusters) {
        this.ConfigureState.dispatchAction({type: CLONE_CLUSTERS, clusters});
    }

    loadList(list) {
        this.ConfigureState.dispatchAction({type: LOAD_LIST, list});
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
            return this.$state.go(
                state.list.clusters.size <= 1
                    ? 'base.configuration.tabs.basic'
                    : 'base.configuration.tabs.advanced.clusters',
                {clusterID}
            );
        })
        .subscribe();
    }
}
