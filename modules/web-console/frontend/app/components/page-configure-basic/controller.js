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
        '$scope',
        'PageConfigureBasic',
        'Clusters',
        'ConfigureState',
        'ConfigurationDownload',
        'IgniteVersion',
        '$state',
        '$element'
    ];

    constructor($scope, pageService, Clusters, ConfigureState, ConfigurationDownload, Version, $state, $element) {
        Object.assign(this, {$scope, pageService, Clusters, ConfigureState, ConfigurationDownload, Version, $state, $element});
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    $onInit() {
        this.subscription = this.getObservable(this.ConfigureState.state$, this.Version.currentSbj).subscribe();
        this.discoveries = this.Clusters.discoveries;
        this.minMemorySize = this.Clusters.minMemoryPolicySize;

        // TODO IGNITE-5271: extract into size input component
        this.sizesMenu = [
            {label: 'Kb', value: 1024},
            {label: 'Mb', value: 1024 * 1024},
            {label: 'Gb', value: 1024 * 1024 * 1024}
        ];

        this.memorySizeScale = this.sizesMenu[2];
        // this.pageService.setCluster(this.$state.params.clusterID || '-1');
        this.extraFormActions = [
            {text: 'Save changes and download project', click: () => this.saveAndDownload()},
            {text: 'Save changes', click: () => this.save()}
        ];
    }

    getObservable(state$, version$) {
        return state$.pluck('clusterConfiguration')
        .combineLatest(version$, (clusterConfiguration, version) => ({
            clusterConfiguration,
            allClusterCaches: this.getAllClusterCaches(clusterConfiguration),
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
        this.pageService.addCache();
    }

    removeCache(cache) {
        this.pageService.removeCache(cache);
    }

    save() {
        return this.pageService.saveClusterAndCaches(this.state.cluster, this.allClusterCaches);
    }

    saveAndDownload() {
        return this.save().then(([clusterID]) => (
            this.ConfigurationDownload.downloadClusterConfiguration({_id: clusterID, name: this.state.cluster.name})
        ));
    }

    getCachesMenu(caches = []) {
        return [...caches.values()].map((c) => ({_id: c._id, name: c.name}));
    }

    getAllClusterCaches(state = {originalCaches: [], newCaches: []}) {
        return [...state.originalCaches, ...state.newCaches];
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
