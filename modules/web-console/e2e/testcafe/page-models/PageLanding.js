const {Selector, t} = require('testcafe');
const {AngularJSSelector} = require('testcafe-angular-selectors');

class PageLanding {
    async openPage() {
        await t
            .navigateTo('http://localhost:9001/');
        this.inputLoginEmail = AngularJSSelector.byModel('ui.email');
        this.inputLoginPassword = AngularJSSelector.byModel('ui.password');
        this.signinButton = Selector('#login');
    }

    async login(email, password) {
        return await t
            .typeText(this.inputLoginEmail, email)
            .typeText(this.inputLoginPassword, password)
            .click(this.signinButton);
    }
}

module.exports = {PageLanding};
