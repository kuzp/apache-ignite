import {Subject} from 'rxjs/Subject';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {empty} from 'rxjs/observable/empty';
import {never} from 'rxjs/observable/never';
import {merge} from 'rxjs/observable/merge';
import {of} from 'rxjs/observable/of';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/publishReplay';
import 'rxjs/add/operator/toPromise';
import {uniqueName} from 'app/utils/uniqueName';
import naturalCompare from 'natural-compare-lite';
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
        static $inject = ['$state', 'conf', 'Caches', 'PageConfigureOverviewService'];
        constructor($state, conf, Caches, overview) {
            Object.assign(this, {$state, conf, Caches, overview});
        }
        $onInit() {
            this.cluster$ = this.conf.cluster$;
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
            }];
            this.itemActions$ = new Subject();
            this.changedItems$ = this.cluster$.switchMap((cluster) => {
                if (!cluster) return empty();
                return this.itemActions$
                .startWith({caches: {ids: cluster.caches, changedItems: []}})
                .scan((state, action) => {
                    switch (action.type) {
                        case 'ADD': {
                            const item = this.Caches.getBlankCache();
                            return {
                                ...state,
                                [action.itemType]: {
                                    ids: state[action.itemType].ids.concat(item._id),
                                    changedItems: state[action.itemType].changedItems.concat({...item, name: item._id})
                                }
                            };
                        }
                        case 'CHANGE': {
                            return {
                                ...state,
                                [action.itemType]: {
                                    ids: state[action.itemType].ids.filter((_id) => _id !== action.item._id).concat(action.item._id),
                                    changedItems: state[action.itemType].changedItems.filter(({_id}) => _id !== action.item._id).concat(action.item)
                                }
                            };
                        }
                        case 'REMOVE': {
                            return {
                                ...state,
                                [action.itemType]: {
                                    ids: state[action.itemType].ids.filter((_id) => _id !== action.item._id),
                                    changedItems: state[action.itemType].changedItems.filter(({_id}) => _id !== action.item._id)
                                }
                            };
                        }
                        default:
                            return state;
                    }
                });
            }).publishReplay(1).refCount();
            this.clusterItems$ = combineLatest(this.changedItems$, this.conf.clusterItems$, (changed, old) => {
                return {
                    caches: changed.caches.ids.map((id) => {
                        const sameID = ({_id}) => _id === id;
                        return changed.caches.changedItems.find(sameID) || old.caches.find(sameID);
                    }).sort((a, b) => naturalCompare(a.name, b.name))
                };
            })
            // .filter((v) => v.caches.every((v) => v))
            .do((v) => console.log('cluster items', v));
        }
        onSelectionChange(selectedClusters) {
            return this.clustersActions = this.makeClusterActions(selectedClusters);
        }
        makeClusterActions(selectedClusters) {
            return [
                {
                    action: 'Edit',
                    click: () => this.$state.go('conf.edit', {mode: 'basic', clusterID: selectedClusters[0]._id}),
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
            console.log('basic save', cluster);
            this.changedItems$.take(1).do((changedItems) => {
                this.conf.saveBasic(cluster, changedItems);
            }).subscribe();
        }
        onItemChange({type, item}) {
            this.itemActions$.next({
                type: 'CHANGE',
                itemType: type,
                item
            });
        }
        onItemAdd({type}) {
            const item = this.Caches.getBlankCache();
            this.itemActions$.next({
                type: 'ADD',
                itemType: type,
                item: {...item, name: item._id}
            });
        }
        onItemRemove({type, item}) {
            this.itemActions$.next({
                type: 'REMOVE',
                itemType: type,
                item
            });
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
    static $inject = ['ConfigureState', '$uiRouter', 'Clusters', '$state', 'PageConfigureBasic'];
    constructor(ConfigureState, {globals: {params$}}, Clusters, $state, PageConfigureBasic) {
        Object.assign(this, {ConfigureState});
        const {state$, actions$} = ConfigureState;

        const basicRedirects$ = actions$
            .filter((a) => a.type === 'BASIC_SAVE_CLUSTER_AND_CACHES_OK')
            .do((a) => $state.go('conf.edit', {mode: 'basic', clusterID: a.cluster._id}, {location: 'replace'}));

        merge(basicRedirects$, params$).subscribe();

        const shortClusters$ = state$
            .pluck('shortClusters')
            .distinctUntilChanged()
            .filter((v) => v)
            .pluck('value')
            .map((v) => [...v.values()])
            .do((v) => console.log('shortClusters', v))
            .publishReplay(1).refCount();

        const clusters$ = state$.pluck('clusters')
            .distinctUntilChanged();

        const clusterID$ = params$.pluck('clusterID')
            .filter((v) => v !== 'new')
            .distinctUntilChanged();

        const oldCluster$ = combineLatest(clusterID$, clusters$)
            .filter(([, v]) => v)
            .map(([id, clusters]) => clusters.get(id))
            .distinctUntilChanged();

        const newCluster$ = params$.pluck('clusterID')
            .distinctUntilChanged()
            .filter((v) => v === 'new')
            .map(() => Object.assign(Clusters.getBlankCluster(), {name: 'New cluster'}))
            .withLatestFrom(shortClusters$, (cluster, clusters) => {
                return Object.assign(cluster, {
                    name: uniqueName(cluster.name, clusters.filter(({_id}) => _id !== cluster._id))
                });
            })
            .do((v) => console.log('new'));

        const cluster$ = merge(oldCluster$, newCluster$)
            .do((v) => {
                console.log('cluster', v);
            })
            .publishReplay(1).refCount();

        const clusterShortCaches$ = combineLatest(
                state$.pluck('shortCaches').distinctUntilChanged(),
                cluster$.filter((v) => v)
        )
            .switchMap(([shortCaches, cluster]) => {
                if (!cluster.caches.length) return of([]);
                if (shortCaches && cluster.caches.every((_id) => shortCaches.has(_id)))
                    return of(cluster.caches.map((_id) => shortCaches.get(_id)));

                Clusters.getClusterCaches(cluster._id).then(({data: items}) => {
                    ConfigureState.dispatchAction({
                        type: shortCachesActionTypes.UPSERT,
                        items
                    });
                });
                return empty();
            });

        const clusterItems$ = combineLatest(clusterShortCaches$, ((caches) => {
            return {caches};
        })).share();

        Object.assign(this, {shortClusters$, cluster$, clusterItems$});
    }
    saveBasic(cluster, changedItems) {
        this.ConfigureState.dispatchAction({
            type: 'BASIC_SAVE_CLUSTER_AND_CACHES',
            cluster: {...cluster, caches: changedItems.caches.ids},
            caches: changedItems.caches
        });
    }
})
.component('confBasicForm', {
    template: `
        <form ng-submit='$ctrl.onSave({$event: $ctrl.item})'>
            <input type="text" name='name' ng-model='$ctrl.item.name' required/>
            <pc-list-editable
                ng-model='$ctrl.clusterItems.caches'
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
        $onChanges(changes) {
            if ('cluster' in changes && changes.cluster.currentValue)
                this.item = angular.copy(changes.cluster.currentValue);
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
    },
    bindings: {
        cluster: '<?',
        clusterItems: '<?',
        onCacheAdd: '&?',
        onCacheChange: '&?',
        onCacheRemove: '&?',
        onSave: '&?',
        onSaveAndDownload: '&?'
    }
})
.config(['$stateProvider', ($stateProvider) => {
    $stateProvider
    .state('conf', {
        url: '/conf',
        component: 'pageConf',
        resolve: {
            shortClusters: ['ConfigureState', 'Clusters', (ConfigureState, Clusters) => {
                return Clusters.getClustersOverview().then(({data}) => ConfigureState.dispatchAction({
                    type: shortClustersActionTypes.UPSERT,
                    items: data
                }));
            }]
        }
    })
    // .state('conf.edit', {
    //     url: '/{clusterID:string}/{mode:string}',
    //     redirectTo: ($transition$) => {
    //         const clusters = $transition$.injector().getAsync('clustersTable');
    //         const cluster = $transition$.injector().getAsync('cluster');
    //         return Promise.all([clusters, cluster]).then(([clusters, cluster]) => {
    //             return (clusters.length > 10 || cluster.caches.length > 5)
    //                 ? 'base.configuration.tabs.advanced'
    //                 : 'base.configuration.tabs.basic';
    //         });
    //     },       
    // })
    .state('conf.edit', {
        url: '/{clusterID:string}/{mode:string}',
        resolve: {
            cluster: ['ConfigureState', 'Clusters', '$transition$', (ConfigureState, Clusters, $transition$) => {
                const clusterID = $transition$.params().clusterID;
                return ConfigureState.state$
                    .pluck('clusters')
                    .take(1)
                    .map((c) => c && c.get(clusterID))
                    .switchMap((c) => c
                        ? of(c)
                        : Clusters
                            .getCluster(clusterID)
                            .then(({data}) => ConfigureState.dispatchAction({
                                type: clustersActionTypes.UPSERT,
                                items: [data]
                            }))
                    )
                    .toPromise();
            }]
        },
        template: `
            <conf-basic-form
                ng-if='$resolve.$stateParams.mode === "basic"'
                cluster='($ctrl.cluster$|async:this)'
                cluster-items='$ctrl.clusterItems$|async:this'
                on-cache-add='$ctrl.onItemAdd($event)'
                on-cache-change='$ctrl.onItemChange($event)'
                on-cache-remove='$ctrl.onItemRemove($event)'
                on-save='$ctrl.onBasicSave($event)'
            ></conf-basic-form>
            <conf-advanced-form
                ng-if='$resolve.$stateParams.mode === "basic"'
                cluster='($ctrl.cluster$|async:this)'
                cluster-items='$ctrl.clusterItems$|async:this'

                on-cache-add='$ctrl.onItemAdd($event)'
                on-cache-change='$ctrl.onItemChange($event)'
                on-cache-remove='$ctrl.onItemRemove($event)'

                on-save='$ctrl.onBasicSave($event)'
            >
            </conf-advanced-form>
        `
    });
}]);
