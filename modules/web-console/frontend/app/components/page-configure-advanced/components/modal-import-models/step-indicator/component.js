import template from './template.pug';
import './style.scss';

export class ModalImportModelsStepIndicator {
    isVisited(index) {
        return index <= this.steps.findIndex((step) => step.value === this.currentStep);
    }
}

export const component = {
    name: 'modalImportModelsStepIndicator',
    template,
    controller: ModalImportModelsStepIndicator,
    bindings: {
        steps: '<',
        currentStep: '<'
    }
};
