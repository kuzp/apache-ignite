class FakeUiCanExitController {
    static $inject = ['$element', '$transitions'];
    static CALLBACK_NAME = 'uiCanExit';
    constructor($element, $transitions) {
        Object.assign(this, {$element, $transitions});
    }
    $onInit() {
        const data = this.$element.data();
        const {CALLBACK_NAME} = this.constructor;
        const controllerWithCallback = Object.keys(data)
            .map((key) => data[key])
            .find((controller) => controller[CALLBACK_NAME]);
        if (!controllerWithCallback) return;
        const off = this.$transitions.onBefore({from: this.fromState}, (...args) => {
            return controllerWithCallback[CALLBACK_NAME](...args);
        });
    }
    $onDestroy() {
        if (this.off) this.off();
        this.$element = null;
    }
}

export default function fakeUiCanExit() {
    return {
        bindToController: {
            fromState: '@fakeUiCanExit'
        },
        controller: FakeUiCanExitController
    };
}
