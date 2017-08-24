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
// import Invites from "../../frontend/app/services/Invites";

// Fire me up!

module.exports = {
    implements: 'routes/public',
    inject: ['require(lodash)', 'require(express)', 'require(passport)', 'mongo', 'services/mails', 'services/users', 'services/auth']
};

/**
 * @param _
 * @param express
 * @param passport
 * @param mongo
 * @param mailsService
 * @param {UsersService} usersService
 * @param {AuthService} authService
 * @returns {Promise}
 */
module.exports.factory = function(_, express, passport, mongo, mailsService, usersService, authService) {
    return new Promise((factoryResolve) => {
        const router = new express.Router();

        // GET user.
        router.post('/user', (req, res) => {
            usersService.get(req.user, req.session.viewedUser)
                .then(res.api.ok)
                .catch(res.api.error);
        });

        const _signup = (req, res, user) => {
            return usersService.create(req.origin(), user)
                .then((user) => new Promise((resolve, reject) => {
                    req.logIn(user, {}, (err) => {
                        if (err)
                            reject(err);

                        resolve(user);
                    });
                }))
                .then(res.api.ok)
                .catch(res.api.error);
        };

        const _signin = (req, res, next, cb) => {
            passport.authenticate('local', (errAuth, user) => {
                if (errAuth)
                    return res.status(401).send(errAuth.message);

                if (!user)
                    return res.status(401).send('Invalid email or password');

                req.logIn(user, {}, (errLogIn) => {
                    if (errLogIn)
                        return res.status(401).send(errLogIn.message);

                    if (cb)
                        return cb(req, res);

                    return res.sendStatus(200);
                });
            })(req, res, next);
        };

        /**
         * Register new account.
         */
        router.post('/signup', (req, res) => _signup(req, res, req.body));

        /**
         * Sign in into exist account.
         */
        router.post('/signin', (req, res, next) => _signin(req, res, next));

        // Find invite and return data.
        router.post('/invites/find', (req, res) => {
            mongo.Invite.findOne({token: req.body.token}).exec()
                .then((invite) => {
                    if (invite) {
                        mongo.Organization.findOne({_id: invite.organization})
                            .then((organization) => {
                                res.api.ok({
                                    organization,
                                    email: invite.email,
                                    existingUser: !_.isNil(invite.account),
                                    found: true
                                });
                            });
                    }
                    else
                        res.api.ok({found: false});
                })
                .catch(res.api.error);
        });

        /**
         * Accept invite and signup if needed.
         */
        router.post('/invites/accept', (req, res, next) => {
            const data = req.body;
            const token = data.token;

            mongo.Invite.findOne({token}).exec()
                .then((invite) => {
                    if (invite) {
                        mongo.Invite.remove({_id: invite._id}).exec().then(() => {
                            if (data.existingUser) {
                                return mongo.Account.update({email: data.email}, {$set: {organization: data.organization._id}}).exec()
                                    .then(() => _signin(req, res, next));
                            }

                            const user = {
                                email: data.email,
                                password: data.password,
                                firstName: data.firstName,
                                lastName: data.lastName,
                                company: data.organization.name,
                                country: data.country,
                                organization: data.organization._id
                            };

                            return _signup(req, res, user);
                        });
                    }
                    else
                        throw new Error(`Failed to accept invite, token not found: ${token}`);
                })
                .catch((err) => console.log(err));
        });

        /**
         * Logout.
         */
        router.post('/logout', (req, res) => {
            req.logout();

            res.sendStatus(200);
        });

        /**
         * Send e-mail to user with reset token.
         */
        router.post('/password/forgot', (req, res) => {
            authService.resetPasswordToken(req.body.email)
                .then((user) => mailsService.emailUserResetLink(req.origin(), user))
                .then(() => 'An email has been sent with further instructions.')
                .then(res.api.ok)
                .catch(res.api.error);
        });

        /**
         * Change password with given token.
         */
        router.post('/password/reset', (req, res) => {
            const {token, password} = req.body;

            authService.resetPasswordByToken(token, password)
                .then((user) => mailsService.emailPasswordChanged(req.origin(), user))
                .then((user) => user.email)
                .then(res.api.ok)
                .then(res.api.error);
        });

        /* GET reset password page. */
        router.post('/password/validate/token', (req, res) => {
            const token = req.body.token;

            authService.validateResetToken(token)
                .then(res.api.ok)
                .catch(res.api.error);
        });

        factoryResolve(router);
    });
};
