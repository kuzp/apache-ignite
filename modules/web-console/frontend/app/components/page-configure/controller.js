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

import get from 'lodash/get';
import 'rxjs/add/observable/merge';
import {combineLatest} from 'rxjs/observable/combineLatest';
import {merge} from 'rxjs/observable/merge';
import 'rxjs/add/operator/distinctUntilChanged';
import {selectEditCluster, selectEditClusterItems} from 'app/components/page-configure/reducer';

export default class PageConfigureController {
    static $inject = ['$uiRouter', 'ConfigureState', 'IgniteLoading', 'conf'];

    constructor($uiRouter, ConfigureState, IgniteLoading, conf) {
        Object.assign(this, {$uiRouter, ConfigureState, IgniteLoading, conf});
    }

    $onInit() {
        this.cluster$ = this.ConfigureState.state$.let(selectEditCluster);
        this.clusterItems$ = this.ConfigureState.state$.let(selectEditClusterItems);
        this.isNew$ = this.$uiRouter.globals.params$.pluck('clusterID').map((v) => v === 'new');
        this.clusterName$ = combineLatest(this.cluster$, this.isNew$, (cluster, isNew) => {
            return `${isNew ? 'Create' : 'Edit'} cluster configuration ${isNew ? '' : `‘${get(cluster, 'name')}’`}`;
        });

        // const loading = this.ConfigureState.state$
        //     .pluck('configurationLoading')
        //     .distinctUntilChanged()
        //     .do(({loadingText, isLoading}) => {
        //         this.loadingText = loadingText;
        //         if (isLoading)
        //             this.IgniteLoading.start('configuration');
        //         else
        //             this.IgniteLoading.finish('configuration');
        //     });

        // this.subscription = merge(loading).subscribe();
        this.tooltipsVisible = true;
    }

    onBasicSave(e) {
        this.conf.saveBasic(e);
    }

    onAdvancedSave(e) {
        console.log('onAdvancedSave', e);
        this.conf.saveAdvanced(e);
    }

    onItemChange({type, item}) {
        this.conf.changeItem(type, item);
    }

    onItemAdd({type}) {
        this.conf.addItem(type);
    }

    onItemRemove({type, item}) {
        this.conf.removeItem(type, item._id);
    }

    onEditCancel() {
        this.conf.onEditCancel();
    }

    $onDestroy() {
        // this.subscription.unsubscribe();
    }
}
