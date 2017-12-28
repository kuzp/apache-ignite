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

package org.apache.ignite.internal.sql;

import java.util.concurrent.Callable;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_SQL_PARSER_DISABLE_H2_FALLBACK;

/**
 * SQL parser test utils.
 */
public class SqlParserTestUtils {
    /**
     * Execute command with particular H2 fallback state.
     *
     * @param disabled Whether fallback should be disabled.
     * @param cmd Command to be executed.
     */
    public static void withH2Fallback(Callable cmd, boolean disabled) throws Exception {
        String oldVal = System.getProperty(IGNITE_SQL_PARSER_DISABLE_H2_FALLBACK);

        try {
            System.setProperty(IGNITE_SQL_PARSER_DISABLE_H2_FALLBACK, Boolean.toString(disabled));

            cmd.call();
        }
        finally {
            System.setProperty(IGNITE_SQL_PARSER_DISABLE_H2_FALLBACK, oldVal);
        }
    }

    /**
     * Default constructor.
     */
    private SqlParserTestUtils() {
        // No-op.
    }
}
