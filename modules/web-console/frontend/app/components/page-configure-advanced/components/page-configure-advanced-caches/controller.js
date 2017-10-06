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
import {Observable} from 'rxjs';
import {merge} from 'rxjs/observable/merge';
import get from 'lodash/get';
import cloneDeep from 'lodash/cloneDeep';
import naturalCompare from 'natural-compare-lite';
import {combineLatest} from 'rxjs/observable/combineLatest';

// Controller for Caches screen.
export default ['conf', 'ConfigSelectors', 'configSelectionManager', '$uiRouter', 'PageConfigureAdvancedCaches', '$transitions', 'ConfigureState', '$scope', '$http', '$state', '$filter', '$timeout', '$modal', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteLegacyTable', 'IgniteVersion', '$q', 'Caches',
    function Controller(conf, ConfigSelectors, configSelectionManager, $uiRouter, pageService, $transitions, ConfigureState, $scope, $http, $state, $filter, $timeout, $modal, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, Resource, ErrorPopover, FormUtils, LegacyTable, Version, $q, Caches) {
        Object.assign(this, {conf, ConfigSelectors, configSelectionManager, $uiRouter, pageService, $transitions, ConfigureState, $scope, $state, Confirm, Caches, FormUtils});

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
            const cacheID$ = this.$uiRouter.globals.params$.pluck('cacheID').publishReplay(1).refCount();

            this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortCaches);
            this.shortModels$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectShortModelsValue());
            this.originalCache$ = cacheID$.distinctUntilChanged().switchMap((id) => {
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectCacheToEdit(id));
            });

            this.isNew$ = cacheID$.map((id) => id === 'new');
            this.itemEditTitle$ = combineLatest(this.isNew$, this.originalCache$, (isNew, cache) => {
                return `${isNew ? 'Create' : 'Edit'} cache ${cache.name ? `‘${cache.name}’` : ''}`;
            });
            this.selectionManager = this.configSelectionManager({
                itemID$: cacheID$,
                selectedItemRows$: this.selectedRows$,
                visibleRows$: this.visibleRows$,
                loadedItems$: this.shortCaches$
            });

            this.subscription = merge(
                this.originalCache$,
                this.selectionManager.editGoes$.do((id) => this.edit(id)),
                this.selectionManager.editLeaves$.do(() => this.$state.go('base.configuration.edit.advanced.caches'))
            ).subscribe();

            this.isBlocked$ = cacheID$;

            this.tableActions$ = this.selectionManager.selectedItemIDs$.map((selectedItems) => [
                {
                    action: 'Clone',
                    click: () => this.clone(selectedItems),
                    available: false
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
            this.conf.removeItem({itemIDs, type: 'caches', andSave: true});
        };

        this.$onDestroy = function() {
            this.subscription.unsubscribe();
            this.visibleRows$.complete();
            this.selectedRows$.complete();
        };

        this.edit = (cacheID) => {
            this.$state.go('base.configuration.edit.advanced.caches.cache', {cacheID});
        };

        this.save = function(cache) {
            this.conf.saveAdvanced({cache});
        };

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
    }
];
