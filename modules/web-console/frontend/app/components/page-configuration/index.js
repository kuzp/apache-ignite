import {Subject} from 'rxjs/Subject';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {fromPromise} from 'rxjs/observable/fromPromise';
import {empty} from 'rxjs/observable/empty';
import {never} from 'rxjs/observable/never';
import {merge} from 'rxjs/observable/merge';
import {of} from 'rxjs/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/publishReplay';
import 'rxjs/add/operator/toPromise';
import {uniqueName} from 'app/utils/uniqueName';
import naturalCompare from 'natural-compare-lite';
import camelCase from 'lodash/camelCase';
import cloneDeep from 'lodash/cloneDeep';
// import {Observable} from 'rxjs/observable';
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
.component('pageConf', {
    controller: class PageConfController {
        static $inject = ['$uiRouter', '$state', 'conf', 'Caches', 'PageConfigureOverviewService', 'ConfigureState', 'IGFSs'];
        constructor({globals: {params$}}, $state, conf, Caches, overview, ConfigureState, IGFSs) {
            Object.assign(this, {params$, $state, conf, Caches, overview, ConfigureState, IGFSs});
        }
        $onInit() {
            this.cluster$ = this.ConfigureState.state$.pluck('edit', 'changes', 'cluster');
            this.clusters$ = this.conf.shortClusters$;
            this.clustersDefs = [{
                name: 'name',
                displayName: 'Name',
                field: 'name',
                enableHiding: false,
                filter: {
                    placeholder: 'Filter by name…'
                },
                sort: {direction: 'asc', priority: 0},
                minWidth: 165
            }, {
                name: 'caches',
                displayName: 'Caches',
                field: 'cachesCount',
                cellClass: 'ui-grid-number-cell',
                enableFiltering: false,
                width: 95
            }, {
                name: 'igfss',
                displayName: 'IGFSs',
                field: 'igfsCount',
                cellClass: 'ui-grid-number-cell',
                enableFiltering: false,
                width: 95
            }];
            const clusterShortCaches$ = combineLatest(
                this.ConfigureState.state$.pluck('edit', 'changes', 'caches').distinctUntilChanged(),
                this.ConfigureState.state$.pluck('shortCaches', 'value').distinctUntilChanged()
            )
                .map(([{ids, changedItems}, shortCaches]) => {
                    if (!ids.length || !shortCaches) return [];
                    return ids.map((id) => changedItems.find(({_id}) => _id === id) || shortCaches.get(id));
                });

            const clusterShortIGFS$ = combineLatest(
                this.ConfigureState.state$.pluck('edit', 'changes', 'igfss').distinctUntilChanged(),
                this.ConfigureState.state$.pluck('shortIgfss', 'value').distinctUntilChanged().do((v) => console.log('shortIgfss', v))
            )
                .map(([{ids, changedItems}, shortItems]) => {
                    if (!ids.length || !shortItems) return [];
                    return ids.map((id) => changedItems.find(({_id}) => _id === id) || shortItems.get(id));
                });

            this.clusterItems$ = combineLatest(
                clusterShortCaches$,
                clusterShortIGFS$,
                (shortCaches, shortIgfss) => ({shortCaches, shortIgfss})
            ).publishReplay(1).refCount();
            this.changedItems$ = this.ConfigureState.state$.pluck('edit', 'changes');
        }
        onSelectionChange(selectedClusters) {
            return this.clustersActions = this.makeClusterActions(selectedClusters);
        }
        makeClusterActions(selectedClusters) {
            return [
                {
                    action: 'Edit',
                    click: () => this.$state.go('conf.edit', {clusterID: selectedClusters[0]._id}),
                    available: selectedClusters.length === 1
                },
                {
                    action: 'Delete',
                    click: () => this.overview.removeClusters(selectedClusters),
                    available: true
                }
            ];
        }
        addNew() {
            this.$state.go('conf.edit', {mode: 'basic', clusterID: 'new'});
        }
        onBasicSave(cluster) {
            this.changedItems$.take(1).do((changedItems) => {
                this.conf.saveBasic(changedItems);
            }).subscribe();
        }
        onAdvancedSave() {
            this.changedItems$.take(1).do((changedItems) => {
                this.conf.saveAdvanced(changedItems);
            }).subscribe();
        }
        onClusterChange(cluster) {
            this.conf.upsertCluster(cluster);
        }
        onItemChange({type, item}) {
            this.conf.changeItem(type, item);
        }
        onItemAdd({type}) {
            this.conf.addItem(type);
        }
        onItemRemove({type, item}) {
            this.conf.removeItem(type, item._id);
        }
        goToItemCreation({type, item}) {
            this.conf.goToItemCreation(type);
        }
    },
    template: `
        <pc-items-table
            column-defs='$ctrl.clustersDefs'
            actions-menu='$ctrl.clustersActions'
            on-selection-change='$ctrl.onSelectionChange($event)'
            items='$ctrl.clusters$|async:this'
        ></pc-items-table>
        <button type='button' ng-click='$ctrl.addNew()'>Add new</button>
        <button type='button' ui-sref='conf.edit({mode: "basic"})'>Basic</button>
        <button type='button' ui-sref='conf.edit({mode: "advanced"})'>Advanced</button>
        <h1>Edit</h1>
        <ui-view></ui-view>
        <details>
            <summary>params$</summary>
            <pre>{{$ctrl.params$|async:this|json}}</pre>
        </details>
        <details>
            <summary>changedItems</summary>
            <pre>{{$ctrl.changedItems$|async:this|json}}</pre>
        </details>
        <details>
            <summary>clusterItems</summary>
            <pre>{{$ctrl.clusterItems$|async:this|json}}</pre>
        </details>
        <details>
            <summary>cluster</summary>
            <pre>{{$ctrl.cluster$|async:this|json}}</pre>
        </details>
    `
})
.service('conf', class Conf {
    static $inject = ['ConfigureState', '$uiRouter', 'Clusters', '$state', 'PageConfigureBasic', 'Caches', 'IGFSs', 'PageConfigureAdvanced'];
    constructor(ConfigureState, {globals: {params$}}, Clusters, $state, PageConfigureBasic, Caches, IGFSs, PageConfigureAdvanced) {
        Object.assign(this, {ConfigureState, Caches, IGFSs, params$, $state});
        const {state$, actions$} = ConfigureState;

        const basicRedirects$ = actions$
            .filter((a) => a.type === 'BASIC_SAVE_CLUSTER_AND_CACHES_OK')
            .do((a) => $state.go('conf.edit', {mode: 'basic', clusterID: a.changedItems.cluster._id}, {location: 'replace'}));

        merge(basicRedirects$, params$).subscribe();

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
        this.ConfigureState.dispatchAction({
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
    removeItem(type, itemID) {
        this.ConfigureState.dispatchAction({
            type: 'REMOVE_CLUSTER_ITEM',
            itemType: type,
            itemID
        });
    }
    upsertCluster(cluster) {
        this.ConfigureState.dispatchAction({
            type: 'UPSERT_CLUSTER',
            cluster
        });
    }
    _applyChangedIDs = (changes) => ({
        cluster: {
            ...changes.cluster,
            caches: changes.caches.ids,
            igfss: changes.igfss.ids,
            models: changes.models.ids
        },
        caches: changes.caches.changedItems,
        igfss: changes.igfss.changedItems,
        models: changes.models.changedItems
    });
    saveBasic(changedItems) {
        this.ConfigureState.dispatchAction({
            type: 'BASIC_SAVE_CLUSTER_AND_CACHES',
            changedItems: this._applyChangedIDs(changedItems)
        });
    }
    saveAdvanced(changedItems) {
        this.ConfigureState.dispatchAction({
            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
            changedItems: this._applyChangedIDs(changedItems)
        });
    }
    goToItemCreation(type) {
        this.$state.go('conf.edit.item', {itemType: type, itemID: 'new'});
    }
})
.component('confBasicForm', {
    template: `
        <h1>Basic</h1>
        <form ng-submit='$ctrl.save()' name='$ctrl.form'>
            <input type="text" name='name' ng-model='$ctrl.item.name' required/>
            <pc-list-editable
                ng-model='$ctrl.clusterItems.shortCaches'
                pc-list-editable-one-way
                on-item-change='$ctrl.changeCache($event)'
                on-item-remove='$ctrl.removeCache($event)'
            >
                <pc-list-editable-item-view>
                    {{ $parent.item.name }}
                </pc-list-editable-item-view>
                <pc-list-editable-item-edit>
                    <input type="text" ng-model='$parent.item.name' name='name' required>
                </pc-list-editable-item-edit>
            </pc-list-editable>
            <button type='button' ng-click='$ctrl.addCache()'>Add cache</button>
            <button type='submit'>Save</button>
        </form>
    `,
    controller: class ConfBasicController {
        $onInit() {
            this.saveMethods = ['onSave', 'onSaveAndDownload'];
            this.saveMethod = this.saveMethods[0];
        }
        $onChanges(changes) {
            if ('cluster' in changes && changes.cluster.currentValue)
                this.item = cloneDeep(changes.cluster.currentValue);
        }
        addCache(cache) {
            this.onCacheAdd({$event: {item: cache, type: 'caches'}});
        }
        removeCache(cache) {
            this.onCacheRemove({$event: {item: cache, type: 'caches'}});
        }
        changeCache(cache) {
            this.onCacheChange({$event: {item: cache, type: 'caches'}});
        }
        save() {
            if (this.form.$invalid) return;
            this.onClusterChange({$event: cloneDeep(this.item)});
            this[this.saveMethod]();
        }
    },
    bindings: {
        cluster: '<?',
        clusterItems: '<?',
        onClusterChange: '&',
        onCacheAdd: '&?',
        onCacheChange: '&?',
        onCacheRemove: '&?',
        onSave: '&?',
        onSaveAndDownload: '&?'
    }
})
.component('confAdvancedForm', {
    template: `
        <h1>Advanced</h1>
        <form ng-submit='$ctrl.save()' name='$ctrl.form'>
            <input type="text" name='name' ng-model='$ctrl.item.name' required/>
            <details>
                <summary>Caches</summary>
                <ul>
                    <li ng-repeat='sc in $ctrl.clusterItems.shortCaches track by sc._id'>
                        <a ui-sref='conf.edit.item({itemID: sc._id, itemType: "caches"})'>{{sc.name}}</a>
                    </li>
                </ul>
                <pc-list-editable
                    hidden
                    ng-model='$ctrl.clusterItems.shortCaches'
                    pc-list-editable-one-way
                    on-item-change='$ctrl.changeItem($event, "caches")'
                    on-item-remove='$ctrl.removeCache($event, "caches")'
                >
                    <pc-list-editable-item-view>
                        {{ $parent.item.name }}
                    </pc-list-editable-item-view>
                    <pc-list-editable-item-edit>
                        <input type="text" ng-model='$parent.item.name' name='name' required>
                    </pc-list-editable-item-edit>
                </pc-list-editable>
                <ui-view name='caches'></ui-view>
                <a ui-sref='conf.edit.item({itemType: "caches", itemID: "new"})'>Add cache</a>
            </details>
            <details open>
                <summary>IGFSS</summary>
                <ul>
                    <li ng-repeat='i in $ctrl.clusterItems.shortIgfss track by i._id'>
                        <a ui-sref='conf.edit.item({itemID: i._id, itemType: "igfss"})'>{{i.name}}</a>
                    </li>
                </ul>
                <pc-list-editable
                    hidden
                    ng-model='$ctrl.clusterItems.shortIgfss'
                    pc-list-editable-one-way
                    on-item-change='$ctrl.changeItem($event, "igfss")'
                    on-item-remove='$ctrl.removeCache($event, "igfss")'
                >
                    <pc-list-editable-item-view>
                        {{ $parent.item.name }}
                    </pc-list-editable-item-view>
                    <pc-list-editable-item-edit>
                        <input type="text" ng-model='$parent.item.name' name='name' required>
                    </pc-list-editable-item-edit>
                </pc-list-editable>
                <ui-view name='igfss'></ui-view>
                <a ui-sref='conf.edit.item({itemType: "igfss", itemID: "new"})'>Add IGFS</a>
            </details>
            <button type='submit'>Save</button>
        </form>
    `,
    controller: class ConfAdvancedController {
        $onChanges(changes) {
            if ('cluster' in changes && changes.cluster.currentValue)
                this.item = cloneDeep(changes.cluster.currentValue);
        }
        addItem(type) {
            this.onItemAdd({$event: {type}});
        }
        removeItem(item, type) {
            this.onItemRemove({$event: {item, type}});
        }
        changeItem(item, type) {
            this.onItemChange({$event: {item, type}});
        }
        save() {
            if (this.form.$invalid) return;
            this.onClusterChange({$event: cloneDeep(this.item)});
            this.onSave();
        }
    },
    bindings: {
        cluster: '<?',
        clusterItems: '<?',
        onClusterChange: '&',
        onItemAdd: '&?',
        onItemChange: '&?',
        onItemRemove: '&?',
        onSave: '&?'
    }
})
.service('ConfigResolvers', class ConfigResolvers {
    static $inject = ['Clusters', 'ConfigureState', 'Caches', 'IGFSs'];
    constructor(Clusters, ConfigureState, Caches, IGFSs) {
        Object.assign(this, {Clusters, ConfigureState, Caches, IGFSs});
    }
    loadCluster$(id, shortClusters = []) {
        if (id === 'new') {
            return of({
                ...this.Clusters.getBlankCluster(),
                name: uniqueName('New cluster', shortClusters)
            })
            .do((cluster) => {
                this.ConfigureState.dispatchAction({
                    type: 'EDIT_CLUSTER',
                    cluster
                });
            });
        }
        return this.ConfigureState.state$
            .pluck('clusters')
            .take(1)
            .map((c) => c && c.get(id))
            .switchMap((c) => c
                ? of(c)
                : fromPromise(this.Clusters.getCluster(id)).pluck('data')
                    .do((cluster) => {
                        this.ConfigureState.dispatchAction({
                            type: clustersActionTypes.UPSERT,
                            items: [cluster]
                        });
                    })
            )
            .do((cluster) => {
                this.ConfigureState.dispatchAction({
                    type: 'EDIT_CLUSTER',
                    cluster
                });
            });
    }
    loadShortClusters$() {
        return fromPromise(this.Clusters.getClustersOverview())
            .pluck('data')
            .do((items) => this.ConfigureState.dispatchAction({
                type: shortClustersActionTypes.UPSERT,
                items
            }));
    }
    loadShortItems$(cluster, itemType) {
        const load = {
            caches: (...args) => this.Clusters.getClusterCaches(...args),
            igfss: (...args) => this.Clusters.getClusterIGFSs(...args)
        };
        const at = {
            caches: shortCachesActionTypes,
            igfss: shortIGFSsActionTypes
        };
        if (!cluster[itemType].length) return of([]);
        return this.ConfigureState.state$
            .pluck(camelCase(`short ${itemType}`), 'value')
            .take(1)
            .switchMap((shortItems) => {
                if (shortItems && cluster[itemType].every((_id) => shortItems.has(_id)))
                    return of(cluster[itemType].map((_id) => shortItems.get(_id)));

                return fromPromise(load[itemType](cluster._id))
                .pluck('data')
                .do((items) => {
                    this.ConfigureState.dispatchAction({
                        type: at[itemType].UPSERT,
                        items
                    });
                });
            });
    }
    resolveItem$(itemType, itemID) {
        const load = {
            caches: (...args) => this.Caches.getCache(...args),
            igfss: (...args) => this.IGFSs.getIGFS(...args)
        };
        const make = {
            caches: () => this.Caches.getBlankCache(),
            igfss: () => this.IGFSs.getBlankIGFS()
        };
        const at = {
            caches: cachesActionTypes,
            igfss: igfssActionTypes
        };
        if (itemID === 'new') {
            return of(make[itemType]())
                .do((item) => {
                    this.ConfigureState.dispatchAction({
                        type: 'EDIT_CLUSTER_ITEM',
                        itemType,
                        item
                    });
                });
        }
        return this.ConfigureState.state$
            .pluck(itemType)
            .take(1)
            .switchMap((items) => {
                if (items && items.has(itemID)) return of(items.get(itemID));
                return fromPromise(load[itemType](itemID))
                    .pluck('data')
                    .do((item) => {
                        this.ConfigureState.dispatchAction({
                            type: at[itemType].UPSERT,
                            items: [item]
                        });
                        this.ConfigureState.dispatchAction({
                            type: 'EDIT_CLUSTER_ITEM',
                            itemType,
                            item
                        });
                    });
            });
    }
})
.component('cacheForm', {
    bindings: {
        isNew: '<',
        cache: '<',
        onCacheChange: '&'
    },
    controller: class Controller {
        $onChanges(changes) {
            if ('cache' in changes && changes.cache.currentValue)
                this.item = cloneDeep(changes.cache.currentValue);
        }
        save() {
            if (this.form.$invalid) return;
            this.onCacheChange({$event: cloneDeep(this.item)});
        }
    },
    template: `
        <h1>{{$ctrl.isNew ? 'Create' : 'Edit'}} cache {{$ctrl.cache.name}}</h1>
        <div ng-form='$ctrl.form' name='$ctrl.form'>
            <label for="name">Cache name</label>
            <input type="text" ng-model='$ctrl.item.name' name='name' id='name' required/>
            <button type='button' ng-click='$ctrl.save()' >Save cache</button>
        </div>
    `
})
.component('igfsForm', {
    bindings: {
        isNew: '<',
        igfs: '<',
        onIgfsChange: '&'
    },
    controller: class Controller {
        $onChanges(changes) {
            if ('igfs' in changes && changes.igfs.currentValue)
                this.item = cloneDeep(changes.igfs.currentValue);
        }
        save() {
            if (this.form.$invalid) return;
            this.onIgfsChange({$event: cloneDeep(this.item)});
        }
    },
    template: `
        <h1>{{$ctrl.isNew ? 'Create' : 'Edit'}} IGFS {{$ctrl.igfs.name}}</h1>
        <div ng-form='$ctrl.form' name='$ctrl.form'>
            <label for="name">IGFS name</label>
            <input type="text" ng-model='$ctrl.item.name' name='name' id='name' required/>
            <label>ipcEndpointEnabled <input type="checkbox" name="ipcEndpointEnabled" id="ipcEndpointEnabled" ng-model="$ctrl.item.ipcEndpointEnabled"/></label>

            <button type='button' ng-click='$ctrl.save()' >Save IGFS</button>
        </div>
    `
})
.config(['$stateProvider', ($stateProvider) => {
    $stateProvider
    .state('conf', {
        url: '/conf',
        component: 'pageConf',
        resolve: {
            shortClusters: ['ConfigResolvers', (ConfigResolvers) => ConfigResolvers.loadShortClusters$().toPromise()]
        }
    })
    .state('conf.edit', {
        url: '/:clusterID/:mode',
        params: {
            mode: {
                value: 'advanced'
            }
        },
        resolve: {
            cluster: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                return $transition$.injector().getAsync('shortClusters').then((shortClusters) => {
                    return ConfigResolvers.loadCluster$($transition$.params().clusterID, shortClusters).toPromise();
                });
            }],
            shortCaches: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                return $transition$.injector().getAsync('cluster').then((cluster) => {
                    return ConfigResolvers.loadShortItems$(cluster, 'caches').toPromise();
                });
            }],
            shortIgfss: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                return $transition$.injector().getAsync('cluster').then((cluster) => {
                    return ConfigResolvers.loadShortItems$(cluster, 'igfss').toPromise();
                });
            }]
        },
        redirectTo: ($transition$) => {
            return $transition$.injector().getAsync('cluster').catch(() => {
                return 'conf';
            });
        },
        template: `
            <conf-basic-form
                ng-if='($ctrl.params$|async:this).mode === "basic"'
                cluster='($ctrl.cluster$|async:this)'
                cluster-items='$ctrl.clusterItems$|async:this'

                on-cluster-change='$ctrl.onClusterChange($event)'
                on-cache-add='$ctrl.onItemAdd($event)'
                on-cache-change='$ctrl.onItemChange($event)'
                on-cache-remove='$ctrl.onItemRemove($event)'

                on-save='$ctrl.onBasicSave()'
            ></conf-basic-form>
            <conf-advanced-form
                ng-if='($ctrl.params$|async:this).mode === "advanced"'
                cluster='($ctrl.cluster$|async:this)'
                cluster-items='$ctrl.clusterItems$|async:this'

                on-item-change='$ctrl.onItemChange($event)'
                on-item-remove='$ctrl.onItemRemove($event)'
                on-cluster-change='$ctrl.onClusterChange($event)'

                on-save='$ctrl.onAdvancedSave()'
            >
            </conf-advanced-form>
        `
    })
    .state('conf.edit.item', {
        url: '/:itemType/:itemID',
        resolve: {
            item: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                const {itemType, itemID} = $transition$.params();
                return ConfigResolvers.resolveItem$(itemType, itemID).toPromise();
            }]
        },
        views: {
            caches: {
                template: `
                    <cache-form
                        cache='$resolve.item'
                        is-new='$resolve.itemID === "new"'
                        on-cache-change='$ctrl.changeItem($event, "caches")'
                    ></cache-form>
                    <pre>{{$resolve.item|json}}</pre>
                `
            },
            igfss: {
                template: `
                    <igfs-form
                        igfs='$resolve.item'
                        is-new='$resolve.itemID === "new"'
                        on-igfs-change='$ctrl.changeItem($event, "igfss")'
                    ></igfs-form>
                    <pre>{{$resolve.item|json}}</pre>
                `
            }
        }
    });
}]);
