export default class ModalImportModels {
    static $inject = ['$modal', 'AgentManager', '$uiRouter'];
    constructor($modal, AgentManager, $uiRouter) {
        Object.assign(this, {$modal, AgentManager, $uiRouter});
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
        return this.AgentManager.startAgentWatch('Back', this.$uiRouter.globals.current.name)
        .then(() => this.importDomainModal.$promise)
        .then((m) => {
            this.importDomainModal.show();
            m.locals.modalInstance = m;
        });
    }
}
