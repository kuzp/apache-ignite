import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {fromPromise} from 'rxjs/observable/fromPromise';
import {empty} from 'rxjs/observable/empty';
import {never} from 'rxjs/observable/never';
import {merge} from 'rxjs/observable/merge';
import {of} from 'rxjs/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/exhaustMap';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/publishReplay';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/partition';
import 'rxjs/add/operator/let';
import {selectShortClusters, selectShortClustersValue, selectCluster, selectEditCluster, selectShortCaches, selectShortCachesValue, selectCache, selectEditCache} from 'app/components/page-configure/reducer';
import {uniqueName} from 'app/utils/uniqueName';
import naturalCompare from 'natural-compare-lite';
import camelCase from 'lodash/camelCase';
import cloneDeep from 'lodash/cloneDeep';
import isMatch from 'lodash/isMatch';
import angular from 'angular';
import 'angular1-async-filter';
import {
    shortIGFSsActionTypes,
    igfssActionTypes,
    shortModelsActionTypes,
    modelsActionTypes,
    shortClustersActionTypes,
    cachesActionTypes,
    shortCachesActionTypes,
    clustersActionTypes,
    basicCachesActionTypes,
    RECEIVE_CLUSTER_EDIT,
    RECEIVE_CACHE_EDIT,
    RECEIVE_MODELS_EDIT,
    RECEIVE_MODEL_EDIT,
    RECEIVE_IGFSS_EDIT,
    RECEIVE_IGFS_EDIT,
    SHOW_CONFIG_LOADING,
    LOAD_ITEMS,
    HIDE_CONFIG_LOADING
} from 'app/components/page-configure/reducer';
export default angular
.module('page-configuration', ['asyncFilter'])

.service('conf', class Conf {
    static $inject = ['$window', 'ConfigureState', '$uiRouter', 'Clusters', '$state', 'PageConfigureBasic', 'Caches', 'IGFSs'];
    constructor($window, ConfigureState, {globals: {params$}}, Clusters, $state, PageConfigureBasic, Caches, IGFSs) {
        Object.assign(this, {$window, ConfigureState, Caches, IGFSs, params$, $state});
        const {state$, actions$} = ConfigureState;

        const shortClusters$ = state$
            .pluck('shortClusters')
            .distinctUntilChanged()
            .filter((v) => v)
            .pluck('value')
            .map((v) => [...v.values()])
            .do((v) => console.log('shortClusters', v))
            .publishReplay(1).refCount();

        Object.assign(this, {shortClusters$});
    }
    changeItem(type, item) {
        return this.ConfigureState.dispatchAction({
            type: 'UPSERT_CLUSTER_ITEM',
            itemType: type,
            item
        });
    }
    addItem(type) {
        const make = {
            caches: () => this.Caches.getBlankCache(),
            igfss: () => this.IGFSs.getBlankIGFS()
        };
        const item = make[type]();
        this.ConfigureState.dispatchAction({
            type: 'UPSERT_CLUSTER_ITEM',
            itemType: type,
            item: {...item, name: item._id}
        });
    }
    removeItem({type, itemIDs, andSave}) {
        this.ConfigureState.dispatchAction({
            type: 'REMOVE_CLUSTER_ITEMS',
            itemType: type,
            itemIDs
        });
        if (andSave) this.saveAdvanced();
    }
    upsertCluster(cluster) {
        return this.ConfigureState.dispatchAction({
            type: 'UPSERT_CLUSTER',
            cluster
        });
    }
    _applyChangedIDs = (edit) => ({
        cluster: {
            ...edit.changes.cluster,
            caches: edit.changes.caches.ids,
            igfss: edit.changes.igfss.ids,
            models: edit.changes.models.ids
        },
        caches: edit.changes.caches.changedItems,
        igfss: edit.changes.igfss.changedItems,
        models: edit.changes.models.changedItems
    });
    saveBasic({download, cluster}) {
        const prevActions = [];
        if (cluster) prevActions.push(this.upsertCluster(cluster));
        this.ConfigureState.state$.pluck('edit').take(1).do((edit) => {
            this.ConfigureState.dispatchAction({
                type: 'BASIC_SAVE_CLUSTER_AND_CACHES',
                changedItems: this._applyChangedIDs(edit),
                prevActions
            });
        }).subscribe();
    }
    saveAdvanced({cluster, cache, igfs, model} = {}) {
        const prevActions = [];
        if (cluster) prevActions.push(this.upsertCluster(cluster));
        if (cache) prevActions.push(this.changeItem('caches', cache));
        if (igfs) prevActions.push(this.changeItem('igfss', igfs));
        if (model) prevActions.push(this.changeItem('models', model));
        this.ConfigureState.state$.pluck('edit').take(1).do((edit) => {
            this.ConfigureState.dispatchAction({
                type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
                changedItems: this._applyChangedIDs(edit),
                prevActions
            });
        }).subscribe();
    }
    goToItemCreation(type) {
        this.$state.go('conf.edit.item', {itemType: type, itemID: 'new'});
    }
    confirmExit(itemType, original, changed) {
        return isMatch(original, changed) || this.$window.confirm(`You have unsaved ${itemType}, wanna leave?`);
    }
    onEditCancel() {
        this.ConfigureState.dispatchAction({
            type: 'RESET_EDIT_CHANGES'
        });
    }
});
