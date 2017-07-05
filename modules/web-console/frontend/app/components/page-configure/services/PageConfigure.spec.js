import {suite, test} from 'mocha';
import {assert} from 'chai';
import {spy} from 'sinon';
import {TestScheduler} from 'rxjs/testing/TestScheduler';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';

const mocks = () => new Map([
    ['IgniteConfigurationResource', {}],
    ['$state', {}],
    ['ConfigureState', {}],
    ['Clusters', {}]
]);

import {REMOVE_CLUSTERS_LOCAL_REMOTE} from './PageConfigure';
import PageConfigure from './PageConfigure';
import {REMOVE_CLUSTERS, LOAD_LIST} from '../reducer';

suite('PageConfigure service', () => {
    suite('removeCluster$ effect', () => {
        test('successfull clusters removal', () => {
            const testScheduler = new TestScheduler((...args) => assert.deepEqual(...args));

            const values = {
                a: {
                    type: REMOVE_CLUSTERS_LOCAL_REMOTE,
                    clusters: [1, 2, 3, 4, 5].map((i) => ({_id: i}))
                },
                b: {
                    type: REMOVE_CLUSTERS,
                    clusterIDs: [1, 2, 3, 4, 5]
                },
                c: {
                    type: LOAD_LIST,
                    list: []
                },
                d: {
                    type: REMOVE_CLUSTERS,
                    clusterIDs: [1, 2, 3, 4, 5]
                },
                s: {
                    list: []
                }
            };

            const actions = '-a';
            const state   = 's-';
            const output  = '-d';

            const deps = mocks()
            .set('ConfigureState', {
                actions$: testScheduler.createHotObservable(actions, values),
                state$: testScheduler.createHotObservable(state, values),
                dispatchAction: spy()
            })
            .set('Clusters', {
                removeCluster$: (v) => Observable.of(v)
            });
            const s = new PageConfigure(...deps.values());

            testScheduler.expectObservable(s.removeClusters$).toBe(output, values);
            testScheduler.flush();
            assert.equal(s.ConfigureState.dispatchAction.callCount, 1);
        });
        test('some clusters removal failure', () => {
            const testScheduler = new TestScheduler((...args) => assert.deepEqual(...args));

            const values = {
                a: {
                    type: REMOVE_CLUSTERS_LOCAL_REMOTE,
                    clusters: [1, 2, 3, 4, 5].map((i) => ({_id: i}))
                },
                b: {
                    type: REMOVE_CLUSTERS,
                    clusterIDs: [1, 2, 3, 4, 5]
                },
                c: {
                    type: LOAD_LIST,
                    list: []
                },
                d: {
                    type: REMOVE_CLUSTERS,
                    clusterIDs: [1, 3, 5]
                },
                s: {
                    list: []
                }
            };

            const actions = '-a----';
            const state   = 's-----';
            const output  = '-(bcd)';

            const deps = mocks()
            .set('ConfigureState', {
                actions$: testScheduler.createHotObservable(actions, values),
                state$: testScheduler.createHotObservable(state, values),
                dispatchAction: spy()
            })
            .set('Clusters', {
                removeCluster$: (v) => v._id % 2 ? Observable.of(v) : Observable.throw()
            });
            const s = new PageConfigure(...deps.values());

            testScheduler.expectObservable(s.removeClusters$).toBe(output, values);
            testScheduler.flush();
            assert.equal(s.ConfigureState.dispatchAction.callCount, 3);
        });
    });
});
