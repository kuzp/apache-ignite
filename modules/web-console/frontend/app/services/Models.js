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

import ObjectID from 'bson-objectid';
import omit from 'lodash/fp/omit';

export default class Models {
    static $inject = ['$http'];

    constructor($http) {
        Object.assign(this, {$http});
    }

    getModel(modelID) {
        return this.$http.get(`/api/v1/configuration/domains/${modelID}`);
    }

    getBlankModel() {
        return {
            _id: ObjectID.generate(),
            generatePojo: true,
            caches: [],
            queryMetadata: 'Configuration'
        };
    }

    queryMetadata = {
        values: [
            {label: 'Annotations', value: 'Annotations'},
            {label: 'Configuration', value: 'Configuration'}
        ]
    };

    indexType = {
        values: [
            {label: 'SORTED', value: 'SORTED'},
            {label: 'FULLTEXT', value: 'FULLTEXT'},
            {label: 'GEOSPATIAL', value: 'GEOSPATIAL'}
        ]
    };

    indexSortDirection = {
        values: [
            {value: true, label: 'ASC'},
            {value: false, label: 'DESC'}
        ],
        default: true
    };

    normalize = omit(['__v', 'space']);

    addIndexField(fields) {
        return fields[fields.push({direction: true}) - 1];
    }
}
