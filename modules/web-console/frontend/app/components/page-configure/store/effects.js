import 'rxjs/add/operator/ignoreElements';
import {merge} from 'rxjs/observable/merge';
import {empty} from 'rxjs/observable/empty';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';
import {uniqueName} from 'app/utils/uniqueName';
import pick from 'lodash/fp/pick';

import {
    clustersActionTypes,
    cachesActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    shortModelsActionTypes,
    shortIGFSsActionTypes,
    modelsActionTypes,
    igfssActionTypes
} from './../reducer';

const ofType = (type) => (s) => s.filter((a) => a.type === type);

export default class ConfigEffects {
    static $inject = ['ConfigureState', 'Caches', 'IGFSs', 'Models', 'ConfigSelectors', 'Clusters', '$state', 'IgniteMessages'];
    constructor(ConfigureState, Caches, IGFSs, Models, ConfigSelectors, Clusters, $state, IgniteMessages) {
        Object.assign(this, {ConfigureState, Caches, IGFSs, Models, ConfigSelectors, Clusters, $state, IgniteMessages});

        this.loadConfigurationEffect$ = this.ConfigureState.actions$
            .let(ofType('LOAD_COMPLETE_CONFIGURATION'))
            .exhaustMap((action) => {
                // return this.ConfigureState.state$
                // .let(this.ConfigSelectors.selectCompleteClusterConfiguration({clusterID: action.clusterID, isDemo: action.isDemo}))
                // .take(1)
                // .switchMap((configuration) => {
                // if (configuration.__isComplete) return of({type: 'LOAD_COMPLETE_CONFIGURATION_OK', data: configuration});
                return fromPromise(this.Clusters.getConfiguration(action.clusterID))
                    .switchMap(({data}) => of(
                        {type: 'COMPLETE_CONFIGURATION', configuration: data},
                        {type: 'LOAD_COMPLETE_CONFIGURATION_OK', data}
                    ))
                    .catch((error) => ({type: 'LOAD_COMPLETE_CONFIGURATION_ERR', error, action}));
                // });
            });

        this.storlConfiguratiosEffect$ = this.ConfigureState.actions$
            .let(ofType('COMPLETE_CONFIGURATION'))
            .exhaustMap(({configuration: data}) => of(...[
                data.cluster && {type: clustersActionTypes.UPSERT, items: [data.cluster]},
                data.caches && data.caches.length && {type: cachesActionTypes.UPSERT, items: data.caches},
                data.domains && data.domains.length && {type: modelsActionTypes.UPSERT, items: data.domains},
                data.igfss && data.igfss.length && {type: igfssActionTypes.UPSERT, items: data.igfss}
            ].filter((v) => v)));

        this.saveCompleteConfigurationEffect$ = this.ConfigureState.actions$
            .let(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION'))
            .withLatestFrom(this.ConfigureState.state$)
            .switchMap(([action, state]) => {
                const actions = [
                    {
                        type: modelsActionTypes.UPSERT,
                        items: action.changedItems.models
                    },
                    {
                        type: shortModelsActionTypes.UPSERT,
                        items: action.changedItems.models
                    },
                    {
                        type: igfssActionTypes.UPSERT,
                        items: action.changedItems.igfss
                    },
                    {
                        type: shortIGFSsActionTypes.UPSERT,
                        items: action.changedItems.igfss
                    },
                    {
                        type: cachesActionTypes.UPSERT,
                        items: action.changedItems.caches
                    },
                    {
                        type: shortCachesActionTypes.UPSERT,
                        items: action.changedItems.caches.map(Caches.toShortCache)
                    },
                    {
                        type: clustersActionTypes.UPSERT,
                        items: [action.changedItems.cluster]
                    },
                    {
                        type: shortClustersActionTypes.UPSERT,
                        items: [Clusters.toShortCluster(action.changedItems.cluster)]
                    }
                ];

                return of(...actions)
                .merge(
                    fromPromise(Clusters.saveAdvanced(action.changedItems))
                    .switchMap((res) => {
                        return of(
                            {type: 'EDIT_CLUSTER', cluster: action.changedItems.cluster},
                            // {type: 'RESET_EDIT_CHANGES'},
                            {type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK', changedItems: action.changedItems}
                        );
                    })
                    .catch((res) => {
                        return of({
                            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR',
                            changedItems: action.changedItems,
                            action,
                            error: {
                                title: `Failed to save cluster ${action.changedItems.cluster.name}`,
                                message: res.data
                            }
                        }, {
                            type: 'UNDO_ACTIONS',
                            actions: action.prevActions.concat(actions)
                        });
                    })
                );
            });

        this.saveCompleteConfigurationErrEffect$ = this.ConfigureState.actions$
            .let(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_ERR'))
            .do((action) => this.IgniteMessages.showError(action.error.title, action.error.message))
            .ignoreElements();

        this.loadUserClustersEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_USER_CLUSTERS')
            .exhaustMap((a) => {
                return fromPromise(this.Clusters.getClustersOverview())
                    .switchMap(({data}) => of(
                        {type: shortClustersActionTypes.SET, items: data},
                        {type: `${a.type}_OK`}
                    ))
                    .catch((error) => of({type: `${a.type}_ERR`, error, action: a}));
            });

        this.loadCluster$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_CLUSTER')
            .exhaustMap((a) => {
                return fromPromise(this.Clusters.getCluster(a.clusterID))
                .map(({data}) => of({type: 'LOAD_CLUSTER_OK', cluster: data}))
                .catch((error) => of({type: 'LOAD_CLUSTER_ERR', error}));
            });

        this.storeLoadedCluster$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_CLUSTER_OK')
            .map((a) => ({type: clustersActionTypes.UPSERT, items: [a.cluster]}));

        this.loadAndEditClusterEffect$ = ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_AND_EDIT_CLUSTER')
            .exhaustMap((a) => {
                if (a.clusterID === 'new') {
                    return of(
                        {type: 'EDIT_CLUSTER', cluster: this.Clusters.getBlankCluster()},
                        {type: 'LOAD_AND_EDIT_CLUSTER_OK'}
                    );
                }
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectCluster(a.clusterID)).take(1)
                    .switchMap((cluster) => {
                        if (cluster) {
                            return of(
                                {type: 'EDIT_CLUSTER', cluster},
                                {type: 'LOAD_AND_EDIT_CLUSTER_OK'}
                            );
                        }
                        return fromPromise(this.Clusters.getCluster(a.clusterID))
                        .switchMap(({data}) => of(
                            {type: clustersActionTypes.UPSERT, items: [data]},
                            {type: 'EDIT_CLUSTER', cluster: data},
                            {type: 'LOAD_AND_EDIT_CLUSTER_OK'}
                        ))
                        .catch((error) => of({type: 'EDIT_CLUSTER_ERR', error}));
                    });
            });

        this.loadCacheEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_CACHE')
            .exhaustMap((a) => {
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectCache(a.cacheID)).take(1)
                    .switchMap((cache) => {
                        if (cache) return of({type: `${a.type}_OK`, cache});
                        return fromPromise(this.Caches.getCache(a.cacheID))
                        .switchMap(({data}) => of(
                            {type: 'CACHE', cache: data},
                            {type: `${a.type}_OK`, cache: data}
                        ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error}));
            });

        this.storeCacheEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'CACHE')
            .map((a) => ({type: cachesActionTypes.UPSERT, items: [a.cache]}));

        this.loadShortCachesEffect$ = ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_SHORT_CACHES')
            .exhaustMap((a) => {
                if (!(a.ids || []).length) return of({type: `${a.type}_OK`});
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectShortCaches()).take(1)
                    .switchMap((items) => {
                        if (!items.pristine && a.ids && a.ids.every((_id) => items.value.has(_id)))
                            return of({type: `${a.type}_OK`});

                        return fromPromise(this.Clusters.getClusterCaches(a.clusterID))
                            .switchMap(({data}) => of(
                                {type: shortCachesActionTypes.UPSERT, items: data},
                                {type: `${a.type}_OK`}
                            ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error, action: a}));
            });

        this.loadIGFSEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_IGFS')
            .exhaustMap((a) => {
                if (a.igfsID === 'new') return of({type: `${a.type}_OK`});
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectIGFS(a)).take(1)
                    .switchMap((cache) => {
                        if (cache) return of({type: `${a.type}_OK`});
                        return fromPromise(this.IGFSs.getIGFS(a.igfsID))
                        .switchMap(({data}) => of(
                            {type: igfssActionTypes.UPSERT, items: [data]},
                            {type: `${a.type}_OK`}
                        ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error}));
            });

        this.loadShortIgfssEffect$ = ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_SHORT_IGFSS')
            .exhaustMap((a) => {
                if (!(a.ids || []).length) {
                    return of(
                        {type: shortIGFSsActionTypes.UPSERT, items: []},
                        {type: `${a.type}_OK`}
                    );
                }
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectShortIGFSs()).take(1)
                    .switchMap((items) => {
                        if (!items.pristine && a.ids && a.ids.every((_id) => items.value.has(_id)))
                            return of({type: `${a.type}_OK`});

                        return fromPromise(this.Clusters.getClusterIGFSs(a.clusterID))
                            .switchMap(({data}) => of(
                                {type: shortIGFSsActionTypes.UPSERT, items: data},
                                {type: `${a.type}_OK`}
                            ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error, action: a}));
            });

        this.loadModelEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_MODEL')
            .exhaustMap((a) => {
                if (a.modelID === 'new') return of({type: `${a.type}_OK`});
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectModel(a)).take(1)
                    .switchMap((cache) => {
                        if (cache) return of({type: `${a.type}_OK`});
                        return fromPromise(this.Models.getModel(a.modelID))
                        .switchMap(({data}) => of(
                            {type: modelsActionTypes.UPSERT, items: [data]},
                            {type: `${a.type}_OK`}
                        ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error}));
            });

        this.loadShortModelsEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_SHORT_MODELS')
            .exhaustMap((a) => {
                if (!(a.ids || []).length) {
                    return of(
                        {type: shortModelsActionTypes.UPSERT, items: []},
                        {type: `${a.type}_OK`}
                    );
                }
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectShortModels()).take(1)
                    .switchMap((items) => {
                        if (!items.pristine && a.ids && a.ids.every((_id) => items.value.has(_id)))
                            return of({type: `${a.type}_OK`});

                        return fromPromise(this.Clusters.getClusterModels(a.clusterID))
                            .switchMap(({data}) => of(
                                {type: shortModelsActionTypes.UPSERT, items: data},
                                {type: `${a.type}_OK`}
                            ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error, action: a}));
            });

        this.advancedSaveRedirectEffect$ = this.ConfigureState.actions$
            .let(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION_OK'))
            .withLatestFrom(this.ConfigureState.actions$.let(ofType('ADVANCED_SAVE_COMPLETE_CONFIGURATION')))
            .pluck('1', 'changedItems')
            .map((req) => {
                const firstChangedItem = Object.keys(req).filter((k) => k !== 'cluster')
                    .map((k) => Array.isArray(req[k]) ? [k, req[k][0]] : [k, req[k]])
                    .filter((v) => v[1])
                    .pop();
                return firstChangedItem || ['cluster', req.cluster];
            })
            .do(([type, value]) => {
                switch (type) {
                    case 'models': {
                        const state = 'base.configuration.edit.advanced.models.model';
                        this.IgniteMessages.showInfo(`Model ${value.name} saved`);
                        if (
                            this.$state.is(state) && this.$state.params.modelID !== value._id
                        ) return this.$state.go(state, {modelID: value._id}, {location: 'replace'});
                        break;
                    }
                    case 'caches': {
                        const state = 'base.configuration.edit.advanced.caches.cache';
                        this.IgniteMessages.showInfo(`Cache ${value.name} saved`);
                        if (
                            this.$state.is(state) && this.$state.params.cacheID !== value._id
                        ) return this.$state.go(state, {cacheID: value._id}, {location: 'replace'});
                        break;
                    }
                    case 'igfss': {
                        const state = 'base.configuration.edit.advanced.igfs.igfs';
                        this.IgniteMessages.showInfo(`IGFS ${value.name} saved`);
                        if (
                            this.$state.is(state) && this.$state.params.igfsID !== value._id
                        ) return this.$state.go(state, {igfsID: value._id}, {location: 'replace'});
                        break;
                    }
                    case 'cluster': {
                        const state = 'base.configuration.edit.advanced.cluster';
                        this.IgniteMessages.showInfo(`Cluster ${value.name} saved`);
                        if (
                            this.$state.is(state) && this.$state.params.clusterID !== value._id
                        ) return this.$state.go(state, {clusterID: value._id}, {location: 'replace'});
                        break;
                    }
                    default: break;
                }
            })
            .debug('adv save change')
            .switchMap(() => empty());
    }

    etp = (action, params) => {
        const ok = `${action}_OK`;
        const err = `${action}_ERR`;
        setTimeout(() => this.ConfigureState.dispatchAction({type: action, ...params}));
        return this.ConfigureState.actions$
            .filter((a) => a.type === ok || a.type === err)
            .take(1)
            .map((a) => {
                if (a.type === err)
                    throw a;
                else
                    return a;
            })
            .toPromise();
    };

    connect() {
        return merge(
            ...Object.keys(this).filter((k) => k.endsWith('Effect$')).map((k) => this[k])
        ).do((a) => this.ConfigureState.dispatchAction(a)).subscribe();
    }
}
