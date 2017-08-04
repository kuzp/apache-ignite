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
import 'rxjs/add/operator/pluck';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/take';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/observable/empty';
import {Observable} from 'rxjs/Observable';

import map from 'lodash/fp/map';
import flatMap from 'lodash/fp/flatMap';
import flow from 'lodash/fp/flow';
import get from 'lodash/fp/get';
import uniq from 'lodash/fp/uniq';

const allNames = (items) => items.map((i) => i.name).join(', ');

const cellTemplate = `
    <div class="ui-grid-cell-contents">
        <a
            class="link-success"
            ui-sref="{{ ::col.colDef.url(row) }}")
            title='Click to edit'
        >{{ row.entity[col.field] }}</a>
    </div>
`;

export default class PageConfigureOverviewController {
    static $inject = ['ConfigureState', '$scope', '$state', 'PageConfigure', 'IgniteConfirm', 'Clusters'];

    constructor(ConfigureState, $scope, $state, PageConfigure, IgniteConfirm, Clusters) {
        Object.assign(this, {ConfigureState, $scope, $state, PageConfigure, IgniteConfirm, Clusters});
    }

    $onDestroy() {
        this.subscription.unsubscribe();
    }

    $onInit() {
        this.subscription = this.getObservable(this.ConfigureState.state$).subscribe();
        this.clustersColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by name…'
                },
                url: (row) => `base.configuration.tabs.advanced.cluster({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                minWidth: 165
            },
            {
                name: 'discovery',
                displayName: 'Discovery',
                field: 'discovery',
                multiselectFilterOptions: this.Clusters.discoveries,
                width: 150
            },
            {
                name: 'caches',
                displayName: 'Caches',
                field: 'cachesCount',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.caches({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                enableFiltering: false,
                width: 95
            },
            {
                name: 'models',
                displayName: 'Models',
                field: 'modelsCount',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.models({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                enableFiltering: false,
                width: 95
            },
            {
                name: 'igfs',
                displayName: 'IGFS',
                field: 'igfsCount',
                cellClass: 'ui-grid-number-cell',
                url: (row) => `base.configuration.tabs.advanced.igfs({clusterID: '${row.entity._id}'})`,
                cellTemplate,
                enableFiltering: false,
                width: 80
            }
        ];
    }

    getObservable(state$) {
        return state$
        .pluck('configurationOverview')
        .do((value) => this.applyValue(value));
    }

    applyValue(value) {
        this.$scope.$applyAsync(() => Object.assign(this, value));
    }

    onClustersAction(action) {
        switch (action.type) {
            case 'EDIT':
                return this.$state.go('^.tabs', {clusterID: action.items[0]._id});
                // return this.PageConfigure.editCluster(action.items[0]._id);
            case 'CLONE':
                return this.PageConfigure.cloneClusters(action.items);
            case 'DELETE':
                return this.IgniteConfirm
                    .confirm(`Are you sure want to remove cluster(s): ${allNames(action.items)}?`)
                    .then(() => this.PageConfigure.removeClustersLocalRemote(action.items));
            default:
                return;
        }
    }

    createCluster() {
        this.PageConfigure.editCluster();
    }
}
