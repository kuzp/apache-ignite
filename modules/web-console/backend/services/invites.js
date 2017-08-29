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
    implements: 'services/invites',
    inject: ['mongo', 'errors', 'services/utils']
};

/**
 * @param mongo
 * @param errors
 * @param {UtilsService} utilsService
 * @returns {InvitesService}
 */
module.exports.factory = (mongo, errors, utilsService) => {
    /**
     * Service for manipulate Invites entities.
     */
    class InvitesService {
        static create(user, email) {
            if (user.companyAdmin && user.registeredCompany)
                return Promise.reject(new errors.IllegalArgumentException('Invite user to company is not permitted for this account'));

            if (_.isEmpty(email))
                return Promise.reject(new errors.IllegalArgumentException('User e-mail was not specified!'));

            return mongo.Account.findOne({email})
                .then((account) => {
                    const invite = {
                        account: _.get(account, '_id'),
                        token: utilsService.randomString(),
                        company: user.registeredCompany,
                        email
                    };

                    return mongo.Invite.create(invite);
                });
        }
    }

    return InvitesService;
};
