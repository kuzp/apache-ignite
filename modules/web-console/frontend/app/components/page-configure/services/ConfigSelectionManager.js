/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Observable} from 'rxjs/Observable';
import {merge} from 'rxjs/observable/merge';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {RejectType} from '@uirouter/angularjs';

/**
 * @param {uirouter.TransitionService} $transitions
 */
export default function configSelectionManager($transitions) {
    /**
     * Determines what items should be marked as selected and if something is being edited at the moment.
     */
    return ({itemID$, selectedItemRows$, visibleRows$, loadedItems$}) => {
        // Aborted transitions happen when form has unsaved changes, user attempts to leave
        // but decides to stay after screen asks for leave confirmation.
        const abortedTransitions$ = Observable.create((observer) => {
            return $transitions.onError({}, (t) => observer.next(t));
        })
        .filter((t) => t.error().type === RejectType.ABORTED);

        const firstItemID$ = visibleRows$.withLatestFrom(itemID$, loadedItems$)
            .filter(([rows, id, items]) => !id && rows && rows.length === items.length)
            .pluck('0', '0', 'entity', '_id');

        const singleSelectionEdit$ = selectedItemRows$.filter((r) => r && r.length === 1).pluck('0', '_id');
        const selectedMultipleOrNone$ = selectedItemRows$.filter((r) => r.length > 1 || r.length === 0);

        // Edit first loaded item or when there's only one item selected
        const editGoes$ = merge(firstItemID$, singleSelectionEdit$).filter((v) => v);
        // Stop edit when multiple items are selected
        const editLeaves$ = merge(selectedMultipleOrNone$);

        const selectedItemIDs$ = merge(
            // Select nothing when creating an item or select current item
            itemID$.filter((id) => id).map((id) => id === 'new' ? [] : [id]),
            // Restore previous item selection when transition gets aborted
            abortedTransitions$.withLatestFrom(itemID$, (_, id) => [id]),
            // Select all incoming selected rows
            selectedItemRows$.map((rows) => rows.map((r) => r._id))
        )
        .publishReplay(1).refCount();

        return {selectedItemIDs$, editGoes$, editLeaves$};
    };
}
configSelectionManager.$inject = ['$transitions'];
