export default class ModalImportModels {
    static $inject = ['$modal'];
    constructor($modal) {
        Object.assign(this, {$modal});
        this.importDomainModal = this.$modal({
            template: `
                <modal-import-models
                    on-hide='$hide()'
                ></modal-import-models>
            `,
            show: false
        });
    }
    open() {
        return this.importDomainModal.$promise.then((m) => {
            this.importDomainModal.show();
            m.locals.modalInstance = m;
        });
    }
}
