package org.apache.ignite.plugin;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;

public class NodeRunner {
    public static void main(String[] args) {
        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setDataStorageConfiguration(
            new DataStorageConfiguration()
                .setWalMode(WALMode.LOG_ONLY)
                .setDefaultDataRegionConfiguration(
                    new DataRegionConfiguration()
                        .setPersistenceEnabled(true)
                )
        );

        Ignition.start(cfg);
    }
}
