import 'rxjs/add/operator/pluck';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/let';
import {selectShortClustersValue} from 'app/components/page-configure/reducer';

export default class PageConfigureOverviewService {
    static $inject = ['ConfigureState', '$state', 'PageConfigure', 'IgniteConfirm'];

    constructor(ConfigureState, $state, PageConfigure, IgniteConfirm) {
        Object.assign(this, {ConfigureState, $state, PageConfigure, IgniteConfirm});
        this.shortClusters$ = this.ConfigureState.state$.let(selectShortClustersValue);
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
