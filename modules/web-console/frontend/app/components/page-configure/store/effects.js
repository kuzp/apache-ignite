import {merge} from 'rxjs/observable/merge';
import {of} from 'rxjs/observable/of';
import {fromPromise} from 'rxjs/observable/fromPromise';

import {
    cachesActionTypes
} from './../reducer';

export default class ConfigEffects {
    static $inject = ['ConfigureState', 'Caches', 'ConfigSelectors'];
    constructor(ConfigureState, Caches, ConfigSelectors) {
        Object.assign(this, {ConfigureState, Caches, ConfigSelectors});

        this.loadCacheEffect$ = this.ConfigureState.actions$
            .filter((a) => a.type === 'LOAD_CACHE')
            .exhaustMap((a) => {
                return this.ConfigureState.state$.let(this.ConfigSelectors.selectCache(a)).take(1)
                    .switchMap((cache) => {
                        if (cache) return of({type: `${a.type}_OK`});
                        return fromPromise(this.Caches.getCache(a.cacheID))
                        .switchMap(({data}) => of(
                            {type: cachesActionTypes.UPSERT, items: [data]},
                            {type: `${a.type}_OK`}
                        ));
                    })
                    .catch((error) => of({type: `${a.type}_ERR`, error}));
            });
    }
    connect() {
        return merge(this.loadCacheEffect$).do((a) => this.ConfigureState.dispatchAction(a)).subscribe();
    }
}
