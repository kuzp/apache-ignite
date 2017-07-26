export default class PCFormFieldSizeController {
    static $inject = ['$element'];

    constructor($element) {
        Object.assign(this, {$element});
    }

    $onDestroy() {
        this.$element = null;
    }

    $onInit() {
        if (!this.min) this.min = 0;
        this.$element.addClass('ignite-form-field');
        this.sizesMenu = [
            {label: 'Kb', value: 1024},
            {label: 'Mb', value: 1024 * 1024},
            {label: 'Gb', value: 1024 * 1024 * 1024}
        ];
        this.sizeScale = this.sizesMenu[2];
    }
}
