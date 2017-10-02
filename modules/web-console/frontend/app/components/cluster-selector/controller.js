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

import 'rxjs/add/operator/startWith';
import map from 'lodash/fp/map';

export default class {
    static $inject = ['AgentManager', 'IgniteConfirm'];

    constructor(agentMgr, Confirm) {
        Object.assign(this, { agentMgr, Confirm });

        this.clusters = [];

        const _map = map((cluster) => {
            cluster.label = `Cluster ${_.id8(cluster.id)}`;
            cluster.active = false;

            return cluster;
        });

        this.clusters$ = this.agentMgr
            .connectionSbj
            .startWith({ clusters: [] })
            .do(({ clusters }) => {
                const removed = _.differenceBy(this.clusters, clusters, 'id');

                if (_.nonEmpty(removed))
                    _.pullAll(this.clusters, removed);
            })
            .do(({ clusters }) => {
                const added = _.differenceBy(clusters, this.clusters, 'id');

                this.clusters.push(..._map(added));
            })
            .do(({ cluster }) => {
                if (cluster)
                    this.cluster = _.find(this.clusters, {id: cluster.id});
            })
            .map(() => {
                return this.clusters;
            });
    }

    $onInit() {
        this.clusters$.subscribe(() => {});
    }

    $onDestroy() {
        this.clusters$.unsubscribe();
    }

    change() {
        this.agentMgr.saveToStorage(this.cluster);
    }

    toggle($event) {
        const toggleClusterState = () => {
            this.cluster.$inProgress = true;

            this.agentMgr.toggleClusterState()
                .then(() => this.cluster.active = !this.cluster.active)
                .finally(() => this.cluster.$inProgress = false);
        };

        if (this.cluster.active) {
            $event.preventDefault();

            this.Confirm.confirm('Are you sure you want to deactivate cluster?')
                .then(() => toggleClusterState());
        } else
            toggleClusterState();
    }
}
