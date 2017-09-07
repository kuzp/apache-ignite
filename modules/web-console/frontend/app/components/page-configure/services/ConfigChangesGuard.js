import isMatch from 'lodash/isMatch';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';

export default class ConfigChangesGuard {
    static $inject = ['ConfigureState', 'IgniteConfirm', 'IgniteModelNormalizer'];

    constructor(ConfigureState, IgniteConfirm, IgniteModelNormalizer) {
        Object.assign(this, {ConfigureState, IgniteConfirm, IgniteModelNormalizer});
    }

    _hasChanges({cluster}, edit) {
        return cluster
            && !isMatch(
                this.IgniteModelNormalizer.normalize(cluster),
                this.IgniteModelNormalizer.normalize(edit.cluster)
            )
            || ['caches', 'igfss', 'models'].some((type) => {
                return edit.changes[type].changedItems.length
                    || !isMatch(edit.changes[type].ids, edit.cluster[type]);
            });
    }

    _confirm(changes) {
        return this.IgniteConfirm.confirm(`
            You have unsaved changes.
            Are you sure you want to discard them?
        `);
    }

    guard({cluster}) {
        return this.ConfigureState.state$.pluck('edit')
        .take(1)
        .map((edit) => this._hasChanges({cluster}, edit))
        .switchMap((changes) => changes ? this._confirm(changes) : of(true))
        .toPromise();
    }
}
