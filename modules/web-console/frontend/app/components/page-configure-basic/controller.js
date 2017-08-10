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

        this.extraFormActions = [
            {text: 'Save changes and download project', click: () => this.saveAndDownload()},
            {text: 'Save changes', click: () => this.save()}
        ];
    }

    getObservable(state$, version$) {
        return state$/* .pluck('clusterConfiguration')*/
        .combineLatest(version$, (state, version) => ({
            clusterConfiguration: state.clusterConfiguration,
            caches: state.clusterConfiguration.originalCaches,
            allClusterCaches: this.getAllClusterCaches(state),
            defaultMemoryPolicy: this.getDefaultClusterMemoryPolicy(state.clusterConfiguration.originalCluster),
            memorySizeInputVisible: this.getMemorySizeInputVisibility(version)
        }))
        // return state$.combineLatest(version$, (state, version) => ({
        //     clusters: state.list.clusters,
        //     caches: state.list.caches,
        //     state: state.configureBasic,
        //     allClusterCaches: this.getAllClusterCaches(state.configureBasic),
        //     cachesMenu: this.getCachesMenu(state.list.caches),
        //     defaultMemoryPolicy: this.getDefaultClusterMemoryPolicy(state.configureBasic.cluster),
        //     memorySizeInputVisible: this.getMemorySizeInputVisibility(version)
        // }))
        .do((value) => this.applyValue(value));
    }

    applyValue(value) {
        this.$scope.$applyAsync(() => Object.assign(this, value));
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

    // set clusterID(value) {
    //     this.pageService.setCluster(value);
    // }

    // get clusterID() {
    //     return get(this, 'state.clusterID');
    // }

    set oldClusterCaches(value) {
        this.pageService.setSelectedCaches(value);
    }

    _oldClusterCaches = [];

    get oldClusterCaches() {
        // TODO IGNITE-5271 Keep ng-model reference the same, otherwise ng-repeat in bs-select will enter into
        // infinite digest loop.
        this._oldClusterCaches.splice(0, this._oldClusterCaches.length, ...get(this, 'state.oldClusterCaches', []).map((c) => c._id));
        return this._oldClusterCaches;
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
            this.clusterConfiguration.originalCluster,
            this.ConfigureState.state$.value.basicCaches
        );
    }

    saveAndDownload() {
        return this.save().then(([clusterID]) => (
            this.ConfigurationDownload.downloadClusterConfiguration({_id: clusterID, name: this.state.cluster.name})
        ));
    }

    getAllClusterCaches(state) {
        const idx = (id) => (cache) => cache._id === id;
        return [...state.basicCaches.ids.values()].map((id) => (
            state.basicCaches.changedItems.get(id) ||
            (state.clusterConfiguration.originalCaches || []).find(idx(id))
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
