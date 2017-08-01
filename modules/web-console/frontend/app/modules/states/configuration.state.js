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

import clustersCtrl from 'Controllers/clusters-controller';
import domainsCtrl from 'Controllers/domains-controller';
import cachesCtrl from 'Controllers/caches-controller';
import igfsCtrl from 'Controllers/igfs-controller';

import base2 from 'views/base2.pug';

import {RECEIVE_CONFIGURE_OVERVIEW} from 'app/components/page-configure-overview/reducer';
import {RECEIVE_CLUSTER_EDIT} from 'app/components/page-configure/reducer';

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
                url: '/configuration/{clusterID:string}',
                permission: 'configuration',
                component: 'pageConfigure',
                resolve: {
                    cluster: ['Caches', 'Clusters', '$transition$', 'ConfigureState', (Caches, Clusters, $transition$, ConfigureState) => {
                        const newCluster = Promise.resolve(Clusters.getBlankCluster());
                        const {clusterID} = $transition$.params();
                        const cluster = clusterID === 'new'
                            ? newCluster
                            : Clusters.getCluster(clusterID).then(({data}) => data);
                        const caches = clusterID === 'new'
                            ? Promise.resolve([])
                            // : Caches.getCachesOverview().then(({data}) => data);
                            : Clusters.getClusterCaches(clusterID).then(({data}) => data);

                        return Promise.all([cluster, caches]).then(([cluster, caches]) => {
                            ConfigureState.dispatchAction({
                                type: RECEIVE_CLUSTER_EDIT,
                                cluster,
                                caches
                            });
                            return cluster;
                        });
                    }]
                },
                redirectTo: ($transition$) => {
                    return $transition$.injector().getAsync('cluster').then((cluster) => {
                        return cluster.caches.length > 10
                            ? 'base.configuration.tabs.advanced'
                            : 'base.configuration.tabs.basic';
                    });
                },
                // redirectTo: (trans) => {
                //     const PageConfigure = trans.injector().get('PageConfigure');

                //     return PageConfigure.onStateEnterRedirect(trans.to());
                // },
                tfMetaTags: {
                    title: 'Configuration'
                }
            })
            .state('base.configuration.tabs.basic', {
                url: '/basic',
                permission: 'configuration',
                // url: '/basic?{clusterID:string}',
                // template: '<page-configure-basic></page-configure-basic>',
                tfMetaTags: {
                    title: 'Basic Configuration'
                }
                /*                resolve: {
                    cluster: ['Clusters', 'ConfigureState', '$q', '$transition$', (Clusters, ConfigureState, $q, $transition$) => {
                        return $q.all([
                            Clusters.getCluster($transition$.params().clusterID)
                        ]);
                    }]
                  list: ['IgniteConfigurationResource', 'PageConfigure', (configuration, pageConfigure) => {
                        // TODO IGNITE-5271: remove when advanced config is hooked into ConfigureState too.
                        // This resolve ensures that basic always has fresh data, i.e. after going back from advanced
                        // after adding a cluster.
                        return configuration.read().then((data) => {
                            pageConfigure.loadList(data);
                        });
                    }]
                }*/
            })
            .state('base.configuration.tabs.advanced', {
                url: '/advanced',
                component: 'pageConfigureAdvanced',
                redirectTo: 'base.configuration.tabs.advanced.cluster'
            })
            .state('base.configuration.tabs.advanced.cluster', {
                url: '/cluster',
                templateUrl: clustersTpl,
                permission: 'configuration',
                tfMetaTags: {
                    title: 'Configure Clusters'
                },
                // params: {
                //     clusterID: {
                //         dynamic: true
                //     }
                // },
                controller: clustersCtrl,
                controllerAs: '$ctrl'
            })
            .state('base.configuration.tabs.advanced.caches', {
                url: '/caches?{clusterID:string}',
                templateUrl: cachesTpl,
                permission: 'configuration',
                tfMetaTags: {
                    title: 'Configure Caches'
                },
                controller: cachesCtrl,
                controllerAs: '$ctrl'
            })
            .state('base.configuration.tabs.advanced.domains', {
                url: '/domains?{clusterID:string}',
                templateUrl: domainsTpl,
                permission: 'configuration',
                tfMetaTags: {
                    title: 'Configure Domain Model'
                },
                controller: domainsCtrl,
                controllerAs: '$ctrl'
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
