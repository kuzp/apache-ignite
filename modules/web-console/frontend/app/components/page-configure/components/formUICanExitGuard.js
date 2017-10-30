class FormUICanExitGuardController {
    static $inject = ['$element', 'ConfigChangesGuard'];
    constructor($element, ConfigChangesGuard) {
        Object.assign(this, {$element, ConfigChangesGuard});
    }
    $onDestroy() {
        this.$element = null;
    }
    $onInit() {
        const data = this.$element.data();
        const controller = Object.keys(data)
            .map((key) => data[key])
            .find(this._itQuacks);

        if (!controller) return;

        controller.uiCanExit = ($transition$) => {
            if ($transition$.options().custom.justIDUpdate) return true;
            $transition$.onSuccess({}, controller.reset);
            return this.ConfigChangesGuard.guard(...controller.getValuesToCompare());
        };
    }
    _itQuacks(controller) {
        return controller.reset instanceof Function &&
            controller.getValuesToCompare instanceof Function &&
            !controller.uiCanExit;
    }
}

export default function formUiCanExitGuard() {
    return {
        priority: 10,
        controller: FormUICanExitGuardController
    };
}
