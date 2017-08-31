import cloneDeep from 'lodash/cloneDeep';
import matches from 'lodash/fp/matches';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {empty} from 'rxjs/observable/empty';

export default class PageConfigureAdvancedCachesService {
    static $inject = ['ConfigureState', '$state', 'Caches', 'Clusters', 'IgniteConfirm'];

    constructor(ConfigureState, $state, Caches, Clusters, IgniteConfirm) {
        Object.assign(this, {ConfigureState, $state, Caches, Clusters, IgniteConfirm});
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

    clone(items = []) {

    }

    remove(items = [], cluster) {
        return this.ConfigureState.state$
            .pluck('shortCaches')
            .take(1)
            .map((shortCaches) => items.map((id) => shortCaches.get(id)))
            .switchMap((shortCaches) => {
                const names = `<p><ul>${shortCaches.map((c) => `<li>${c.name}</li>`).join('')}</ul></p>`;
                return this.IgniteConfirm.confirm(`Are you sure want to remove these caches? ${names}`);
            })
            .catch(() => {})
            .do(() => {
                this.ConfigureState.dispatchAction({
                    type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
                    cluster,
                    caches: {
                        ids: cluster.caches.filter((id) => !items.includes(id)),
                        changedItems: []
                    }
                });
            })
            .subscribe();
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

        const modelsMenu = shortItems('shortModels').map(({shortModels}) => ({
            modelsMenu: shortModels.map((m) => ({value: m._id, label: m.valueType}))
        }));

        return combineLatest(
            cluster,
            cache,
            shortClusters,
            shortCaches,
            modelsMenu,
            (...values) => Object.assign({}, ...values)
        );
    }
}
