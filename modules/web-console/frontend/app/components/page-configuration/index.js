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
    // bindings: {
    //     shortClusters: '<'
    // },
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
            // this.changedItems$ = this.ConfigureState.state$.pluck('edit', 'changes');
            this.modes = [
                {label: 'Basic', state: 'conf.edit.basic'},
                {label: 'Advanced', state: 'conf.edit.advanced'}
            ];
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
            this.$state.go('conf.edit.basic', {clusterID: 'new'});
        }
        onBasicSave({cluster, download}) {
            console.log('basic save', cluster, download);
            this.conf.saveBasic({cluster, download});
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
        confirmExit(itemType, original, changed) {
            return this.conf.confirmExit(itemType, original, changed);
        }
    },
    template: `
        <h1>Clusters</h1>
        <pc-items-table
            column-defs='$ctrl.clustersDefs'
            actions-menu='$ctrl.clustersActions'
            on-selection-change='$ctrl.onSelectionChange($event)'
            items='$ctrl.clusters$|async:this'
        ></pc-items-table>
        <button type='button' ng-click='$ctrl.addNew()'>Add cluster</button>
        <div>
            <a
                ui-sref='{{::mode.state}}'
                ui-sref-active='link-success'
                ng-repeat='mode in ::$ctrl.modes'
            >{{::mode.label}}</a>
        </div>
        <ui-view
            cluster='$ctrl.cluster$|async:this'
            cluster-items='$ctrl.clusterItems$|async:this'
            on-basic-save='$ctrl.onBasicSave($event)'
            on-item-add='$ctrl.onItemAdd($event)'
            on-item-change='$ctrl.onItemChange($event)'
            on-item-remove='$ctrl.onItemRemove($event)'
        ></ui-view>
    `
})
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
        // this.ConfigureState.state$.let(selectEditCluster).take(1).do((cluster) => {
        //     this.ConfigureState.dispatchAction({
        //         type: 'EDIT_CLUSTER',
        //         cluster: {...cluster}
        //     });
        // }).subscribe();
    }
})
.component('confEditBasic', {
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
            <details>
                <summary>clusterItems</summary>
                <pre>{{$ctrl.clusterItems|json}}</pre>
            </details>
            <details>
                <summary>cluster</summary>
                <pre>{{$ctrl.item|json}}</pre>
            </details>
            <button type='submit'>Save basic</button>
        </form>
    `,
    controller: class ConfBasicController {
        $onChanges(changes) {
            if ('cluster' in changes && changes.cluster.currentValue)
                this.item = cloneDeep(changes.cluster.currentValue);
        }
        addCache(cache) {
            this.onItemAdd({$event: {item: cache, type: 'caches'}});
        }
        removeCache(cache) {
            this.onItemRemove({$event: {item: cache, type: 'caches'}});
        }
        changeCache(cache) {
            this.onItemChange({$event: {item: cache, type: 'caches'}});
        }
        save() {
            if (this.form.$invalid) return;
            this.onBasicSave({$event: {download: false, cluster: cloneDeep(this.item)}});
        }
    },
    bindings: {
        cluster: '<',
        clusterItems: '<',
        onClusterChange: '&',
        onItemAdd: '&?',
        onItemChange: '&?',
        onItemRemove: '&?',
        onBasicSave: '&'
    }
})
.component('confEditAdvanced', {
    template: `
        <h1>Advanced</h1>
        <div>
            <a
                ui-sref='{{::tab.state}}'
                ui-sref-active='link-success'
                ng-repeat='tab in ::$ctrl.tabs'
            >{{::tab.label}}</a>
        </div>
        <ui-view
            name='list'
            cluster='$ctrl.cluster'
            short-caches='$ctrl.clusterItems.shortCaches'
            on-item-add='$ctrl.onItemAdd($event)'
            on-item-remove='$ctrl.onItemRemove($event)'
        ></ui-view>
        <ui-view
            name='edit'
            cluster='$ctrl.cluster'
            cluster-items='$ctrl.clusterItems'
            on-item-change='$ctrl.onItemChange($event)'
            on-item-remove='$ctrl.onItemRemove($event)'
        ></ui-view>
    `,
    _old: `
        <div hidden>
            <details>
                <summary>Cluster</summary>
                <form ng-submit='$ctrl.save()'>
                    <input type="text" name='name' ng-model='$ctrl.item.name' required/>
                </form>
            </details>
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

                <a ui-sref='conf.edit.item({itemType: "igfss", itemID: "new"})'>Add IGFS</a>
            </details>
            <button type='submit'>Save</button>
        </div>

        `,
    controller: class ConfAdvancedController {
        $onInit() {
            this.tabs = [
                {state: 'conf.edit.advanced.cluster', label: 'Cluster'},
                {state: 'conf.edit.advanced.caches', label: 'Caches'},
                {state: 'conf.edit.advanced.models', label: 'Models'},
                {state: 'conf.edit.advanced.igfss', label: 'IGFS'}
            ];
        }
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
        onAdvancedSave: '&?'
    }
})
.component('cachesList', {
    bindings: {
        onItemAdd: '&',
        onItemRemove: '&',
        shortCaches: '<'
    },
    template: `
        <pc-list-editable
            ng-model='$ctrl.shortCaches'
            pc-list-editable-one-way
            on-item-remove='$ctrl.onItemRemove({$event: {$event: $event, type: "caches"}})'
        >
            <pc-list-editable-item-view>
                {{ $parent.item.name }}
            </pc-list-editable-item-view>
            <pc-list-editable-item-edit>
                <input type="text" ng-model='$parent.item.name' name='name' required>
            </pc-list-editable-item-edit>
        </pc-list-editable>
    `
})
.component('clusterForm', {
    bindings: {
        isNew: '<',
        originalItem: '<cluster',
        onItemChange: '&?onClusterChange'
    },
    controller: class Controller {
        $onChanges(changes) {
            if ('originalItem' in changes && changes.originalItem.currentValue)
                this.item = cloneDeep(changes.originalItem.currentValue);
        }
        save() {
            if (this.form.$invalid) return;
            this.onItemChange({$event: cloneDeep(this.item)});
        }
        uiCanExit() {
            if (this.pageConf) return this.pageConf.confirmExit('caches', this.originalItem, this.item);
        }
    },
    template: `
        <h1>{{$ctrl.isNew ? 'Create' : 'Edit'}} cluster {{$ctrl.originalItem.name}}</h1>
        <form name='$ctrl.form' ng-submit='$ctrl.save()'>
            <label for="name">Cluster name</label>
            <input type="text" ng-model='$ctrl.item.name' name='name' id='name' required/>
            <button type='submit'>Save cluster</button>
        </form>
    `
})
.component('cacheForm', {
    bindings: {
        isNew: '<',
        originalItem: '<',
        onItemChange: '&?'
    },
    controller: class Controller {
        $onChanges(changes) {
            if ('originalItem' in changes && changes.originalItem.currentValue)
                this.item = cloneDeep(changes.originalItem.currentValue);
        }
        save() {
            if (this.form.$invalid) return;
            this.onItemChange({$event: cloneDeep(this.item)});
        }
        uiCanExit() {
            if (this.pageConf) return this.pageConf.confirmExit('caches', this.originalItem, this.item);
        }
    },
    template: `
        <h1>{{$ctrl.isNew ? 'Create' : 'Edit'}} cache {{$ctrl.item.name}}</h1>
        <form name='$ctrl.form' ng-submit='$ctrl.save()'>
            <label for="name">Cache name</label>
            <input type="text" ng-model='$ctrl.item.name' name='name' id='name' required/>
            <button type='submit'>Save cache</button>
        </form>
    `
})
.component('igfsForm', {
    bindings: {
        isNew: '<',
        originalItem: '<',
        onItemChange: '&?'
    },
    require: {
        pageConf: '^^?pageConf'
    },
    controller: class Controller {
        $onChanges(changes) {
            if ('originalItem' in changes && changes.originalItem.currentValue)
                this.item = cloneDeep(changes.originalItem.currentValue);
        }
        save() {
            if (this.form.$invalid) return;
            this.onItemChange({$event: cloneDeep(this.item)});
        }
        uiCanExit() {
            if (this.pageConf) return this.pageConf.confirmExit('igfss', this.originalItem, this.item);
        }
    },
    template: `
        <h1>{{$ctrl.isNew ? 'Create' : 'Edit'}} IGFS {{$ctrl.igfs.name}}</h1>
        <form name='$ctrl.form' ng-submit='$ctrl.save()'>
            <label for="name">IGFS name</label>
            <input type="text" ng-model='$ctrl.item.name' name='name' id='name' required/>
            <label>ipcEndpointEnabled <input type="checkbox" name="ipcEndpointEnabled" id="ipcEndpointEnabled" ng-model="$ctrl.item.ipcEndpointEnabled"/></label>

            <button type='submit'>Save IGFS</button>
        </form>
    `
});
({config() {}}).config(['$stateProvider', ($stateProvider) => {
    $stateProvider
    .state('conf', {
        url: '/conf',
        component: 'pageConf',
        resolve: {
            shortClusters: ['ConfigResolvers', (ConfigResolvers) => ConfigResolvers.loadShortClusters$().toPromise()]
        }
    })
    .state('conf.edit', {
        url: '/:clusterID',
        resolve: {
            _cluster: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                return $transition$.injector().getAsync('shortClusters').then((shortClusters) => {
                    return ConfigResolvers.loadCluster$($transition$.params().clusterID).toPromise();
                });
            }]
        },
        redirectTo: ($transition$) => {
            const clusters = $transition$.injector().getAsync('shortClusters');
            const cluster = $transition$.injector().getAsync('_cluster');
            return Promise.all([clusters, cluster]).then(([clusters, cluster]) => {
                return (clusters.length > 10 || cluster.caches.length > 5)
                    ? 'conf.edit.advanced'
                    : 'conf.edit.basic';
            });
        }
    })
    .state('conf.edit.basic', {
        url: '/basic',
        resolve: {
            shortCaches: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
                return $transition$.injector().getAsync('_cluster').then((cluster) => {
                    return ConfigResolvers.loadShortItems$(cluster, 'caches').toPromise();
                });
            }]
        },
        views: {
            '$default@^.^': {
                component: 'confEditBasic'
            }
        }
    })
    .state('conf.edit.advanced', {
        url: '/advanced',
        views: {
            '$default@^.^': {
                component: 'confEditAdvanced'
            }
        }
    })
    .state('conf.edit.advanced.cluster', {
        url: '/cluster',
        views: {
            edit: {
                component: 'clusterForm'
            }
        }
    })
    .state('conf.edit.advanced.caches', {
        url: '/caches',
        views: {
            list: {
                component: 'cachesList'
            }
        }
    })
    .state('conf.edit.advanced.caches.edit', {
        url: '/:itemID',
        views: {
            list: {
                component: 'cachesList'
            },
            edit: {
                component: 'cachesForm'
            }
        }
    });
    // .state('conf.edit', {
    //     params: {
    //         mode: {
    //             value: 'advanced'
    //         }
    //     },
    //     resolve: {
    //         cluster: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
    //             return $transition$.injector().getAsync('shortClusters').then((shortClusters) => {
    //                 return ConfigResolvers.loadCluster$($transition$.params().clusterID, shortClusters).toPromise();
    //             });
    //         }],
    //         shortCaches: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
    //             return $transition$.injector().getAsync('cluster').then((cluster) => {
    //                 return ConfigResolvers.loadShortItems$(cluster, 'caches').toPromise();
    //             });
    //         }],
    //         shortIgfss: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
    //             return $transition$.injector().getAsync('cluster').then((cluster) => {
    //                 return ConfigResolvers.loadShortItems$(cluster, 'igfss').toPromise();
    //             });
    //         }]
    //     },
    //     redirectTo: ($transition$) => {
    //         return $transition$.injector().getAsync('cluster').catch(() => {
    //             return 'conf';
    //         });
    //     },
    //     template: `
    //         <conf-basic-form
    //             ng-if='($ctrl.params$|async:this).mode === "basic"'
    //             cluster='($ctrl.cluster$|async:this)'
    //             cluster-items='$ctrl.clusterItems$|async:this'

    //             on-cluster-change='$ctrl.onClusterChange($event)'
    //             on-cache-add='$ctrl.onItemAdd($event)'
    //             on-cache-change='$ctrl.onItemChange($event)'
    //             on-cache-remove='$ctrl.onItemRemove($event)'

    //             on-save='$ctrl.onBasicSave()'
    //         ></conf-basic-form>
    //         <conf-advanced-form
    //             ng-if='($ctrl.params$|async:this).mode === "advanced"'
    //             cluster='($ctrl.cluster$|async:this)'
    //             cluster-items='$ctrl.clusterItems$|async:this'

    //             on-item-change='$ctrl.onItemChange($event)'
    //             on-item-remove='$ctrl.onItemRemove($event)'
    //             on-cluster-change='$ctrl.onClusterChange($event)'

    //             on-save='$ctrl.onAdvancedSave()'
    //         >
    //         </conf-advanced-form>
    //     `
    // })
    // .state('conf.edit.item', {
    //     url: '/:itemType/:itemID',
    //     resolve: {
    //         originalItem: ['ConfigResolvers', '$transition$', (ConfigResolvers, $transition$) => {
    //             const {itemType, itemID} = $transition$.params();
    //             return ConfigResolvers.resolveItem$(itemType, itemID).toPromise();
    //         }]
    //         // isNew: ['$transition$', ($transition$) => Promise.resolve($transition$.params().itemID === 'new')]
    //     },
    //     views: {
    //         caches: {
    //             template: `
    //                 <cache-form
    //                     cache='$resolve.item'
    //                     is-new='$resolve.itemID === "new"'
    //                     on-cache-change='$ctrl.changeItem($event, "caches")'
    //                 ></cache-form>
    //                 <pre>{{$resolve.item|json}}</pre>
    //             `
    //         },
    //         igfss: {
    //             component: 'igfsForm'
    //         }
    //     }
    // });
}]);
