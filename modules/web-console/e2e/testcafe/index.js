const glob = require('glob');
const argv = require('minimist')(process.argv.slice(2));
const { startTestcafe } = require('./testcafe');

const enableEnvironment = argv.env;

// See all supported browsers at http://devexpress.github.io/testcafe/documentation/using-testcafe/common-concepts/browsers/browser-support.html#locally-installed-browsers
const BROWSERS = ['chromium:headless --no-sandbox']; // For example: ['chrome', 'firefox'];

const FIXTURES_PATHS = glob.sync('./fixtures/*.js');

// Set auth and session stuff to globals to allow extension.
const { signUp, signIn } = require('./roles');
global.signUp = signUp;
global.signIn = signIn;

const testcafeRunnerConfig = {
    browsers: BROWSERS,
    enableEnvironment: enableEnvironment || false,
    reporter: 'spec',
    fixturesPathsArray: FIXTURES_PATHS
};

startTestcafe(testcafeRunnerConfig);
