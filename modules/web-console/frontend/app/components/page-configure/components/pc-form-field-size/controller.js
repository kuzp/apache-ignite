import get from 'lodash/get';

export default class PCFormFieldSizeController {
    static $inject = ['$element', '$attrs'];

    static sizeTypes = {
        bytes: [
            {label: 'Kb', value: 1024},
            {label: 'Mb', value: 1024 * 1024},
            {label: 'Gb', value: 1024 * 1024 * 1024}
        ],
        seconds: [
            {label: 'ns', value: 1 / 1000},
            {label: 'ms', value: 1},
            {label: 's', value: 1000}
        ]
    };

    constructor($element, $attrs) {
        Object.assign(this, {$element, $attrs});
        this.id = Math.random();
    }

    $onDestroy() {
        this.$element = null;
    }

    $onInit() {
        if (!this.min) this.min = 0;
        if (!this.sizesMenu) this.setDefaultSizeType();
        this.$element.addClass('ignite-form-field');
    }

    $postLink() {
        if ('min' in this.$attrs)
            this.ngModel.$validators.min = (value) => this.ngModel.$isEmpty(value) || value === void 0 || value >= this.min;
        if ('max' in this.$attrs)
            this.ngModel.$validators.max = (value) => this.ngModel.$isEmpty(value) || value === void 0 || value <= this.max;
    }

    $onChanges(changes) {
        if ('sizeType' in changes) {
            this.sizesMenu = PCFormFieldSizeController.sizeTypes[changes.sizeType.currentValue];
            this.sizeScale = this.chooseSizeScale(get(changes, 'sizeScaleLabel.currentValue'));
        }
        if (!this.sizesMenu) this.setDefaultSizeType();
        if ('sizeScaleLabel' in changes)
            this.sizeScale = this.chooseSizeScale(changes.sizeScaleLabel.currentValue);

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
        if (!this.sizesMenu) this.setDefaultSizeType();
        return this.value = rawValue
            ? rawValue / this.sizeScale.value
            : rawValue;
    }

    onValueChange() {
        this.ngModel.$setViewValue(this.value ? this.value * this.sizeScale.value : this.value);
    }

    _defaultLabel() {
        if (!this.sizesMenu) return;
        return this.sizesMenu[1].label;
    }

    chooseSizeScale(label = this._defaultLabel()) {
        if (!label) return;
        return this.sizesMenu.find((option) => option.label.toLowerCase() === label.toLowerCase());
    }

    setDefaultSizeType() {
        this.sizesMenu = PCFormFieldSizeController.sizeTypes.bytes;
        this.sizeScale = this.chooseSizeScale();
    }
}
