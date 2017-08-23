export default class PCFormFieldSizeController {
    static $inject = ['$element', '$attrs'];

    constructor($element, $attrs) {
        Object.assign(this, {$element, $attrs});
        this.sizesMenu = [
            {label: 'Kb', value: 1024},
            {label: 'Mb', value: 1024 * 1024},
            {label: 'Gb', value: 1024 * 1024 * 1024}
        ];
        this.sizeScale = this.sizesMenu[2];
        this.id = Math.random();
    }

    $onDestroy() {
        this.$element = null;
    }

    $onInit() {
        if (!this.min) this.min = 0;
        this.$element.addClass('ignite-form-field');
    }

    $postLink() {
        if ('min' in this.$attrs)
            this.ngModel.$validators.min = (value) => this.ngModel.$isEmpty(value) || value === void 0 || value >= this.min;
        if ('max' in this.$attrs)
            this.ngModel.$validators.max = (value) => this.ngModel.$isEmpty(value) || value === void 0 || value <= this.max;
    }

    $onChanges(changes) {
        if ('sizeScaleLabel' in changes) this.sizeScale = this.chooseSizeScale(changes.sizeScaleLabel.currentValue);
        if ('rawValue' in changes) this.assignValue(changes.rawValue.currentValue);
        if ('min' in changes) this.ngModel.$validate();
    }

    set sizeScale(value) {
        this._sizeScale = value;
        if (this.onScaleChange) this.onScaleChange({$event: this.sizeScale});
        if (this.ngModel) this.assignValue(this.ngModel.$viewValue);
        return this.sizeScale;
    }

    get sizeScale() {
        return this._sizeScale;
    }

    assignValue(rawValue) {
        return this.value = rawValue
            ? rawValue / this.sizeScale.value
            : rawValue;
    }

    onValueChange() {
        this.ngModel.$setViewValue(this.value ? this.value * this.sizeScale.value : this.value);
    }

    chooseSizeScale(label = this.sizesMenu[1].label) {
        return this.sizesMenu.find((option) => option.label.toLowerCase() === label.toLowerCase());
    }
}
