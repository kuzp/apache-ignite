import {
    REMOVE_CLUSTER_ITEMS,
    REMOVE_CLUSTER_ITEMS_CONFIRMED,
    SAVE_ADVANCED,
    ADVANCED_SAVE_COMPLETE_CONFIGURATION
} from './actionTypes';

/**
 * @typedef {object} IRemoveClusterItemsAction
 * @prop {'REMOVE_CLUSTER_ITEMS'} type
 * @prop {('caches'|'igfss'|'models')} itemType
 * @prop {string} clusterID
 * @prop {Array<string>} itemIDs
 * @prop {boolean} save
 * @prop {boolean} confirm
 */

/**
 * @param {string} clusterID
 * @param {('caches'|'igfss'|'models')} itemType
 * @param {Array<string>} itemsIDs
 * @param {boolean} [save=false]
 * @param {boolean} [confirm=true]
 * @returns {IRemoveClusterItemsAction}
 */
export const removeClusterItems = (clusterID, itemType, itemIDs, save = false, confirm = true) => ({
    type: REMOVE_CLUSTER_ITEMS,
    itemType,
    clusterID,
    itemIDs,
    save,
    confirm
});

/**
 * @typedef {object} IRemoveClusterItemsConfirmed
 * @prop {'REMOVE_CLUSTER_ITEMS_CONFIRMED'} type
 * @prop {('caches'|'igfss'|'models')} itemType
 * @prop {Array<string>} itemIDs
 */

/**
 * @param {(('caches'|'igfss'|'models'))} itemType
 * @param {Array<string>} itemIDs
 * @returns {IRemoveClusterItemsConfirmed}
 */
export const removeClusterItemsConfirmed = (itemType, itemIDs) => ({
    type: REMOVE_CLUSTER_ITEMS_CONFIRMED,
    itemType,
    itemIDs
});

const applyChangedIDs = (edit) => ({
    cluster: {
        ...edit.changes.cluster,
        caches: edit.changes.caches.ids,
        igfss: edit.changes.igfss.ids,
        models: edit.changes.models.ids
    },
    caches: edit.changes.caches.changedItems,
    igfss: edit.changes.igfss.changedItems,
    models: edit.changes.models.changedItems
});

const upsertCluster = (cluster) => ({
    type: 'UPSERT_CLUSTER',
    cluster
});

const changeItem = (type, item) => ({
    type: 'UPSERT_CLUSTER_ITEM',
    itemType: type,
    item
});

/**
 * @typedef {object} IAdvancedSaveCompleteConfigurationAction
 * @prop {'ADVANCED_SAVE_COMPLETE_CONFIGURATION'} type
 * @prop {object} changedItems
 * @prop {Array<object>} [prevActions]
 */

/**
 * @returns {IAdvancedSaveCompleteConfigurationAction}
 */
// TODO: add support for prev actions
export const advancedSaveCompleteConfiguration = (edit) => {
    return {
        type: ADVANCED_SAVE_COMPLETE_CONFIGURATION,
        changedItems: applyChangedIDs(edit)
    };
};
