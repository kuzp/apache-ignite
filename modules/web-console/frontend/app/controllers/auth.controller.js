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
        this.auth = Auth.auth;
        this.forgotPassword = Auth.forgotPassword;
        this.action = 'signin';
        this.countries = Countries.getAll();
        this.showSignIn = false;
        this.ui = {};
        this.ui_signup = {};
        const self = this;

        this.invite = _.get($uiRouterGlobals.params, 'invite');

        this.signInByInvite = _.nonEmpty(this.invite);

        if (this.signInByInvite) {
            Invites.find(this.invite)
                .then((res) => {
                    if (res.data.found) {
                        self.ui.email = res.data.email;
                        self.ui.company = res.data.organization.name;
                    }
                    else {
                        self.ui_signup.email = res.data.email;
                        self.ui_signup.company = res.data.organization.name;
                    }

                    self.showSignIn = true;
                });
        }
    }
}
