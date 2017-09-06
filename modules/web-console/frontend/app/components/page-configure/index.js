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

import angular from 'angular';

import {UIRouterRx} from '@uirouter/rx';
import {Visualizer} from '@uirouter/visualizer';
import uiValidate from 'angular-ui-validate';

import component from './component';
import ConfigureState from './services/ConfigureState';
import PageConfigure from './services/PageConfigure';
import ConfigurationDownload from './services/ConfigurationDownload';
import projectStructurePreview from './components/pc-project-structure-preview';
import itemsTable from './components/pc-items-table';
import pcUiGridFilters from './components/pc-ui-grid-filters';
import pcFormFieldSize from './components/pc-form-field-size';
import pcListEditable from './components/pc-list-editable';
import isInCollection from './components/pcIsInCollection';
import pcValidation from './components/pcValidation';

import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/skip';

import {
    editReducer2,
    reducer,
    editReducer,
    loadingReducer,
    itemsEditReducerFactory,
    mapStoreReducerFactory,
    mapCacheReducerFactory,
    basicCachesActionTypes,
    clustersActionTypes,
    shortClustersActionTypes,
    cachesActionTypes,
    shortCachesActionTypes,
    modelsActionTypes,
    shortModelsActionTypes,
    igfssActionTypes,
    shortIGFSsActionTypes
} from './reducer';
import {reducer as reduxDevtoolsReducer, devTools} from './reduxDevtoolsIntegration';

export default angular
    .module('ignite-console.page-configure', [
        uiValidate,
        pcFormFieldSize.name,
        pcUiGridFilters.name,
        pcListEditable.name,
        projectStructurePreview.name,
        itemsTable.name,
        pcValidation.name
    ])
    .run(['ConfigureState', '$uiRouter', (ConfigureState, $uiRouter) => {
        $uiRouter.plugin(UIRouterRx);
        $uiRouter.plugin(Visualizer);
        if (devTools) {
            devTools.subscribe((e) => {
                if (e.type === 'DISPATCH' && e.state) ConfigureState.actions$.next(e);
            });

            ConfigureState.actions$
            .filter((e) => e.type !== 'DISPATCH')
            .withLatestFrom(ConfigureState.state$.skip(1))
            .subscribe(([action, state]) => devTools.send(action, state));

            ConfigureState.addReducer(reduxDevtoolsReducer);
        }
        ConfigureState.addReducer((state, action) => Object.assign({}, state, {
            clusterConfiguration: editReducer(state.clusterConfiguration, action),
            configurationLoading: loadingReducer(state.configurationLoading, action),
            basicCaches: itemsEditReducerFactory(basicCachesActionTypes)(state.basicCaches, action),
            clusters: mapStoreReducerFactory(clustersActionTypes)(state.clusters, action),
            shortClusters: mapCacheReducerFactory(shortClustersActionTypes)(state.shortClusters, action),
            caches: mapStoreReducerFactory(cachesActionTypes)(state.caches, action),
            shortCaches: mapCacheReducerFactory(shortCachesActionTypes)(state.shortCaches, action),
            models: mapStoreReducerFactory(modelsActionTypes)(state.models, action),
            shortModels: mapStoreReducerFactory(shortModelsActionTypes)(state.shortModels, action),
            igfss: mapStoreReducerFactory(igfssActionTypes)(state.igfss, action),
            shortIgfss: mapCacheReducerFactory(shortIGFSsActionTypes)(state.shortIgfss, action),
            edit: editReducer2(state.edit, action)
        }));
    }])
    .component('pageConfigure', component)
    .directive(isInCollection.name, isInCollection)
    .service('PageConfigure', PageConfigure)
    .service('ConfigureState', ConfigureState)
    .service('ConfigurationDownload', ConfigurationDownload);
