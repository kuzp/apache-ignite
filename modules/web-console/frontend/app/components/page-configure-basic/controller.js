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

import 'rxjs/add/operator/do';

export default class PageConfigureBasicController {
    static $inject = [
        'IgniteConfirm',
        '$scope',
        'PageConfigureBasic',
        'ConfigurationDownload',
        'IgniteVersion',
        '$state',
        '$element'
    ];

    constructor(IgniteConfirm, $scope, pageService, ConfigurationDownload, Version, $state, $element) {
        Object.assign(this, {IgniteConfirm, $scope, pageService, ConfigurationDownload, Version, $state, $element});
    }

    $postLink() {
        this.$element.addClass('panel--ignite');
    }

    $onInit() {
        this.subscription = this.pageService.getObservable()
            .do((state) => this.$scope.$applyAsync(() => Object.assign(this, state)))
            .subscribe();

        // this.removeChangesGuard = this.ConfigurationChangesGuard.install({
        //     fromState: 'base.configuration.tabs.basic',
        //     getItems: () => ([this.original])
        // })

        this.discoveries = this.pageService.clusterDiscoveries;
        this.minMemorySize = this.pageService.minMemoryPolicySize;

        this.formActionsMenu = [
            {
                text: 'Save changes and download project',
                click: () => this.saveAndDownload(),
                icon: 'download'
            },
            {
                text: 'Save changes',
                click: () => this.save(),
                icon: 'checkmark'
            }
        ];
    }

    $onDestroy() {
        this.subscription.unsubscribe();
        // this.removeChangesGuard();
    }

    addCache() {
        this.pageService.addCache(this.allClusterCaches);
    }

    removeCache(cache) {
        this.pageService.removeCache(cache);
    }

    updateCache(cache) {
        this.pageService.updateCache(cache);
    }

    save() {
        this.pageService.save(this.clonedCluster);
    }

    saveAndDownload() {
        this.pageService.saveAndDownload(this.clonedCluster);
    }
}
