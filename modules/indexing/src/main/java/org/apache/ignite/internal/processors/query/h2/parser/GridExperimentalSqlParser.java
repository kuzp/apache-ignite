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
package org.apache.ignite.internal.processors.query.h2.parser;

import java.util.List;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.ignite.Ignite;
import org.apache.ignite.internal.parser.IgniteSqlLexer;
import org.apache.ignite.internal.parser.IgniteSqlParser;
import org.apache.ignite.internal.parser.GridExperimentalSqlParserListener;
import org.apache.ignite.internal.processors.query.IgniteSQLException;
import org.apache.ignite.internal.processors.query.h2.sql.GridSqlStatement;

/**
 * Use 'mvn generate-sources' to regenerate IgniteSqlLexer and IgniteSqlParser classes
 * The resulting files will be put to moudules\indexing\target\generated-sources.
 *
 * Don't forget to mark the above directory as "Generated Sources Root" in IDEA.
 */
public class GridExperimentalSqlParser {
    /** */
    String input;

    /** */
    IgniteSqlLexer lexer;

    /** */
    IgniteSqlParser parser;

    /** */
    GridExperimentalSqlParserListener listener;

    /**
     * Constructor.
     *
     * @param input SQL text.
     */
    public GridExperimentalSqlParser(String input) {
        this.input = input;

        lexer = new IgniteSqlLexer(CharStreams.fromString(input));

        lexer.removeErrorListeners();

        lexer.addErrorListener(GridExperimentalSqlParserErrorListener.INSTANCE);

        TokenStream tokenStream = new CommonTokenStream(lexer);

        parser = new IgniteSqlParser(tokenStream);

        parser.removeErrorListeners();

        parser.addErrorListener(GridExperimentalSqlParserErrorListener.INSTANCE);

        parser.setErrorHandler(new BailErrorStrategy());

        listener = new GridExperimentalSqlParserListener();

        parser.addParseListener(listener);
    }

    /**
     * @return List of SQL statements.
     */
    public List<GridSqlStatement> parse() {
        try {
            // Two stage parsing: SLL fail-over to LL.
            try {
                parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

                parser.root();
            }
            catch (Exception e) {
                lexer.reset();

                parser.reset();

                parser.getInterpreter().setPredictionMode(PredictionMode.LL);

                parser.root();
            }
        }
        catch (ParseCancellationException e) {
            if (e.getCause() instanceof GridExperimentalSqlParserException) {
                GridExperimentalSqlParserException ex = (GridExperimentalSqlParserException)e.getCause();

                throw new IgniteSQLException(formatParserError(input, ex.getMessage(), ex.getLine(), ex.getPosition()),
                    ex.getCause());
            }

            throw new IgniteSQLException("Unknown experimental parser error", e);
        }

        return listener.getStatements();
    }

    /**
     * Formats error message.
     *
     * @param inputText Input SQL text.
     * @param errorMessage Error message.
     * @param line Line where error has occurred.
     * @param position Position in line where error has occurred.
     * @return Formatted error message.
     */
    private static String formatParserError(String inputText, String errorMessage, int line, int position) {
        String err = "SyntaxError: line " + line + ": position " + position + ": " + errorMessage + ": ";

        StringBuilder sb = new StringBuilder(err);

        String lines[] = inputText.split("\n\r");

        for (int i = 0; i < lines.length; ++i) {
            if (i + 1 == line) {
                String l = lines[i];
                sb.append(l.substring(0, position));
                sb.append("[*]");
                sb.append(l.substring(position, l.length()));
            }
            else {
                sb.append(lines[i] + "\n");
            }
        }

        return sb.toString();
    }
}
