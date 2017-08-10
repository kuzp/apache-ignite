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

export default class AuthSrv {
    static $inject = ['$http', '$rootScope', '$state', '$window', 'IgniteErrorPopover', 'IgniteMessages', 'gettingStarted', 'User'];

    constructor($http, $root, $state, $window, ErrorPopover, Messages, gettingStarted, User) {
        Object.assign(this, {$http, $root, $state, $window, ErrorPopover, Messages, gettingStarted, User});
    }

    _errorPopover(err, id) {
        return this.ErrorPopover.show(id, this.Messages.errorMessage(null, err));
    }

    _afterLogin() {
        return this.User.read()
            .then((user) => {
                this.$root.$broadcast('user', user);

                this.$state.go('base.configuration.tabs');

                this.gettingStarted.tryShow();
            });
    }

    forgotPassword(userInfo) {
        this.$http.post('/api/v1/password/forgot', userInfo)
            .then(() => this.$state.go('password.send'))
            .catch((err) => this._errorPopover('forgot_email', err));
    }

    signup(userInfo) {
        this.$http.post('/api/v1/signup', userInfo)
            .then(() => this._afterLogin())
            .catch((err) => this._errorPopover('signup_email', err));
    }

    signin(userInfo) {
        this.$http.post('/api/v1/signin', userInfo)
            .then(() => this._afterLogin())
            .catch((err) => this._errorPopover('signin_email', err));
    }

    acceptInvite(invite) {
        const userInfo = {
            email: invite.email
        };

        this.$http.post('/api/v1/invite/accept', userInfo)
            .then(() => this._afterLogin())
            .catch(this.Messages.showError);
    }

    logout() {
        this.$http.post('/api/v1/logout')
            .then(() => {
                this.User.clean();

                this.$window.open(this.$state.href('signin'), '_self');
            })
            .catch(this.Messages.showError);
    }
}
