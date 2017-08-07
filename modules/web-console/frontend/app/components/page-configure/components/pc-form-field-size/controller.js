export default class PCFormFieldSizeController {
    static $inject = ['$element'];

    constructor($element) {
        Object.assign(this, {$element});
        this.sizesMenu = [
            {label: 'Kb', value: 1024},
            {label: 'Mb', value: 1024 * 1024},
            {label: 'Gb', value: 1024 * 1024 * 1024}
        ];
        this.sizeScale = this.sizesMenu[2];
    }

    $onDestroy() {
        this.$element = null;
    }

    $onInit() {
        if (!this.min) this.min = 0;
        this.$element.addClass('ignite-form-field');
    }

    $onChanges(changes) {
        if ('sizeScaleLabel' in changes)
            this.sizeScale = this.chooseSizeScale(changes.sizeScaleLabel.currentValue);
    }

    set sizeScale(value) {
        this._sizeScale = value;
        if (this.onScaleChange) this.onScaleChange({$event: this.sizeScale});
        return this.sizeScale;
    }

    get sizeScale() {
        return this._sizeScale;
    }

    chooseSizeScale(label = 'Mb') {
        return this.sizesMenu.find((option) => option.label.toLowerCase() === label.toLowerCase());
    }
}
