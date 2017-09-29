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

import {combineLatest} from 'rxjs/observable/combineLatest';
import difference from 'lodash/difference';

export const LOAD_LIST = Symbol('LOAD_LIST');
export const ADD_CLUSTER = Symbol('ADD_CLUSTER');
export const ADD_CLUSTERS = Symbol('ADD_CLUSTERS');
export const REMOVE_CLUSTERS = Symbol('REMOVE_CLUSTERS');
export const UPDATE_CLUSTER = Symbol('UPDATE_CLUSTER');
export const UPSERT_CLUSTERS = Symbol('UPSERT_CLUSTERS');
export const ADD_CACHE = Symbol('ADD_CACHE');
export const UPDATE_CACHE = Symbol('UPDATE_CACHE');
export const UPSERT_CACHES = Symbol('UPSERT_CACHES');
export const REMOVE_CACHE = Symbol('REMOVE_CACHE');

const defaults = {clusters: new Map(), caches: new Map(), spaces: new Map()};
const mapByID = (items) => {
    return Array.isArray(items) ? new Map(items.map((item) => [item._id, item])) : new Map(items);
};
import cloneDeep from 'lodash/cloneDeep';

export const uniqueName = (name, items, fn = ({name, i}) => `${name} (${i})`) => {
    let i = 0;
    let newName = name;
    const isUnique = (item) => item.name === newName;
    while (items.some(isUnique)) {
        i += 1;
        newName = fn({name, i});
    }
    return newName;
};

export const reducer = (state = defaults, action) => {
    switch (action.type) {
        case LOAD_LIST: {
            return {
                clusters: mapByID(action.list.clusters),
                domains: mapByID(action.list.domains),
                caches: mapByID(action.list.caches),
                spaces: mapByID(action.list.spaces),
                plugins: mapByID(action.list.plugins)
            };
        }
        case ADD_CLUSTER: {
            return Object.assign({}, state, {
                clusters: new Map([...state.clusters.entries(), [action.cluster._id, action.cluster]])
            });
        }
        case ADD_CLUSTERS: {
            return Object.assign({}, state, {
                clusters: new Map([...state.clusters.entries(), ...action.clusters.map((c) => [c._id, c])])
            });
        }
        case REMOVE_CLUSTERS: {
            return Object.assign({}, state, {
                clusters: new Map([...state.clusters.entries()].filter(([id, value]) => !action.clusterIDs.includes(id)))
            });
        }
        case UPDATE_CLUSTER: {
            const id = action._id || action.cluster._id;
            return Object.assign({}, state, {
                // clusters: new Map(state.clusters).set(id, Object.assign({}, state.clusters.get(id), action.cluster))
                clusters: new Map(Array.from(state.clusters.entries()).map(([_id, cluster]) => {
                    return _id === id
                        ? [action.cluster._id || _id, Object.assign({}, cluster, action.cluster)]
                        : [_id, cluster];
                }))
            });
        }
        case UPSERT_CLUSTERS: {
            return action.clusters.reduce((state, cluster) => reducer(state, {
                type: state.clusters.has(cluster._id) ? UPDATE_CLUSTER : ADD_CLUSTER,
                cluster
            }), state);
        }
        case ADD_CACHE: {
            return Object.assign({}, state, {
                caches: new Map([...state.caches.entries(), [action.cache._id, action.cache]])
            });
        }
        case UPDATE_CACHE: {
            const id = action.cache._id;
            return Object.assign({}, state, {
                caches: new Map(state.caches).set(id, Object.assign({}, state.caches.get(id), action.cache))
            });
        }
        case UPSERT_CACHES: {
            return action.caches.reduce((state, cache) => reducer(state, {
                type: state.caches.has(cache._id) ? UPDATE_CACHE : ADD_CACHE,
                cache
            }), state);
        }
        case REMOVE_CACHE:
            return state;
        default:
            return state;
    }
};


export const RECEIVE_CLUSTER_EDIT = Symbol('RECEIVE_CLUSTER_EDIT');
export const RECEIVE_CACHE_EDIT = Symbol('RECEIVE_CACHE_EDIT');
export const RECEIVE_IGFSS_EDIT = Symbol('RECEIVE_IGFSS_EDIT');
export const RECEIVE_IGFS_EDIT = Symbol('RECEIVE_IGFS_EDIT');
export const RECEIVE_MODELS_EDIT = Symbol('RECEIVE_MODELS_EDIT');
export const RECEIVE_MODEL_EDIT = Symbol('RECEIVE_MODEL_EDIT');

export const editReducer = (state = {originalCluster: null}, action) => {
    switch (action.type) {
        case RECEIVE_CLUSTER_EDIT:
            return {
                ...state,
                originalCluster: action.cluster
            };
        case RECEIVE_CACHE_EDIT: {
            return {
                ...state,
                originalCache: action.cache
            };
        }
        case RECEIVE_IGFSS_EDIT:
            return {
                ...state,
                originalIGFSs: action.igfss
            };
        case RECEIVE_IGFS_EDIT: {
            return {
                ...state,
                originalIGFS: action.igfs
            };
        }
        case RECEIVE_MODELS_EDIT:
            return {
                ...state,
                originalModels: action.models
            };
        case RECEIVE_MODEL_EDIT: {
            return {
                ...state,
                originalModel: action.model
            };
        }
        default:
            return state;
    }
};

