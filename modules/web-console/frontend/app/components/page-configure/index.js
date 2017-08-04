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

import {Visualizer} from '@uirouter/visualizer';

import component from './component';
import ConfigureState from './services/ConfigureState';
import PageConfigure from './services/PageConfigure';
import ConfigurationDownload from './services/ConfigurationDownload';
import projectStructurePreview from './components/pc-project-structure-preview';
import itemsTable from './components/pc-items-table';
import pcUiGridFilters from './components/pc-ui-grid-filters';
import pcFormFieldSize from './components/pc-form-field-size';

import 'rxjs/add/operator/withLatestFrom';
import 'rxjs/add/operator/skip';

import {reducer, editReducer, loadingReducer} from './reducer';
import {reducer as reduxDevtoolsReducer, devTools} from './reduxDevtoolsIntegration';

export default angular
    .module('ignite-console.page-configure', [
        pcFormFieldSize.name,
        pcUiGridFilters.name,
        projectStructurePreview.name,
        itemsTable.name
    ])
    .run(['ConfigureState', '$uiRouter', (ConfigureState, $uiRouter) => {
        // $uiRouter.plugin(Visualizer);
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
        ConfigureState.addReducer((state, action) => Object.assign(state, {
            list: reducer(state.list, action),
            clusterConfiguration: editReducer(state.clusterConfiguration, action),
            configurationLoading: loadingReducer(state.configurationLoading, action)
        }));
    }])
    .component('pageConfigure', component)
    .service('PageConfigure', PageConfigure)
    .service('ConfigureState', ConfigureState)
    .service('ConfigurationDownload', ConfigurationDownload);
