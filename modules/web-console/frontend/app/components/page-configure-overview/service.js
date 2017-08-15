import 'rxjs/add/operator/pluck';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/distinctUntilChanged';

export default class PageConfigureOverviewService {
    static $inject = ['ConfigureState', '$state', 'PageConfigure', 'IgniteConfirm', 'Clusters'];

    constructor(ConfigureState, $state, PageConfigure, IgniteConfirm, Clusters) {
        Object.assign(this, {ConfigureState, $state, PageConfigure, IgniteConfirm});
        this.clusterDiscoveries = Clusters.discoveries;
    }

    _allNames(items) {
        return items.map((i) => i.name).join(', ');
    }

    getObservable() {
        return this.ConfigureState.state$
        .pluck('shortClusters')
        .distinctUntilChanged()
        .map((shortClustersMap) => ({
            clusters: [...shortClustersMap.values()]
        }));
    }

    editCluster(cluster) {
        return this.$state.go('^.tabs', {clusterID: cluster._id});
    }

    cloneClusters(clusters) {
        return this.PageConfigure.cloneClusters(clusters);
    }

    removeClusters(clusters) {
        return this.IgniteConfirm
            .confirm(`Are you sure want to remove cluster(s): ${this._allNames(clusters)}?`)
            .then(() => this.PageConfigure.removeClustersLocalRemote(clusters));
    }
}
