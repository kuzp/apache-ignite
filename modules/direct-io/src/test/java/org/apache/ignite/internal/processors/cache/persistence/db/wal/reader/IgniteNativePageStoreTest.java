package org.apache.ignite.internal.processors.cache.persistence.db.wal.reader;

import com.google.common.base.Strings;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.NotNull;

public class IgniteNativePageStoreTest extends GridCommonAbstractTest {

    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration configuration = super.getConfiguration(igniteInstanceName);
        DataStorageConfiguration dsCfg = new DataStorageConfiguration();

        dsCfg.setPageSize(4 * 1024);

        DataRegionConfiguration regCfg = new DataRegionConfiguration();
/*
        regCfg.setName("dfltDataRegion");
        regCfg.setInitialSize(1024 * 1024 * 1024);
        regCfg.setMaxSize(1024 * 1024 * 1024);*/
        regCfg.setPersistenceEnabled(true);

        dsCfg.setDefaultDataRegionConfiguration(regCfg);

        configuration.setDataStorageConfiguration(dsCfg);
        return configuration;
    }

    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();

        GridTestUtils.deleteDbFiles();
    }

    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
    }

    public void testRecoveryAfterCpEnd() throws Exception {

        IgniteEx ignite = startGrid(0);
        ignite.active(true);

        IgniteCache<Object, Object> cache = ignite.getOrCreateCache("cache");

        for (int i = 0; i < 1000; i++) {
            cache.put(i, valueForKey(i));
        }
        /*boolean successfulWaiting = GridTestUtils.waitForCondition(new PAX() {
            @Override public boolean applyx() {
                return ignite.cache("partitioned") != null;
            }
        }, 10_000);*/

        ignite.context().cache().context().database().waitForCheckpoint("test");

        stopAllGrids();


        IgniteEx igniteRestart = startGrid(0);
        igniteRestart.active(true);

        IgniteCache<Object, Object> cacheRestart = igniteRestart.getOrCreateCache("cache");

        for (int i = 0; i < 1000; i++) {
            assertEquals(valueForKey(i), cacheRestart.get(i));
        }

    }

    @NotNull private String valueForKey(int i) {
        return Strings.repeat(Integer.toString(i), 10);
    }
}
