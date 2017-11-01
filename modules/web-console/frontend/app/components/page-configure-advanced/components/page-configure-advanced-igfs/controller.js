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
import {combineLatest} from 'rxjs/observable/combineLatest';
import naturalCompare from 'natural-compare-lite';
import {merge} from 'rxjs/observable/merge';
import get from 'lodash/get';
import {
    removeClusterItems
} from 'app/components/page-configure/store/actionCreators';

export default class PageConfigureAdvancedIGFS {
    static $inject = ['ConfigSelectors', 'ConfigureState', '$uiRouter', 'IGFSs', '$state', 'conf', 'configSelectionManager'];
    constructor(ConfigSelectors, ConfigureState, $uiRouter, IGFSs, $state, conf, configSelectionManager) {
        Object.assign(this, {ConfigSelectors, ConfigureState, $uiRouter, IGFSs, $state, conf, configSelectionManager});
    }
    $onDestroy() {
        this.subscription.unsubscribe();
        this.visibleRows$.complete();
        this.selectedRows$.complete();
    }
    $onInit() {
        this.visibleRows$ = new Subject();
        this.selectedRows$ = new Subject();
        this.columnDefs = [
            {
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by name…'
                },
                sort: {direction: 'asc', priority: 0},
                sortingAlgorithm: naturalCompare,
                minWidth: 165
            },
            {
                name: 'defaultMode',
                displayName: 'Mode',
                field: 'defaultMode',
                multiselectFilterOptions: this.IGFSs.defaultMode.values,
                width: 160
            },
            {
                name: 'affinnityGroupSize',
                displayName: 'Group size',
                field: 'affinnityGroupSize',
                enableFiltering: false,
                width: 130
            }
        ];
        this.itemID$ = this.$uiRouter.globals.params$.pluck('igfsID');
        this.shortItems$ = this.ConfigureState.state$
            .let(this.ConfigSelectors.selectCurrentShortIGFSs)
            .map((items = []) => items.map((i) => ({
                _id: i._id,
                name: i.name,
                affinnityGroupSize: i.affinnityGroupSize || this.IGFSs.affinnityGroupSize.default,
                defaultMode: i.defaultMode || this.IGFSs.defaultMode.default
            })));
        this.originalItem$ = this.itemID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectIGFSToEdit(id));
        })/* .take(1)*/.distinctUntilChanged().publishReplay(1).refCount().debug('igfs to edit');
        this.isNew$ = this.itemID$.map((id) => id === 'new');
        this.itemEditTitle$ = combineLatest(this.isNew$, this.originalItem$, (isNew, item) => {
            return `${isNew ? 'Create' : 'Edit'} IGFS ${!isNew && get(item, 'name') ? `‘${get(item, 'name')}’` : ''}`;
        });
        this.selectionManager = this.configSelectionManager({
            itemID$: this.itemID$,
            selectedItemRows$: this.selectedRows$,
            visibleRows$: this.visibleRows$,
            loadedItems$: this.shortItems$
        });
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
        this.subscription = merge(
            this.originalItem$,
            this.selectionManager.editGoes$.do((id) => this.edit(id)),
            this.selectionManager.editLeaves$.do(() => this.$state.go('base.configuration.edit.advanced.igfs'))
        ).subscribe();
    }
    edit(igfsID) {
        this.$state.go('base.configuration.edit.advanced.igfs.igfs', {igfsID});
    }
    save(igfs) {
        this.conf.saveAdvanced({igfs});
    }
    remove(itemIDs) {
        this.ConfigureState.dispatchAction(
            removeClusterItems(this.$uiRouter.globals.params.clusterID, 'igfss', itemIDs, true, true)
        );
    }
}
