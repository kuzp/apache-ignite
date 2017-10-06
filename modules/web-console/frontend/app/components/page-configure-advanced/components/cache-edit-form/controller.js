import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class CacheEditFormController {
    static $inject = ['IgniteConfirm', 'IgniteVersion', '$scope', 'Caches', 'IgniteFormUtils'];
    constructor(IgniteConfirm, IgniteVersion, $scope, Caches, IgniteFormUtils) {
        Object.assign(this, {IgniteConfirm, IgniteVersion, $scope, Caches, IgniteFormUtils});
    }
    $onInit() {
        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        const rebuildDropdowns = () => {
            this.$scope.affinityFunction = [
                {value: 'Rendezvous', label: 'Rendezvous'},
                {value: 'Custom', label: 'Custom'},
                {value: null, label: 'Default'}
            ];

            if (this.available(['1.0.0', '2.0.0']))
                this.$scope.affinityFunction.splice(1, 0, {value: 'Fair', label: 'Fair'});
        };

        rebuildDropdowns();

        const filterModel = () => {
            if (this.$scope.backupItem) {
                if (this.available('2.0.0')) {
                    if (_.get(this.$scope.backupItem, 'affinity.kind') === 'Fair')
                        this.$scope.backupItem.affinity.kind = null;
                }
            }
        };

        this.IgniteVersion.currentSbj.subscribe({
            next: () => {
                rebuildDropdowns();

                filterModel();
            }
        });

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0];
        this.$scope.ui.topPanels = [0, 1, 2, 3];
    }
    $onChanges(changes) {
        if (
            'cache' in changes && get(this.clonedCache, '_id') !== get(this.cache, '_id')
        ) {
            this.clonedCache = cloneDeep(changes.cache.currentValue);
            if (this.$scope.ui && this.$scope.ui.inputForm) {
                this.$scope.ui.inputForm.$setPristine();
                this.$scope.ui.inputForm.$setUntouched();
            }
        }
        if ('models' in changes)
            this.modelsMenu = (changes.models.currentValue || []).map((m) => ({value: m._id, label: m.valueType}));
    }
    getValuesToCompare() {
        return [this.cache, this.clonedCache].map(this.Caches.normalize);
    }
    save() {
        if (this.$scope.ui.inputForm.$invalid)
            return this.IgniteFormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
        this.onSave({$event: cloneDeep(this.clonedCache)});
    }
    reset = (forReal) => forReal ? this.clonedCache = cloneDeep(this.cache) : void 0;
    confirmAndReset() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current cache?')
        .then(this.reset);
    }
}
