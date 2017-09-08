// Generated from IgniteSqlParser.g4 by ANTLR 4.7

package org.apache.ignite.internal.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IgniteSqlParser}.
 */
public interface IgniteSqlParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(IgniteSqlParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(IgniteSqlParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#sqlStatements}.
	 * @param ctx the parse tree
	 */
	void enterSqlStatements(IgniteSqlParser.SqlStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#sqlStatements}.
	 * @param ctx the parse tree
	 */
	void exitSqlStatements(IgniteSqlParser.SqlStatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void enterSqlStatement(IgniteSqlParser.SqlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void exitSqlStatement(IgniteSqlParser.SqlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdlStatement(IgniteSqlParser.DdlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdlStatement(IgniteSqlParser.DdlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void enterDropIndex(IgniteSqlParser.DropIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void exitDropIndex(IgniteSqlParser.DropIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void enterIfExists(IgniteSqlParser.IfExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void exitIfExists(IgniteSqlParser.IfExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(IgniteSqlParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(IgniteSqlParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#indexNameWithSchema}.
	 * @param ctx the parse tree
	 */
	void enterIndexNameWithSchema(IgniteSqlParser.IndexNameWithSchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#indexNameWithSchema}.
	 * @param ctx the parse tree
	 */
	void exitIndexNameWithSchema(IgniteSqlParser.IndexNameWithSchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link IgniteSqlParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(IgniteSqlParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IgniteSqlParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(IgniteSqlParser.EmptyStatementContext ctx);
}