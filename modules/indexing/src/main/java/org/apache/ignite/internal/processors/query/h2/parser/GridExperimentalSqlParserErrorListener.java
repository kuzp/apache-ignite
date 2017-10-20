package org.apache.ignite.internal.processors.query.h2.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/** Error listener */
public class GridExperimentalSqlParserErrorListener extends BaseErrorListener {
    public static final GridExperimentalSqlParserErrorListener INSTANCE = new GridExperimentalSqlParserErrorListener();

    /** {@inheritDoc} */
    @Override public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e) {

        //Skip errors in SLL mode.
        if ((recognizer.getInterpreter() instanceof ParserATNSimulator) &&
            ((ParserATNSimulator)recognizer.getInterpreter()).getPredictionMode() == PredictionMode.SLL)
            return;

        throw new ParseCancellationException(msg,
            new GridExperimentalSqlParserException(msg, line, charPositionInLine, e));
    }

}
