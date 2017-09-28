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

import angular from 'angular';

// Common directives.
import previewPanel from './configuration/preview-panel.directive.js';

// Summary screen.
import ConfigurationResource from './configuration/Configuration.resource';
import IgniteSummaryZipper from './configuration/summary/summary-zipper.service';

import base2 from 'views/base2.pug';

import {RECEIVE_CONFIGURE_OVERVIEW} from 'app/components/page-configure-overview/reducer';
import {
    shortIGFSsActionTypes,
    igfssActionTypes,
    shortModelsActionTypes,
    modelsActionTypes,
    shortClustersActionTypes,
    cachesActionTypes,
    shortCachesActionTypes,
    clustersActionTypes,
    basicCachesActionTypes,
    RECEIVE_CLUSTER_EDIT,
    RECEIVE_CACHE_EDIT,
    RECEIVE_MODELS_EDIT,
    RECEIVE_MODEL_EDIT,
    RECEIVE_IGFSS_EDIT,
    RECEIVE_IGFS_EDIT,
    SHOW_CONFIG_LOADING,
    LOAD_ITEMS,
    HIDE_CONFIG_LOADING,
    selectShortClustersValue,
    selectEditCluster
} from 'app/components/page-configure/reducer';
import pageConfigureAdvancedClusterComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-cluster/component';
import pageConfigureAdvancedModelsComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-models/component';
import pageConfigureAdvancedCachesComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-caches/component';
import pageConfigureAdvancedIGFSComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-igfs/component';

import {uniqueName} from 'app/utils/uniqueName';
import get from 'lodash/get';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/race';

const getErrorMessage = (e) => get(e, 'error.data', e);

const makeClusterItemsResolver = ({
    feedbackName,
    cachePath,
    getItemsMethodName,
    actionTypes
}) => ['Clusters', '$transition$', 'ConfigureState', 'IgniteMessages', (Clusters, $transition$, ConfigureState, IgniteMessages) => {
    const {clusterID} = $transition$.params();
    const cachedValue = get(ConfigureState.state$.value, cachePath);

    if (clusterID === 'new') return Promise.resolve([]);
    if (cachedValue && cachedValue.size) return Promise.resolve([...cachedValue.values()]);

    ConfigureState.dispatchAction({
        type: SHOW_CONFIG_LOADING,
        loadingText: `Loading ${feedbackName}…`
    });

    return Clusters[getItemsMethodName](clusterID).then(({data}) => data).then((items) => {
        ConfigureState.dispatchAction({
            type: HIDE_CONFIG_LOADING
        });
        ConfigureState.dispatchAction({
            type: actionTypes.UPSERT,
            items
        });
        return items;
    })
    .catch((e) => {
        ConfigureState.dispatchAction({
            type: HIDE_CONFIG_LOADING
        });
        $transition$.router.stateService.go('base.configuration.overview', null, {
            location: 'replace'
        });
        IgniteMessages.showError(`Failed to load ${feedbackName} for cluster ${clusterID}. ${getErrorMessage(e)}`);
        return Promise.reject(e);
    });
}];

const clustersTableResolve = ['Clusters', 'ConfigureState', (Clusters, ConfigureState) => {
    const cachedValue = get(ConfigureState.state$.value, 'shortClusters');
    if (cachedValue && cachedValue.size) return Promise.resolve(cachedValue);

    ConfigureState.dispatchAction({
        type: SHOW_CONFIG_LOADING,
        loadingText: 'Loading clusters…'
    });

    return Clusters.getClustersOverview()
    .then(({data}) => {
        ConfigureState.dispatchAction({
            type: HIDE_CONFIG_LOADING
        });

        ConfigureState.dispatchAction({
            type: shortClustersActionTypes.UPSERT,
            items: data
        });

        return data;
    });
}];

export const cachesResolve = makeClusterItemsResolver({
    feedbackName: 'caches',
    cachePath: 'shortCaches',
    getItemsMethodName: 'getClusterCaches',
    actionTypes: shortCachesActionTypes
});

const modelsResolve = makeClusterItemsResolver({
    feedbackName: 'domain models',
    cachePath: 'shortModels',
    getItemsMethodName: 'getClusterModels',
    actionTypes: shortModelsActionTypes
});

const igfssResolve = makeClusterItemsResolver({
    feedbackName: 'IGFSs',
    cachePath: 'shortIGFSs',
    getItemsMethodName: 'getClusterIGFSs',
    actionTypes: shortIGFSsActionTypes
});

const resetFormItemToNull = ({actionKey, actionType}) => {
    const fn = ['ConfigureState', '$transition$', (ConfigureState, $transition$) => {
        if (!$transition$.params().cacheID) {
            ConfigureState.dispatchAction({
                type: actionType,
                [actionKey]: null
            });
        }
        return $transition$;
    }];
    return {onEnter: fn, onRetain: fn};
};

// Observable.prototype.cache = function(times) {
//     return this.publishReplay(times).refCount();
// };

