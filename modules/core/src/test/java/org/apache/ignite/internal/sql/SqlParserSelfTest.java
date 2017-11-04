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

import org.apache.ignite.internal.sql.command.SqlCreateIndexCommand;
import org.apache.ignite.internal.sql.command.SqlIndexColumn;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Test for parser.
 */
@SuppressWarnings({"UnusedReturnValue", "ThrowableNotThrown"})
public class SqlParserSelfTest extends GridCommonAbstractTest {
    /**
     * Tests for CREATE INDEX command.
     *
     * @throws Exception If failed.
     */
    public void testCreateIndex() throws Exception {
        // Base.
        parseValidate(null, "CREATE INDEX idx ON tbl(a)", null, "TBL", "IDX", "A", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a ASC)", null, "TBL", "IDX", "A", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a DESC)", null, "TBL", "IDX", "A", true);

        // Case (in)sensitivity.
        parseValidate(null, "CREATE INDEX IDX ON TBL(COL)", null, "TBL", "IDX", "COL", false);
        parseValidate(null, "CREATE INDEX iDx ON tBl(cOl)", null, "TBL", "IDX", "COL", false);

        parseValidate(null, "CREATE INDEX \"idx\" ON tbl(col)", null, "TBL", "idx", "COL", false);
        parseValidate(null, "CREATE INDEX \"iDx\" ON tbl(col)", null, "TBL", "iDx", "COL", false);

        parseValidate(null, "CREATE INDEX idx ON \"tbl\"(col)", null, "tbl", "IDX", "COL", false);
        parseValidate(null, "CREATE INDEX idx ON \"tBl\"(col)", null, "tBl", "IDX", "COL", false);

        parseValidate(null, "CREATE INDEX idx ON tbl(\"col\")", null, "TBL", "IDX", "col", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(\"cOl\")", null, "TBL", "IDX", "cOl", false);

        parseValidate(null, "CREATE INDEX idx ON tbl(\"cOl\" ASC)", null, "TBL", "IDX", "cOl", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(\"cOl\" DESC)", null, "TBL", "IDX", "cOl", true);

        // Columns.
        parseValidate(null, "CREATE INDEX idx ON tbl(a, b)", null, "TBL", "IDX", "A", false, "B", false);

        parseValidate(null, "CREATE INDEX idx ON tbl(a ASC, b)", null, "TBL", "IDX", "A", false, "B", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a, b ASC)", null, "TBL", "IDX", "A", false, "B", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a ASC, b ASC)", null, "TBL", "IDX", "A", false, "B", false);

        parseValidate(null, "CREATE INDEX idx ON tbl(a DESC, b)", null, "TBL", "IDX", "A", true, "B", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a, b DESC)", null, "TBL", "IDX", "A", false, "B", true);
        parseValidate(null, "CREATE INDEX idx ON tbl(a DESC, b DESC)", null, "TBL", "IDX", "A", true, "B", true);

        parseValidate(null, "CREATE INDEX idx ON tbl(a ASC, b DESC)", null, "TBL", "IDX", "A", false, "B", true);
        parseValidate(null, "CREATE INDEX idx ON tbl(a DESC, b ASC)", null, "TBL", "IDX", "A", true, "B", false);

        parseValidate(null, "CREATE INDEX idx ON tbl(a, b, c)", null, "TBL", "IDX", "A", false, "B", false, "C", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a DESC, b, c)", null, "TBL", "IDX", "A", true, "B", false, "C", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a, b DESC, c)", null, "TBL", "IDX", "A", false, "B", true, "C", false);
        parseValidate(null, "CREATE INDEX idx ON tbl(a, b, c DESC)", null, "TBL", "IDX", "A", false, "B", false, "C", true);

        // Negative cases.
        parseError(null, "CREATE INDEX idx ON tbl()", "Unexpected token");
        parseError(null, "CREATE INDEX idx ON tbl(a, a)", "Column already defined: A");
        parseError(null, "CREATE INDEX idx ON tbl(a, b, a)", "Column already defined: A");
        parseError(null, "CREATE INDEX idx ON tbl(b, a, a)", "Column already defined: A");
    }

    /**
     * Make sure that parse error occurs.
     *
     * @param schema Schema.
     * @param sql SQL.
     * @param msg Expected error message.
     */
    private static void parseError(final String schema, final String sql, String msg) {
        GridTestUtils.assertThrows(null, new Callable<Void>() {
            @Override public Void call() throws Exception {
                new SqlParser(schema, sql).nextCommand();

                return null;
            }
        }, SqlParseException.class, msg);
    }

    /**
     * Parse and validate SQL script.
     *
     * @param schema Schema.
     * @param sql SQL.
     * @param expSchemaName Expected schema name.
     * @param expTblName Expected table name.
     * @param expIdxName Expected index name.
     * @param expColDefs Expected column definitions.
     * @return Command.
     */
    private static SqlCreateIndexCommand parseValidate(String schema, String sql, String expSchemaName,
        String expTblName, String expIdxName, Object... expColDefs) {
        SqlCreateIndexCommand cmd = (SqlCreateIndexCommand)new SqlParser(schema, sql).nextCommand();

        validate(cmd, expSchemaName, expTblName, expIdxName, expColDefs);

        return cmd;
    }

    /**
     * Validate create index command.
     *
     * @param cmd Command.
     * @param expSchemaName Expected schema name.
     * @param expTblName Expected table name.
     * @param expIdxName Expected index name.
     * @param expColDefs Expected column definitions.
     */
    private static void validate(SqlCreateIndexCommand cmd, String expSchemaName, String expTblName, String expIdxName,
        Object... expColDefs) {
        assertEquals(expSchemaName, cmd.schemaName());
        assertEquals(expTblName, cmd.tableName());
        assertEquals(expIdxName, cmd.indexName());

        if (F.isEmpty(expColDefs) || expColDefs.length % 2 == 1)
            throw new IllegalArgumentException("Column definitions must be even.");

        Collection<SqlIndexColumn> cols = cmd.columns();

        assertEquals(expColDefs.length / 2, cols.size());

        Iterator<SqlIndexColumn> colIter = cols.iterator();

        for (int i = 0; i < expColDefs.length;) {
            SqlIndexColumn col = colIter.next();

            String expColName = (String)expColDefs[i++];
            Boolean expDesc = (Boolean) expColDefs[i++];

            assertEquals(expColName, col.name());
            assertEquals(expDesc, (Boolean)col.descending());
        }
    }
}
