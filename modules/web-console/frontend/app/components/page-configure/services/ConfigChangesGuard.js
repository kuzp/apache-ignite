import isEqual from 'lodash/isEqual';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';

import {Confirm} from 'app/services/Confirm.service';
// import {diff} from 'jsondiffpatch';

export default class ConfigChangesGuard {
    static $inject = [Confirm.name];

    /**
     * @param {Confirm} Confirm
     */
    constructor(Confirm) {
        this.Confirm = Confirm;
    }

    _hasChanges(a, b) {
        // return diff(a, b)
        return !isEqual(a, b);
    }

    _confirm(changes) {
        return this.Confirm.confirm(`
            You have unsaved changes.
            Are you sure you want to discard them?
        `);
    }

    /**
     * Compares values and asks user if he wants to continue.
     * @template T
     * @param {T} a - Left comparison value
     * @param {T} b - Right comparison value
     */
    guard(a, b) {
        if (!a && !b) return Promise.resolve(true);
        return of(this._hasChanges(a, b))
        .switchMap((changes) => changes ? this._confirm(changes).then(() => true) : of(true))
        .catch(() => of(false))
        .toPromise();
    }
}
