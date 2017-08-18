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
    HIDE_CONFIG_LOADING
} from 'app/components/page-configure/reducer';
import pageConfigureAdvancedClusterComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-cluster/component';
import pageConfigureAdvancedModelsComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-models/component';
import pageConfigureAdvancedCachesComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-caches/component';
import pageConfigureAdvancedIGFSComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-igfs/component';

import {uniqueName} from 'app/utils/uniqueName';
import get from 'lodash/get';

const getErrorMessage = (e) => e.message || e.data || e;

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
                views: {
                    '@': {
                        template: base2
                    }
                }
            })
            .state('base.configuration.overview', {
                url: '/configuration/overview',
                component: 'pageConfigureOverview',
                permission: 'configuration',
                metaTags: {
                    title: 'Configuration'
                },
                resolve: {
                    clustersTable: clustersTableResolve
                }
            })
            .state('base.configuration.tabs', {
                url: '/configuration/:clusterID',
                permission: 'configuration',
                component: 'pageConfigure',
                resolve: {
                    clustersTable: clustersTableResolve,
                    cluster: ['Caches', 'Clusters', '$transition$', 'ConfigureState', 'IgniteMessages', (Caches, Clusters, $transition$, ConfigureState, IgniteMessages) => {
                        const newCluster = () => $transition$.injector().getAsync('clustersTable')
                            .then((clusters) => {
                                return {
                                    ...Clusters.getBlankCluster(),
                                    name: uniqueName('New cluster', [...clusters.values()])
                                };
                            });

                        const {clusterID} = $transition$.params();
                        const cachedValue = get(ConfigureState.state$.value, 'clusters', new Map()).get(clusterID);

                        const cluster = cachedValue
                            ? Promise.resolve(cachedValue)
                            : clusterID === 'new'
                                ? Promise.resolve(newCluster())
                                : Clusters.getCluster(clusterID).then(({data}) => {
                                    ConfigureState.dispatchAction({
                                        type: clustersActionTypes.UPSERT,
                                        items: [data]
                                    });
                                    return data;
                                });

                        ConfigureState.dispatchAction({
                            type: RECEIVE_CLUSTER_EDIT,
                            cluster: null
                        });
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading cluster…'
                        });

                        return cluster.then((cluster) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CLUSTER_EDIT,
                                cluster
                            });
                            return cluster;
                        })
                        .catch((e) => {
                            IgniteMessages.showError(`Failed to load cluster ${clusterID}. ${getErrorMessage(e)}`);
                            $transition$.router.stateService.go('base.configuration.overview', null, {
                                location: 'replace'
                            });
                            return Promise.reject(e);
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                redirectTo: ($transition$) => {
                    const clusters = $transition$.injector().getAsync('clustersTable');
                    const cluster = $transition$.injector().getAsync('cluster');
                    return Promise.all([clusters, cluster]).then(([clusters, cluster]) => {
                        return (clusters.length > 10 || cluster.caches.length > 5)
                            ? 'base.configuration.tabs.advanced'
                            : 'base.configuration.tabs.basic';
                    });
                },
                tfMetaTags: {
                    title: 'Configuration'
                }
            })
            .state('base.configuration.tabs.basic', {
                url: '/basic',
                component: 'pageConfigureBasic',
                permission: 'configuration',
                resolve: {
                    caches: cachesResolve
                },
                onEnter: ['ConfigureState', '$transition$', (ConfigureState, $transition$) => {
                    const caches = $transition$.injector().getAsync('caches');
                    const cluster = $transition$.injector().getAsync('cluster');
                    ConfigureState.dispatchAction({
                        type: basicCachesActionTypes.RESET
                    });
                    Promise.all([caches, cluster]).then(([caches, cluster]) => {
                        ConfigureState.dispatchAction({
                            type: basicCachesActionTypes.LOAD,
                            items: caches
                        });
                    });
                }],
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Basic Configuration'
                }
            })
            .state('base.configuration.tabs.advanced', {
                url: '/advanced',
                component: 'pageConfigureAdvanced',
                permission: 'configuration',
                redirectTo: 'base.configuration.tabs.advanced.cluster'
            })
            .state('base.configuration.tabs.advanced.cluster', {
                url: '/cluster',
                component: pageConfigureAdvancedClusterComponent.name,
                permission: 'configuration',
                resolve: {
                    caches: cachesResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                tfMetaTags: {
                    title: 'Configure Cluster'
                }
            })
            .state('base.configuration.tabs.advanced.caches', {
                url: '/caches',
                permission: 'configuration',
                component: pageConfigureAdvancedCachesComponent.name,
                resolve: {
                    caches: cachesResolve,
                    models: modelsResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                ...resetFormItemToNull({actionType: RECEIVE_CACHE_EDIT, actionKey: 'cache'}),
                redirectTo: ($transition$) => {
                    const cacheStateName = 'base.configuration.tabs.advanced.caches.cache';
                    const fromState = $transition$.from();
                    const toState = $transition$.to();
                    return fromState.name === cacheStateName
                        ? toState
                        : $transition$.injector().getAsync('caches').then((caches) => {
                            return caches.length
                                ? {
                                    state: cacheStateName,
                                    params: {
                                        cacheID: caches[0]._id,
                                        clusterID: $transition$.params().clusterID
                                    }
                                }
                                : toState;
                        });
                },
                tfMetaTags: {
                    title: 'Configure Caches'
                }
            })
            .state('base.configuration.tabs.advanced.caches.cache', {
                url: '/{cacheID:string}',
                permission: 'configuration',
                resolve: {
                    cache: ['IgniteMessages', 'Caches', 'Clusters', '$transition$', 'ConfigureState', (IgniteMessages, Caches, Clusters, $transition$, ConfigureState) => {
                        const cluster = $transition$.injector().getAsync('cluster');
                        const caches = $transition$.injector().getAsync('caches');
                        const {cacheID, clusterID} = $transition$.params();
                        const cachedValue = get(ConfigureState.state$.value, 'caches', new Map()).get(cacheID);

                        const cache = cachedValue
                            ? Promise.resolve(cachedValue)
                            : cacheID === 'new'
                                ? Promise.all([cluster, caches]).then(([cluster, caches]) => Object.assign(Caches.getBlankCache(), {
                                    name: uniqueName('New cache', caches),
                                    clusters: [cluster._id]
                                }))
                                : Caches.getCache(cacheID).then(({data}) => {
                                    ConfigureState.dispatchAction({
                                        type: cachesActionTypes.UPSERT,
                                        items: [data]
                                    });
                                    return data;
                                });

                        return cache.then((cache) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CACHE_EDIT,
                                cache
                            });
                            return cache;
                        })
                        .catch((e) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            $transition$.router.stateService.go('base.configuration.tabs.advanced.caches', null, {
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
            .state('base.configuration.tabs.advanced.models', {
                url: '/models',
                component: pageConfigureAdvancedModelsComponent.name,
                permission: 'configuration',
                resolve: {
                    models: modelsResolve,
                    caches: cachesResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                ...resetFormItemToNull({actionType: RECEIVE_MODEL_EDIT, actionKey: 'model'}),
                redirectTo: ($transition$) => {
                    const modelStateName = 'base.configuration.tabs.advanced.models.model';
                    const fromState = $transition$.from();
                    const toState = $transition$.to();
                    return fromState.name === modelStateName
                        ? toState
                        : $transition$.injector().getAsync('models').then((models) => {
                            return models.length
                                ? {
                                    state: modelStateName,
                                    params: {
                                        modelID: models[0]._id,
                                        clusterID: $transition$.params().clusterID
                                    }
                                }
                                : toState;
                        });
                },
                tfMetaTags: {
                    title: 'Configure SQL Schemes'
                }
            })
            .state('base.configuration.tabs.advanced.models.model', {
                url: '/{modelID:string}',
                resolve: {
                    model: ['IgniteMessages', 'Models', 'Clusters', '$transition$', 'ConfigureState', (IgniteMessages, Models, Clusters, $transition$, ConfigureState) => {
                        const cluster = $transition$.injector().getAsync('cluster');
                        const models = $transition$.injector().getAsync('models');
                        const {modelID, clusterID} = $transition$.params();
                        const cachedValue = get(ConfigureState.state$.value, 'models', new Map()).get(modelID);

                        const model = cachedValue
                            ? Promise.resolve(cachedValue)
                            : modelID === 'new'
                                ? Promise.resolve(Models.getBlankModel())
                                : Models.getModel(modelID).then(({data}) => {
                                    ConfigureState.dispatchAction({
                                        type: modelsActionTypes.UPSERT,
                                        items: [data]
                                    });
                                    return data;
                                });

                        return model.then((model) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_MODEL_EDIT,
                                model
                            });
                            return model;
                        })
                        .catch((e) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            $transition$.router.stateService.go('base.configuration.tabs.advanced.models', null, {
                                location: 'replace'
                            });
                            IgniteMessages.showError(`Failed to load domain model ${modelID} for cluster ${clusterID}. ${getErrorMessage(e)}`);
                            return Promise.reject(e);
                        });
                    }]
                },
                permission: 'configuration',
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            })
            .state('base.configuration.tabs.advanced.igfs', {
                url: '/igfs',
                component: pageConfigureAdvancedIGFSComponent.name,
                permission: 'configuration',
                resolve: {
                    igfss: igfssResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
                redirectTo: ($transition$) => {
                    const igfsStateName = 'base.configuration.tabs.advanced.igfs.igfs';
                    const fromState = $transition$.from();
                    const toState = $transition$.to();
                    return fromState.name === igfsStateName
                        ? toState
                        : $transition$.injector().getAsync('igfss').then((igfss) => {
                            return igfss.length
                                ? {
                                    state: igfsStateName,
                                    params: {
                                        igfsID: igfss[0]._id,
                                        clusterID: $transition$.params().clusterID
                                    }
                                }
                                : toState;
                        });
                },
                tfMetaTags: {
                    title: 'Configure IGFS'
                }
            })
            .state('base.configuration.tabs.advanced.igfs.igfs', {
                url: '/{igfsID:string}',
                permission: 'configuration',
                resolve: {
                    igfs: ['IgniteMessages', 'IGFSs', 'Clusters', '$transition$', 'ConfigureState', (IgniteMessages, IGFSs, Clusters, $transition$, ConfigureState) => {
                        const cluster = $transition$.injector().getAsync('cluster');
                        const igfss = $transition$.injector().getAsync('igfss');
                        const {igfsID, clusterID} = $transition$.params();
                        const cachedValue = get(ConfigureState.state$.value, 'igfss', new Map()).get(igfsID);

                        const igfs = cachedValue
                            ? Promise.resolve(cachedValue)
                            : igfsID === 'new'
                                ? Promise.all([cluster, igfss]).then(([cluster, igfss]) => Object.assign(IGFSs.getBlankIGFS(), {
                                    name: uniqueName('New IGFS', igfss),
                                    cluster: [cluster._id]
                                }))
                                : IGFSs.getIGFS(igfsID).then(({data}) => {
                                    ConfigureState.dispatchAction({
                                        type: igfssActionTypes.UPSERT,
                                        items: [data]
                                    });
                                    return data;
                                });

                        return igfs.then((igfs) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_IGFS_EDIT,
                                igfs
                            });
                            return igfs;
                        })
                        .catch((e) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            $transition$.router.stateService.go('base.configuration.tabs.advanced.igfs', null, {
                                location: 'replace'
                            });
                            IgniteMessages.showError(`Failed to load IGFS ${igfsID} for cluster ${clusterID}. ${getErrorMessage(e)}`);
                            return Promise.reject(e);
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            });
    }]);
