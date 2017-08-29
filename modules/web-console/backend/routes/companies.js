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

'use strict';

const express = require('express');

// Fire me up!

module.exports = {
    implements: 'routes/companies',
    inject: ['mongo', 'services/companies']
};

/**
 * @param mongo
 * @param companiesService
 * @returns {Promise}
 */
module.exports.factory = function(mongo, companiesService) {
    return new Promise((resolveFactory) => {
        const router = new express.Router();

        // Create company.
        router.put('/', (req, res) => {
            companiesService.upsert(req.currentUserId(), req.body)
                .then((saved) => res.api.ok(saved._id))
                .catch(res.api.error);
        });

        resolveFactory(router);
    });
};
