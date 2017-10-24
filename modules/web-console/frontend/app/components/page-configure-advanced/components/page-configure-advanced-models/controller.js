import {Subject} from 'rxjs/Subject';
import {combineLatest} from 'rxjs/observable/combineLatest';
import naturalCompare from 'natural-compare-lite';
import {merge} from 'rxjs/observable/merge';
import get from 'lodash/get';

import keyCellTemplate from './keyCell.template.pug';
import valueCellTemplate from './valueCell.template.pug';

import {
    removeClusterItems
} from 'app/components/page-configure/store/actionCreators';

export default class PageConfigureAdvancedModels {
    static $inject = ['ConfigSelectors', 'ConfigureState', '$uiRouter', 'Models', '$state', 'conf', 'configSelectionManager'];
    constructor(ConfigSelectors, ConfigureState, $uiRouter, Models, $state, conf, configSelectionManager) {
        Object.assign(this, {ConfigSelectors, ConfigureState, $uiRouter, Models, $state, conf, configSelectionManager});
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
                name: 'keyType',
                displayName: 'Key type',
                field: 'keyType',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by key type…'
                },
                cellTemplate: keyCellTemplate,
                minWidth: 165
            },
            {
                name: 'valueType',
                displayName: 'Value type',
                field: 'valueType',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by value type…'
                },
                sort: {direction: 'asc', priority: 0},
                cellTemplate: valueCellTemplate,
                minWidth: 165
            }
        ];
        this.itemID$ = this.$uiRouter.globals.params$.pluck('modelID');
        this.shortItems$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortModels);
        this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectCurrentShortCaches);
        this.originalItem$ = this.itemID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectModelToEdit(id));
        })/* .take(1)*/.distinctUntilChanged().publishReplay(1).refCount().debug('model to edit');
        this.isNew$ = this.itemID$.map((id) => id === 'new');
        this.itemEditTitle$ = combineLatest(this.isNew$, this.originalItem$, (isNew, item) => {
            return `${isNew ? 'Create' : 'Edit'} model ${!isNew && get(item, 'valueType') ? `‘${get(item, 'valueType')}’` : ''}`;
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
            this.selectionManager.editLeaves$.do(() => this.$state.go('base.configuration.edit.advanced.models'))
        ).subscribe();
    }
    edit(modelID) {
        this.$state.go('base.configuration.edit.advanced.models.model', {modelID});
    }
    save(model) {
        this.conf.saveAdvanced({model});
    }
    remove(itemIDs) {
        this.ConfigureState.dispatchAction(
            removeClusterItems(this.$uiRouter.globals.params.clusterID, 'models', itemIDs, true, true)
        );
    }
}
