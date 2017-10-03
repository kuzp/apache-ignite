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

import JSZip from 'jszip';
import cloneDeep from 'lodash/cloneDeep';
import groupBy from 'lodash/groupBy';
import map from 'lodash/map';
import get from 'lodash/get';
import values from 'lodash/values';
import reduce from 'lodash/reduce';
import modalTemplate from './modal.pug';
import {Observable} from 'rxjs/Observable';

export default class ProjectStructurePreviewController {
    static $inject = [
        '$modal',
        '$rootScope'
    ];

    constructor($modal, $rootScope) {
        Object.assign(this, {$modal, $rootScope});
    }

    $onDestroy() {
        this.$rootScope = null;
    }

    showModal() {
        this.$modal({
            title: 'See project structure',
            template: modalTemplate,
            resolve: {
                cluster: () => this.cluster
            },
            controller: class ProjectStructurePreviewModalController {
                static $inject = [
                    'ConfigureState',
                    'ConfigSelectors',
                    'IgniteConfigurationResource',
                    'IgniteSummaryZipper',
                    '$rootScope',
                    'IgniteVersion',
                    '$scope',
                    'cluster',
                    'ConfigurationDownload',
                    'IgniteLoading',
                    'IgniteMessages'
                ];

                constructor(ConfigureState, ConfigSelectors, IgniteConfigurationResource, summaryZipper, $rootScope, IgniteVersion, $scope, cluster, ConfigurationDownload, IgniteLoading, IgniteMessages) {
                    Object.assign(this, {ConfigureState, ConfigSelectors, IgniteConfigurationResource, summaryZipper, $rootScope, IgniteVersion, $scope, cluster, ConfigurationDownload, IgniteLoading, IgniteMessages});
                    this.$onInit();
                }

                $onInit() {
                    this.treeOptions = {
                        nodeChildren: 'children',
                        dirSelectable: false,
                        injectClasses: {
                            iExpanded: 'fa fa-folder-open-o',
                            iCollapsed: 'fa fa-folder-o'
                        }
                    };
                    this.doStuff(this.cluster, !!this.$rootScope.IgniteDemoMode);
                }

                showPreview(node) {
                    this.fileText = '';
                    if (!node) return;
                    this.fileExt = node.file.name.split('.').reverse()[0].toLowerCase();
                    if (node.file.dir) return;
                    node.file.async('string').then((text) => {
                        this.fileText = text;
                        this.$scope.$applyAsync();
                    });
                }

                loadData({clusterID, isDemo}) {
                    return Observable.merge(
                        Observable.timer(1).take(1).do(() => this.ConfigureState.dispatchAction({type: 'LOAD_ALL_CONFIGURATIONS'})).ignoreElements(),
                        this.ConfigureState.state$.let(this.ConfigSelectors.selectCompleteClusterConfiguration({clusterID, isDemo}))
                    )
                    .take(1)
                    .toPromise();
                }

                doStuff(cluster, isDemo) {
                    this.IgniteLoading.start('projectStructurePreview');
                    return this.loadData({clusterID: cluster._id, isDemo})
                    .then((data) => {
                        return this.IgniteConfigurationResource.populate(data);
                    })
                    .then(({clusters}) => {
                        return clusters.find(({_id}) => _id === cluster._id);
                    })
                    .then((cluster) => {
                        return this.summaryZipper({
                            cluster,
                            data: {},
                            IgniteDemoMode: this.$rootScope.IgniteDemoMode,
                            targetVer: this.IgniteVersion.currentSbj.getValue()
                        });
                    })
                    .then(JSZip.loadAsync)
                    .then((val) => {
                        const convert = (files) => {
                            return Object.keys(files)
                            .map((path, i, paths) => ({
                                fullPath: path,
                                path: path.replace(/\/$/, ''),
                                file: files[path],
                                parent: files[paths.filter((p) => path.startsWith(p) && p !== path).sort((a, b) => b.length - a.length)[0]]
                            }))
                            .map((node, i, nodes) => Object.assign(node, {
                                path: node.parent ? node.path.replace(node.parent.name, '') : node.path,
                                children: nodes.filter((n) => n.parent && n.parent.name === node.file.name)
                            }));
                        };

                        const nodes = convert(val.files);

                        this.data = [{
                            path: this.ConfigurationDownload.nameFile(cluster),
                            file: {dir: true},
                            children: nodes.filter((n) => !n.parent)
                        }];

                        this.selectedNode = nodes.find((n) => n.path.includes('server.xml'));
                        this.expandedNodes = [
                            ...this.data,
                            ...nodes.filter((n) => {
                                return !n.fullPath.startsWith('src/main/java/')
                                    || /src\/main\/java(\/(config|load|startup))?\/$/.test(n.fullPath);
                            })
                        ];
                        this.showPreview(this.selectedNode);
                        this.IgniteLoading.finish('projectStructurePreview');
                    })
                    .catch((e) => {
                        this.IgniteMessages.showError(`Failed to generate project files. ${e.message}`);
                        this.$scope.$hide();
                    });
                }

                orderBy() {
                    return;
                }
            },
            controllerAs: '$ctrl',
            show: true
        });
    }
}
