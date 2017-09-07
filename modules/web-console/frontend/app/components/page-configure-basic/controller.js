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

import 'rxjs/add/operator/map';
import cloneDeep from 'lodash/cloneDeep';

export default class PageConfigureBasicController {
    static $inject = [
        'Clusters', 'Caches', 'IgniteVersion', '$element', 'ConfigChangesGuard', 'IgniteFormUtils', '$scope'
    ];

    constructor(Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope) {
        Object.assign(this, {Clusters, Caches, IgniteVersion, $element, ConfigChangesGuard, IgniteFormUtils, $scope});
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    uiCanExit() {
        if (this.form.$invalid) {
            this.IgniteFormUtils.triggerValidation(this.form, this.$scope);
            return false;
        }
        return this.ConfigChangesGuard.guard({cluster: this.clonedCluster});
    }

    $onInit() {
        this.memorySizeInputVisible$ = this.IgniteVersion.currentSbj
            .map((version) => this.IgniteVersion.since(version.ignite, '2.0.0'));

        this.formActionsMenu = [
            {
                text: 'Save changes and download project',
                click: () => this.save(true),
                icon: 'download'
            },
            {
                text: 'Save changes',
                click: () => this.save(),
                icon: 'checkmark'
            }
        ];
    }

    addCache() {
        this.onItemAdd({$event: {type: 'caches'}});
    }

    removeCache(cache) {
        this.onItemRemove({$event: {item: cache, type: 'caches'}});
    }

    changeCache(cache) {
        this.onItemChange({$event: {item: cache, type: 'caches'}});
    }

    save(andDownload = false) {
        if (this.form.$invalid) return this.IgniteFormUtils.triggerValidation(this.form, this.$scope);
        this.onBasicSave({$event: {andDownload, cluster: cloneDeep(this.clonedCluster)}});
    }

    $onChanges(changes) {
        if ('originalCluster' in changes) {
            this.clonedCluster = cloneDeep(changes.originalCluster.currentValue);
            this.defaultMemoryPolicy = this.Clusters.getDefaultClusterMemoryPolicy(this.clonedCluster);
        }
    }
}
