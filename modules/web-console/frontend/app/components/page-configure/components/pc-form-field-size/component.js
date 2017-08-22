import template from './template.pug';
import controller from './controller';
import './style.scss';

export default {
    controller,
    template,
    transclude: true,
    require: {
        form: '^form'
    },
    bindings: {
        ngModel: '=',
        id: '@',
        label: '@',
        name: '@',
        placeholder: '@',
        min: '@',
        max: '@?',
        tip: '@',
        required: '<?',
        sizeScaleLabel: '@?',
        onScaleChange: '&?',
        ngDisabled: '<?'
    }
};
