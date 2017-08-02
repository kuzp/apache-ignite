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

import clustersTpl from 'views/configuration/clusters.tpl.pug';
import cachesTpl from 'views/configuration/caches.tpl.pug';
import domainsTpl from 'views/configuration/domains.tpl.pug';
import igfsTpl from 'views/configuration/igfs.tpl.pug';

// import clustersCtrl from 'Controllers/clusters-controller';
import domainsCtrl from 'Controllers/domains-controller';
import cachesCtrl from 'Controllers/caches-controller';
import igfsCtrl from 'Controllers/igfs-controller';

import base2 from 'views/base2.pug';

import {RECEIVE_CONFIGURE_OVERVIEW} from 'app/components/page-configure-overview/reducer';
import {RECEIVE_CLUSTER_EDIT, RECEIVE_CACHES_EDIT, RECEIVE_CACHE_EDIT} from 'app/components/page-configure/reducer';
import pageConfigureAdvancedClusterComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-cluster/component';
import pageConfigureAdvancedDomainsComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-domains/component';
import pageConfigureAdvancedCachesComponent from 'app/components/page-configure-advanced/components/page-configure-advanced-caches/component';

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
                // template: '<page-configure-overview></page-configure-overview>',
                metaTags: {
                    title: 'Configuration'
                },
                resolve: {
                    clustersTable: ['Clusters', 'ConfigureState', (Clusters, ConfigureState) => {
                        return Clusters.getClustersOverview()
                        .then(({data}) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CONFIGURE_OVERVIEW,
                                clustersTable: data
                            });
                        });
                    }]
                }
            })
            .state('base.configuration.tabs', {
                url: '/configuration/:clusterID',
                permission: 'configuration',
                component: 'pageConfigure',
                resolve: {
                    cluster: ['Caches', 'Clusters', '$transition$', 'ConfigureState', (Caches, Clusters, $transition$, ConfigureState) => {
                        const newCluster = Promise.resolve(Clusters.getBlankCluster());
                        const {clusterID} = $transition$.params();
                        const cluster = clusterID === 'new'
                            ? newCluster
                            : Clusters.getCluster(clusterID).then(({data}) => data);

                        return cluster.then((cluster) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CLUSTER_EDIT,
                                cluster
                            });
                            return cluster;
                        });
                    }]
                },
                redirectTo: ($transition$) => {
                    return $transition$.injector().getAsync('cluster').then((cluster) => {
                        return cluster.caches.length > 5
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
                permission: 'configuration',
                resolve: {
                    caches: ['Clusters', '$transition$', 'ConfigureState', (Clusters, $transition$, ConfigureState) => {
                        const {clusterID} = $transition$.params();
                        const caches = clusterID === 'new'
                            ? Promise.resolve([])
                            : Clusters.getClusterCaches(clusterID).then(({data}) => data);

                        return caches.then((caches) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CACHES_EDIT,
                                caches
                            });
                            return caches;
                        });
                    }]
                },
                tfMetaTags: {
                    title: 'Basic Configuration'
                }
            })
            .state('base.configuration.tabs.advanced', {
                url: '/advanced',
                component: 'pageConfigureAdvanced',
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
                    caches: ['Clusters', '$transition$', 'ConfigureState', (Clusters, $transition$, ConfigureState) => {
                        const {clusterID} = $transition$.params();
                        return clusterID === 'new'
                            ? Promise.resolve([])
                            : Clusters.getClusterCaches(clusterID).then(({data}) => {
                                ConfigureState.dispatchAction({
                                    type: RECEIVE_CACHES_EDIT,
                                    caches: data
                                });
                                return data;
                            });
                    }]
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
                                        cacheID: caches[0]._id
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
                resolve: {
                    cache: ['Caches', 'Clusters', '$transition$', 'ConfigureState', (Caches, Clusters, $transition$, ConfigureState) => {
                        const {cacheID} = $transition$.params();
                        console.debug(`Loading cache: ${cacheID}`);
                        const cache = cacheID
                            ? Caches.getCache(cacheID).then(({data}) => data)
                            : Promise.resolve(null);

                        return cache.then((cache) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CACHE_EDIT,
                                cache
                            });
                            return cache;
                        });
                    }]
                },
                tfMetaTags: {
                    title: 'Configure Caches'
                }
            })
            .state('base.configuration.tabs.advanced.domains', {
                url: '/domains',
                component: pageConfigureAdvancedDomainsComponent.name,
                permission: 'configuration',
                tfMetaTags: {
                    title: 'Configure Domain Model'
                }
            })
            .state('base.configuration.tabs.advanced.igfs', {
                url: '/igfs?{clusterID:string}',
                templateUrl: igfsTpl,
                permission: 'configuration',
                tfMetaTags: {
                    title: 'Configure IGFS'
                },
                controller: igfsCtrl,
                controllerAs: '$ctrl'
            });
    }]);
