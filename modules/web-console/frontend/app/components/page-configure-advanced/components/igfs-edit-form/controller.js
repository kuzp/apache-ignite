import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class IgfsEditFormController {
    static $inject = ['IgniteConfirm', 'ConfigChangesGuard', '$transitions', 'IgniteVersion', '$scope', 'IGFSs', 'IgniteFormUtils'];
    constructor( IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, IGFSs, IgniteFormUtils) {
        Object.assign(this, { IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, IGFSs, IgniteFormUtils});
    }
    $onInit() {
        this.$onDestroy = this.$transitions.onBefore({}, (...args) => this.uiCanExit(...args));

        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0];
        this.$scope.ui.topPanels = [0];
        this.$scope.ui.expanded = true;
        this.$scope.ui.loadedPanels = ['general', 'secondaryFileSystem', 'misc'];
    }

    $onChanges(changes) {
        if (
            'igfs' in changes && get(this.$scope.backupItem, '_id') !== get(this.igfs, '_id')
        ) {
            this.$scope.backupItem = cloneDeep(changes.igfs.currentValue);
            if (this.$scope.ui && this.$scope.ui.inputForm) {
                this.$scope.ui.inputForm.$setPristine();
                this.$scope.ui.inputForm.$setUntouched();
            }
        }
    }
    uiCanExit($transition$) {
        return this.ConfigChangesGuard.guard(...[this.igfs, this.$scope.backupItem].map(this.IGFSs.normalize));
    }
    save() {
        if (this.$scope.ui.inputForm.$invalid)
            return this.IgniteFormUtils.triggerValidation(this.$scope.ui.inputForm, this.$scope);
        this.onSave({$event: cloneDeep(this.$scope.backupItem)});
    }
    resetAll() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current IGFS?')
        .then(() => this.$scope.backupItem = cloneDeep(this.igfs));
    }
}
