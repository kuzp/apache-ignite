package org.apache.ignite.internal.processors.query.h2.parser;

import org.apache.ignite.IgniteException;

/** */
public class GridExperimentalSqlParserException extends IgniteException {
    /** Error line. */
    private int line;

    /** Error position on line. */
    private int pos;

    /**
     * Constructor.
     *
     * @param msg Error message.
     * @param line Line number where error has occurred.
     * @param pos Position on line where error has occurred.
     * @param cause Cause.
     */
    public GridExperimentalSqlParserException(String msg, int line, int pos, Exception cause) {
        super(msg, cause);

        this.line = line;
        this.pos = pos;
    }

    /**
     * @return Line number where error occurred.
     */
    public int getLine() {
        return line;
    }

    /**
     * @return Position of error on line.
     */
    public int getPosition() {
        return pos;
    }
}
