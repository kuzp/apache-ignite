/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the 'License'); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Sign in controller.
export default class SigninCtrl {
    static $inject = ['$scope', '$uiRouterGlobals', 'IgniteFocus', 'IgniteCountries', 'Auth', 'Invites'];

    constructor($scope, $uiRouterGlobals, Focus, Countries, Auth, Invites) {
        const self = this;

        self.auth = Auth.auth;
        self.forgotPassword = Auth.forgotPassword;
        self.action = 'signin';
        self.countries = Countries.getAll();
        self.showSignIn = false;
        self.ui = {};
        self.ui_signup = {};
        self.invite = {};

        self.invite.token = _.get($uiRouterGlobals.params, 'invite');

        if (_.nonEmpty(self.invite.token)) {
            Invites.find(self.invite.token)
                .then((res) => {
                    self.invite.found = res.data.found;

                    if (self.invite.found) {
                        self.invite.organization = res.data.organization.name;
                        self.invite.existingUser = res.data.existingUser;
                        self.invite.email = res.data.email;
                    }

                    self.showSignIn = true;
                });
        }
    }
}
