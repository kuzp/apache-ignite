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

package org.apache.ignite.scenario;

import com.beust.jcommander.Parameter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.scenario.internal.AbstractTask;
import org.apache.ignite.scenario.internal.Utils;

/**
 * Fill cache with generated data: - key is random UUID. - value is generated BinaryObject with random String fields.
 */
public class LoadCacheTask extends AbstractTask {
    /** */
    @Parameter(names = "-cfg", description = "Path to client node configuration file.", required = true)
    private String config;

    /** */
    @Parameter(names = "-c", description = "Cache name.")
    private String cacheName = "default";

    /** */
    @Parameter(names = "-s", description = "Dataset size.")
    private Integer size = 100_000;

    /** */
    @Parameter(names = "-f", description = "Fields per entry.")
    private Integer fields = 25;

    /** */
    @Parameter(names = "-fs", description = "Field size.")
    private Integer fieldSize = 50;

    /** */
    private Ignite ignite;

    /**
     * @param args
     */
    public static void main(String[] args) {
        LoadCacheTask task = new LoadCacheTask();

        try {
            task.parseProperties(args);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        task.setStartDir(args[1]);

        task.run();
    }

    /** {@inheritDoc} */
    @Override protected void setUp() {
        super.setUp();

        ignite = Ignition.start(startDir + sep + config);
    }

    /** {@inheritDoc} */
    @Override protected void tearDown() {
        super.tearDown();

        ignite.close();
    }

    /** {@inheritDoc} */
    @Override public void body() {
        assert fields > 0;

        try (IgniteDataStreamer<Object, Object> streamer = ignite.dataStreamer(cacheName)) {
            BinaryObjectBuilder builder = ignite.binary().builder("SampleObject");

            for (int i = 0; i < size; i++) {
                for (int f = 1; f <= fields; f++)
                    builder.setField("fld-" + f, Utils.randomString(fieldSize));

                streamer.addData(UUID.randomUUID(), builder.build());
            }
        }
    }

    public Properties parseProperties(String[] args) throws IOException {
        Properties properties = new Properties();

        properties.load(new FileInputStream(args[0]));

        for (Object property : properties.keySet()){

            switch (property.toString()) {

                case "config":

                    config=properties.getProperty(property.toString());

                    break;

                case "cacheName":

                    cacheName=properties.getProperty(property.toString());

                    break;

                case "size":

                    size=Integer.valueOf(properties.getProperty(property.toString()));

                    break;

                case "fields":

                    fields=Integer.valueOf(properties.getProperty(property.toString()));

                    break;

                case "fieldSize":

                    fieldSize=Integer.valueOf(properties.getProperty(property.toString()));

                    break;


                default:

                    break;

            }

        }

        return properties;
    }
}
