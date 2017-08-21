import angular from 'angular';

export default angular.module('ignite-console.page-configure.validation', [])
    .directive('pcNotInCollection', function() {
        class Controller {
            $onInit() {
                this.ngModel.$validators.notInCollection = (item) => {
                    if (!this.items) return true;
                    return !this.items.includes(item);
                };
            }

            $onChanges() {
                this.ngModel.$validate();
            }
        }

        return {
            controller: Controller,
            require: {
                ngModel: 'ngModel'
            },
            bindToController: {
                items: '<pcNotInCollection'
            }
        };
    })
    .directive('bsCollapseTarget', function() {
        return {
            require: {
                bsCollapse: '^^bsCollapse'
            },
            bindToController: true,
            controller: ['$element', '$scope', function($element, $scope) {
                this.open = function() {
                    const index = this.bsCollapse.$targets.indexOf($element);
                    const isActive = this.bsCollapse.$targets.$active.includes(index);
                    if (!isActive) this.bsCollapse.$setActive(index);
                };
                this.$onDestroy = () => this.open = $element = null;
            }]
        };
    })
    .directive('ngModel', ['$timeout', function($timeout) {
        return {
            require: ['ngModel', '?^^bsCollapseTarget', '?^^igniteFormField'],
            link(scope, el, attr, [ngModel, bsCollapseTarget, igniteFormField]) {
                const off = scope.$on('$showValidationError', (e, target) => {
                    if (target !== ngModel) return;
                    ngModel.$setTouched();
                    bsCollapseTarget && bsCollapseTarget.open();
                    $timeout(() => {
                        el[0].scrollIntoViewIfNeeded();
                        igniteFormField && igniteFormField.notifyAboutError();
                    });
                    console.debug(target, el, bsCollapseTarget);
                });
            }
        };
    }])
    .directive('igniteFormField', function() {
        const animName = 'ignite-form-field__error-blink';
        const eventName = 'webkitAnimationEnd oAnimationEnd msAnimationEnd animationend';
        class Controller {
            static $inject = ['$element'];
            constructor($element) {
                Object.assign(this, {$element});
            }
            $postLink() {
                this.onAnimEnd = () => this.$element.removeClass(animName);
                this.$element.on(eventName, this.onAnimEnd);
            }
            $onDestroy() {
                this.$element.off(eventName, this.onAnimEnd);
                this.$element = this.onAnimEnd = null;
            }
            notifyAboutError() {
                this.$element.addClass(animName);
            }
        }
        return {
            restrict: 'C',
            controller: Controller
        };
    });
