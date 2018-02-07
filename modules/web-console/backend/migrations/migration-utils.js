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

function log(msg) {
    console.log(`[${new Date().toISOString()}] ${msg}`);
}

function recreateIndex0(done, model, oldIdxName, oldIdx, newIdx) {
    return model.indexExists(oldIdxName)
        .then((exists) => {
            if (exists) {
                return model.dropIndex(oldIdx)
                    .then(() => model.createIndex(newIdx, {unique: true, background: false}));
            }
        })
        .then(() => done())
        .catch((err) => {
            if (err.code === 12587) {
                console.log(`Background operation in progress for: ${oldIdxName}, will retry in 3 seconds.`);

                setTimeout(() => recreateIndex0(done, model, oldIdxName, oldIdx, newIdx), 3000);
            }
            else {
                console.log(err);

                done();
            }
        });
}

function recreateIndex(done, model, oldIdxName, oldIdx, newIdx) {
    setTimeout(() => recreateIndex0(done, model, oldIdxName, oldIdx, newIdx), 1000);
}

function getClusterForMigration(clusterModel, space) {
    const LOST_AND_FOUND = 'LOST_AND_FOUND';

    return clusterModel.findOne({name: LOST_AND_FOUND}).lean().exec()
        .then((cluster) => {
            if (!cluster) {
                return clusterModel.create({
                    space,
                    name: LOST_AND_FOUND,
                    connector: {noDelay: true},
                    communication: {tcpNoDelay: true},
                    igfss: [],
                    caches: [],
                    binaryConfiguration: {
                        compactFooter: true,
                        typeConfigurations: []
                    },
                    discovery: {
                        kind: 'Multicast',
                        Multicast: {addresses: ['127.0.0.1:47500..47510']},
                        Vm: {addresses: ['127.0.0.1:47500..47510']}
                    }
                })
                    .catch((err) => {
                        if (err.code === 11000)
                            return clusterModel.findOne({name: LOST_AND_FOUND}).lean().exec();

                        throw err;
                    });
            }

            return cluster;
        });
}

module.exports = {
    log,
    recreateIndex,
    getClusterForMigration
};




