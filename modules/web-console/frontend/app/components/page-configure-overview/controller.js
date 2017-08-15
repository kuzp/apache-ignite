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

import 'rxjs/add/operator/do';

const cellTemplate = (state) => `
    <div class="ui-grid-cell-contents">
        <a
            class="link-success"
            ui-sref="${state}({clusterID: row.entity._id})"
            title='Click to edit'
        >{{ row.entity[col.field] }}</a>
    </div>
`;

export default class PageConfigureOverviewController {
    static $inject = ['PageConfigureOverviewService', '$scope'];

    constructor(pageService, $scope) {
        Object.assign(this, {pageService, $scope});
    }

    $onDestroy() {
        this.subscription.unsubscribe();
    }

    $onInit() {
        this.subscription = this.pageService.getObservable()
            .do((state) => this.$scope.$applyAsync(() => Object.assign(this, state)))
            .subscribe();

        this.clustersColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by name…'
                },
                sort: {direction: 'asc', priority: 0},
                cellTemplate: cellTemplate('base.configuration.tabs'),
                minWidth: 165
            },
            {
                name: 'discovery',
                displayName: 'Discovery',
                field: 'discovery',
                multiselectFilterOptions: this.pageService.clusterDiscoveries,
                width: 150
            },
            {
                name: 'caches',
                displayName: 'Caches',
                field: 'cachesCount',
                cellClass: 'ui-grid-number-cell',
                cellTemplate: cellTemplate('base.configuration.tabs.advanced.caches'),
                enableFiltering: false,
                width: 95
            },
            {
                name: 'models',
                displayName: 'Models',
                field: 'modelsCount',
                cellClass: 'ui-grid-number-cell',
                cellTemplate: cellTemplate('base.configuration.tabs.advanced.models'),
                enableFiltering: false,
                width: 95
            },
            {
                name: 'igfs',
                displayName: 'IGFS',
                field: 'igfsCount',
                cellClass: 'ui-grid-number-cell',
                cellTemplate: cellTemplate('base.configuration.tabs.advanced.igfs'),
                enableFiltering: false,
                width: 80
            }
        ];
    }

    onSelectionChange(selectedClusters) {
        return this.clusterActions = this.makeClusterActions(selectedClusters);
    }

    makeClusterActions(selectedClusters) {
        return [
            {
                action: 'Edit',
                click: () => this.pageService.editCluster(selectedClusters[0]),
                available: selectedClusters.length === 1
            },
            {
                action: 'Clone',
                click: () => this.pageService.cloneClusters(selectedClusters),
                available: true
            },
            {
                action: 'Delete',
                click: () => this.pageService.removeClusters(selectedClusters),
                available: true
            }
        ];
    }
}
