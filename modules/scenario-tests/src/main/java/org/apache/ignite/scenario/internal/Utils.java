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

import org.apache.commons.text.RandomStringGenerator;

/** */
public class Utils {
    /** */
    private static ThreadLocal<RandomStringGenerator> _generator = new ThreadLocal<>();

    /** */
    static RandomStringGenerator literalsGenerator() {
        RandomStringGenerator generator = _generator.get();
        if (generator == null) {
            generator = new RandomStringGenerator.Builder().withinRange('a', 'z')
                .build();
            _generator.set(generator);
        }
        return generator;
    }

    /**
     * @param len results string length.
     * @return random literals string.
     */
    public static String randomString(int len) {
        return literalsGenerator().generate(len);
    }
}