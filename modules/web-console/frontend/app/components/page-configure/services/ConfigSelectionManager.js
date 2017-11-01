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
import 'rxjs/add/operator/sample';
import clone from 'lodash/clone';
import {RejectType} from '@uirouter/angularjs';

export default function configSelectionManager($transitions) {
    return ({itemID$, selectedItemRows$, visibleRows$, loadedItems$}) => {
        const abortedTransitions$ = Observable.create((observer) => {
            return $transitions.onError({}, (t) => observer.next(t));
        })
        .filter((t) => t.error().type === RejectType.ABORTED)
        .debug('abortedTransitions');

        const firstItemID$ = visibleRows$.withLatestFrom(itemID$, loadedItems$)
            .filter(([rows, id, items]) => !id && rows && rows.length === items.length)
            .pluck('0', '0', 'entity', '_id');

        const singleSelectionEdit$ = selectedItemRows$.filter((r) => r && r.length === 1).pluck('0', '_id');
        const selectedMultipleOrNone$ = selectedItemRows$.filter((r) => r.length > 1 || r.length === 0);

        const editGoes$ = merge(firstItemID$, singleSelectionEdit$).filter((v) => v).debug('go');
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
