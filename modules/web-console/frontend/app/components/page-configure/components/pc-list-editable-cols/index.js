import angular from 'angular';

import cols from './cols.directive.js';
import row from './row.directive.js';

export default angular
.module('pc-list-editable-cols', [])
.directive(cols.name, cols)
.directive('pcListEditableItemView', row)
.directive('pcListEditableItemEdit', row);
