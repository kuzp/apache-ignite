// Generated from IgniteSqlParser.g4 by ANTLR 4.7

package org.apache.ignite.internal.parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IgniteSqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IgniteSqlParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(IgniteSqlParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#sqlStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlStatements(IgniteSqlParser.SqlStatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlStatement(IgniteSqlParser.SqlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#ddlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStatement(IgniteSqlParser.DdlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#dropIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndex(IgniteSqlParser.DropIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#ifExists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExists(IgniteSqlParser.IfExistsContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(IgniteSqlParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#indexNameWithSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexNameWithSchema(IgniteSqlParser.IndexNameWithSchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link IgniteSqlParser#emptyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyStatement(IgniteSqlParser.EmptyStatementContext ctx);
}