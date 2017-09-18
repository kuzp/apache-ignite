import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class IgfsEditFormController {
    static $inject = ['IgniteLegacyUtils', 'IgniteConfirm', 'ConfigChangesGuard', '$transitions', 'IgniteVersion', '$scope', 'IGFSs', 'IgniteFormUtils'];
    constructor(IgniteLegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, IGFSs, IgniteFormUtils) {
        Object.assign(this, {IgniteLegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, IGFSs, IgniteFormUtils});
    }
    $onInit() {
        this.$onDestroy = this.$transitions.onBefore({}, (...args) => this.uiCanExit(...args));

        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0];
        this.$scope.ui.topPanels = [0];
        this.$scope.ui.expanded = true;
        this.$scope.compactJavaName = this.IgniteFormUtils.compactJavaName;
        this.$scope.widthIsSufficient = this.IgniteFormUtils.widthIsSufficient;
        this.$scope.saveBtnTipText = this.IgniteFormUtils.saveBtnTipText;
        this.$scope.igfsModes = this.IgniteLegacyUtils.mkOptions(['PRIMARY', 'PROXY', 'DUAL_SYNC', 'DUAL_ASYNC']);

    }

    // validate(item) {
    //     ErrorPopover.hide();

    //     if (LegacyUtils.isEmptyString(item.name))
    //         return ErrorPopover.show('igfsNameInput', 'IGFS name should not be empty!', $scope.ui, 'general');

    //     if (!LegacyUtils.checkFieldValidators($scope.ui))
    //         return false;

    //     if (!item.secondaryFileSystemEnabled && (item.defaultMode === 'PROXY'))
    //         return ErrorPopover.show('secondaryFileSystem-title', 'Secondary file system should be configured for "PROXY" IGFS mode!', $scope.ui, 'secondaryFileSystem');

    //     if (item.pathModes) {
    //         for (let pathIx = 0; pathIx < item.pathModes.length; pathIx++) {
    //             if (!item.secondaryFileSystemEnabled && item.pathModes[pathIx].mode === 'PROXY')
    //                 return ErrorPopover.show('secondaryFileSystem-title', 'Secondary file system should be configured for "PROXY" path mode!', $scope.ui, 'secondaryFileSystem');
    //         }
    //     }

    //     return true;
    // }

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
