export default class ModalImportModels {
    static $inject = ['$modal', 'AgentManager', '$uiRouter'];
    constructor($modal, AgentManager, $uiRouter) {
        Object.assign(this, {$modal, AgentManager, $uiRouter});
    }
    _goToDynamicState() {
        if (this._state) this.$uiRouter.stateRegistry.deregister(this._state);
        this._state = this.$uiRouter.stateRegistry.register({
            name: 'importModels',
            parent: this.$uiRouter.stateService.current,
            onEnter: () => {
                this._open();
            },
            onExit: () => {
                this._modal && this._modal.hide();
            }
        });
        return this.$uiRouter.stateService.go(this._state, this.$uiRouter.stateService.params);
    }
    _open() {
        this._modal = this.$modal({
            template: `
                <modal-import-models
                    on-hide='$ctrl.$state.go("^")'
                    cluster-id='$ctrl.$state.params.clusterID'
                ></modal-import-models>
            `,
            controller: ['$state', function($state) {this.$state = $state;}],
            controllerAs: '$ctrl',
            show: false
        });
        return this.AgentManager.startAgentWatch('Back', this.$uiRouter.globals.current.name)
        .then(() => this._modal.$promise)
        .then(() => this._modal.show());
    }
    open() {
        this._goToDynamicState();
    }
}
