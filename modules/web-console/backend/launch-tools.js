'use strict';

const _ = require('lodash');
const http = require('http');
const https = require('https');
const MigrateMongoose = require('migrate-mongoose');

/**
 * Event listener for HTTP server "error" event.
 */
const _onError = (addr, error) => {
    if (error.syscall !== 'listen')
        throw error;

    // Handle specific listen errors with friendly messages.
    switch (error.code) {
        case 'EACCES':
            console.error(`Requires elevated privileges for bind to ${addr}`);
            process.exit(1);

            break;
        case 'EADDRINUSE':
            console.error(`${addr} is already in use`);
            process.exit(1);

            break;
        default:
            throw error;
    }
};

/**
 * @param settings
 * @param {ApiServer} apiSrv
 * @param {AgentsHandler} agentsHnd
 * @param {BrowsersHandler} browsersHnd
 */
const init = ([settings, apiSrv, agentsHnd, browsersHnd]) => {
    // Start rest server.
    const srv = settings.server.SSLOptions ? https.createServer(settings.server.SSLOptions) : http.createServer();

    srv.listen(settings.server.port, settings.server.host);

    const addr = `${settings.server.host}:${settings.server.port}`;

    srv.on('error', _onError.bind(null, addr));
    srv.on('listening', () => console.log(`Start listening on ${addr}`));

    apiSrv.attach(srv);

    agentsHnd.attach(srv, browsersHnd);
    browsersHnd.attach(srv, agentsHnd);

    // Used for automated test.
    if (process.send)
        process.send('running');
};

/**
 * Run mongo model migration.
 *
 * @param dbConnectionUri Mongo connection url.
 * @param group Migrations group.
 * @param migrationsPath Migrations path.
 * @param collectionName Name of collection where migrations write info about applied scripts.
 */
const migrate = (dbConnectionUri, group, migrationsPath, collectionName) => {
    const migrator = new MigrateMongoose({
        migrationsPath,
        dbConnectionUri,
        collectionName,
        autosync: true
    });

    console.log(`Running ${group} migrations...`);

    return migrator.run('up')
        .then(() => console.log(`All ${group} migrations finished successfully.`))
        .catch((err) => {
            const msg = _.get(err, 'message');

            if (_.startsWith(msg, 'There are no migrations to run') || _.startsWith(msg, 'There are no pending migrations.')) {
                console.log(`There are no ${group} migrations to run.`);

                return;
            }

            throw err;
        });
};

module.exports = { migrate, init };
