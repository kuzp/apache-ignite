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

import {Subject} from 'rxjs/Subject';
import {merge} from 'rxjs/observable/merge';
import get from 'lodash/get';
import cloneDeep from 'lodash/cloneDeep';
import naturalCompare from 'natural-compare-lite';

// Controller for Caches screen.
export default ['configSelectionManager', '$uiRouter', 'PageConfigureAdvancedCaches', '$transitions', 'ConfigureState', '$scope', '$http', '$state', '$filter', '$timeout', '$modal', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteLegacyTable', 'IgniteVersion', '$q', 'Caches',
    function(configSelectionManager, $uiRouter, pageService, $transitions, ConfigureState, $scope, $http, $state, $filter, $timeout, $modal, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, Resource, ErrorPopover, FormUtils, LegacyTable, Version, $q, Caches) {
        Object.assign(this, {configSelectionManager, $uiRouter, pageService, $transitions, ConfigureState, $scope, $state, Confirm, Caches, FormUtils});

        this.visibleRows$ = new Subject();
        this.selectedRows$ = new Subject();
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

        this.$onInit = function() {
            const cacheID$ = this.$uiRouter.globals.params$.pluck('cacheID');

            this.isNew$ = cacheID$.map((id) => id === 'new');
            this.selectionManager = this.configSelectionManager({
                itemID$: cacheID$,
                selectedItemRows$: this.selectedRows$,
                visibleRows$: this.visibleRows$,
                getLoadedLength: () => get(this, 'clusterItems.shortCaches.length')
            });

            this.subscription = merge(
                this.selectionManager.editGoes$.do((id) => this.edit(id)),
                this.selectionManager.editLeaves$.do(() => this.$state.go('base.configuration.edit.advanced.caches'))
            ).subscribe();

            this.isBlocked$ = cacheID$;

            this.tableActions$ = this.selectionManager.selectedItemIDs$.map((selectedItems) => [
                {
                    action: 'Clone',
                    click: () => this.clone(selectedItems),
                    available: true
                },
                {
                    action: 'Delete',
                    click: () => {
                        this.remove(selectedItems);
                    },
                    available: true
                }
            ]);
        };

        this.remove = function(itemIDs) {
            this.onItemRemove({$event: {itemIDs, type: 'caches', andSave: true}});
        };

        this.$onDestroy = function() {
            this.subscription.unsubscribe();
            this.visibleRows$.unsubscribe();
            this.selectedRows$.unsubscribe();
        };

        this.edit = (cacheID) => this.$state.go('base.configuration.edit.advanced.caches.cache', {cacheID});

        // this.remove = function(items) {
        //     this.pageService.remove(items, this.originalCluster);
        // };

        /*        this.selectionHook = function(selected) {
            const selectedItemIDs = selected.map((r) => r._id);
            return selectedItemIDs.length === 1
                ? this.$state.go('base.configuration.edit.advanced.caches.cache', {
                    cacheID: selectedItemIDs[0],
                    selectedCaches: selectedItemIDs
                }, {
                    location: 'replace'
                })
                : this.$state.go('base.configuration.edit.advanced.caches', {
                    selectedCaches: selectedItemIDs
                }, {
                    location: 'replace'
                });
        };*/

        this.$onChanges = function(changes) {
            if ('itemToEdit' in changes) {
                // if (
                //     this.clonedCache &&
                //     changes.itemToEdit.currentValue.caches &&
                //     this.clonedCache._id === changes.itemToEdit.currentValue.caches._id
                // ) return;
                // if (this.$scope.ui.inputForm) {
                //     this.$scope.ui.inputForm.$setPristine();
                //     this.$scope.ui.inputForm.$setUntouched();
                // }
                // if (
                //     this.$state.is('base.configuration.edit.advanced.caches.cache') &&
                //     !changes.itemToEdit.currentValue.caches
                // ) return;
                this.originalCache = changes.itemToEdit.currentValue.caches;
                this.clonedCache = cloneDeep(this.originalCache);
            }
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

        this.save = function() {
            if (this.$scope.ui.inputForm.$invalid)
                return this.FormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
            this.onAdvancedSave({$event: {cache: cloneDeep(this.clonedCache)}});
        };

        this.resetAll = function() {
            this.onEditCancel();
            // return this.Confirm.confirm('Are you sure you want to undo all changes for current cache?')
            // .then(() => this.clonedCache = cloneDeep(this.originalCache));
        };
    }
];
