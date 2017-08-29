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
                    click: () => this.cloneItems(selectedItems),
                    available: true
                },
                {
                    action: 'Delete',
                    click: () => selectedItems.length === this.cachesTable.length
                        ? $scope.removeAllItems()
                        : $scope.removeItem(selectedItems[0]),
                    available: true
                }
            ];
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




        // We need to initialize backupItem with empty object in order to properly used from angular directives.
        // $scope.backupItem = emptyCache;

        $scope.ui = FormUtils.formUI();
        $scope.ui.activePanels = [0];
        $scope.ui.topPanels = [0, 1, 2, 3];

        $scope.offHeapMode = 'DISABLED';

        $scope.caches = [];
        $scope.domains = [];

        function cacheDomains(item) {
            return _.reduce($scope.domains, function(memo, domain) {
                if (item && _.includes(item.domains, domain.value))
                    memo.push(domain.meta);

                return memo;
            }, []);
        }

        const setOffHeapMode = (item) => {
            if (_.isNil(item.offHeapMaxMemory))
                return;

            return item.offHeapMode = Math.sign(item.offHeapMaxMemory);
        };

        const setOffHeapMaxMemory = (value) => {
            const item = $scope.backupItem;

            if (_.isNil(value) || value <= 0)
                return item.offHeapMaxMemory = value;

            item.offHeapMaxMemory = item.offHeapMaxMemory > 0 ? item.offHeapMaxMemory : null;
        };

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

        $scope.selectItem = (item) => {
            // $timeout(() => FormUtils.ensureActivePanel($scope.ui, 'general', 'cacheNameInput'));

            // if (item && !_.get(item.cacheStoreFactory.CacheJdbcBlobStoreFactory, 'connectVia'))
            //     _.set(item.cacheStoreFactory, 'CacheJdbcBlobStoreFactory.connectVia', 'DataSource');

            // setOffHeapMode($scope.backupItem);
            // filterModel();
        };

        function cacheClusters() {
            return _.filter($scope.clusters, (cluster) => _.includes($scope.backupItem.clusters, cluster.value));
        }

        function clusterCaches(cluster) {
            const caches = _.filter($scope.caches,
                (cache) => cache._id !== $scope.backupItem._id && _.includes(cluster.caches, cache._id));

            caches.push($scope.backupItem);

            return caches;
        }

        const _objToString = (type, name, prefix = '') => {
            if (type === 'checkpoint')
                return `${prefix} checkpoint configuration in cluster "${name}"`;
            if (type === 'cluster')
                return `${prefix} discovery IP finder in cluster "${name}"`;

            return `${prefix} ${type} "${name}"`;
        };

        function checkDataSources() {
            const clusters = cacheClusters();

            let checkRes = {checked: true};

            const failCluster = _.find(clusters, (cluster) => {
                const caches = clusterCaches(cluster);

                checkRes = LegacyUtils.checkDataSources(cluster, caches, $scope.backupItem);

                return !checkRes.checked;
            });

            if (!checkRes.checked) {
                return ErrorPopover.show(checkRes.firstObj.cacheStoreFactory.kind === 'CacheJdbcPojoStoreFactory' ? 'pojoDialectInput' : 'blobDialectInput',
                    'Found ' + _objToString(checkRes.secondType, checkRes.secondObj.name || failCluster.label) + ' with the same data source bean name "' +
                    checkRes.firstDs.dataSourceBean + '" and different database: "' +
                    LegacyUtils.cacheStoreJdbcDialectsLabel(checkRes.firstDs.dialect) + '" in ' + _objToString(checkRes.firstType, checkRes.firstObj.name, 'current') + ' and "' +
                    LegacyUtils.cacheStoreJdbcDialectsLabel(checkRes.secondDs.dialect) + '" in ' + _objToString(checkRes.secondType, checkRes.secondObj.name || failCluster.label),
                    $scope.ui, 'store', 10000);
            }

            return true;
        }

        function checkEvictionPolicy(evictionPlc) {
            if (evictionPlc && evictionPlc.kind) {
                const plc = evictionPlc[evictionPlc.kind];

                if (plc && !plc.maxMemorySize && !plc.maxSize)
                    return ErrorPopover.show('evictionPolicymaxMemorySizeInput', 'Either maximum memory size or maximum size should be great than 0!', $scope.ui, 'memory');
            }

            return true;
        }

        function checkSQLSchemas() {
            const clusters = cacheClusters();

            let checkRes = {checked: true};

            const failCluster = _.find(clusters, (cluster) => {
                const caches = clusterCaches(cluster);

                checkRes = LegacyUtils.checkCacheSQLSchemas(caches, $scope.backupItem);

                return !checkRes.checked;
            });

            if (!checkRes.checked) {
                return ErrorPopover.show('sqlSchemaInput',
                    'Found cache "' + checkRes.secondCache.name + '" in cluster "' + failCluster.label + '" ' +
                    'with the same SQL schema name "' + checkRes.firstCache.sqlSchema + '"',
                    $scope.ui, 'query', 10000);
            }

            return true;
        }

        function checkStoreFactoryBean(storeFactory, beanFieldId) {
            if (!LegacyUtils.isValidJavaIdentifier('Data source bean', storeFactory.dataSourceBean, beanFieldId, $scope.ui, 'store'))
                return false;

            return checkDataSources();
        }

        function checkStoreFactory(item) {
            const cacheStoreFactorySelected = item.cacheStoreFactory && item.cacheStoreFactory.kind;

            if (cacheStoreFactorySelected) {
                const storeFactory = item.cacheStoreFactory[item.cacheStoreFactory.kind];

                if (item.cacheStoreFactory.kind === 'CacheJdbcPojoStoreFactory' && !checkStoreFactoryBean(storeFactory, 'pojoDataSourceBean'))
                    return false;

                if (item.cacheStoreFactory.kind === 'CacheJdbcBlobStoreFactory' && storeFactory.connectVia !== 'URL'
                    && !checkStoreFactoryBean(storeFactory, 'blobDataSourceBean'))
                    return false;
            }

            if ((item.readThrough || item.writeThrough) && !cacheStoreFactorySelected)
                return ErrorPopover.show('cacheStoreFactoryInput', (item.readThrough ? 'Read' : 'Write') + ' through are enabled but store is not configured!', $scope.ui, 'store');

            if (item.writeBehindEnabled && !cacheStoreFactorySelected)
                return ErrorPopover.show('cacheStoreFactoryInput', 'Write behind enabled but store is not configured!', $scope.ui, 'store');

            if (cacheStoreFactorySelected && !item.readThrough && !item.writeThrough)
                return ErrorPopover.show('readThroughLabel', 'Store is configured but read/write through are not enabled!', $scope.ui, 'store');

            return true;
        }

        // Check cache logical consistency.
        function validate(item) {
            ErrorPopover.hide();

            if (LegacyUtils.isEmptyString(item.name))
                return ErrorPopover.show('cacheNameInput', 'Cache name should not be empty!', $scope.ui, 'general');

            if (item.memoryMode === 'ONHEAP_TIERED' && item.offHeapMaxMemory > 0 && !LegacyUtils.isDefined(item.evictionPolicy.kind))
                return ErrorPopover.show('evictionPolicyKindInput', 'Eviction policy should be configured!', $scope.ui, 'memory');

            if (!LegacyUtils.checkFieldValidators($scope.ui))
                return false;

            if (item.memoryMode === 'OFFHEAP_VALUES' && !_.isEmpty(item.domains))
                return ErrorPopover.show('memoryModeInput', 'Query indexing could not be enabled while values are stored off-heap!', $scope.ui, 'memory');

            if (item.memoryMode === 'OFFHEAP_TIERED' && item.offHeapMaxMemory === -1)
                return ErrorPopover.show('offHeapModeInput', 'Invalid value!', $scope.ui, 'memory');

            if (!checkEvictionPolicy(item.evictionPolicy))
                return false;

            if (!checkSQLSchemas())
                return false;

            if (!checkStoreFactory(item))
                return false;

            if (item.writeBehindFlushSize === 0 && item.writeBehindFlushFrequency === 0)
                return ErrorPopover.show('writeBehindFlushSizeInput', 'Both "Flush frequency" and "Flush size" are not allowed as 0!', $scope.ui, 'store');

            if (item.nodeFilter && item.nodeFilter.kind === 'OnNodes' && _.isEmpty(item.nodeFilter.OnNodes.nodeIds))
                return ErrorPopover.show('nodeFilter-title', 'At least one node ID should be specified!', $scope.ui, 'nodeFilter');

            return true;
        }

        // Save cache in database.

        this.save = function(item) {
            // _.merge(item, LegacyUtils.autoCacheStoreConfiguration(item, cacheDomains(item)));
            this.FormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
            if (!validate(item)) return;
            if (this.$scope.ui.inputForm.$valid) this.pageService.save(item, this.originalCluster);
        };

        function _cacheNames() {
            return _.map($scope.caches, (cache) => cache.name);
        }

        // Clone cache with new name.
        this.cloneItems = (items = []) => items.reduce((prev, item) => prev.then(() => {
            return Input.clone(item.name, _cacheNames()).then((newName) => {
                const clonedItem = angular.copy(item);
                delete clonedItem._id;
                clonedItem.name = newName;
                return save(clonedItem);
            });
        }), $q.resolve());

        // Remove cache from db.
        $scope.removeItem = (selectedItem) => {
            Confirm.confirm('Are you sure you want to remove cache: "' + selectedItem.name + '"?')
                .then(() => {
                    const _id = selectedItem._id;

                    $http.post('/api/v1/configuration/caches/remove', {_id})
                        .then(() => {
                            Messages.showInfo('Cache has been removed: ' + selectedItem.name);

                            const caches = $scope.caches;

                            const idx = _.findIndex(caches, function(cache) {
                                return cache._id === _id;
                            });

                            if (idx >= 0) {
                                caches.splice(idx, 1);

                                $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();


                                _.forEach($scope.clusters, (cluster) => _.remove(cluster.caches, (id) => id === _id));
                                _.forEach($scope.domains, (domain) => _.remove(domain.meta.caches, (id) => id === _id));
                            }
                        })
                        .catch(Messages.showError);
                });
        };

        // Remove all caches from db.
        $scope.removeAllItems = () => {
            Confirm.confirm('Are you sure you want to remove all caches?')
                .then(() => {
                    $http.post('/api/v1/configuration/caches/remove/all')
                        .then(() => {
                            Messages.showInfo('All caches have been removed');

                            $scope.caches = [];

                            _.forEach($scope.clusters, (cluster) => cluster.caches = []);
                            _.forEach($scope.domains, (domain) => domain.meta.caches = []);

                        })
                        .catch(Messages.showError);
                });
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
