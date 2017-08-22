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

// Sign in controller.
export default class SigninCtrl {
    static $inject = ['$uiRouterGlobals', 'IgniteFocus', 'IgniteCountries', 'Auth', 'Invites', 'IgniteMessages'];

    constructor($uiRouterGlobals, Focus, Countries, Auth, Invites, Messages) {
        const self = this;

        self.Auth = Auth;
        self.countries = Countries.getAll();
        self.action = 'signin';
        self.showSignIn = false;
        self.ui_signin = {};
        self.ui_signup = {};
        self.ui_exclude = {};
        self.invite = {};

        self.invite.token = _.get($uiRouterGlobals.params, 'invite');

        self.invited = _.nonEmpty(self.invite.token);

        if (self.invited) {
            Invites.find(self.invite.token)
                .then((res) => {
                    self.invite.found = res.data.found;

                    if (self.invite.found) {
                        self.invite.organization = res.data.organization.name;
                        self.invite.existingUser = res.data.existingUser;
                        self.invite.email = res.data.email;

                        Focus.move(self.invite.existingUser ? 'signin_user_password' : 'signup_user_password');
                    }

                    self.showSignIn = true;
                })
                .catch((err) => Messages.showError('Failed to find invite: ', err));
        }
    }


    signup() {
        this.Auth.signup(this.ui_signup);
    }

    signin() {
        this.Auth.signin(this.ui_signin);
    }

    acceptInvite() {
        this.Auth.acceptInvite(_.merge(this.invite, this.invite.existingUser ? this.ui_signin : this.ui_signup));
    }

    forgotPassword() {
        this.Auth.forgotPassword(this.ui_signin);
    }
}
