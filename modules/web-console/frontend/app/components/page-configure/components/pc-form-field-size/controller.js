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
        if ('sizeScaleLabel' in changes) this.sizeScale = this.chooseSizeScale(changes.sizeScaleLabel.currentValue);
        if ('rawValue' in changes) this.assignValue();
    }

    set sizeScale(value) {
        this._sizeScale = value;
        if (this.onScaleChange) this.onScaleChange({$event: this.sizeScale});
        this.assignValue();
        return this.sizeScale;
    }

    get sizeScale() {
        return this._sizeScale;
    }

    assignValue() {
        return this.value = this.rawValue
            ? this.rawValue / this.sizeScale.value
            : this.rawValue;
    }

    onValueChange() {
        this.ngModel.$setViewValue(this.value ? this.value * this.sizeScale.value : this.value);
    }

    chooseSizeScale(label = this.sizesMenu[1].label) {
        return this.sizesMenu.find((option) => option.label.toLowerCase() === label.toLowerCase());
    }
}
