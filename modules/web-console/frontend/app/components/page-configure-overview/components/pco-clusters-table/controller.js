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

import cellTemplate from './linkedCellTemplate.pug';

export default class ClustersTableController {
    static $inject = ['$scope'];

    constructor($scope) {
        Object.assign(this, {$scope});
    }

    $onInit() {
        this.grid = {
            data: [],
            columnDefs: this.getColumnDefs(),
            rowHeight: 46,
            enableColumnMenus: false,
            enableFullRowSelection: true,
            enableSelectionBatchEvent: true,
            selectionRowHeaderWidth: 52,
            enableColumnCategories: true,
            rowIdentity(row) {
                return row._id;
            },
            onRegisterApi: (api) => {
                this.gridAPI = api;
                api.selection.on.rowSelectionChanged(this.$scope, (e) => this.onRowsSelectionChange([e]));
                api.selection.on.rowSelectionChangedBatch(this.$scope, (e) => this.onRowsSelectionChange(e));
                this.$scope.$watch(() => api.grid.getVisibleRows().length, (rows) => this.adjustHeight(api, rows));
            }
        };
    }

    onRowsSelectionChange() {
        this.actionsMenu = this.makeActionsMenu();
    }

    makeActionsMenu() {
        return [
            {
                action: 'Edit',
                click: () => this.dispatchAction('EDIT'),
                available: this.gridAPI.selection.getSelectedCount() === 1
            },
            {
                action: 'Delete',
                click: () => this.dispatchAction('DELETE'),
                available: true
            },
            {
                action: 'Clone',
                click: () => this.dispatchAction('CLONE'),
                available: true
            }
        ];
    }

    dispatchAction(type) {
        this.onAction({$event: {type, items: this.gridAPI.selection.getSelectedRows()}});
        this.gridAPI.selection.clearSelectedRows();
    }

    $onChanges(changes) {
        if (
            'clusters' in changes &&
            changes.clusters.currentValue !== changes.clusters.previousValue &&
            this.grid
        )
            this.grid.data = this.prepareData(changes.clusters.currentValue);

    }

    adjustHeight(api, rows) {
        // Add header height.
        const height = Math.min(rows, 11) * 46 + 69 + 15;
        api.grid.element.css('height', height + 'px');
        api.core.handleWindowResize();
    }

    getColumnDefs() {
        return [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                minWidth: 165
            },
            {
                name: 'discovery',
                displayName: 'Discovery',
                field: 'discovery',
                width: 110
            },
            {
                name: 'caches',
                displayName: 'Caches',
                field: 'caches',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.caches({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                width: 95
            },
            {
                name: 'models',
                displayName: 'Models',
                field: 'models',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.domains({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                width: 95
            },
            {
                name: 'igfs',
                displayName: 'IGFS',
                field: 'igfs',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.igfs({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                width: 80
            }
        ];
    }

    prepareData(data = []) {
        return data;
    }
}