angular.module('ignite-console.states.configuration', ['ui.router'])
    .directive(...previewPanel)
    // Services.
    .service('IgniteSummaryZipper', IgniteSummaryZipper)
    .service('IgniteConfigurationResource', ConfigurationResource)
    // Configure state provider.
    .config(['$stateProvider', ($stateProvider) => {
        // Setup the states.
        $stateProvider
            .state('base.configuration', {
                abstract: true,
                permission: 'configuration',
                onEnter: ['ConfigureState', (ConfigureState) => ConfigureState.dispatchAction({type: 'PRELOAD_STATE', state: {}})],
                views: {
                    '@': {
                        template: base2
                    }
                },
                resolve: {
                    _shortClusters: ['ConfigEffects', ({etp}) => {
                        return etp('LOAD_USER_CLUSTERS');
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            })
            .state('base.configuration.overview', {
                url: '/configuration/overview',
                component: 'pageConfigureOverview',
                permission: 'configuration',
                metaTags: {
                    title: 'Configuration'
                }
            })
            .state('base.configuration.edit', {
                url: '/configuration/:clusterID',
                permission: 'configuration',
                component: 'pageConfigure',
                resolve: {
                    _cluster: ['ConfigEffects', '$transition$', ({etp}, $transition$) => {
                        return $transition$.injector().getAsync('_shortClusters').then(() => {
                            return etp('LOAD_AND_EDIT_CLUSTER', {clusterID: $transition$.params().clusterID});
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                // resolve: {
                //     clustersTable: clustersTableResolve,
                //     cluster: ['Caches', 'Clusters', '$transition$', 'ConfigureState', 'IgniteMessages', (Caches, Clusters, $transition$, ConfigureState, IgniteMessages) => {
                //         const newCluster = () => $transition$.injector().getAsync('clustersTable')
                //             .then((clusters) => {
                //                 return {
                //                     ...Clusters.getBlankCluster(),
                //                     name: uniqueName('New cluster', [...clusters.values()])
                //                 };
                //             });

                //         const {clusterID} = $transition$.params();
                //         const cachedValue = get(ConfigureState.state$.value, 'clusters', new Map()).get(clusterID);

                //         const cluster = cachedValue
                //             ? Promise.resolve(cachedValue)
                //             : clusterID === 'new'
                //                 ? Promise.resolve(newCluster())
                //                 : Clusters.getCluster(clusterID).then(({data}) => {
                //                     ConfigureState.dispatchAction({
                //                         type: clustersActionTypes.UPSERT,
                //                         items: [data]
                //                     });
                //                     return data;
                //                 });

                //         ConfigureState.dispatchAction({
                //             type: RECEIVE_CLUSTER_EDIT,
                //             cluster: null
                //         });
                //         ConfigureState.dispatchAction({
                //             type: SHOW_CONFIG_LOADING,
                //             loadingText: 'Loading cluster…'
                //         });

                //         return cluster.then((cluster) => {
                //             ConfigureState.dispatchAction({
                //                 type: HIDE_CONFIG_LOADING
                //             });
                //             ConfigureState.dispatchAction({
                //                 type: RECEIVE_CLUSTER_EDIT,
                //                 cluster
                //             });
                //             return cluster;
                //         })
                //         .catch((e) => {
                //             IgniteMessages.showError(`Failed to load cluster ${clusterID}. ${getErrorMessage(e)}`);
                //             $transition$.router.stateService.go('base.configuration.overview', null, {
                //                 location: 'replace'
                //             });
                //             return Promise.reject(e);
                //         });
                //     }]
                // },
                // redirectTo: ($transition$) => {
                //     const cluster = $transition$.injector().getAsync('_cluster');
                //     const clusters = $transition$.injector().getAsync('_shortClusters');
                //     return Promise.all([clusters, cluster]).then(([clusters, cluster]) => {
                //         return (clusters.value.size > 10 || cluster.caches.length > 5)
                //             ? 'base.configuration.edit.advanced'
                //             : 'base.configuration.edit.basic';
                //     });
                // },
                // redirectTo: 'base.configuration.edit.advanced',
                redirectTo: ($transition$) => {
                    const [ConfigureState, ConfigSelectors] = ['ConfigureState', 'ConfigSelectors'].map((t) => $transition$.injector().get(t));
                    const waitFor = ['_cluster', '_shortClusters'].map((t) => $transition$.injector().getAsync(t));
                    return Observable.fromPromise(Promise.all(waitFor)).switchMap(() => {
                        return Observable.combineLatest(
                            ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1),
                            ConfigureState.state$.let(ConfigSelectors.selectShortCaches()).take(1)
                        );
                    })
                    .map(([cluster = {caches: []}, clusters]) => {
                        return (clusters.value.size > 10 || cluster.caches.length > 5)
                            ? 'base.configuration.edit.advanced'
                            : 'base.configuration.edit.basic';
                    })
                    .toPromise();
                },
                tfMetaTags: {
                    title: 'Configuration'
                }
            })
            .state('base.configuration.edit.basic', {
                url: '/basic',
                component: 'pageConfigureBasic',
                permission: 'configuration',
                resolve: {
                    _shortCaches: ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
                        return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
                        .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
                        .switchMap((cluster) => {
                            return cluster
                                ? etp('LOAD_SHORT_CACHES', {ids: cluster.caches, clusterID: cluster._id})
                                : Observable.of(true);
                        })
                        .toPromise();
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Basic Configuration'
                }
            })
            .state('base.configuration.edit.advanced', {
                url: '/advanced',
                component: 'pageConfigureAdvanced',
                permission: 'configuration',
                redirectTo: 'base.configuration.edit.advanced.cluster'
            })
            .state('base.configuration.edit.advanced.cluster', {
                url: '/cluster',
                component: pageConfigureAdvancedClusterComponent.name,
                permission: 'configuration',
                resolve: {
                    _shortCaches: ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
                        return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
                        .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
                        .switchMap((cluster) => {
                            return etp('LOAD_SHORT_CACHES', {ids: cluster.caches, clusterID: cluster._id});
                        })
                        .toPromise();
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure Cluster'
                }
            })
            .state('base.configuration.edit.advanced.caches', {
                url: '/caches',
                permission: 'configuration',
                component: pageConfigureAdvancedCachesComponent.name,
                resolve: {
                    _shortCachesAndModels: ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
                        return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
                        .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
                        .map((cluster) => {
                            return Promise.all([
                                etp('LOAD_SHORT_CACHES', {ids: cluster.caches, clusterID: cluster._id}),
                                etp('LOAD_SHORT_MODELS', {ids: cluster.models, clusterID: cluster._id})
                            ]);
                        })
                        .toPromise();
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure Caches'
                }
            })
            .state('base.configuration.edit.advanced.caches.cache', {
                url: '/{cacheID:string}',
                permission: 'configuration',
                resolve: {
                    _cache: ['IgniteMessages', 'ConfigEffects', '$transition$', (IgniteMessages, {etp}, $transition$) => {
                        const {clusterID, cacheID} = $transition$.params();
                        if (cacheID === 'new') return Promise.resolve();
                        return etp('LOAD_CACHE', {cacheID})
                        .catch((e) => {
                            $transition$.router.stateService.go('base.configuration.edit.advanced.caches', null, {
                                location: 'replace'
                            });
                            IgniteMessages.showError(`Failed to load cache ${cacheID} for cluster ${clusterID}. ${getErrorMessage(e)}`);
                            return Promise.reject(e);
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure Caches'
                }
            })
            .state('base.configuration.edit.advanced.models', {
                url: '/models',
                component: pageConfigureAdvancedModelsComponent.name,
                permission: 'configuration',
                resolve: {
                    _shortCachesAndModels: ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
                        return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
                        .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
                        .map((cluster) => {
                            return Promise.all([
                                etp('LOAD_SHORT_CACHES', {ids: cluster.caches, clusterID: cluster._id}),
                                etp('LOAD_SHORT_MODELS', {ids: cluster.models, clusterID: cluster._id})
                            ]);
                        })
                        .toPromise();
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure SQL Schemes'
                }
            })
            .state('base.configuration.edit.advanced.models.model', {
                url: '/{modelID:string}',
                resolve: {
                    _cache: ['IgniteMessages', 'ConfigEffects', '$transition$', (IgniteMessages, {etp}, $transition$) => {
                        const {clusterID, modelID} = $transition$.params();
                        return etp('LOAD_MODEL', {modelID})
                        .catch((e) => {
                            $transition$.router.stateService.go('base.configuration.edit.advanced.models', null, {
                                location: 'replace'
                            });
                            IgniteMessages.showError(`Failed to load model ${modelID} for cluster ${clusterID}. ${getErrorMessage(e)}`);
                            return Promise.reject(e);
                        });
                    }]
                },
                permission: 'configuration',
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            })
            .state('base.configuration.edit.advanced.igfs', {
                url: '/igfs',
                component: pageConfigureAdvancedIGFSComponent.name,
                permission: 'configuration',
                resolve: {
                    _shortIGFSs: ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
                        return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
                        .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
                        .map((cluster) => {
                            return Promise.all([
                                etp('LOAD_SHORT_IGFSS', {ids: cluster.igfss, clusterID: cluster._id})
                            ]);
                        })
                        .toPromise();
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure IGFS'
                }
            })
            .state('base.configuration.edit.advanced.igfs.igfs', {
                url: '/{igfsID:string}',
                permission: 'configuration',
                resolve: {
                    _igfs: ['IgniteMessages', 'ConfigEffects', '$transition$', (IgniteMessages, {etp}, $transition$) => {
                        const {clusterID, igfsID} = $transition$.params();
                        return etp('LOAD_IGFS', {igfsID})
                        .catch((e) => {
                            $transition$.router.stateService.go('base.configuration.edit.advanced.igfs', null, {
                                location: 'replace'
                            });
                            IgniteMessages.showError(`Failed to load IGFS ${igfsID} for cluster ${clusterID}. ${getErrorMessage(e)}`);
                            console.debug(e);
                            return Promise.reject(e);
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            });
    }]);
