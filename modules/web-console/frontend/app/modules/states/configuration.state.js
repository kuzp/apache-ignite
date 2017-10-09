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
import pageConfigureAdvancedClusterComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-cluster/component';
import pageConfigureAdvancedModelsComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-models/component';
import pageConfigureAdvancedCachesComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-caches/component';
import pageConfigureAdvancedIGFSComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-igfs/component';

import get from 'lodash/get';
import {Observable} from 'rxjs/Observable';

const idRegex = `new|[a-z0-9]+`;

const getErrorMessage = (e) => get(e, 'error.data', e);

const shortCachesResolve = ['ConfigSelectors', 'ConfigureState', 'ConfigEffects', '$transition$', (ConfigSelectors, ConfigureState, {etp}, $transition$) => {
    return Observable.fromPromise($transition$.injector().getAsync('_cluster'))
    .switchMap(() => ConfigureState.state$.let(ConfigSelectors.selectCluster($transition$.params().clusterID)).take(1))
    .switchMap((cluster) => {
        return etp('LOAD_SHORT_CACHES', {ids: cluster.caches, clusterID: cluster._id});
    })
    .toPromise();
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
                url: '/configuration',
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
                url: '/overview',
                component: 'pageConfigureOverview',
                permission: 'configuration',
                metaTags: {
                    title: 'Configuration'
                }
            })
            .state('base.configuration.edit', {
                url: `/{clusterID:${idRegex}}`,
                params: {
                    justIDUpdate: {
                        type: 'bool',
                        value: false
                    }
                },
                permission: 'configuration',
                component: 'pageConfigure',
                resolve: {
                    _cluster: ['ConfigEffects', '$transition$', ({etp}, $transition$) => {
                        return $transition$.injector().getAsync('_shortClusters').then(() => {
                            return etp('LOAD_AND_EDIT_CLUSTER', {clusterID: $transition$.params().clusterID});
                        });
                    }]
                },
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
                    _shortCaches: shortCachesResolve
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
                    _shortCaches: shortCachesResolve
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
                url: `/{cacheID:${idRegex}}`,
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
                url: `/{modelID:${idRegex}}`,
                resolve: {
                    _cache: ['IgniteMessages', 'ConfigEffects', '$transition$', (IgniteMessages, {etp}, $transition$) => {
                        const {clusterID, modelID} = $transition$.params();
                        if (modelID === 'new') return Promise.resolve();
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
                url: `/{igfsID:${idRegex}}`,
                permission: 'configuration',
                resolve: {
                    _igfs: ['IgniteMessages', 'ConfigEffects', '$transition$', (IgniteMessages, {etp}, $transition$) => {
                        const {clusterID, igfsID} = $transition$.params();
                        if (igfsID === 'new') return Promise.resolve();
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
