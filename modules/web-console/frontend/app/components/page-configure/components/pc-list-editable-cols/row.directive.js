// @ts-check

import {PcListEditableColsController} from './cols.directive';

/** @returns {ng.IDirective} */
export default function() {
    return {
        require: '?^pcListEditableCols',
        /** @param {PcListEditableColsController} ctrl */
        link(scope, el, attr, ctrl) {
            if (!ctrl || !ctrl.colDefs.length) return;
            const children = el.children();
            if (children.length !== ctrl.colDefs.length) return;
            if (ctrl.rowClass) el.addClass(ctrl.rowClass);
            ctrl.colDefs.forEach((colDef, index) => {
                children[index].classList.add(colDef.cellClass);
            });
        }
    };
}
