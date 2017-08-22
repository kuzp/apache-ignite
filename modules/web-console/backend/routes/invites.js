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

// Fire me up!

module.exports = {
    implements: 'routes/invites',
    inject: ['require(lodash)', 'require(express)', 'mongo', 'settings', 'services/mails', 'services/utils']
};

/**
 *
 * @param _ Lodash module
 * @param express Express module
 * @param mongo
 * @param settings
 * @param {MailsService} mailsService
 * @param {UtilsService} utilsService
 * @returns {Promise}
 */
module.exports.factory = function(_, express, mongo, settings, mailsService, utilsService) {
    return new Promise((resolveFactory) => {
        const router = new express.Router();

        const _createInvite = (host, user, data) => {
            if (_.isEmpty(data.email))
                throw new Error('User e-mail was not specified!');

            return mongo.Account.findOne({email: data.email})
                .then((foundAccount) => {
                    const invite = {
                        token: utilsService.randomString(settings.tokenLength),
                        organization: data.organization,
                        email: data.email
                    };

                    if (foundAccount)
                        invite.account = foundAccount._id;

                    return mongo.Invite.create(invite)
                        .then((savedInvite) => {
                            return mailsService.emailInvite(host, user, savedInvite);
                        })
                        .catch((err) => {
                            console.log(err);
                        });
                });
        };

        // Invite user to join organization.
        router.post('/create', (req, res) => {
            _createInvite(req.origin(), {name: 'Test'}, req.body)
                .then(res.api.ok)
                .catch(res.api.error);
        });

        resolveFactory(router);
    });
};
