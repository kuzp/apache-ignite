import template from './template.pug';
import './style.scss';
export const component = {
    name: 'selectedItemsAmountIndicator',
    template,
    bindings: {
        selectedAmount: '<',
        totalAmount: '<'
    }
};
