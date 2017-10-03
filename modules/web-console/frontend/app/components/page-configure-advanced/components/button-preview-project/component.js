import template from './template.pug';

export class ButtonPreviewProject {
    static $inject = ['ModalPreviewProject'];
    constructor(ModalPreviewProject) {
        Object.assign(this, {ModalPreviewProject});
    }
    preview() {
        return this.ModalPreviewProject.open(this.cluster);
    }
}
export const component = {
    name: 'buttonPreviewProject',
    controller: ButtonPreviewProject,
    template,
    bindings: {
        cluster: '<'
    }
};
