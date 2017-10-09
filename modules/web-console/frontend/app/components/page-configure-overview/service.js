export default class PageConfigureOverviewService {
    static $inject = ['$state', 'PageConfigure', 'IgniteConfirm'];

    constructor($state, PageConfigure, IgniteConfirm) {
        Object.assign(this, {$state, PageConfigure, IgniteConfirm});
    }

    _allNames(items) {
        return items.map((i) => i.name).join(', ');
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
