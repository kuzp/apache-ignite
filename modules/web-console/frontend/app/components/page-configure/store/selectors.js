import {uniqueName} from 'app/utils/uniqueName';
import {of} from 'rxjs/observable/of';
import {empty} from 'rxjs/observable/empty';

export default class ConfigSelectors {
    static $inject = ['Caches'];
    constructor(Caches) {
        Object.assign(this, {Caches});
    }
    selectCache = (id) => (state$) => state$.pluck('caches').map((v) => v && v.get(id));
    selectShortCaches = () => (state$) => state$.pluck('shortCaches').filter((v) => v);
    selectShortCachesValue = () => (state$) => this.selectShortCaches()(state$).map((v) => v && [...v.value.values()]);
    selectCacheToEdit = (cacheID) => (state$) => state$
        .let(this.selectCache(cacheID))
        .distinctUntilChanged()
        .switchMap((cache) => {
            if (cache) return of(cache);
            if (cacheID === 'new') {
                return state$.let(this.selectShortCachesValue()).map((shortCaches) => Object.assign(
                    this.Caches.getBlankCache(), {name: uniqueName('New cache', shortCaches)}
                ));
            }
            if (!cacheID) return of(null);
            return empty();
        });
}
