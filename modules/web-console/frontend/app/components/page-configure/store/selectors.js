import {uniqueName} from 'app/utils/uniqueName';
import {of} from 'rxjs/observable/of';
import {empty} from 'rxjs/observable/empty';
import {Observable} from 'rxjs/Observable';

const isDefined = (s) => s.filter((v) => v);
const selectItems = (path) => (s) => s.filter((s) => s).pluck(path).filter((v) => v);
const selectValues = (s) => s.map((v) => v && [...v.value.values()]);
const selectMapItem = (mapPath, key) => (s) => s.pluck(mapPath).map((v) => v && v.get(key));
const selectItemToEdit = ({items, itemFactory, defaultName, itemID}) => (s) => s.switchMap((item) => {
    if (item) return of(item);
    if (itemID === 'new') return items.take(1).map((items) => Object.assign(itemFactory(), {name: uniqueName(defaultName, items)}));
    if (!itemID) return of(null);
    return empty();
});
const currentShortItems = ({changesKey, shortKey}) => (state$) => {
    return Observable.combineLatest(
        state$.pluck('edit', 'changes', changesKey).let(isDefined).distinctUntilChanged(),
        state$.pluck(shortKey, 'value').let(isDefined).distinctUntilChanged()
    )
        .map(([{ids, changedItems}, shortItems]) => {
            if (!ids.length || !shortItems) return [];
            return ids.map((id) => changedItems.find(({_id}) => _id === id) || shortItems.get(id));
            // return ids.map((id) => shortItems.get(id) || changedItems.find(({_id}) => _id === id));
        })
        .map((v) => v.filter((v) => v));
};

export default class ConfigSelectors {
    static $inject = ['Caches', 'Clusters', 'IGFSs'];
    constructor(Caches, Clusters, IGFSs) {
        Object.assign(this, {Caches, Clusters, IGFSs});
    }
    selectCluster = (id) => selectMapItem('clusters', id);
    selectShortClusters = () => selectItems('shortClusters');
    selectShortClustersValue = () => (state$) => state$.let(this.selectShortClusters()).let(selectValues);
    selectCache = (id) => selectMapItem('caches', id);
    selectIGFS = (id) => selectMapItem('igfss', id);
    selectShortCaches = () => selectItems('shortCaches');
    selectShortCachesValue = () => (state$) => state$.let(this.selectShortCaches()).let(selectValues);
    selectShortIGFSs = () => selectItems('shortIgfss');
    selectShortIGFSsValue = () => (state$) => state$.let(this.selectShortIGFSs()).let(selectValues);
    selectCacheToEdit = (cacheID) => (state$) => state$
        .let(this.selectCache(cacheID))
        .distinctUntilChanged()
        .let(selectItemToEdit({
            items: state$.let(this.selectShortCachesValue()),
            itemFactory: () => this.Caches.getBlankCache(),
            defaultName: 'New cache',
            itemID: cacheID
        }));
    selectIGFSToEdit = (itemID) => (state$) => state$
        .let(this.selectIGFS(itemID))
        .distinctUntilChanged()
        .let(selectItemToEdit({
            items: state$.let(this.selectShortIGFSsValue()),
            itemFactory: () => this.IGFSs.getBlankIGFS(),
            defaultName: 'New IGFS',
            itemID
        }));
    selectClusterToEdit = (clusterID) => (state$) => state$
        .let(this.selectCluster(clusterID))
        .distinctUntilChanged()
        .debug('what')
        .let(selectItemToEdit({
            items: state$.let(this.selectShortClustersValue()),
            itemFactory: () => this.Clusters.getBlankCluster(),
            defaultName: 'New cluster',
            itemID: clusterID
        }));
    selectCurrentShortCaches = currentShortItems({changesKey: 'caches', shortKey: 'shortCaches'});
    selectCurrentShortIGFSs = currentShortItems({changesKey: 'igfss', shortKey: 'shortIgfss'});
    selectShortModels = () => selectItems('shortModels');
    selectShortModelsValue = () => (state$) => state$.let(this.selectShortModels()).let(selectValues);
}
