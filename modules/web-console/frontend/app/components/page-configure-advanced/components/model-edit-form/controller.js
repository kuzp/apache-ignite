import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class IgfsEditFormController {
    static $inject = ['IgniteLegacyUtils', 'IgniteConfirm', 'ConfigChangesGuard', '$transitions', 'IgniteVersion', '$scope', 'Models', 'IgniteFormUtils'];
    constructor(IgniteLegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils) {
        Object.assign(this, {IgniteLegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils});
    }
    $onInit() {
        this.$onDestroy = this.$transitions.onBefore({}, (...args) => this.uiCanExit(...args));

        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0, 1];
        this.$scope.ui.topPanels = [0, 1, 2];
        this.$scope.ui.expanded = true;

        this.$scope.javaBuiltInClasses = this.IgniteLegacyUtils.javaBuiltInClasses;
        this.$scope.supportedJdbcTypes = this.IgniteLegacyUtils.mkOptions(this.IgniteLegacyUtils.SUPPORTED_JDBC_TYPES);
        this.$scope.supportedJavaTypes = this.IgniteLegacyUtils.mkOptions(this.IgniteLegacyUtils.javaBuiltInTypes);
        // Create list of fields to show in index fields dropdown.
        this.$scope.fields = (prefix, cur) => {
            const fields = _.map(this.$scope.backupItem.fields, (field) => ({value: field.name, label: field.name}));

            if (prefix === 'new')
                return fields;

            _.forEach(_.isArray(cur) ? cur : [cur], (value) => {
                if (!_.find(fields, {value}))
                    fields.push({value, label: value + ' (Unknown field)'});
            });

            return fields;
        };
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
    onQueryFieldsChange(queryFields) {
        // this.keyFieldsOptions = this.fields('cur', this.keyFieldsOptions);
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
