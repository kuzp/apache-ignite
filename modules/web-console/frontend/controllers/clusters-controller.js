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

import get from 'lodash/get';
import isEqual from 'lodash/isEqual';

// Controller for Clusters screen.
export default ['IgniteModelNormalizer', 'PageConfigureAdvancedCluster', 'ConfigureState', '$rootScope', '$scope', '$http', '$state', '$timeout', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteEventGroups', 'DemoInfo', 'IgniteLegacyTable', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteVersion', 'Clusters', 'ConfigurationDownload', '$q',
    function(IgniteModelNormalizer, pageService, ConfigureState, $root, $scope, $http, $state, $timeout, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, igniteEventGroups, DemoInfo, LegacyTable, Resource, ErrorPopover, FormUtils, Version, Clusters, ConfigurationDownload, $q) {
        Object.assign(this, {IgniteModelNormalizer, pageService, ConfigureState, Clusters, $scope, Confirm, FormUtils, Version});

        this.available = function(...args) {
            return this.Version.available(...args);
        };

        this.$onInit = function() {
            this.subscription = this.pageService.getObservable()
                .do((state) => this.$scope.$applyAsync(() => Object.assign(this, state)))
                .subscribe();

            let __original_value;

            const rebuildDropdowns = () => {
                this.eventStorage = [
                    {value: 'Memory', label: 'Memory'},
                    {value: 'Custom', label: 'Custom'}
                ];

                this.marshallerVariant = [
                    {value: 'JdkMarshaller', label: 'JdkMarshaller'},
                    {value: null, label: 'Default'}
                ];

                if (this.available('2.0.0')) {
                    this.eventStorage.push({value: null, label: 'Disabled'});

                    this.eventGroups = _.filter(igniteEventGroups, ({value}) => value !== 'EVTS_SWAPSPACE');
                }
                else {
                    this.eventGroups = igniteEventGroups;

                    this.marshallerVariant.splice(0, 0, {value: 'OptimizedMarshaller', label: 'OptimizedMarshaller'});
                }
            };

            rebuildDropdowns();

            const filterModel = (cluster) => {
                if (cluster) {
                    if (this.available('2.0.0')) {
                        const evtGrps = _.map(this.eventGroups, 'value');

                        // _.remove(__original_value, (evtGrp) => !_.includes(evtGrps, evtGrp));
                        _.remove(cluster.includeEventTypes, (evtGrp) => !_.includes(evtGrps, evtGrp));

                        if (_.get(cluster, 'marshaller.kind') === 'OptimizedMarshaller')
                            cluster.marshaller.kind = null;
                    }
                    else if (cluster && !_.get(cluster, 'eventStorage.kind'))
                        _.set(cluster, 'eventStorage.kind', 'Memory');
                }
            };

            this.versionSubscription = this.Version.currentSbj.subscribe({
                next: () => {
                    rebuildDropdowns();
                    filterModel(this.clonedCluster);
                }
            });
            this.supportedJdbcTypes = LegacyUtils.mkOptions(LegacyUtils.SUPPORTED_JDBC_TYPES);

            $scope.ui = FormUtils.formUI();
            $scope.ui.loadedPanels = ['checkpoint', 'serviceConfiguration', 'odbcConfiguration'];
            $scope.ui.activePanels = [0];
            $scope.ui.topPanels = [0];
        };

        this.$onDestroy = function() {
            this.subscription.unsubscribe();
            this.versionSubscription.unsubscribe();
        };

        this.uiCanExit = function() {
            // TODO Refactor this
            const items = [this.originalCluster, this.clonedCluster].map((c) => this.IgniteModelNormalizer.normalize(c));
            return isEqual(...items)
                ? true
                : this.Confirm.confirm('You have unsaved changes. Are you sure want to discard them?');
        };

        this.cancelEdit = () => this.pageService.cancelEdit();
        this.downloadConfiguration = (cluster) => ConfigurationDownload.downloadClusterConfiguration(cluster);
        this.save = function(cluster = this.clonedCluster) {
            this.FormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
            if (this.$scope.ui.inputForm.$valid) this.pageService.save(this.clonedCluster);
        };
    }
];
