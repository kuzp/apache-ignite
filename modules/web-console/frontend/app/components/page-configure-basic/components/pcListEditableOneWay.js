import isMatch from 'lodash/isMatch';

export default function() {
    return {
        require: {
            list: 'pcListEditable'
        },
        bindToController: {
            onItemChange: '&?',
            onItemRemove: '&?'
        },
        controller: class Controller {
            static $inject = ['$scope'];
            constructor($scope) {
                Object.assign(this, {$scope});
            }
            $onInit() {
                this.list.save = (item, index) => {
                    if (!isMatch(this.list.ngModel.$viewValue[index], item))
                        this.onItemChange({$event: item});
                    else {
                        this.list.stopEditView(index);
                        this.$scope.$applyAsync();
                    }
                };
                this.list.remove = (index) => this.onItemRemove({$event: this.list.ngModel.$viewValue[index]});
            }
        }
    };
}