export const SHOW_CONFIG_LOADING = Symbol('SHOW_CONFIG_LOADING');
export const HIDE_CONFIG_LOADING = Symbol('HIDE_CONFIG_LOADING');
const loadingDefaults = {isLoading: false, loadingText: 'Loading...'};

export const loadingReducer = (state = loadingDefaults, action) => {
    switch (action.type) {
        case SHOW_CONFIG_LOADING:
            return {...state, isLoading: true, loadingText: action.loadingText};
        case HIDE_CONFIG_LOADING:
            return {...state, isLoading: false};
        default:
            return state;
    }
};

export const setStoreReducerFactory = (actionTypes) => (state = new Set(), action = {}) => {
    switch (action.type) {
        case actionTypes.SET:
            return action.state;
        case actionTypes.RESET:
            return new Set();
        case actionTypes.UPSERT:
            return action.items.reduce((acc, item) => {acc.add(item._id); return acc;}, new Set(state));
        case actionTypes.REMOVE:
            return action.items.reduce((acc, item) => {acc.delete(item); return acc;}, new Set(state));
        default:
            return state;
    }
};

export const mapStoreReducerFactory = (actionTypes) => (state = new Map(), action = {}) => {
    switch (action.type) {
        case actionTypes.SET:
            return action.state;
        case actionTypes.RESET:
            return new Map();
        case actionTypes.UPSERT:
            if (!action.items.length) return state;
            return action.items.reduce((acc, item) => {acc.set(item._id, item); return acc;}, new Map(state));
        case actionTypes.REMOVE:
            if (!action.ids.length) return state;
            return action.ids.reduce((acc, id) => {acc.delete(id); return acc;}, new Map(state));
        default:
            return state;
    }
};

export const mapCacheReducerFactory = (actionTypes) => {
    const mapStoreReducer = mapStoreReducerFactory(actionTypes);
    return (state = {value: mapStoreReducer(), pristine: true}, action) => {
        switch (action.type) {
            case actionTypes.SET:
                return action.state;
            case actionTypes.REMOVE:
            case actionTypes.UPSERT:
                return {
                    value: mapStoreReducer(state.value, action),
                    pristine: false
                };
            case actionTypes.RESET:
                return {
                    value: mapStoreReducer(state.value, action),
                    pristine: true
                };
            default:
                return state;
        }
    };
};

export const basicCachesActionTypes = {
    SET: 'SET_BASIC_CACHES',
    RESET: 'RESET_BASIC_CACHES',
    LOAD: 'LOAD_BASIC_CACHES',
    UPSERT: 'UPSERT_BASIC_CACHES',
    REMOVE: 'REMOVE_BASIC_CACHES'
};

export const mapStoreActionTypesFactory = (NAME) => ({
    SET: `SET_${NAME}`,
    RESET: `RESET_${NAME}`,
    UPSERT: `UPSERT_${NAME}`,
    REMOVE: `REMOVE_${NAME}`
});

export const clustersActionTypes = mapStoreActionTypesFactory('CLUSTERS');
export const shortClustersActionTypes = mapStoreActionTypesFactory('SHORT_CLUSTERS');
export const cachesActionTypes = mapStoreActionTypesFactory('CACHES');
export const shortCachesActionTypes = mapStoreActionTypesFactory('SHORT_CACHES');
export const modelsActionTypes = mapStoreActionTypesFactory('MODELS');
export const shortModelsActionTypes = mapStoreActionTypesFactory('SHORT_MODELS');
export const igfssActionTypes = mapStoreActionTypesFactory('IGFSS');
export const shortIGFSsActionTypes = mapStoreActionTypesFactory('SHORT_IGFSS');

export const itemsEditReducerFactory = (actionTypes) => {
    const setStoreReducer = setStoreReducerFactory(actionTypes);
    const mapStoreReducer = mapStoreReducerFactory(actionTypes);
    return (state = {ids: setStoreReducer(), changedItems: mapStoreReducer()}, action) => {
        switch (action.type) {
            case actionTypes.SET:
                return action.state;
            case actionTypes.LOAD:
                return {
                    ...state,
                    ids: setStoreReducer(state.ids, {...action, type: actionTypes.UPSERT})
                };
            case actionTypes.RESET:
            case actionTypes.UPSERT:
                return {
                    ids: setStoreReducer(state.ids, action),
                    changedItems: mapStoreReducer(state.changedItems, action)
                };
            case actionTypes.REMOVE:
                return {
                    ids: setStoreReducer(state.ids, {type: action.type, items: action.ids}),
                    changedItems: mapStoreReducer(state.changedItems, action)
                };
            default:
                return state;
        }
    };
};

