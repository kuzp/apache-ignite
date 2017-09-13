import {merge} from 'rxjs/observable/merge';
import 'rxjs/add/operator/sample';

export default function configSelectionManager() {
    return ({itemID$, selectedItemRows$, visibleRows$, getLoadedLength}) => {
        const firstItemID$ = visibleRows$.withLatestFrom(itemID$)
            .filter(([rows, id]) => rows && rows.length === getLoadedLength())
            .take(1)
            .filter(([rows, id]) => !id)
            .pluck('0', '0', 'entity', '_id');

        const singleSelectionEdit$ = selectedItemRows$.filter((r) => r && r.length === 1).pluck('0', '_id');
        const selectedMultipleOrNone$ = selectedItemRows$.filter((r) => r.length > 1 || r.length === 0);

        const editGoes$ = merge(firstItemID$, singleSelectionEdit$).debug('go');
        const editLeaves$ = merge(selectedMultipleOrNone$).debug('leave');

        const selectedItemIDs$ = merge(
            itemID$.filter((id) => id).map((id) => id === 'new' ? [] : [id]),
            selectedItemRows$.map((rows) => rows.map((r) => r._id)).sample(itemID$.filter((id) => !id))
        ).publishReplay(1).refCount();

        return {selectedItemIDs$, editGoes$, editLeaves$};
    };
}
