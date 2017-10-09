import isEqual from 'lodash/isEqual';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';
// import {diff} from 'jsondiffpatch';

export default class ConfigChangesGuard {
    static $inject = ['ConfigureState', 'IgniteConfirm', 'IgniteModelNormalizer'];

    constructor(ConfigureState, IgniteConfirm, IgniteModelNormalizer) {
        Object.assign(this, {ConfigureState, IgniteConfirm, IgniteModelNormalizer});
    }

    _hasChanges(a, b) {
        // return diff(a, b)
        return !isEqual(a, b);
    }

    _confirm(changes) {
        return this.IgniteConfirm.confirm(`
            You have unsaved changes.
            Are you sure you want to discard them?
        `);
    }

    guard(a, b) {
        if (!a && !b) return Promise.resolve(true);
        return of(this._hasChanges(a, b))
        .switchMap((changes) => changes ? this._confirm(changes) : of(true))
        .catch(() => of(false))
        .toPromise();
    }
}
