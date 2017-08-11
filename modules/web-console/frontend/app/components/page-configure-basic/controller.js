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
import cloneDeep from 'lodash/cloneDeep';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/merge';
import 'rxjs/add/observable/combineLatest';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/combineLatest';

export default class PageConfigureBasicController {
    static $inject = [
        'IgniteConfirm',
        '$scope',
        'PageConfigureBasic',
        'Clusters',
        'ConfigureState',
        'ConfigurationDownload',
        'IgniteVersion',
        '$state',
        '$element'
    ];

    constructor(IgniteConfirm, $scope, pageService, Clusters, ConfigureState, ConfigurationDownload, Version, $state, $element) {
        Object.assign(this, {IgniteConfirm, $scope, pageService, Clusters, ConfigureState, ConfigurationDownload, Version, $state, $element});
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    $onInit() {
        this.subscription = this.getObservable(this.ConfigureState.state$, this.Version.currentSbj).subscribe();
        this.discoveries = this.Clusters.discoveries;
        this.minMemorySize = this.Clusters.minMemoryPolicySize;

        this.formActionsMenu = [
            {text: 'Save changes and download project', click: () => this.saveAndDownload()},
            {text: 'Save changes', click: () => this.save()}
        ];
    }

    getObservable(state$, version$) {
        const cluster = state$
            .pluck('clusterConfiguration', 'originalCluster')
            .distinctUntilChanged()
            .map((cluster) => cloneDeep(cluster))
            .do((clonedCluster) => this.$scope.$applyAsync(() => {
                this.clonedCluster = clonedCluster;
                this.defaultMemoryPolicy = this.getDefaultClusterMemoryPolicy(clonedCluster);
            }));

        const allCaches = Observable.combineLatest(
            state$.pluck('basicCaches', 'ids').distinctUntilChanged().map((ids) => [...ids.values()]),
            state$.pluck('basicCaches', 'changedItems').distinctUntilChanged(),
            state$.pluck('shortCaches').distinctUntilChanged(),
            (ids, changedCaches, oldCaches) => {
                return ids.map((id) => changedCaches.get(id) || oldCaches.get(id)).filter((v) => v);
            }
        )
        .do((caches) => this.$scope.$applyAsync(() => this.allClusterCaches = caches));

        const memorySizeInputVisible = version$.do((version) => {
            this.memorySizeInputVisible = this.getMemorySizeInputVisibility(version);
        });

        return Observable.merge(cluster, allCaches, memorySizeInputVisible);
    }

    uiCanExit() {
        // TODO Refactor this
        return !this.form.$dirty || this.IgniteConfirm.confirm(`
            You have unsaved changes. Are you sure want to discard them?
        `);
    }

    $onDestroy() {
        this.subscription.unsubscribe();
    }

    addCache() {
        this.pageService.addCache(this.allClusterCaches);
    }

    removeCache(cache) {
        this.pageService.removeCache(cache);
    }

    updateCache(cache) {
        this.pageService.updateCache(cache);
    }

    save() {
        return this.pageService.transcationalSaveClusterAndCaches(
            this.clonedCluster,
            this.ConfigureState.state$.value.basicCaches
        );
    }

    saveAndDownload() {
        return this.save().then(([clusterID]) => (
            this.ConfigurationDownload.downloadClusterConfiguration({_id: clusterID, name: this.state.cluster.name})
        ));
    }

    getDefaultClusterMemoryPolicy(cluster, version) {
        if (this.Version.since(version.ignite, ['2.1.0', '2.3.0']))
            return get(cluster, 'memoryConfiguration.memoryPolicies', []).find((p) => p.name === 'default');

        return get(cluster, 'dataStorageConfiguration.defaultDataRegionConfiguration') ||
            get(cluster, 'dataStorageConfiguration.dataRegionConfigurations', []).find((p) => p.name === 'default');
    }

    getMemorySizeInputVisibility(version) {
        return this.Version.since(version.ignite, '2.0.0');
    }
}
