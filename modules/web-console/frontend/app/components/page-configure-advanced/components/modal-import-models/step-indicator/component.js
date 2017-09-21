import template from './template.pug';
import './style.scss';

// export class ModalImportModelsStepIndicator {
//     $onInit() {

//     }
// }

export const component = {
    name: 'modalImportModelsStepIndicator',
    template,
    bindings: {
        steps: '<',
        currentStep: '<'
    }
};