export const editReducer2 = (state = editReducer2.getDefaults(), action) => {
    switch (action.type) {
        case 'SET_EDIT':
            return action.state;
        case 'EDIT_CLUSTER': {
            return {
                ...state,
                changes: {
                    ...['caches', 'models', 'igfss'].reduce((a, t) => ({
                        ...a,
                        [t]: {
                            ids: action.cluster ? action.cluster[t] || [] : [],
                            changedItems: []
                        }
                    }), state.changes),
                    cluster: action.cluster
                }
            };
        }
        case 'RESET_EDIT_CHANGES': {
            return {
                ...state,
                changes: {
                    ...['caches', 'models', 'igfss'].reduce((a, t) => ({
                        ...a,
                        [t]: {
                            ids: state.changes.cluster ? state.changes.cluster[t] || [] : [],
                            changedItems: []
                        }
                    }), state.changes),
                    cluster: {...state.changes.cluster}
                }
            };
        }
        case 'UPSERT_CLUSTER': {
            return {
                ...state,
                changes: {
                    ...state.changes,
                    cluster: action.cluster
                }
            };
        }
        case 'UPSERT_CLUSTER_ITEM': {
            const {itemType, item} = action;
            return {
                ...state,
                changes: {
                    ...state.changes,
                    [itemType]: {
                        ids: state.changes[itemType].ids.filter((_id) => _id !== item._id).concat(item._id),
                        changedItems: state.changes[itemType].changedItems.filter(({_id}) => _id !== item._id).concat(item)
                    }
                }
            };
        }
        case 'REMOVE_CLUSTER_ITEMS': {
            const {itemType, itemIDs} = action;
            return {
                ...state,
                changes: {
                    ...state.changes,
                    [itemType]: {
                        ids: state.changes[itemType].ids.filter((_id) => !itemIDs.includes(_id)),
                        changedItems: state.changes[itemType].changedItems.filter(({_id}) => !itemIDs.includes(_id))
                    }
                }
            };
        }
        default: return state;
    }
};
editReducer2.getDefaults = () => ({
    changes: ['caches', 'models', 'igfss'].reduce((a, t) => ({...a, [t]: {ids: [], changedItems: []}}), {cluster: null})
});

export const refsReducer = (state, action) => {
    switch (action.type) {
        case 'ADVANCED_SAVE_COMPLETE_CONFIGURATION': {
            console.time('refs advanced save');
            let val = state;
            if (state && state.models && state.clusters && state.caches.size) {
                const newCluster = action.changedItems.cluster;
                const oldCluster = state.clusters.get(newCluster._id) || {caches: [], models: []};
                const addedModels = new Set(difference(newCluster.models, oldCluster.models));
                const removedModels = new Set(difference(oldCluster.models, newCluster.models));
                const changedModels = new Map(action.changedItems.models.map((m) => [m._id, m]));

                const caches = new Map();
                const maybeAddCache = (id) => {
                    if (!caches.has(id)) caches.set(id, {models: {add: new Set(), remove: new Set()}});
                    return caches.get(id);
                };

                [...state.caches.values()].forEach((cache) => {
                    cache.domains
                    .filter((modelID) => removedModels.has(modelID))
                    .forEach((modelID) => maybeAddCache(cache._id).models.remove.add(modelID));
                });
                [...addedModels.values()].forEach((modelID) => {
                    const model = changedModels.get(modelID);
                    model.caches.forEach((cacheID) => {
                        maybeAddCache(cacheID).models.add.add(modelID);
                    });
                });
                action.changedItems.models.filter((m) => !addedModels.has(m._id)).forEach((model) => {
                    const newModel = model;
                    const oldModel = state.models.get(model._id);
                    const addedCaches = difference(newModel.caches, oldModel.caches);
                    const removedCaches = difference(oldModel.caches, newModel.caches);
                    addedCaches.forEach((cacheID) => {
                        maybeAddCache(cacheID).models.add.add(model._id);
                    });
                    removedCaches.forEach((cacheID) => {
                        maybeAddCache(cacheID).models.remove.add(model._id);
                    });
                });
                const result = [...caches.entries()]
                    .filter(([cacheID]) => state.caches.has(cacheID))
                    .map(([cacheID, changes]) => {
                        const cache = state.caches.get(cacheID);
                        return [
                            cacheID,
                            {
                                ...cache,
                                domains: cache.domains
                                    .filter((modelID) => !changes.models.remove.has(modelID))
                                    .concat([...changes.models.add.values()])
                            }
                        ];
                    });

                if (result.length) {
                    val = {
                        ...state,
                        caches: new Map([...state.caches.entries()].concat(result))
                    };
                }

                console.debug('added', addedModels, 'removed', removedModels, 'changed', changedModels);
                console.debug('val', val);
            }
            console.timeEnd('refs advanced save');
            return val;
        }
        default:
            return state;
    }
};
