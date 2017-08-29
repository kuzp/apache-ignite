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

const _ = require('lodash');

// Fire me up!

module.exports = {
    implements: 'services/companies',
    inject: ['mongo', 'services/spaces', 'errors']
};

/**
 * @param mongo
 * @param {SpacesService} spaceService
 * @param errors
 * @returns {CompaniesService}
 */
module.exports.factory = (mongo, spaceService, errors) => {
    /**
     * Service for manipulate Company entities.
     */
    class CompaniesService {
        static upsert(userId, company) {
            if (_.isEmpty(company) || _.isNil(company.name))
                throw new errors.IllegalArgumentException('Company name was not specified');

            const query = _.pick(company, 'name');

            return mongo.Company.findOneAndUpdate(query, {$set: company}, {upsert: true, new: true}).exec()
                .then((savedCompany) => {
                    return mongo.Account.findById({_id: userId})
                        .then((user) => {
                            user.registeredCompany = savedCompany._id;
                            user.companyAdmin = true;

                            return user.save();
                        });
                })
                .catch((err) => {
                    if (err.code === mongo.errCodes.DUPLICATE_KEY_ERROR)
                        throw new errors.DuplicateKeyException(`Company with name: "${company.name}" already exist.`);

                    throw err;
                });
        }
    }

    return CompaniesService;
};
