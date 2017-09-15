import {merge} from 'rxjs/observable/merge';
import {empty} from 'rxjs/observable/empty';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';
import {uniqueName} from 'app/utils/uniqueName';

import {
    clustersActionTypes,
    cachesActionTypes,
    shortClustersActionTypes,
    shortCachesActionTypes,
    shortModelsActionTypes
} from './../reducer';

const ofType = (type) => (s) => s.filter((a) => a.type === type);

export default class ConfigEffects {
    static $inject = ['ConfigureState', 'Caches', 'ConfigSelectors', 'Clusters', '$state', 'IgniteMessages'];
    constructor(ConfigureState, Caches, ConfigSelectors, Clusters, $state, IgniteMessages) {
        Object.assign(this, {ConfigureState, Caches, ConfigSelectors, Clusters, $state, IgniteMessages});

        this.loadUserClustersEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_USER_CLUSTERS')
            .exhaustMap((a) => {
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectShortClusters()).take(1)
                .switchMap((shortClusters) => {
                    if (shortClusters.pristine) {
                        return fromPromise(this.Clusters.getClustersOverview())
                        .switchMap(({data}) => of(
                            {type: shortClustersActionTypes.UPSERT, items: data},
                            {type: `${a.type}_OK`}
                        ));
                    } return of({type: `${a.type}_OK`});
                })
                .catch((error) => of({type: `${a.type}_ERR`, error, action: a}));
            });

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
                if (a.cacheID === 'new') return of({type: `${a.type}_OK`});
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectCache(a)).take(1)
                    .switchMap((cache) => {
                        if (cache) return of({type: `${a.type}_OK`});
                        return fromPromise(this.Caches.getCache(a.cacheID))
                        .switchMap(({data}) => of(
                            {type: cachesActionTypes.UPSERT, items: [data]},
                            {type: `${a.type}_OK`}
                        ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error}));
            });

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

        this.loadShortModelsEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_SHORT_MODELS')
            .exhaustMap((a) => {
                if (!(a.ids || []).length) return of({type: `${a.type}_OK`});
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
                    case 'caches': {
                        const state = 'base.configuration.edit.advanced.caches.cache';
                        this.IgniteMessages.showInfo(`Cache ${value.name} saved`);
                        if (
                            this.$state.is(state) && this.$state.params.cacheID !== value._id
                        ) return this.$state.go(state, {cacheID: value._id}, {location: 'replace'});
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
