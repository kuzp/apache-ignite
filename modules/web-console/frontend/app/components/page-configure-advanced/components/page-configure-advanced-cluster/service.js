import cloneDeep from 'lodash/cloneDeep';
import matches from 'lodash/fp/matches';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {fromPromise} from 'rxjs/observable/fromPromise';
import {empty} from 'rxjs/observable/empty';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/mapTo';
import {uniqueName} from 'app/utils/uniqueName';

const confirmCancelEditAction = {type: 'CONFIRM_CANCEL_EDIT', for: 'cluster'};
const cancelEditAction = {type: 'CANCEL_EDIT', for: 'cluster'};

export default class PageConfigureAdvancedClusterService {
    static $inject = ['ConfigureState', 'PageConfigureBasic', 'PageConfigureAdvanced', 'Clusters', '$state', 'IgniteConfirm'];

    constructor(ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters, $state, IgniteConfirm) {
        Object.assign(this, {ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters, $state, IgniteConfirm});

        this.cancelEditEffect$ = this.ConfigureState.actions$
            .filter(matches(confirmCancelEditAction))
            .switchMap((a) => {
                return fromPromise(
                    this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current cluster?')
                ).mapTo(cancelEditAction).catch(() => empty());
            });

        this.cancelEditEffect$.do((a) => this.ConfigureState.actions$.next(a)).subscribe();
    }

    save(cluster) {
        console.debug(cluster);
        this.ConfigureState.dispatchAction({
            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
            cluster,
            caches: {ids: cluster.caches, changedItems: []}
        });
    }

    cancelEdit() {
        this.ConfigureState.dispatchAction(confirmCancelEditAction);
    }

    getObservable() {
        const {state$, actions$} = this.ConfigureState;

        const cluster = state$
            .pluck('clusters')
            .map((clusters) => {
                return clusters.has(this.$state.params.clusterID)
                    ? clusters.get(this.$state.params.clusterID)
                    : {
                        ...this.Clusters.getBlankCluster(),
                        name: uniqueName('New cluster', [...clusters.values()])
                    };
            })
            .distinctUntilChanged()
            .switchMap((v) => actions$.filter(matches(cancelEditAction)).mapTo(v).startWith(v))
            .map((cluster) => {
                return {
                    originalCluster: cluster,
                    clonedCluster: Object.assign(this.Clusters.getBlankCluster(), cloneDeep(cluster))
                };
            });

        const shortItems = (type) => state$
            .pluck(type)
            .distinctUntilChanged()
            .map((items) => ({
                [type]: [...items.values()]
            }));

        const shortClusters = shortItems('shortClusters');
        const shortCaches = shortItems('shortCaches');
        const cachesMenu = shortCaches.map(({shortCaches}) => {
            const cachesMenu = shortCaches.map((cache) => ({label: cache.name, value: cache._id}));
            const emptyCachesMenu = cachesMenu.concat({label: 'Not set'});
            return {cachesMenu, emptyCachesMenu};
        });

        return combineLatest(
            cluster,
            shortClusters,
            shortCaches,
            cachesMenu,
            (...values) => Object.assign({}, ...values)
        );
    }
}
