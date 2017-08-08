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
    RECEIVE_CLUSTER_EDIT,
    RECEIVE_CACHES_EDIT,
    RECEIVE_CACHE_EDIT,
    RECEIVE_MODELS_EDIT,
    RECEIVE_MODEL_EDIT,
    RECEIVE_IGFSS_EDIT,
    RECEIVE_IGFS_EDIT,
    SHOW_CONFIG_LOADING,
    HIDE_CONFIG_LOADING
} from 'app/components/page-configure/reducer';
import pageConfigureAdvancedClusterComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-cluster/component';
import pageConfigureAdvancedModelsComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-models/component';
import pageConfigureAdvancedCachesComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-caches/component';
import pageConfigureAdvancedIGFSComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-igfs/component';

import {uniqueName} from 'app/utils/uniqueName';

const getErrorMessage = (e) => e.message || e.data || e;

// TODO: move into effects or service
export const cachesResolve = ['Clusters', '$transition$', 'ConfigureState', 'IgniteMessages', (Clusters, $transition$, ConfigureState, IgniteMessages) => {
    const {clusterID} = $transition$.params();
    ConfigureState.dispatchAction({
        type: SHOW_CONFIG_LOADING,
        loadingText: 'Loading caches…'
    });
    const caches = clusterID === 'new'
        ? Promise.resolve([])
        : Clusters.getClusterCaches(clusterID).then(({data}) => data);

    return caches.then((caches) => {
        ConfigureState.dispatchAction({
            type: HIDE_CONFIG_LOADING
        });
        ConfigureState.dispatchAction({
            type: RECEIVE_CACHES_EDIT,
            caches
        });
        return caches;
    })
    .catch((e) => {
        ConfigureState.dispatchAction({
            type: HIDE_CONFIG_LOADING
        });
        $transition$.router.stateService.go('base.configuration.overview', null, {
            location: 'replace'
        });
        IgniteMessages.showError(`Failed to load caches for cluster ${clusterID}. ${getErrorMessage(e)}`);
        return Promise.reject(e);
    });
}];

const clustersTableResolve = ['Clusters', 'ConfigureState', (Clusters, ConfigureState) => {
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
            type: RECEIVE_CONFIGURE_OVERVIEW,
            clustersTable: data
        });
        return data;
    });
}];

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
                        const newCluster = Promise.resolve(Clusters.getBlankCluster());
                        const {clusterID} = $transition$.params();
                        const cluster = clusterID === 'new'
                            ? newCluster
                            : Clusters.getCluster(clusterID).then(({data}) => data);

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
                tfMetaTags: {
                    title: 'Configure Cluster'
                }
            })
            .state('base.configuration.tabs.advanced.caches', {
                url: '/caches',
                permission: 'configuration',
                component: pageConfigureAdvancedCachesComponent.name,
                resolve: {
                    caches: cachesResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
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
                    cache: ['Caches', 'Clusters', '$transition$', 'ConfigureState', (Caches, Clusters, $transition$, ConfigureState) => {
                        const {cacheID} = $transition$.params();
                        const cache = cacheID
                            ? Caches.getCache(cacheID).then(({data}) => data)
                            : Promise.resolve(null);
                        ConfigureState.dispatchAction({
                            type: RECEIVE_CACHE_EDIT,
                            cache: null
                        });
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading cache…'
                        });
                        return cache.then((cache) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
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
                    models: ['Clusters', '$transition$', 'ConfigureState', (Clusters, $transition$, ConfigureState) => {
                        const {clusterID} = $transition$.params();
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading models…'
                        });
                        return clusterID === 'new'
                            ? Promise.resolve([])
                            : Clusters.getClusterModels(clusterID).then(({data}) => {
                                ConfigureState.dispatchAction({
                                    type: HIDE_CONFIG_LOADING
                                });
                                ConfigureState.dispatchAction({
                                    type: RECEIVE_MODELS_EDIT,
                                    models: data
                                });
                                return data;
                            });
                    }],
                    caches: cachesResolve
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                },
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
                    model: ['Models', '$transition$', 'ConfigureState', (Models, $transition$, ConfigureState) => {
                        const {modelID, clusterID} = $transition$.params();
                        const model = modelID
                            ? Models.getModel(modelID).then(({data}) => data)
                            : Promise.resolve(null);

                        ConfigureState.dispatchAction({
                            type: RECEIVE_MODEL_EDIT,
                            model: null
                        });
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading model…'
                        });

                        return model.then((model) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            ConfigureState.dispatchAction({
                                type: RECEIVE_MODEL_EDIT,
                                model
                            });
                            return model;
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
                    igfss: ['Clusters', '$transition$', 'ConfigureState', (Clusters, $transition$, ConfigureState) => {
                        const {clusterID} = $transition$.params();
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading IGFSs…'
                        });
                        return clusterID === 'new'
                            ? Promise.resolve([])
                            : Clusters.getClusterIGFSs(clusterID).then(({data}) => {
                                ConfigureState.dispatchAction({
                                    type: HIDE_CONFIG_LOADING
                                });
                                ConfigureState.dispatchAction({
                                    type: RECEIVE_IGFSS_EDIT,
                                    igfss: data
                                });
                                return data;
                            });
                    }]
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
                    igfs: ['IGFSs', '$transition$', 'ConfigureState', (IGFSs, $transition$, ConfigureState) => {
                        const {igfsID, clusterID} = $transition$.params();
                        const igfs = igfsID
                            ? IGFSs.getIGFS(igfsID).then(({data}) => data)
                            : Promise.resolve(null);
                        ConfigureState.dispatchAction({
                            type: RECEIVE_IGFS_EDIT,
                            igfs: null
                        });
                        ConfigureState.dispatchAction({
                            type: SHOW_CONFIG_LOADING,
                            loadingText: 'Loading IGFS…'
                        });

                        return igfs.then((igfs) => {
                            ConfigureState.dispatchAction({
                                type: HIDE_CONFIG_LOADING
                            });
                            ConfigureState.dispatchAction({
                                type: RECEIVE_IGFS_EDIT,
                                igfs
                            });
                            return igfs;
                        });
                    }]
                },
                resolvePolicy: {
                    async: 'NOWAIT'
                }
            });
    }]);
