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

// Controller for IGFS screen.
export default ['ConfigureState', '$scope', '$http', '$state', '$filter', '$timeout', 'IgniteLegacyUtils', 'IgniteMessages', 'IgniteConfirm', 'IgniteInput', 'IgniteLoading', 'IgniteModelNormalizer', 'IgniteUnsavedChangesGuard', 'IgniteLegacyTable', 'IgniteConfigurationResource', 'IgniteErrorPopover', 'IgniteFormUtils', 'IgniteVersion', '$q', 'IGFSs',
    function(ConfigureState, $scope, $http, $state, $filter, $timeout, LegacyUtils, Messages, Confirm, Input, Loading, ModelNormalizer, UnsavedChangesGuard, LegacyTable, Resource, ErrorPopover, FormUtils, Version, $q, IGFSs) {
        Object.assign(this, {ConfigureState, $scope, $state});

        this.available = Version.available.bind(Version);

        // UnsavedChangesGuard.install($scope);

        const emptyIgfs = {empty: true};

        let __original_value;

        const blank = {
            ipcEndpointConfiguration: {},
            secondaryFileSystem: {}
        };

        // We need to initialize backupItem with empty object in order to properly used from angular directives.
        // $scope.backupItem = emptyIgfs;

        $scope.ui = FormUtils.formUI();
        $scope.ui.activePanels = [0];
        $scope.ui.topPanels = [0];
        $scope.ui.expanded = true;

        $scope.compactJavaName = FormUtils.compactJavaName;
        $scope.widthIsSufficient = FormUtils.widthIsSufficient;
        $scope.saveBtnTipText = FormUtils.saveBtnTipText;

        this.$onInit = function() {
            this.subscription = this.getObservable(this.ConfigureState.state$).subscribe();
        };

        this.getObservable = function(state$) {
            // return state$.pluck('clusterConfiguration.originalCaches').distinctUntilChanged()
            return state$.pluck('clusterConfiguration')
            .do((value) => this.applyValue(value));
        };

        this.applyValue = function(state) {
            this.$scope.$applyAsync(() => {
                this.assignIGFSs(state.originalIGFSs);
                this.$scope.selectItem(state.originalIGFS);
            });
        };

        this.assignIGFSs = function(igfss) {
            this.$scope.igfss = igfss;
            this.IGFSsTable = this.buildIGFSsTable(this.$scope.igfss);
        };

        $scope.tableSave = function(field, index, stopEdit) {
            if (field.type === 'pathModes' && LegacyTable.tablePairSaveVisible(field, index))
                return LegacyTable.tablePairSave($scope.tablePairValid, $scope.backupItem, field, index, stopEdit);

            return true;
        };

        $scope.tableReset = (trySave) => {
            const field = LegacyTable.tableField();

            if (trySave && LegacyUtils.isDefined(field) && !$scope.tableSave(field, LegacyTable.tableEditedRowIndex(), true))
                return false;

            LegacyTable.tableReset();

            return true;
        };

        $scope.tableNewItem = function(field) {
            if ($scope.tableReset(true))
                LegacyTable.tableNewItem(field);
        };

        $scope.tableNewItemActive = LegacyTable.tableNewItemActive;

        $scope.tableStartEdit = function(item, field, index) {
            if ($scope.tableReset(true))
                LegacyTable.tableStartEdit(item, field, index, $scope.tableSave);
        };

        $scope.tableEditing = LegacyTable.tableEditing;
        $scope.tablePairSave = LegacyTable.tablePairSave;
        $scope.tablePairSaveVisible = LegacyTable.tablePairSaveVisible;

        $scope.tableRemove = function(item, field, index) {
            if ($scope.tableReset(true))
                LegacyTable.tableRemove(item, field, index);
        };

        $scope.tablePairValid = function(item, field, index, stopEdit) {
            const pairValue = LegacyTable.tablePairValue(field, index);

            const model = item[field.model];

            if (LegacyUtils.isDefined(model)) {
                const idx = _.findIndex(model, function(pair) {
                    return pair.path === pairValue.key;
                });

                // Found duplicate.
                if (idx >= 0 && idx !== index) {
                    if (stopEdit)
                        return false;

                    return ErrorPopover.show(LegacyTable.tableFieldId(index, 'KeyPathMode'), 'Such path already exists!', $scope.ui, 'misc');
                }
            }

            return true;
        };

        $scope.tblPathModes = {
            type: 'pathModes',
            model: 'pathModes',
            focusId: 'PathMode',
            ui: 'table-pair',
            keyName: 'path',
            valueName: 'mode',
            save: $scope.tableSave
        };

        $scope.igfsModes = LegacyUtils.mkOptions(['PRIMARY', 'PROXY', 'DUAL_SYNC', 'DUAL_ASYNC']);

        $scope.contentVisible = function() {
            return !get($scope, 'backupItem.empty');
        };

        $scope.toggleExpanded = function() {
            $scope.ui.expanded = !$scope.ui.expanded;

            ErrorPopover.hide();
        };

        this.defaultValues = {
            affinnityGroupSize: 512,
            defaultMode: 'DUAL_ASYNC'
        };

        this.buildIGFSsTable = (igfss = []) => igfss.map((i) => ({
            _id: i._id,
            name: i.name,
            groupSize: i.affinnityGroupSize || this.defaultValues.affinnityGroupSize,
            mode: i.defaultMode || this.defaultValues.defaultMode
        }));

        this.onIGFSAction = (action) => {
            const realItems = action.items.map((item) => $scope.igfss.find(({_id}) => _id === item._id));
            switch (action.type) {
                case 'EDIT':
                    return this.$state.go('base.configuration.tabs.advanced.igfs.igfs', {
                        igfsID: action.items[0] ? action.items[0]._id : null
                    });
                case 'CLONE':
                    return this.cloneItems(realItems);
                case 'DELETE':
                    return realItems.length === this.IGFSsTable.length
                        ? $scope.removeAllItems()
                        : $scope.removeItem(realItems[0]);
                default:
                    return;
            }
        };

        this.IGFSColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by name…'
                },
                minWidth: 165
            },
            {
                name: 'mode',
                displayName: 'Mode',
                field: 'mode',
                multiselectFilterOptions: IGFSs.igfsModes,
                width: 160
            },
            {
                name: 'groupSize',
                displayName: 'Group size',
                field: 'groupSize',
                enableFiltering: false,
                width: 130
            }
        ];

        $scope.clusters = [];

        function selectFirstItem() {
            if ($scope.igfss.length > 0)
                $scope.selectItem($scope.igfss[0]);
            else
                $scope.createItem();
        }

        // Loading.start('loadingIgfsScreen');

        // When landing on the page, get IGFSs and show them.
        // Resource.read()
        //     .then(({spaces, clusters, igfss}) => {
        //         $scope.spaces = spaces;

        //         $scope.igfss = igfss || [];
        //         this.IGFSsTable = this.buildIGFSsTable($scope.igfss);

        //         // For backward compatibility set colocateMetadata and relaxedConsistency default values.
        //         _.forEach($scope.igfss, (igfs) => {
        //             if (_.isUndefined(igfs.colocateMetadata))
        //                 igfs.colocateMetadata = true;

        //             if (_.isUndefined(igfs.relaxedConsistency))
        //                 igfs.relaxedConsistency = true;
        //         });

        //         $scope.clusters = _.map(clusters || [], (cluster) => ({
        //             label: cluster.name,
        //             value: cluster._id
        //         }));

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

        //         $scope.$watch('ui.activePanels.length', () => {
        //             ErrorPopover.hide();
        //         });
        //     })
        //     .catch(Messages.showError)
        //     .then(() => {
        //         $scope.ui.ready = true;
        //         $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

        //         Loading.finish('loadingIgfsScreen');
        //     });

        $scope.selectItem = function(item, backup) {
            function selectItem() {
                LegacyTable.tableReset();

                $scope.selectedItem = item;

                if (backup)
                    $scope.backupItem = backup;
                else if (item)
                    $scope.backupItem = angular.copy(item);
                else
                    $scope.backupItem = emptyIgfs;

                $scope.backupItem = _.merge({}, blank, $scope.backupItem);

                if ($scope.ui.inputForm) {
                    $scope.ui.inputForm.$error = {};
                    $scope.ui.inputForm.$setPristine();
                }

                __original_value = ModelNormalizer.normalize($scope.backupItem);

                if (LegacyUtils.getQueryVariable('new'))
                    $state.go('base.configuration.tabs.advanced.igfs');
            }
            selectItem();
        };

        $scope.linkId = () => $scope.backupItem._id ? $scope.backupItem._id : 'create';

        function prepareNewItem(linkId) {
            return {
                space: $scope.spaces[0]._id,
                ipcEndpointEnabled: true,
                fragmentizerEnabled: true,
                colocateMetadata: true,
                relaxedConsistency: true,
                clusters: linkId && _.find($scope.clusters, {value: linkId}) ? [linkId] :
                    (_.isEmpty($scope.clusters) ? [] : [$scope.clusters[0].value])
            };
        }

        // Add new IGFS.
        $scope.createItem = function(linkId) {
            if ($scope.tableReset(true)) {
                $timeout(() => FormUtils.ensureActivePanel($scope.ui, 'general', 'igfsNameInput'));

                $scope.selectItem(null, prepareNewItem(linkId));
            }
        };

        // Check IGFS logical consistency.
        function validate(item) {
            ErrorPopover.hide();

            if (LegacyUtils.isEmptyString(item.name))
                return ErrorPopover.show('igfsNameInput', 'IGFS name should not be empty!', $scope.ui, 'general');

            if (!LegacyUtils.checkFieldValidators($scope.ui))
                return false;

            if (!item.secondaryFileSystemEnabled && (item.defaultMode === 'PROXY'))
                return ErrorPopover.show('secondaryFileSystem-title', 'Secondary file system should be configured for "PROXY" IGFS mode!', $scope.ui, 'secondaryFileSystem');

            if (item.pathModes) {
                for (let pathIx = 0; pathIx < item.pathModes.length; pathIx++) {
                    if (!item.secondaryFileSystemEnabled && item.pathModes[pathIx].mode === 'PROXY')
                        return ErrorPopover.show('secondaryFileSystem-title', 'Secondary file system should be configured for "PROXY" path mode!', $scope.ui, 'secondaryFileSystem');
                }
            }

            return true;
        }

        // Save IGFS in database.
        const save = (item) => {
            return $http.post('/api/v1/configuration/igfs/save', item)
                .then(({data}) => {
                    const _id = data;

                    $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

                    const idx = _.findIndex($scope.igfss, {_id});

                    if (idx >= 0)
                        _.assign($scope.igfss[idx], item);
                    else {
                        item._id = _id;
                        $scope.igfss.push(item);
                    }
                    this.IGFSsTable = this.buildIGFSsTable($scope.igfss);

                    $scope.selectItem(item);

                    Messages.showInfo(`IGFS "${item.name}" saved.`);
                })
                .catch(Messages.showError);
        };

        // Save IGFS.
        this.saveItem = function(item) {
            if ($scope.tableReset(true)) {
                if (validate(item))
                    save(item);
            }
        };

        function _igfsNames() {
            return _.map($scope.igfss, (igfs) => igfs.name);
        }

        // Clone IGFS with new name.
        this.cloneItems = (items = []) => items.reduce((prev, item) => prev.then(() => {
            return Input.clone(item.name, _igfsNames()).then((newName) => {
                const clonedItem = angular.copy(item);
                delete clonedItem._id;
                clonedItem.name = newName;
                return save(clonedItem);
            });
        }), $q.resolve());

        // Remove IGFS from db.
        $scope.removeItem = (selectedItem) => {
            LegacyTable.tableReset();

            Confirm.confirm('Are you sure you want to remove IGFS: "' + selectedItem.name + '"?')
                .then(() => {
                    const _id = selectedItem._id;

                    $http.post('/api/v1/configuration/igfs/remove', {_id})
                        .then(() => {
                            Messages.showInfo('IGFS has been removed: ' + selectedItem.name);

                            const igfss = $scope.igfss;

                            const idx = _.findIndex(igfss, function(igfs) {
                                return igfs._id === _id;
                            });

                            if (idx >= 0) {
                                igfss.splice(idx, 1);

                                $scope.ui.inputForm && $scope.ui.inputForm.$setPristine();

                                if (igfss.length > 0)
                                    $scope.selectItem(igfss[0]);
                                else
                                    $scope.backupItem = emptyIgfs;
                            }
                            this.IGFSsTable = this.buildIGFSsTable($scope.igfss);
                        })
                        .catch(Messages.showError);
                });
        };

        // Remove all IGFS from db.
        $scope.removeAllItems = () => {
            LegacyTable.tableReset();

            Confirm.confirm('Are you sure you want to remove all IGFS?')
                .then(() => {
                    $http.post('/api/v1/configuration/igfs/remove/all')
                        .then(() => {
                            Messages.showInfo('All IGFS have been removed');

                            $scope.igfss = [];
                            this.IGFSsTable = this.buildIGFSsTable($scope.igfss);
                            $scope.backupItem = emptyIgfs;
                            if ($scope.ui.inputForm) {
                                $scope.ui.inputForm.$error = {};
                                $scope.ui.inputForm.$setPristine();
                            }
                        })
                        .catch(Messages.showError);
                });
        };

        $scope.resetAll = function() {
            LegacyTable.tableReset();

            Confirm.confirm('Are you sure you want to undo all changes for current IGFS?')
                .then(function() {
                    $scope.backupItem = $scope.selectedItem ? angular.copy($scope.selectedItem) : prepareNewItem();
                    if ($scope.ui.inputForm) {
                        $scope.ui.inputForm.$error = {};
                        $scope.ui.inputForm.$setPristine();
                    }
                });
        };
    }
];
