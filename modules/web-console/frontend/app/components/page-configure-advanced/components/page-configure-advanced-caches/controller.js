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

import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs';
import {merge} from 'rxjs/observable/merge';
import get from 'lodash/get';
import cloneDeep from 'lodash/cloneDeep';
import naturalCompare from 'natural-compare-lite';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {
    removeClusterItems
} from 'app/components/page-configure/store/actionCreators';

import ConfigureState from 'app/components/page-configure/services/ConfigureState';
import ConfigSelectors from 'app/components/page-configure/store/selectors';
import Caches from 'app/services/Caches';

// Controller for Caches screen.
export default class Controller {
    static $inject = [
        'conf',
        ConfigSelectors.name,
        'configSelectionManager',
        '$uiRouter',
        '$transitions',
        ConfigureState.name,
        '$state',
        'IgniteFormUtils',
        'IgniteVersion',
        Caches.name
    ];
    /**
     * @param {object} conf
     * @param {ConfigSelectors} ConfigSelectors
     * @param {object} configSelectionManager
     * @param {object} $uiRouter
     * @param {object} $transitions
     * @param {ConfigureState} ConfigureState
     * @param {object} $state
     * @param {object} FormUtils
     * @param {object} Version
     * @param {Caches} Caches
     */
    constructor(conf, ConfigSelectors, configSelectionManager, $uiRouter, $transitions, ConfigureState, $state, FormUtils, Version, Caches) {
        Object.assign(this, {conf, configSelectionManager, $uiRouter, $transitions, $state, FormUtils});
        this.ConfigSelectors = ConfigSelectors;
        this.ConfigureState = ConfigureState;
        this.Caches = Caches;

        this.visibleRows$ = new Subject();
        this.selectedRows$ = new Subject();
        this.cachesColumnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                sort: {direction: 'asc', priority: 0},
                filter: {
                    placeholder: 'Filter by key type…'
                },
                sortingAlgorithm: naturalCompare,
                minWidth: 165
            },
            {
                name: 'cacheMode',
                displayName: 'Mode',
                field: 'cacheMode',
                multiselectFilterOptions: Caches.cacheModes,
                width: 160
            },
            {
                name: 'atomicityMode',
                displayName: 'Atomicity',
                field: 'atomicityMode',
                multiselectFilterOptions: Caches.atomicityModes,
                width: 160
            },
            {
                name: 'backups',
                displayName: 'Backups',
                field: 'backups',
                width: 130,
                enableFiltering: false
            }
        ];
    }

    $onInit() {
        const cacheID$ = this.$uiRouter.globals.params$.pluck('cacheID').publishReplay(1).refCount();

        this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortCaches);
        this.shortModels$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortModels);
        this.shortIGFSs$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortIGFSs);
        this.originalCache$ = cacheID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectCacheToEdit(id));
        });

        this.isNew$ = cacheID$.map((id) => id === 'new');
        this.itemEditTitle$ = combineLatest(this.isNew$, this.originalCache$, (isNew, cache) => {
            return `${isNew ? 'Create' : 'Edit'} cache ${!isNew && cache.name ? `‘${cache.name}’` : ''}`;
        });
        this.selectionManager = this.configSelectionManager({
            itemID$: cacheID$,
            selectedItemRows$: this.selectedRows$,
            visibleRows$: this.visibleRows$,
            loadedItems$: this.shortCaches$
        });

        this.subscription = merge(
            this.originalCache$,
            this.selectionManager.editGoes$.do((id) => this.edit(id)),
            this.selectionManager.editLeaves$.do(() => this.$state.go('base.configuration.edit.advanced.caches'))
        ).subscribe();

        this.isBlocked$ = cacheID$;

        this.tableActions$ = this.selectionManager.selectedItemIDs$.map((selectedItems) => [
            {
                action: 'Clone',
                click: () => this.clone(selectedItems),
                available: false
            },
            {
                action: 'Delete',
                click: () => {
                    this.remove(selectedItems);
                },
                available: true
            }
        ]);
    }

    remove(itemIDs) {
        this.ConfigureState.dispatchAction(
            removeClusterItems(this.$uiRouter.globals.params.clusterID, 'caches', itemIDs, true, true)
        );
    }

    $onDestroy() {
        this.subscription.unsubscribe();
        this.visibleRows$.complete();
        this.selectedRows$.complete();
    }

    edit(cacheID) {
        this.$state.go('base.configuration.edit.advanced.caches.cache', {cacheID});
    }

    save(cache) {
        this.conf.saveAdvanced({cache});
    }
}
