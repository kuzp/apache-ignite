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
        label: '@',
        placeholder: '@',
        min: '@?',
        max: '@?',
        tip: '@',
        required: '<?',
        sizeScaleLabel: '@?',
        onScaleChange: '&?',
        ngDisabled: '<?'
    }
};
