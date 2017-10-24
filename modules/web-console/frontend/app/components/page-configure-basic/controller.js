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
import naturalCompare from 'natural-compare-lite';
import {
    removeClusterItems
} from 'app/components/page-configure/store/actionCreators';

export default class PageConfigureBasicController {
    static $inject = [
        'IgniteConfirm', '$uiRouter', 'ConfigureState', 'ConfigSelectors', 'conf', 'Clusters', 'Caches', 'IgniteVersion', '$element', 'ConfigChangesGuard', 'IgniteFormUtils', '$scope'
    ];

    constructor(IgniteConfirm, $uiRouter, ConfigureState, ConfigSelectors, conf, Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope) {
        Object.assign(this, {IgniteConfirm, $uiRouter, ConfigureState, ConfigSelectors, conf, Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope});
    }

    $onDestroy() {
        this.subscription.unsubscribe();
        if (this.onBeforeTransition) this.onBeforeTransition();
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    _uiCanExit($transition$) {
        if ($transition$.params().justIDUpdate) return true;
        $transition$.onSuccess({}, () => this.reset());
        return Observable.forkJoin(
            this.ConfigureState.state$.pluck('edit', 'changes').take(1),
            this.clusterID$.switchMap((id) => this.ConfigureState.state$.let(this.ConfigSelectors.selectClusterShortCaches(id))).take(1),
            this.shortCaches$.take(1)
        ).toPromise()
        .then(([changes, originalShortCaches, currentCaches]) => {
            return this.ConfigChangesGuard.guard(
                {
                    cluster: this.Clusters.normalize(this.originalCluster),
                    caches: originalShortCaches.map(this.Caches.normalize)
                },
                {
                    cluster: {...this.Clusters.normalize(this.clonedCluster), caches: changes.caches.ids},
                    caches: currentCaches.map(this.Caches.normalize)
                }
            );
        });
    }

    $onInit() {
        this.onBeforeTransition = this.$uiRouter.transitionService.onBefore({}, (t) => this._uiCanExit(t));

        this.memorySizeInputVisible$ = this.IgniteVersion.currentSbj
            .map((version) => this.IgniteVersion.since(version.ignite, '2.0.0'));

        const clusterID$ = this.$uiRouter.globals.params$.take(1).pluck('clusterID').filter((v) => v).take(1);
        this.clusterID$ = clusterID$;

        this.isNew$ = this.$uiRouter.globals.params$.pluck('clusterID').map((id) => id === 'new');
        this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortCaches);
        this.shortClusters$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectShortClustersValue());
        this.originalCluster$ = clusterID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectClusterToEdit(id)).debug('clusterToEdit');
        }).take(1).debug('originalCluster$');

        this.subscription = Observable.merge(
            this.shortCaches$.map((caches) => caches.sort((a, b) => naturalCompare(a.name, b.name))).do((v) => this.shortCaches = v),
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

        this.cachesColDefs = [
            {name: 'Name:', cellClass: 'pc-form-grid-col-10'},
            {name: 'Mode:', cellClass: 'pc-form-grid-col-10'},
            {name: 'Atomicity:', cellClass: 'pc-form-grid-col-10', tip: `
                Atomicity:
                <ul>
                    <li>ATOMIC - in this mode distributed transactions and distributed locking are not supported</li>
                    <li>TRANSACTIONAL - in this mode specified fully ACID-compliant transactional cache behavior</li>
                </ul>
            `},
            {name: 'Backups:', cellClass: 'pc-form-grid-col-10', tip: `
                Number of nodes used to back up single partition for partitioned cache
            `}
        ];
    }

    addCache() {
        this.ConfigureState.dispatchAction({type: 'ADD_CACHE_TO_EDIT'});
    }

    removeCache(cache) {
        this.ConfigureState.dispatchAction(
            removeClusterItems(this.$uiRouter.globals.params.clusterID, 'caches', [cache._id], false, false)
        );
    }

    changeCache(cache) {
        this.conf.changeItem('caches', cache);
    }

    save(andDownload = false) {
        if (this.form.$invalid) return this.IgniteFormUtils.triggerValidation(this.form, this.$scope);
        this.conf.saveBasic({andDownload, cluster: cloneDeep(this.clonedCluster)});
    }

    reset() {
        this.clonedCluster = cloneDeep(this.originalCluster);
        this.ConfigureState.dispatchAction({
            type: 'RESET_EDIT_CHANGES'
        });
    }

    confirmAndReset() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current cluster?')
        .then((forReal = true) => {
            if (forReal) this.reset();
        });
    }
}
