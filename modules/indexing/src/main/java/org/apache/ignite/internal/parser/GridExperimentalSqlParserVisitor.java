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

package org.apache.ignite.internal.parser;

import org.apache.ignite.internal.processors.query.h2.sql.GridSqlStatement;
import org.apache.ignite.internal.processors.query.h2.sql.GridSqlDropIndex;
import org.apache.ignite.internal.parser.IgniteSqlParser.SqlStatementsContext;
import org.apache.ignite.internal.parser.IgniteSqlParser.SqlStatementContext;
import org.apache.ignite.internal.parser.IgniteSqlParser.DropIndexContext;

import java.util.ArrayList;
import java.util.List;

/** */
public class GridExperimentalSqlParserVisitor extends IgniteSqlParserBaseVisitor<List<GridSqlStatement>> {
    /** */
    @Override public List<GridSqlStatement> visitSqlStatements(SqlStatementsContext ctx) {
        List<GridSqlStatement> res = new ArrayList<>();

        DropIndexVisitor v = new DropIndexVisitor();

        for (SqlStatementContext stmt : ctx.sqlStatement())
             res.add(v.visitSqlStatement(stmt));

        return res;
    }

    /** */
    @Override protected List<GridSqlStatement> aggregateResult(List<GridSqlStatement> aggregate,
                                                               List<GridSqlStatement> nextResult) {
        if (aggregate == null)
            return nextResult;

        if (nextResult == null)
            return aggregate;

        aggregate.addAll(nextResult);

        return aggregate;
    }


    /** */
    public static class DropIndexVisitor extends IgniteSqlParserBaseVisitor<GridSqlDropIndex> {
        /** */
        @Override public GridSqlDropIndex visitDropIndex(DropIndexContext ctx) {
            GridSqlDropIndex dropIndex = new GridSqlDropIndex();

            dropIndex.schemaName("public");

            dropIndex.ifExists(ctx.ifExists() != null);

            if (ctx.indexNameWithSchema() != null) {
                dropIndex.schemaName(ctx.indexNameWithSchema().getChild(0).getText());
                dropIndex.indexName(ctx.indexNameWithSchema().getChild(2).getText());
            }
            else {
                dropIndex.schemaName("public"); // Current schema of connection
                dropIndex.indexName(ctx.indexName().getText());
            }

            return dropIndex;
        }
    }
}
