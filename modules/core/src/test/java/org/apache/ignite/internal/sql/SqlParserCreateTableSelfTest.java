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

import org.apache.ignite.internal.sql.command.SqlCreateTableCommand;

/**
 * Tests for SQL parser: CREATE TABLE.
 */
public class SqlParserCreateTableSelfTest extends SqlParserAbstractSelfTest {
    /**
     * Test column types.
     *
     * @throws Exception If failed.
     */
    public void testColumnTypes() throws Exception {
        // TODO: Fix nullability
        SqlCreateTableCommand cmd = parse("CREATE TABLE t (a DECIMAL(1,1) NOT NULL)");

    }

    private static SqlCreateTableCommand assertColumns(SqlCreateTableCommand cmd) {


        return cmd;
    }

    /**
     * Parse command.
     *
     * @param sql SQL.
     * @return Command.
     */
    private static SqlCreateTableCommand parse(String sql) {
        return parse(null, sql);
    }

    /**
     * Parse command.
     *
     * @param schema Schema.
     * @param sql SQL.
     * @return Command.
     */
    private static SqlCreateTableCommand parse(String schema, String sql) {
        SqlParser parser = new SqlParser(schema, sql);

        SqlCreateTableCommand cmd = (SqlCreateTableCommand)parser.nextCommand();

        assert parser.nextCommand() == null;

        return cmd;
    }
}
