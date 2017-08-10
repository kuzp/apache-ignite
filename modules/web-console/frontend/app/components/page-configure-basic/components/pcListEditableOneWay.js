export default function() {
    return {
        require: {
            list: 'pcListEditable'
        },
        bindToController: {
            onItemChange: '&?',
            onItemRemove: '&?'
        },
        controller() {
            this.$onInit = () => {
                this.list.save = (item) => this.onItemChange({$event: item});
                this.list.remove = (index) => this.onItemRemove({$event: this.list.ngModel.$viewValue[index]});
            };
        }
    };
}
