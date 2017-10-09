import template from './template.pug';

export class ButtonImportModels {
    static $inject = ['ModalImportModels'];
    constructor(ModalImportModels) {
        Object.assign(this, {ModalImportModels});
    }
    startImport() {
        return this.ModalImportModels.open(this.clusterID);
    }
}
export const component = {
    name: 'buttonImportModels',
    controller: ButtonImportModels,
    template,
    bindings: {
        clusterID: '<clusterId'
    }
};
