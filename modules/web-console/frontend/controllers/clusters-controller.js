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

// import get from 'lodash/get';
// import isEqual from 'lodash/isEqual';
// import matches from 'lodash/fp/matches';
// import cloneDeep from 'lodash/cloneDeep';

// Controller for Clusters screen.
export default ['ConfigSelectors', 'IgniteModelNormalizer', 'PageConfigureAdvancedCluster', 'ConfigureState', '$rootScope', '$scope', '$http', '$state', '$timeout', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteEventGroups', 'DemoInfo', 'IgniteLegacyTable', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteVersion', 'Clusters', 'ConfigurationDownload', '$q',
    function(ConfigSelectors, IgniteModelNormalizer, pageService, ConfigureState, $root, $scope, $http, $state, $timeout, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, igniteEventGroups, DemoInfo, LegacyTable, Resource, ErrorPopover, FormUtils, Version, Clusters, ConfigurationDownload, $q) {
        Object.assign(this, {ConfigSelectors, IgniteModelNormalizer, $state, pageService, ConfigureState, Clusters, $scope, Confirm, FormUtils, Version});

        this.$onInit = function() {
            const clusterID$ = this.$uiRouter.globals.params$.pluck('clusterID');
            this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectShortCachesValue());
            this.originalCluster$ = clusterID$.distinctUntilChanged().switchMap((id) => {
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectClusterToEdit(id));
            });
            this.isNew$ = clusterID$.map((id) => id === 'new');
            this.isBlocked$ = clusterID$;
        };

        this.save = function(cache) {
            this.conf.saveAdvanced({cache});
        };
    }
];
