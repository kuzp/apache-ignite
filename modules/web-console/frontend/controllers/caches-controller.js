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
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/distinctUntilChanged';
import infoMessageTemplateUrl from 'views/templates/message.tpl.pug';
import get from 'lodash/get';
import matches from 'lodash/fp/matches';
import angular from 'angular';
import naturalCompare from 'natural-compare-lite';

// Controller for Caches screen.
export default ['PageConfigureAdvancedCaches', 'PageConfigureAdvanced', '$transitions', 'ConfigureState', '$scope', '$http', '$state', '$filter', '$timeout', '$modal', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteLegacyTable', 'IgniteVersion', '$q', 'Caches',
    function(pageService, PageConfigureAdvanced, $transitions, ConfigureState, $scope, $http, $state, $filter, $timeout, $modal, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, Resource, ErrorPopover, FormUtils, LegacyTable, Version, $q, Caches) {
        Object.assign(this, {pageService, PageConfigureAdvanced, $transitions, ConfigureState, $scope, $state, Confirm, Caches, FormUtils});

        this.$onInit = function() {
            const redirects = this.ConfigureState.actions$
                .filter(matches({type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK'}))
                .do(() => {
                    this.$state.go('.', {
                        cacheID: this.clonedCache._id,
                        selectedCaches: [this.clonedCache._id]
                    });
                });

            this.subscription = this.pageService.getObservable()
                .do((state) => this.$scope.$applyAsync(() => Object.assign(this, state)))
                .merge(redirects)
                .subscribe();

            this.tableActions = this.makeTableActions(this.selectedItemIDs);
        };

        this.uiOnParamsChanged = function(params) {
            this.tableActions = this.makeTableActions(params.selectedCaches);
        };

        Object.defineProperty(this, 'selectedItemIDs', {
            get() {
                return this.$state.params.selectedCaches;
            }
        });

        Object.defineProperty(this, 'isNew', {
            get() {
                return this.$state.params.cacheID === 'new';
            }
        });

        this.$onDestroy = function() {
            this.subscription.unsubscribe();
        };

        this.cachesColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                sort: {direction: 'asc', priority: 0},
                filter: {
                    placeholder: 'Filter by key type…'
                },
                sortingAlgorithm: naturalCompare,
                minWidth: 165
            },
            {
                name: 'cacheMode',
                displayName: 'Mode',
                field: 'cacheMode',
                multiselectFilterOptions: Caches.cacheModes,
                width: 160
            },
            {
                name: 'atomicityMode',
                displayName: 'Atomicity',
                field: 'atomicityMode',
                multiselectFilterOptions: Caches.atomicityModes,
                width: 160
            },
            {
                name: 'backups',
                displayName: 'Backups',
                field: 'backups',
                width: 130,
                enableFiltering: false
            }
        ];

        this.makeTableActions = function(selectedItems) {
            return [
                {
                    action: 'Clone',
                    click: () => this.clone(selectedItems),
                    available: true
                },
                {
                    action: 'Delete',
                    click: () => this.remove(selectedItems),
                    available: true
                }
            ];
        };

        this.remove = function(items) {
            this.pageService.remove(items, this.originalCluster);
        };

        this.selectionHook = function(selected) {
            const selectedItemIDs = selected.map((r) => r._id);
            return selectedItemIDs.length === 1
                ? this.$state.go('base.configuration.tabs.advanced.caches.cache', {
                    cacheID: selectedItemIDs[0],
                    selectedCaches: selectedItemIDs
                }, {
                    location: 'replace'
                })
                : this.$state.go('base.configuration.tabs.advanced.caches', {
                    selectedCaches: selectedItemIDs
                }, {
                    location: 'replace'
                });
        };

        this.available = Version.available.bind(Version);

        const rebuildDropdowns = () => {
            $scope.affinityFunction = [
                {value: 'Rendezvous', label: 'Rendezvous'},
                {value: 'Custom', label: 'Custom'},
                {value: null, label: 'Default'}
            ];

            if (this.available(['1.0.0', '2.0.0']))
                $scope.affinityFunction.splice(1, 0, {value: 'Fair', label: 'Fair'});
        };

        rebuildDropdowns();

        const filterModel = () => {
            if ($scope.backupItem) {
                if (this.available('2.0.0')) {
                    if (_.get($scope.backupItem, 'affinity.kind') === 'Fair')
                        $scope.backupItem.affinity.kind = null;
                }
            }
        };

        Version.currentSbj.subscribe({
            next: () => {
                rebuildDropdowns();

                filterModel();
            }
        });

        // UnsavedChangesGuard.install($scope);

        $scope.ui = FormUtils.formUI();
        $scope.ui.activePanels = [0];
        $scope.ui.topPanels = [0, 1, 2, 3];

        function cacheDomains(item) {
            return _.reduce($scope.domains, function(memo, domain) {
                if (item && _.includes(item.domains, domain.value))
                    memo.push(domain.meta);

                return memo;
            }, []);
        }

        // Loading.start('loadingCachesScreen');

        // When landing on the page, get caches and show them.
        // Resource.read()
        //     .then(({spaces, clusters, caches, domains, igfss}) => {
        //         const validFilter = $filter('domainsValidation');

        //         $scope.spaces = spaces;
        //         $scope.caches = caches;
        //         this.cachesTable = this.buildCachesTable($scope.caches);
        //         $scope.igfss = _.map(igfss, (igfs) => ({
        //             label: igfs.name,
        //             value: igfs._id,
        //             igfs
        //         }));

        //         _.forEach($scope.caches, (cache) => cache.label = _cacheLbl(cache));

        //         $scope.clusters = _.map(clusters, (cluster) => ({
        //             value: cluster._id,
        //             label: cluster.name,
        //             discovery: cluster.discovery,
        //             checkpointSpi: cluster.checkpointSpi,
        //             caches: cluster.caches
        //         }));

        //         $scope.domains = _.sortBy(_.map(validFilter(domains, true, false), (domain) => ({
        //             label: domain.valueType,
        //             value: domain._id,
        //             kind: domain.kind,
        //             meta: domain
        //         })), 'label');

        //         selectFirstItem();

        //         $scope.$watch('ui.inputForm.$valid', function(valid) {
        //             if (valid && ModelNormalizer.isEqual(__original_value, $scope.backupItem))
        //                 $scope.ui.inputForm.$dirty = false;
        //         });

        //         $scope.$watch('backupItem', function(val) {
        //             if (!$scope.ui.inputForm)
        //                 return;

        //             const form = $scope.ui.inputForm;

        //             if (form.$valid && ModelNormalizer.isEqual(__original_value, val))
        //                 form.$setPristine();
        //             else
        //                 form.$setDirty();
        //         }, true);

        //         $scope.$watch('backupItem.offHeapMode', setOffHeapMaxMemory);

        //         $scope.$watch('ui.activePanels.length', () => {
        //             ErrorPopover.hide();
        //         });
        //     })
        //     .catch(Messages.showError)
        //     .then(() => {
        //         $scope.ui.ready = true;
        //         $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

        //         Loading.finish('loadingCachesScreen');
        //     });

        // Save cache in database.

        this.save = function(item) {
            // _.merge(item, LegacyUtils.autoCacheStoreConfiguration(item, cacheDomains(item)));
            this.FormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
            if (this.$scope.ui.inputForm.$valid) this.pageService.save(item, this.originalCluster);
        };

        function _cacheNames() {
            return _.map($scope.caches, (cache) => cache.name);
        }

        // Clone cache with new name.
        this.clone = function(items) {
            this.pageService.clone(items);
        };

        $scope.resetAll = function() {
            Confirm.confirm('Are you sure you want to undo all changes for current cache?')
                .then(function() {
                    $scope.backupItem = angular.copy($scope.selectedItem);
                    if ($scope.ui.inputForm) {
                        $scope.ui.inputForm.$error = {};
                        $scope.ui.inputForm.$setPristine();
                    }
                });
        };
    }
];
