import { Role, Selector, ClientFunction   } from 'testcafe';
import { AngularJSSelector } from 'testcafe-angular-selectors';

const IgniteUser = Role('http://localhost:9000/', async t => {
    await t
        .click('#signin_show')
        .typeText(AngularJSSelector.byModel('ui.email'), 'test@test.com')
        .typeText(AngularJSSelector.byModel('ui.password'), '12345')
        .click('#signin_submit');

    // close modal window
    await t.click('.modal-header button.close');
});

const mouseenterTrigger = ClientFunction((selector = '') => {
    return new Promise(resolve => {
        window.jQuery(selector).mouseenter();
        resolve();
    });
});

fixture `Checking main menu`
    .page `http://localhost:9000/`;

test('Main menu smoke test', async t => {
    await t.setNativeDialogHandler(() => true);
    await t.useRole(IgniteUser);

    await t
        .click(Selector('a').withAttribute('ui-sref','base.configuration.tabs'))
        .expect(Selector('title').innerText).eql('Configure Clusters – Apache Ignite Web Console');

    await mouseenterTrigger('span:contains(Queries)');
    await t
        .click(Selector('a').withText('Create new notebook'))
        .expect(Selector('h4').withText('New query notebook').exists).ok();
    await t.click('.modal-header button.close');

    await mouseenterTrigger('span:contains(Monitoring)');
    await t
        .click(Selector('a').withText('Dashboard'))
        .expect(Selector('title').innerText).eql('Monitoring dashboard – Apache Ignite Web Console');

    await mouseenterTrigger('span:contains(Monitoring)');
    await t
        .click(Selector('a').withText('Queries history'))
        .expect(Selector('title').innerText).eql('Queries monitoring – Apache Ignite Web Console');

    await mouseenterTrigger('span:contains(Monitoring)');
    await t
        .click(Selector('a').withText('Running queries'))
        .expect(Selector('title').innerText).eql('Running queries monitoring – Apache Ignite Web Console');

    await t
        .click(Selector('a').withAttribute('ui-sref','base.snapshots'))
        .expect(Selector('title').innerText).eql('Disk storage snapshots – Apache Ignite Web Console');
});
