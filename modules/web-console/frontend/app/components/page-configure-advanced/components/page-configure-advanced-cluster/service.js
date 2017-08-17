import cloneDeep from 'lodash/cloneDeep';
import {combineLatest} from 'rxjs/observable/combineLatest';

export default class PageConfigureAdvancedClusterService {
    static $inject = ['ConfigureState', 'PageConfigureBasic', 'PageConfigureAdvanced', 'Clusters'];

    constructor(ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters) {
        Object.assign(this, {ConfigureState, PageConfigureBasic, PageConfigureAdvanced, Clusters});
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
            .pluck('clusterConfiguration', 'originalCluster')
            .distinctUntilChanged()
            .map((cluster) => {
                return {
                    originalCluster: cluster,
                    clonedCluster: Object.assign(this.Clusters.getBlankCluster(), cloneDeep(cluster))
                };
            });

        const shortClusters = state$
            .pluck('shortClusters')
            .distinctUntilChanged()
            .map((clusters) => ({
                shortClusters: [...clusters.values()]
            }));

        return combineLatest(cluster, shortClusters, (...values) => Object.assign({}, ...values));
    }
}
