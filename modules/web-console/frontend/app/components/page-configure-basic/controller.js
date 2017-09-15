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
import 'rxjs/add/operator/map';
import cloneDeep from 'lodash/cloneDeep';

export default class PageConfigureBasicController {
    static $inject = [
        'IgniteConfirm', '$uiRouter', 'ConfigureState', 'ConfigSelectors', 'conf', 'Clusters', 'Caches', 'IgniteVersion', '$element', 'ConfigChangesGuard', 'IgniteFormUtils', '$scope'
    ];

    constructor(IgniteConfirm, $uiRouter, ConfigureState, ConfigSelectors, conf, Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope) {
        Object.assign(this, {IgniteConfirm, $uiRouter, ConfigureState, ConfigSelectors, conf, Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope});
    }

    $onDestroy() {
        this.subscription.unsubscribe();
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    uiCanExit() {
        return this.ConfigureState.state$.pluck('edit', 'changes').take(1).toPromise().then((changes) => {
            return this.ConfigChangesGuard.guard(
                {
                    cluster: this.Clusters.normalize(this.originalCluster),
                    caches: []
                },
                {
                    cluster: {...this.Clusters.normalize(this.clonedCluster), caches: changes.caches.ids},
                    caches: changes.caches.changedItems.map(this.Caches.normalize)
                }
            );
        });
    }

    $onInit() {
        this.memorySizeInputVisible$ = this.IgniteVersion.currentSbj
            .map((version) => this.IgniteVersion.since(version.ignite, '2.0.0'));

        const clusterID$ = this.$uiRouter.globals.params$.take(1).pluck('clusterID').filter((v) => v).take(1).debug('clusterID$');

        this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortCaches);
        this.shortClusters$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectShortClustersValue());
        this.originalCluster$ = clusterID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectClusterToEdit(id)).debug('clusterToEdit');
        }).debug('originalCluster$');

        this.subscription = Observable.merge(
            this.shortCaches$.do((v) => this.shortCaches = v),
            this.shortClusters$.do((v) => this.shortClusters = v),
            this.originalCluster$.do((v) => {
                this.originalCluster = v;
                this.clonedCluster = cloneDeep(v);
                this.defaultMemoryPolicy = this.Clusters.getDefaultClusterMemoryPolicy(this.clonedCluster);
            })
        ).subscribe();

        this.formActionsMenu = [
            {
                text: 'Save changes and download project',
                click: () => this.save(true),
                icon: 'download'
            },
            {
                text: 'Save changes',
                click: () => this.save(),
                icon: 'checkmark'
            }
        ];
    }

    addCache() {
        this.conf.addItem('caches');
    }

    removeCache(cache) {
        this.conf.removeItem({type: 'caches', itemIDs: [cache._id]});
        // this.conf.removeItem('caches', cache._id);
    }

    changeCache(cache) {
        this.conf.changeItem('caches', cache);
    }

    save(andDownload = false) {
        if (this.form.$invalid) return this.IgniteFormUtils.triggerValidation(this.form, this.$scope);
        this.conf.saveBasic({andDownload, cluster: cloneDeep(this.clonedCluster)});
    }

    resetAll() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current cluster?')
        .then(() => this.conf.onEditCancel());
    }
}
