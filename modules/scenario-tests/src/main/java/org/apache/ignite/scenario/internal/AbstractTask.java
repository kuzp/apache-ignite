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

package org.apache.ignite.scenario.internal;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Abstract task class.
 */
public abstract class AbstractTask implements Runnable {

    protected String startDir;

    protected String sep = "/";

    /** */
    public Properties parseArgs(String[] args) throws IOException, IllegalAccessException {
        Properties properties = new Properties();

        properties.load(new FileInputStream(args[0]));

        Field[] myFields = this.getClass().getDeclaredFields();

        for (Field field : myFields){
            String fieldName  = field.getName();

            if (properties.getProperty(fieldName) != null){
                field.setAccessible(true);
                field.set(this, properties.getProperty(fieldName));
            }
        }

        return properties;
    }

    protected void setStartDir(String path){

        File script = new File(path);

        startDir=script.getParent();
    }

    /** {@inheritDoc} */
    @Override public void run() {
        setUp();

        try {
            final long startTime = System.currentTimeMillis();

            body();

            System.out.println(String.format(getClass().getSimpleName() + " finished in %d msec.",
                System.currentTimeMillis() - startTime));
        }
        finally {
            tearDown();
        }
    }

    /** Before body started */
    protected void setUp() {

    }

    protected abstract void body();

    /** Before body started */
    protected void tearDown() {

    }

}
