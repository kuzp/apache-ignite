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

import {
    REMOVE_CLUSTER_ITEMS,
    REMOVE_CLUSTER_ITEMS_CONFIRMED,
    ADVANCED_SAVE_COMPLETE_CONFIGURATION,
    CONFIRM_CLUSTERS_REMOVAL,
    CONFIRM_CLUSTERS_REMOVAL_OK
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
 * @prop {string} clusterID
 * @prop {'REMOVE_CLUSTER_ITEMS_CONFIRMED'} type
 * @prop {('caches'|'igfss'|'models')} itemType
 * @prop {Array<string>} itemIDs
 */

/**
 * @param {string} clusterID
 * @param {(('caches'|'igfss'|'models'))} itemType
 * @param {Array<string>} itemIDs
 * @returns {IRemoveClusterItemsConfirmed}
 */
export const removeClusterItemsConfirmed = (clusterID, itemType, itemIDs) => ({
    type: REMOVE_CLUSTER_ITEMS_CONFIRMED,
    itemType,
    clusterID,
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

/**
 * @typedef {object} IConfirmClustersRemovalAction
 * @prop {'CONFIRM_CLUSTERS_REMOVAL'} type
 * @prop {Array<string>} clusterIDs
 */

/**
 * @param {Array<string>} clusterIDs
 * @returns {IConfirmClustersRemovalAction}
 */
export const confirmClustersRemoval = (clusterIDs) => ({
    type: CONFIRM_CLUSTERS_REMOVAL,
    clusterIDs
});

/**
 * @typedef {object} IConfirmClustersRemovalActionOK
 * @prop {'CONFIRM_CLUSTERS_REMOVAL_OK'} type
 */

/**
 * @returns {IConfirmClustersRemovalActionOK}
 */
export const confirmClustersRemovalOK = () => ({
    type: CONFIRM_CLUSTERS_REMOVAL_OK
});
