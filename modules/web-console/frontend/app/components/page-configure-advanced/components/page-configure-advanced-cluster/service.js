import cloneDeep from 'lodash/cloneDeep';
import {combineLatest} from 'rxjs/observable/combineLatest';

export default class PageConfigureAdvancedClusterService {
    static $inject = ['ConfigureState', 'PageConfigureBasic', 'PageConfigureAdvanced', 'Clusters', '$state'];

    constructor(ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters, $state) {
        Object.assign(this, {ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters, $state});
    }

    save(cluster) {
        console.debug(cluster);
        this.ConfigureState.dispatchAction({
            type: 'ADVANCED_SAVE_COMPLETE_CONFIGURATION',
            cluster,
            caches: {ids: cluster.caches, changedItems: []}
        });
    }

    getObservable() {
        const {state$} = this.ConfigureState;

        const cluster = state$
            .pluck('clusters')
            .map((clusters) => clusters.get(this.$state.params.clusterID))
            // .pluck('clusterConfiguration', 'originalCluster')
            .distinctUntilChanged()
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
