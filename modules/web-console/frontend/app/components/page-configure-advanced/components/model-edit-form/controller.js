import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';

export default class ModelEditFormController {
    static $inject = ['ModalImportModels', 'IgniteErrorPopover', 'IgniteLegacyUtils', 'IgniteConfirm', 'ConfigChangesGuard', '$transitions', 'IgniteVersion', '$scope', 'Models', 'IgniteFormUtils'];
    constructor(ModalImportModels, ErrorPopover, LegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils) {
        Object.assign(this, {ModalImportModels, ErrorPopover, LegacyUtils, IgniteConfirm, ConfigChangesGuard, $transitions, IgniteVersion, $scope, Models, IgniteFormUtils});
    }
    $onInit() {
        this.$onDestroy = this.$transitions.onBefore({}, (...args) => this.uiCanExit(...args));

        this.available = this.IgniteVersion.available.bind(this.IgniteVersion);

        this.$scope.ui = this.IgniteFormUtils.formUI();
        this.$scope.ui.activePanels = [0, 1];
        this.$scope.ui.topPanels = [0, 1, 2];
        this.$scope.ui.expanded = true;

        this.$scope.javaBuiltInClasses = this.LegacyUtils.javaBuiltInClasses;
        this.$scope.supportedJdbcTypes = this.LegacyUtils.mkOptions(this.LegacyUtils.SUPPORTED_JDBC_TYPES);
        this.$scope.supportedJavaTypes = this.LegacyUtils.mkOptions(this.LegacyUtils.javaBuiltInTypes);
        // Create list of fields to show in index fields dropdown.
        this.$scope.fields = (prefix, cur) => {
            const fields = this.$scope.backupItem
                ? _.map(this.$scope.backupItem.fields, (field) => ({value: field.name, label: field.name}))
                : [];

            if (prefix === 'new')
                return fields;

            _.forEach(_.isArray(cur) ? cur : [cur], (value) => {
                if (!_.find(fields, {value}))
                    fields.push({value, label: value + ' (Unknown field)'});
            });

            return fields;
        };
    }

    importModels() {
        return this.ModalImportModels.open();
    }

    checkQueryConfiguration(item) {
        if (item.queryMetadata === 'Configuration' && this.LegacyUtils.domainForQueryConfigured(item)) {
            if (_.isEmpty(item.fields))
                return this.ErrorPopover.show('queryFields', 'Query fields should not be empty', this.$scope.ui, 'query');

            const indexes = item.indexes;

            if (indexes && indexes.length > 0) {
                if (_.find(indexes, (index, idx) => {
                    if (_.isEmpty(index.fields))
                        return !this.ErrorPopover.show('indexes' + idx, 'Index fields are not specified', this.$scope.ui, 'query');

                    if (_.find(index.fields, (field) => !_.find(item.fields, (configuredField) => configuredField.name === field.name)))
                        return !this.ErrorPopover.show('indexes' + idx, 'Index contains not configured fields', this.$scope.ui, 'query');
                }))
                    return false;
            }
        }

        return true;
    }

    checkStoreConfiguration(item) {
        if (this.LegacyUtils.domainForStoreConfigured(item)) {
            if (this.LegacyUtils.isEmptyString(item.databaseSchema))
                return this.ErrorPopover.show('databaseSchemaInput', 'Database schema should not be empty', this.$scope.ui, 'store');

            if (this.LegacyUtils.isEmptyString(item.databaseTable))
                return this.ErrorPopover.show('databaseTableInput', 'Database table should not be empty', this.$scope.ui, 'store');

            if (_.isEmpty(item.keyFields))
                return this.ErrorPopover.show('keyFields', 'Key fields are not specified', this.$scope.ui, 'store');

            if (this.LegacyUtils.isJavaBuiltInClass(item.keyType) && item.keyFields.length !== 1)
                return this.ErrorPopover.show('keyFields', 'Only one field should be specified in case when key type is a Java built-in type', this.$scope.ui, 'store');

            if (_.isEmpty(item.valueFields))
                return this.ErrorPopover.show('valueFields', 'Value fields are not specified', this.$scope.ui, 'store');
        }

        return true;
    }

    // Check domain model logical consistency.
    validate(item) {
        if (!this.checkQueryConfiguration(item))
            return false;

        if (!this.checkStoreConfiguration(item))
            return false;

        if (!this.LegacyUtils.domainForStoreConfigured(item) && !this.LegacyUtils.domainForQueryConfigured(item) && item.queryMetadata === 'Configuration')
            return this.ErrorPopover.show('query-title', 'SQL query domain model should be configured', this.$scope.ui, 'query');

        if (!this.LegacyUtils.domainForStoreConfigured(item) && item.generatePojo)
            return this.ErrorPopover.show('store-title', 'Domain model for cache store should be configured when generation of POJO classes is enabled', this.$scope.ui, 'store');

        return true;
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
        if (!this.validate(this.$scope.backupItem)) return;
        this.onSave({$event: cloneDeep(this.$scope.backupItem)});
    }
    resetAll() {
        return this.IgniteConfirm.confirm('Are you sure you want to undo all changes for current model?')
        .then(() => this.$scope.backupItem = cloneDeep(this.model));
    }
}
