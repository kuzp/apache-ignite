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

// Controller for Clusters screen.
export default ['PageConfigureAdvancedCluster', 'ConfigureState', '$rootScope', '$scope', '$http', '$state', '$timeout', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteEventGroups', 'DemoInfo', 'IgniteLegacyTable', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteVersion', 'Clusters', 'ConfigurationDownload', '$q',
    function(pageService, ConfigureState, $root, $scope, $http, $state, $timeout, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, igniteEventGroups, DemoInfo, LegacyTable, Resource, ErrorPopover, FormUtils, Version, Clusters, ConfigurationDownload, $q) {
        Object.assign(this, {pageService, ConfigureState, Clusters, $scope, Confirm});

        this.$onInit = function() {
            this.subscription = this.pageService.getObservable()
                .do((state) => this.$scope.$applyAsync(() => Object.assign(this, state)))
                .subscribe();
        };

        this.$onDestroy = function() {
            this.subscription.unsubscribe();
        };

        this.uiCanExit = function() {
            // TODO Refactor this
            return !get(this, '$scope.ui.inputForm.$dirty') || this.Confirm.confirm(`
                You have unsaved changes. Are you sure want to discard them?
            `);
        };

        this.clustersColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                minWidth: 165,
                sort: {direction: 'asc', priority: 0},
                filter: {
                    placeholder: 'Filter by name…'
                }
            },
            {
                name: 'discovery',
                displayName: 'Discovery',
                field: 'discovery',
                multiselectFilterOptions: this.Clusters.discoveries,
                width: 150
            }
        ];


        this.onClusterAction = (action) => {
            const realItems = action.items.map((item) => $scope.clusters.find(({_id}) => _id === item._id));
            switch (action.type) {
                case 'EDIT': {
                    if (!realItems.length) return;
                    return $scope.selectItem(realItems[0]);
                }
                case 'CLONE':
                    return this.cloneItems(realItems);
                default:
                    return;
            }
        };

        let __original_value;

        this.available = Version.available.bind(Version);

        const rebuildDropdowns = () => {
            $scope.eventStorage = [
                {value: 'Memory', label: 'Memory'},
                {value: 'Custom', label: 'Custom'}
            ];

            $scope.marshallerVariant = [
                {value: 'JdkMarshaller', label: 'JdkMarshaller'},
                {value: null, label: 'Default'}
            ];

            if (this.available('2.0.0')) {
                $scope.eventStorage.push({value: null, label: 'Disabled'});

                $scope.eventGroups = _.filter(igniteEventGroups, ({value}) => value !== 'EVTS_SWAPSPACE');
            }
            else {
                $scope.eventGroups = igniteEventGroups;

                $scope.marshallerVariant.splice(0, 0, {value: 'OptimizedMarshaller', label: 'OptimizedMarshaller'});
            }
        };

        rebuildDropdowns();

        const filterModel = () => {
            if ($scope.backupItem) {
                if (this.available('2.0.0')) {
                    const evtGrps = _.map($scope.eventGroups, 'value');

                    _.remove(__original_value, (evtGrp) => !_.includes(evtGrps, evtGrp));
                    _.remove($scope.backupItem.includeEventTypes, (evtGrp) => !_.includes(evtGrps, evtGrp));

                    if (_.get($scope.backupItem, 'marshaller.kind') === 'OptimizedMarshaller')
                        $scope.backupItem.marshaller.kind = null;
                }
                else if ($scope.backupItem && !_.get($scope.backupItem, 'eventStorage.kind'))
                    _.set($scope.backupItem, 'eventStorage.kind', 'Memory');
            }
        };

        Version.currentSbj.subscribe({
            next: () => {
                rebuildDropdowns();

                filterModel();
            }
        });

        // UnsavedChangesGuard.install($scope);

        const emptyCluster = {empty: true};

        const blank = Clusters.getBlankCluster();

        const pairFields = {
            attributes: {id: 'Attribute', idPrefix: 'Key', searchCol: 'name', valueCol: 'key', dupObjName: 'name', group: 'attributes'},
            'collision.JobStealing.stealingAttributes': {id: 'CAttribute', idPrefix: 'Key', searchCol: 'name', valueCol: 'key', dupObjName: 'name', group: 'collision'}
        };

        // $scope.tablePairValid = function(item, field, index, stopEdit) {
        //     const pairField = pairFields[field.model];

        //     const pairValue = LegacyTable.tablePairValue(field, index);

        //     if (pairField) {
        //         const model = _.get(item, field.model);

        //         if (LegacyUtils.isDefined(model)) {
        //             const idx = _.findIndex(model, (pair) => {
        //                 return pair[pairField.searchCol] === pairValue[pairField.valueCol];
        //             });

        //             // Found duplicate by key.
        //             if (idx >= 0 && idx !== index) {
        //                 if (stopEdit)
        //                     return false;

        //                 return ErrorPopover.show(LegacyTable.tableFieldId(index, pairField.idPrefix + pairField.id), 'Attribute with such ' + pairField.dupObjName + ' already exists!', $scope.ui, pairField.group);
        //             }
        //         }
        //     }

        //     return true;
        // };

        // $scope.tableSave = function(field, index, stopEdit) {
        //     if (LegacyTable.tablePairSaveVisible(field, index))
        //         return LegacyTable.tablePairSave($scope.tablePairValid, $scope.backupItem, field, index, stopEdit);

        //     return true;
        // };

        // $scope.tableReset = (trySave) => {
        //     const field = LegacyTable.tableField();

        //     if (trySave && LegacyUtils.isDefined(field) && !$scope.tableSave(field, LegacyTable.tableEditedRowIndex(), true))
        //         return false;

        //     LegacyTable.tableReset();

        //     return true;
        // };

        // $scope.tableNewItem = function(field) {
        //     if ($scope.tableReset(true)) {
        //         if (field.type === 'failoverSpi') {
        //             if (LegacyUtils.isDefined($scope.backupItem.failoverSpi))
        //                 $scope.backupItem.failoverSpi.push({});
        //             else
        //                 $scope.backupItem.failoverSpi = {};
        //         }
        //         else if (field.type === 'loadBalancingSpi') {
        //             const newLoadBalancing = {Adaptive: {
        //                 loadProbe: {
        //                     Job: {useAverage: true},
        //                     CPU: {
        //                         useAverage: true,
        //                         useProcessors: true
        //                     },
        //                     ProcessingTime: {useAverage: true}
        //                 }
        //             }};

        //             if (LegacyUtils.isDefined($scope.backupItem.loadBalancingSpi))
        //                 $scope.backupItem.loadBalancingSpi.push(newLoadBalancing);
        //             else
        //                 $scope.backupItem.loadBalancingSpi = [newLoadBalancing];
        //         }
        //         else if (field.type === 'checkpointSpi') {
        //             const newCheckpointCfg = {
        //                 FS: {
        //                     directoryPaths: []
        //                 },
        //                 S3: {
        //                     awsCredentials: {
        //                         kind: 'Basic'
        //                     },
        //                     clientConfiguration: {
        //                         retryPolicy: {
        //                             kind: 'Default'
        //                         },
        //                         useReaper: true
        //                     }
        //                 }
        //             };

        //             if (LegacyUtils.isDefined($scope.backupItem.checkpointSpi))
        //                 $scope.backupItem.checkpointSpi.push(newCheckpointCfg);
        //             else
        //                 $scope.backupItem.checkpointSpi = [newCheckpointCfg];
        //         }
        //         else if (field.type === 'memoryPolicies')
        //             $scope.backupItem.memoryConfiguration.memoryPolicies.push({});
        //         else if (field.type === 'serviceConfigurations')
        //             $scope.backupItem.serviceConfigurations.push({});
        //         else if (field.type === 'executorConfigurations')
        //             $scope.backupItem.executorConfiguration.push({});
        //         else
        //             LegacyTable.tableNewItem(field);
        //     }
        // };

        $scope.tableNewItemActive = LegacyTable.tableNewItemActive;

        // $scope.tableStartEdit = function(item, field, index) {
        //     if ($scope.tableReset(true))
        //         LegacyTable.tableStartEdit(item, field, index, $scope.tableSave);
        // };

        // $scope.tableEditing = LegacyTable.tableEditing;

        // $scope.tableRemove = function(item, field, index) {
        //     if ($scope.tableReset(true))
        //         LegacyTable.tableRemove(item, field, index);
        // };

        $scope.tablePairSave = LegacyTable.tablePairSave;
        $scope.tablePairSaveVisible = LegacyTable.tablePairSaveVisible;

        $scope.attributesTbl = {
            type: 'attributes',
            model: 'attributes',
            focusId: 'Attribute',
            ui: 'table-pair',
            keyName: 'name',
            valueName: 'value',
            save: $scope.tableSave
        };

        $scope.stealingAttributesTbl = {
            type: 'attributes',
            model: 'collision.JobStealing.stealingAttributes',
            focusId: 'CAttribute',
            ui: 'table-pair',
            keyName: 'name',
            valueName: 'value',
            save: $scope.tableSave
        };

        // $scope.removeFailoverConfiguration = function(idx) {
        //     $scope.backupItem.failoverSpi.splice(idx, 1);
        // };

        $scope.supportedJdbcTypes = LegacyUtils.mkOptions(LegacyUtils.SUPPORTED_JDBC_TYPES);

        // We need to initialize backupItem with empty object in order to properly used from angular directives.
        // $scope.backupItem = emptyCluster;

        $scope.ui = FormUtils.formUI();
        $scope.ui.loadedPanels = ['checkpoint', 'serviceConfiguration', 'odbcConfiguration'];
        $scope.ui.activePanels = [0];
        $scope.ui.topPanels = [0];

        // $scope.saveBtnTipText = FormUtils.saveBtnTipText;
        // $scope.widthIsSufficient = FormUtils.widthIsSufficient;

        // $scope.contentVisible = function() {
        //     return !$scope.backupItem.empty;
        // };

        // $scope.toggleExpanded = function() {
        //     $scope.ui.expanded = !$scope.ui.expanded;

        //     ErrorPopover.hide();
        // };

        $scope.discoveries = [
            {value: 'Vm', label: 'Static IPs'},
            {value: 'Multicast', label: 'Multicast'},
            {value: 'S3', label: 'AWS S3'},
            {value: 'Cloud', label: 'Apache jclouds'},
            {value: 'GoogleStorage', label: 'Google cloud storage'},
            {value: 'Jdbc', label: 'JDBC'},
            {value: 'SharedFs', label: 'Shared filesystem'},
            {value: 'ZooKeeper', label: 'Apache ZooKeeper'},
            {value: 'Kubernetes', label: 'Kubernetes'}
        ];

        $scope.swapSpaceSpis = [
            {value: 'FileSwapSpaceSpi', label: 'File-based swap'},
            {value: null, label: 'Not set'}
        ];

        $scope.affinityFunction = [
            {value: 'Rendezvous', label: 'Rendezvous'},
            {value: 'Custom', label: 'Custom'},
            {value: null, label: 'Default'}
        ];

        $scope.clusters = [];
        // this.clustersTable = this.buildClustersTable($scope.clusters);
        this.onFilterChanged = (visibleRows) => {
            // console.debug(visibleRows);
        };

        // function _clusterLbl(cluster) {
        //     return cluster.name + ', ' + _.find($scope.discoveries, {value: cluster.discovery.kind}).label;
        // }

        // function selectCurrentItem() {
        //     const item = $scope.clusters.find((cluster) => cluster._id === $state.params.clusterID);
        //     if (item)
        //         $scope.selectItem(item);
        //     else
        //         $scope.createItem();

        // }

        // Loading.start('loadingClustersScreen');

        // When landing on the page, get clusters and show them.
        // Resource.read()
        //     .then(({spaces, clusters, caches, domains, igfss}) => {
        //         $scope.spaces = spaces;

        //         $scope.clusters = clusters;
        //         this.clustersTable = this.buildClustersTable($scope.clusters);

        //         $scope.caches = _.map(caches, (cache) => {
        //             cache.domains = _.filter(domains, ({_id}) => _.includes(cache.domains, _id));

        //             if (_.get(cache, 'nodeFilter.kind') === 'IGFS')
        //                 cache.nodeFilter.IGFS.instance = _.find(igfss, {_id: cache.nodeFilter.IGFS.igfs});

        //             return {value: cache._id, label: cache.name, cache};
        //         });

        //         $scope.igfss = _.map(igfss, (igfs) => ({value: igfs._id, label: igfs.name, igfs}));

        //         _.forEach($scope.clusters, (cluster) => {
        //             cluster.label = _clusterLbl(cluster);

        //             if (!cluster.collision || !cluster.collision.kind)
        //                 cluster.collision = {kind: 'Noop', JobStealing: {stealingEnabled: true}, PriorityQueue: {starvationPreventionEnabled: true}};

        //             if (!cluster.failoverSpi)
        //                 cluster.failoverSpi = [];

        //             if (!cluster.logger)
        //                 cluster.logger = {Log4j: { mode: 'Default'}};

        //             if (!cluster.peerClassLoadingLocalClassPathExclude)
        //                 cluster.peerClassLoadingLocalClassPathExclude = [];

        //             if (!cluster.deploymentSpi) {
        //                 cluster.deploymentSpi = {URI: {
        //                     uriList: [],
        //                     scanners: []
        //                 }};
        //             }

        //             if (!cluster.memoryConfiguration)
        //                 cluster.memoryConfiguration = { memoryPolicies: [] };

        //             if (!cluster.dataStorageConfiguration)
        //                 cluster.dataStorageConfiguration = { dataRegionConfigurations: [] };

        //             if (!cluster.hadoopConfiguration)
        //                 cluster.hadoopConfiguration = { nativeLibraryNames: [] };

        //             if (!cluster.serviceConfigurations)
        //                 cluster.serviceConfigurations = [];

        //             if (!cluster.executorConfiguration)
        //                 cluster.executorConfiguration = [];
        //         });

        //         selectCurrentItem();

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

        //             $scope.clusterCaches = _.filter($scope.caches,
        //                 (cache) => _.find($scope.backupItem.caches,
        //                     (selCache) => selCache === cache.value
        //                 )
        //             );

        //             $scope.clusterCachesEmpty = _.clone($scope.clusterCaches);
        //             $scope.clusterCachesEmpty.push({label: 'Not set'});
        //         }, true);

        //         $scope.$watch('ui.activePanels.length', () => {
        //             ErrorPopover.hide();
        //         });

        //         if ($root.IgniteDemoMode && sessionStorage.showDemoInfo !== 'true') {
        //             sessionStorage.showDemoInfo = 'true';

        //             DemoInfo.show();
        //         }
        //     })
        //     .catch(Messages.showError)
        //     .then(() => {
        //         $scope.ui.ready = true;
        //         $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

        //         Loading.finish('loadingClustersScreen');
        //     });

        $scope.clusterCaches = [];
        $scope.clusterCachesEmpty = [];

        // $scope.selectItem = function(item, backup) {
        //     function selectItem() {
        //         // $state.go('.', {clusterID: item ? item._id : null}, {notify: false});
        //         $scope.selectedItem = item;

        //         if (backup)
        //             $scope.backupItem = backup;
        //         else if (item)
        //             $scope.backupItem = angular.copy(item);
        //         else
        //             $scope.backupItem = emptyCluster;

        //         $scope.backupItem = _.merge({}, blank, $scope.backupItem);

        //         if ($scope.ui.inputForm) {
        //             $scope.ui.inputForm.$error = {};
        //             $scope.ui.inputForm.$setPristine();
        //         }

        //         __original_value = ModelNormalizer.normalize($scope.backupItem);

        //         filterModel();

        //         if (LegacyUtils.getQueryVariable('new'))
        //             $state.go('base.configuration.tabs.advanced.clusters');
        //     }
        //     selectItem();
        //     FormUtils.confirmUnsavedChanges($scope.backupItem && $scope.ui.inputForm && $scope.ui.inputForm.$dirty, selectItem);
        // };

        // $scope.linkId = () => $scope.backupItem._id ? $scope.backupItem._id : 'create';

        // function prepareNewItem(linkId) {
        //     return _.merge({}, blank, {
        //         space: $scope.spaces[0]._id,
        //         discovery: {
        //             kind: 'Multicast',
        //             Vm: {addresses: ['127.0.0.1:47500..47510']},
        //             Multicast: {addresses: ['127.0.0.1:47500..47510']},
        //             Jdbc: {initSchema: true}
        //         },
        //         binaryConfiguration: {typeConfigurations: [], compactFooter: true},
        //         communication: {tcpNoDelay: true},
        //         connector: {noDelay: true},
        //         collision: {kind: 'Noop', JobStealing: {stealingEnabled: true}, PriorityQueue: {starvationPreventionEnabled: true}},
        //         failoverSpi: [],
        //         logger: {Log4j: { mode: 'Default'}},
        //         caches: linkId && _.find($scope.caches, {value: linkId}) ? [linkId] : [],
        //         igfss: linkId && _.find($scope.igfss, {value: linkId}) ? [linkId] : []
        //     });
        // }

        // Add new cluster.
        // $scope.createItem = function(linkId) {
        //     $timeout(() => FormUtils.ensureActivePanel($scope.ui, 'general', 'clusterNameInput'));

        //     $scope.selectItem(null, prepareNewItem(linkId));
        // };

        function clusterCaches(item) {
            return _.filter(_.map($scope.caches, (scopeCache) => scopeCache.cache),
                (cache) => _.includes(item.caches, cache._id));
        }

        const _objToString = (type, name, prefix = '') => {
            if (type === 'checkpoint')
                return prefix + ' checkpoint configuration';
            if (type === 'cluster')
                return prefix + ' discovery IP finder';

            return `${prefix} ${type} "${name}"`;
        };

        // Check cluster logical consistency.
        const triggerValidation = (item) => {
            const fe = (m) => Object.keys(m.$error)[0];
            const em = (e) => (m) => {
                if (!e) return;
                const walk = (m) => {
                    if (!m.$error[e]) return;
                    if (m.$error[e] === true) return m;
                    return walk(m.$error[e][0]);
                };
                return walk(m);
            };

            this.$scope.$broadcast('$showValidationError', em(fe(this.$scope.ui.inputForm))(this.$scope.ui.inputForm));
            ErrorPopover.hide();

            return true;
        };

        // Save cluster in database.
        // const save = (item) => {
        //     $http.post('/api/v1/configuration/clusters/save', item)
        //         .then(({data}) => {
        //             const _id = data;

        //             item.label = _clusterLbl(item);

        //             $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

        //             const idx = _.findIndex($scope.clusters, {_id});

        //             if (idx >= 0)
        //                 _.assign($scope.clusters[idx], item);
        //             else {
        //                 item._id = _id;

        //                 $scope.clusters.push(item);
        //             }

        //             _.forEach($scope.caches, (cache) => {
        //                 if (_.includes(item.caches, cache.value))
        //                     cache.cache.clusters = _.union(cache.cache.clusters, [_id]);
        //                 else
        //                     _.pull(cache.cache.clusters, _id);
        //             });

        //             _.forEach($scope.igfss, (igfs) => {
        //                 if (_.includes(item.igfss, igfs.value))
        //                     igfs.igfs.clusters = _.union(igfs.igfs.clusters, [_id]);
        //                 else
        //                     _.pull(igfs.igfs.clusters, _id);
        //             });

        //             $scope.selectItem(item);

        //             Messages.showInfo(`Cluster "${item.name}" saved.`);
        //             this.clustersTable = this.buildClustersTable($scope.clusters);
        //         })
        //         .catch(Messages.showError);
        // };

        // Save cluster.
        // this.saveItem = function(item) {
        //     const swapConfigured = item.swapSpaceSpi && item.swapSpaceSpi.kind;

        //     if (!swapConfigured && _.find(clusterCaches(item), (cache) => cache.swapEnabled))
        //         _.merge(item, {swapSpaceSpi: {kind: 'FileSwapSpaceSpi'}});

        //     if (validate(item))
        //         save(item);
        // };

        function _clusterNames() {
            return _.map($scope.clusters, (cluster) => cluster.name);
        }

        // Clone cluster with new name.
        // this.cloneItems = (items = []) => items.reduce((prev, item) => prev.then(() => {
        //     return Input.clone(item.name, _clusterNames()).then((newName) => {
        //         const clonedItem = angular.copy(item);
        //         delete clonedItem._id;
        //         clonedItem.name = newName;
        //         return save(clonedItem);
        //     });
        // }), $q.resolve());

        // Remove cluster from db.
        // $scope.removeItem = (selectedItem) => {
        //     Confirm.confirm('Are you sure you want to remove cluster: "' + selectedItem.name + '"?')
        //         .then(() => {
        //             const _id = selectedItem._id;

        //             $http.post('/api/v1/configuration/clusters/remove', {_id})
        //                 .then(() => {
        //                     Messages.showInfo('Cluster has been removed: ' + selectedItem.name);

        //                     const clusters = $scope.clusters;

        //                     const idx = _.findIndex(clusters, (cluster) => cluster._id === _id);

        //                     if (idx >= 0) {
        //                         clusters.splice(idx, 1);

        //                         $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

        //                         if (clusters.length > 0)
        //                             $scope.selectItem(clusters[0]);
        //                         else
        //                             $scope.backupItem = emptyCluster;

        //                         _.forEach($scope.caches, (cache) => _.remove(cache.cache.clusters, (id) => id === _id));
        //                         _.forEach($scope.igfss, (igfs) => _.remove(igfs.igfs.clusters, (id) => id === _id));
        //                     }
        //                     this.clustersTable = this.buildClustersTable($scope.clusters);
        //                 })
        //                 .catch(Messages.showError);
        //         });
        // };

        // Remove all clusters from db.
        // $scope.removeAllItems = () => {
        //     Confirm.confirm('Are you sure you want to remove all clusters?')
        //         .then(() => {
        //             $http.post('/api/v1/configuration/clusters/remove/all')
        //                 .then(() => {
        //                     Messages.showInfo('All clusters have been removed');

        //                     $scope.clusters = [];
        //                     this.clustersTable = this.buildClustersTable($scope.clusters);

        //                     _.forEach($scope.caches, (cache) => cache.cache.clusters = []);
        //                     _.forEach($scope.igfss, (igfs) => igfs.igfs.clusters = []);

        //                     $scope.backupItem = emptyCluster;
        //                     if ($scope.ui.inputForm) {
        //                         $scope.ui.inputForm.$error = {};
        //                         $scope.ui.inputForm.$setPristine();
        //                     }
        //                 })
        //                 .catch(Messages.showError);
        //         });
        // };

        // $scope.resetAll = function() {
        //     Confirm.confirm('Are you sure you want to undo all changes for current cluster?')
        //         .then(function() {
        //             $scope.backupItem = $scope.selectedItem ? angular.copy($scope.selectedItem) : prepareNewItem();
        //             if ($scope.ui.inputForm) {
        //                 $scope.ui.inputForm.$error = {};
        //                 $scope.ui.inputForm.$setPristine();
        //             }
        //         });
        // };

        this.cancelEdit = () => this.pageService.cancelEdit();
        this.downloadConfiguration = (cluster) => ConfigurationDownload.downloadClusterConfiguration(cluster);
        const propagateSubmitted = (form) => {
            Object.keys(form).filter((key) => !key.startsWith('$')).forEach((key) => {
                if (form.$submitted !== form[key].$submitted) {
                    form[key].$submitted = form.$submitted;
                    propagateSubmitted(form[key]);
                }
            });
        };
        this.save = function(cluster = this.clonedCluster) {
            const isValid = triggerValidation(cluster) && this.$scope.ui.inputForm.$valid;
            // propagateSubmitted(this.$scope.ui.inputForm);
            // console.log(isValid);
            if (isValid) this.pageService.save(this.clonedCluster);
        };
    }
];
