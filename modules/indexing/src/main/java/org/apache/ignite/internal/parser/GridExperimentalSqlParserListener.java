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

import org.apache.ignite.internal.processors.query.h2.sql.GridSqlDropIndex;
import org.apache.ignite.internal.processors.query.h2.sql.GridSqlStatement;
import org.apache.ignite.internal.parser.IgniteSqlParser.SqlStatementsContext;
import org.apache.ignite.internal.parser.IgniteSqlParser.SqlStatementContext;
import org.apache.ignite.internal.parser.IgniteSqlParser.DropIndexContext;
import org.apache.ignite.internal.parser.IgniteSqlParser.IfExistsContext;

import java.util.ArrayList;
import java.util.List;

/** */
public class GridExperimentalSqlParserListener extends IgniteSqlParserBaseListener {
    /** */
    private GridSqlDropIndex dropIndex;

    /** */
    final List<GridSqlStatement> stmts = new ArrayList<>();

    /** */
    public List<GridSqlStatement> getStatements() {
        return stmts;
    }

    /** */
    @Override public void enterDropIndex(DropIndexContext ctx) {
        dropIndex = new GridSqlDropIndex();
    }

    /** */
    @Override public void exitDropIndex(DropIndexContext ctx) {
        assert dropIndex != null;

        if (ctx.indexNameWithSchema() != null) {
            dropIndex.schemaName(ctx.indexNameWithSchema().getChild(0).getText());
            dropIndex.indexName(ctx.indexNameWithSchema().getChild(2).getText());
        }
        else {
            dropIndex.schemaName("public"); // Current schema of connection
            dropIndex.indexName(ctx.indexName().getText());
        }

        stmts.add(dropIndex);
    }

    /** */
    @Override public void enterIfExists(IfExistsContext ctx) {
        assert dropIndex != null;

        dropIndex.ifExists(true);
    }
}