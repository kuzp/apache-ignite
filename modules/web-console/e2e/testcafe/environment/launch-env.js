const { startEnv, removeData } = require('./envtools');

startEnv();

process.on('SIGINT', async() => {
    await removeData();

    process.exit(0);
});
