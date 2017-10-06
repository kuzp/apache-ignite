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

import {Subject} from 'rxjs/Subject';

// Controller for Clusters screen.
export default class PageConfigureAdvancedCluster {
    static $inject = ['$uiRouter', 'ConfigSelectors', 'ConfigureState', 'conf'];

    constructor($uiRouter, ConfigSelectors, ConfigureState, conf) {
        Object.assign(this, {$uiRouter, ConfigSelectors, ConfigureState, conf});
        this.onDestroy$ = new Subject();
    }

    $onDestroy() {
        // this.originalCluster$.complete();
        // console.debug('PageConfigureAdvancedCluster destroy$');
        this.onDestroy$.next(true);
        this.onDestroy$.complete();
    }

    $onInit() {
        const clusterID$ = this.$uiRouter.globals.params$.take(1).pluck('clusterID').filter((v) => v).take(1).debug('clusterID$');
        this.shortCaches$ = this.ConfigureState.state$.let(this.ConfigSelectors.selectShortCachesValue()).take(1)
        .debug('shortCaches$');
        this.originalCluster$ = clusterID$.distinctUntilChanged().switchMap((id) => {
            return this.ConfigureState.state$.let(this.ConfigSelectors.selectClusterToEdit(id)).debug('clusterToEdit').take(1);
        }).take(1).debug('originalCluster$');
        this.isNew$ = this.$uiRouter.globals.params$.pluck('clusterID').map((id) => id === 'new');
        this.isBlocked$ = clusterID$;
    }

    save(cluster) {
        this.conf.saveAdvanced({cluster});
    }
}
