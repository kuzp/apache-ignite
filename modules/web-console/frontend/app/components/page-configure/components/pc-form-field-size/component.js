import template from './template.pug';
import controller from './controller';
import './style.scss';

export default {
    controller,
    template,
    transclude: true,
    require: {
        ngModel: 'ngModel'
    },
    bindings: {
        rawValue: '<ngModel',
        label: '@',
        placeholder: '@',
        min: '@?',
        max: '@?',
        tip: '@',
        required: '<?',
        sizeType: '@?',
        sizeScaleLabel: '@?',
        onScaleChange: '&?',
        ngDisabled: '<?'
    }
};
