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

import map from 'lodash/fp/map';
import flatMap from 'lodash/fp/flatMap';
import flow from 'lodash/fp/flow';
import get from 'lodash/fp/get';
import uniq from 'lodash/fp/uniq';

export default class PageConfigureOverviewController {
    static $inject = ['ConfigureState', '$scope', '$state', 'PageConfigure'];

    constructor(ConfigureState, $scope, $state, PageConfigure) {
        Object.assign(this, {ConfigureState, $scope, $state, PageConfigure});
    }

    $onDestroy() {
        this.subscription.unsubscribe();
    }

    $onInit() {
        this.subscription = this.getObservable(this.ConfigureState.state$).subscribe();
    }

    getObservable(state$) {
        return state$.pluck('list').map((list) => ({
            clustersTable: this.getClustersTable(list)
        })).do((value) => this.applyValue(value));
    }

    applyValue(value) {
        this.$scope.$applyAsync(() => Object.assign(this, value));
    }

    getClustersTable(list) {
        const countClusterDomains = flow(
            get('caches'),
            flatMap(flow(list.caches.get.bind(list.caches), get('domains'))),
            uniq,
            get('length')
        );
        return [...list.clusters.values()].map((cluster) => ({
            name: cluster.name,
            _id: cluster._id,
            version: null,
            discovery: cluster.discovery.kind,
            caches: cluster.caches.length,
            models: countClusterDomains(cluster),
            igfs: cluster.igfss.length
        }));
    }

    onClustersAction(action) {
        switch (action.type) {
            case 'EDIT':
                return this.PageConfigure.editCluster(action.items[0]._id);
            case 'CLONE':
                return this.PageConfigure.cloneClusters(action.items);
            case 'DELETE':
                return this.PageConfigure.removeClustersLocalRemote(action.items);
            default:
                return;
        }
    }

    createCluster() {
        this.PageConfigure.editCluster();
    }
}
