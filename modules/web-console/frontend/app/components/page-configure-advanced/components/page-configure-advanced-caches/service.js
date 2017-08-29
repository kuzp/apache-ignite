import cloneDeep from 'lodash/cloneDeep';
import matches from 'lodash/fp/matches';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {empty} from 'rxjs/observable/empty';

export default class PageConfigureAdvancedCachesService {
    static $inject = ['ConfigureState', '$state', 'Caches', 'Clusters'];

    constructor(ConfigureState, $state, Caches, Clusters) {
        Object.assign(this, {ConfigureState, $state, Caches, Clusters});
    }

    save(item, cluster) {
        this.ConfigureState.dispatchAction({
            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
            cluster,
            caches: {
                ids: [...new Set(cluster.caches.concat(item._id)).values()],
                changedItems: [item]
            }
        });
    }

    getObservable() {
        const {state$, actions$} = this.ConfigureState;

        const cluster = state$
            .pluck('clusters')
            .map((clusters) => clusters.get(this.$state.params.clusterID))
            .distinctUntilChanged()
            // .switchMap((v) => actions$.filter(matches(cancelEditAction)).mapTo(v).startWith(v))
            .map((cluster) => {
                return {
                    originalCluster: cluster,
                    clonedCluster: Object.assign(this.Clusters.getBlankCluster(), cloneDeep(cluster))
                };
            });

        const cache = state$
            .pluck('clusterConfiguration', 'originalCache')
            .distinctUntilChanged()
            .map((cache) => {
                return {
                    originalCache: cache,
                    clonedCache: Object.assign(this.Caches.getBlankCache(), cloneDeep(cache))
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

        return combineLatest(
            cluster,
            cache,
            shortClusters,
            shortCaches,
            (...values) => Object.assign({}, ...values)
        );
    }
}
