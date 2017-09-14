import {Observable} from 'rxjs/Observable';
import {merge} from 'rxjs/observable/merge';
import {combineLatest} from 'rxjs/observable/combineLatest';
import 'rxjs/add/operator/sample';
import clone from 'lodash/clone';
import {RejectType} from '@uirouter/angularjs';

export default function configSelectionManager($transitions) {
    return ({itemID$, selectedItemRows$, visibleRows$, getLoadedLength}) => {
        const abortedTransitions$ = Observable.create((observer) => {
            return $transitions.onError({}, (t) => observer.next(t));
        })
        .filter((t) => t.error().type === RejectType.ABORTED)
        .debug('abortedTransitions');

        const firstItemID$ = visibleRows$.withLatestFrom(itemID$)
            .filter(([rows, id]) => rows && rows.length === getLoadedLength())
            .filter(([rows, id]) => !id)
            .pluck('0', '0', 'entity', '_id');

        const singleSelectionEdit$ = selectedItemRows$.filter((r) => r && r.length === 1).pluck('0', '_id');
        const selectedMultipleOrNone$ = selectedItemRows$.filter((r) => r.length > 1 || r.length === 0);

        const editGoes$ = merge(firstItemID$, singleSelectionEdit$).debug('go');
        const editLeaves$ = merge(selectedMultipleOrNone$).debug('leave');

        const selectedItemIDs$ = combineLatest(
            merge(
                itemID$.filter((id) => id).map((id) => id === 'new' ? [] : [id]),
                selectedItemRows$.map((rows) => rows.map((r) => r._id)).sample(itemID$.filter((id) => !id))
            ),
            merge(abortedTransitions$).startWith(null),
            clone
        ).publishReplay(1).refCount();

        return {selectedItemIDs$, editGoes$, editLeaves$};
    };
}
configSelectionManager.$inject = ['$transitions'];
