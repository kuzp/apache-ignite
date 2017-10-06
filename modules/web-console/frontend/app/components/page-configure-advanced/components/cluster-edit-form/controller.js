import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class ClusterEditFormController {
    static $inject = ['IgniteLegacyUtils', 'IgniteEventGroups', 'IgniteConfirm', 'IgniteVersion', '$scope', 'Clusters', 'IgniteFormUtils'];
    constructor(IgniteLegacyUtils, IgniteEventGroups, IgniteConfirm, IgniteVersion, $scope, Clusters, IgniteFormUtils) {
        Object.assign(this, {IgniteLegacyUtils, IgniteEventGroups, IgniteConfirm, IgniteVersion, $scope, Clusters, IgniteFormUtils});
    }
    $onDestroy() {
        this.versionSubscription.unsubscribe();
    }
    $onInit() {
        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        let __original_value;

        const rebuildDropdowns = () => {
            this.eventStorage = [
                {value: 'Memory', label: 'Memory'},
                {value: 'Custom', label: 'Custom'}
            ];

            this.marshallerVariant = [
                {value: 'JdkMarshaller', label: 'JdkMarshaller'},
                {value: null, label: 'Default'}
            ];

            if (this.available('2.0.0')) {
                this.eventStorage.push({value: null, label: 'Disabled'});

                this.eventGroups = _.filter(this.IgniteEventGroups, ({value}) => value !== 'EVTS_SWAPSPACE');
            }
            else {
                this.eventGroups = this.IgniteEventGroups;

                this.marshallerVariant.splice(0, 0, {value: 'OptimizedMarshaller', label: 'OptimizedMarshaller'});
            }
        };

        rebuildDropdowns();

        const filterModel = (cluster) => {
            if (cluster) {
                if (this.available('2.0.0')) {
                    const evtGrps = _.map(this.eventGroups, 'value');

                    // _.remove(__original_value, (evtGrp) => !_.includes(evtGrps, evtGrp));
                    _.remove(cluster.includeEventTypes, (evtGrp) => !_.includes(evtGrps, evtGrp));

                    if (_.get(cluster, 'marshaller.kind') === 'OptimizedMarshaller')
                        cluster.marshaller.kind = null;
                }
                else if (cluster && !_.get(cluster, 'eventStorage.kind'))
                    _.set(cluster, 'eventStorage.kind', 'Memory');
            }
        };

        this.versionSubscription = this.IgniteVersion.currentSbj.subscribe({
            next: () => {
                rebuildDropdowns();
                filterModel(this.clonedCluster);
            }
        });
        this.supportedJdbcTypes = this.IgniteLegacyUtils.mkOptions(this.IgniteLegacyUtils.SUPPORTED_JDBC_TYPES);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.loadedPanels = ['checkpoint', 'serviceConfiguration', 'odbcConfiguration'];
        this.$scope.ui.activePanels = [0];
        this.$scope.ui.topPanels = [0];
    }
    $onChanges(changes) {
        if (
            'cluster' in changes && get(this.clonedCluster, '_id') !== get(this.cluster, '_id')
        ) {
            this.clonedCluster = cloneDeep(changes.cluster.currentValue);
            if (this.$scope.ui && this.$scope.ui.inputForm) {
                this.$scope.ui.inputForm.$setPristine();
                this.$scope.ui.inputForm.$setUntouched();
            }
        }
    }
    getValuesToCompare() {
        return [this.cluster, this.clonedCluster].map(this.Clusters.normalize);
    }
    save() {
        if (this.$scope.ui.inputForm.$invalid)
            return this.IgniteFormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
        this.onSave({$event: cloneDeep(this.clonedCluster)});
    }
    reset = () => this.clonedCluster = cloneDeep(this.cluster);
    confirmAndReset() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current cluster?')
        .then(this.reset);
    }
}
