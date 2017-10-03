export default class ModalPreviewProject {
    static $inject = ['$modal'];
    constructor($modal) {
        Object.assign(this, {$modal});
    }
    open(cluster) {
        this.modalInstance = this.$modal({
            locals: {
                cluster
            },
            controller: ['cluster', '$rootScope', function(cluster, $rootScope) {
                this.cluster = cluster;
                this.isDemo = !!$rootScope.IgniteDemoMode;
            }],
            controllerAs: '$ctrl',
            template: `
                <modal-preview-project
                    on-hide='$hide()'
                    cluster='::$ctrl.cluster'
                    is-demo='::$ctrl.isDemo'
                ></modal-preview-project>
            `,
            show: false
        });
        return this.modalInstance.$promise.then((modal) => {
            this.modalInstance.show();
        });
    }
}
