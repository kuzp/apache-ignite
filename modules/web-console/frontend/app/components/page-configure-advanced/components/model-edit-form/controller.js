import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class IgfsEditFormController {
    static $inject = ['IgniteConfirm', 'ConfigChangesGuard', '$transitions', 'IgniteVersion', '$scope', 'Models', 'IgniteFormUtils'];
    constructor( IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils) {
        Object.assign(this, { IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils});
    }
    $onInit() {
        this.$onDestroy = this.$transitions.onBefore({}, (...args) => this.uiCanExit(...args));

        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0, 1];
        this.$scope.ui.topPanels = [0, 1, 2];
        this.$scope.ui.expanded = true;
    }

    $onChanges(changes) {
        if (
            'model' in changes && get(this.$scope.backupItem, '_id') !== get(this.model, '_id')
        ) {
            this.$scope.backupItem = cloneDeep(changes.model.currentValue);
            if (this.$scope.ui && this.$scope.ui.inputForm) {
                this.$scope.ui.inputForm.$setPristine();
                this.$scope.ui.inputForm.$setUntouched();
            }
        }
        if ('caches' in changes)
            this.cachesMenu = (changes.caches.currentValue || []).map((c) => ({label: c.name, value: c._id}));

    }
    uiCanExit($transition$) {
        return this.ConfigChangesGuard.guard(...[this.model, this.$scope.backupItem].map(this.Models.normalize));
    }
    save() {
        if (this.$scope.ui.inputForm.$invalid)
            return this.IgniteFormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
        this.onSave({$event: cloneDeep(this.$scope.backupItem)});
    }
    resetAll() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current model?')
        .then(() => this.$scope.backupItem = cloneDeep(this.model));
    }
}
