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

package org.apache.ignite.internal.processors.odbc.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.QueryCancelledException;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.jdbc2.JdbcSqlFieldsQuery;
import org.apache.ignite.internal.processors.cache.QueryCursorImpl;
import org.apache.ignite.internal.processors.cache.query.IgniteQueryErrorCode;
import org.apache.ignite.internal.processors.odbc.ClientListenerRequest;
import org.apache.ignite.internal.processors.odbc.ClientListenerRequestHandler;
import org.apache.ignite.internal.processors.odbc.ClientListenerResponse;
import org.apache.ignite.internal.processors.odbc.odbc.OdbcQueryGetColumnsMetaRequest;
import org.apache.ignite.internal.processors.query.GridQueryIndexDescriptor;
import org.apache.ignite.internal.processors.query.GridQueryTypeDescriptor;
import org.apache.ignite.internal.processors.query.IgniteSQLException;
import org.apache.ignite.internal.processors.query.QueryUtils;
import org.apache.ignite.internal.util.GridSpinBusyLock;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;

import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.BATCH_EXEC;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_COLUMNS;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_INDEXES;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_PARAMS;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_PRIMARY_KEYS;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_SCHEMAS;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.META_TABLES;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.QRY_CANCEL;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.QRY_CLOSE;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.QRY_EXEC;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.QRY_FETCH;
import static org.apache.ignite.internal.processors.odbc.jdbc.JdbcRequest.QRY_META;

/**
 * JDBC request handler.
 */
public class JdbcRequestHandler implements ClientListenerRequestHandler {
    /** Query ID sequence. */
    private static final AtomicLong QRY_ID_GEN = new AtomicLong();

    /** Kernel context. */
    private final GridKernalContext ctx;

    /** Logger. */
    private final IgniteLogger log;

    /** Busy lock. */
    private final GridSpinBusyLock busyLock;

    /** Maximum allowed cursors. */
    private final int maxCursors;

    /** Current queries cursors. */
    private final ConcurrentHashMap<Long, JdbcQueryCursor> qryCursors = new ConcurrentHashMap<>();

    /** Distributed joins flag. */
    private final boolean distributedJoins;

    /** Enforce join order flag. */
    private final boolean enforceJoinOrder;

    /** Collocated flag. */
    private final boolean collocated;

    /** Replicated only flag. */
    private final boolean replicatedOnly;

    /** Lazy query execution flag. */
    private final boolean lazy;

    /** Automatic close of cursors. */
    private final boolean autoCloseCursors;

    /** Connection handler. */
    private final JdbcConnectionHandler connHnd;

    /**
     * Constructor.
     *
     * @param ctx Context.
     * @param connHnd Connection handler.
     * @param busyLock Shutdown latch.
     * @param maxCursors Maximum allowed cursors.
     * @param distributedJoins Distributed joins flag.
     * @param enforceJoinOrder Enforce join order flag.
     * @param collocated Collocated flag.
     * @param replicatedOnly Replicated only flag.
     * @param autoCloseCursors Flag to automatically close server cursors.
     * @param lazy Lazy query execution flag.
     */
    public JdbcRequestHandler(GridKernalContext ctx, JdbcConnectionHandler connHnd,
        GridSpinBusyLock busyLock, int maxCursors, boolean distributedJoins, boolean enforceJoinOrder,
        boolean collocated, boolean replicatedOnly, boolean autoCloseCursors, boolean lazy) {
        this.ctx = ctx;
        this.connHnd = connHnd;
        this.busyLock = busyLock;
        this.maxCursors = maxCursors;
        this.distributedJoins = distributedJoins;
        this.enforceJoinOrder = enforceJoinOrder;
        this.collocated = collocated;
        this.replicatedOnly = replicatedOnly;
        this.autoCloseCursors = autoCloseCursors;
        this.lazy = lazy;

        log = ctx.log(getClass());
    }

    /** {@inheritDoc} */
    @Override public ClientListenerResponse handle(ClientListenerRequest req0) {
        assert req0 != null;

        assert req0 instanceof JdbcRequest;

        JdbcRequest req = (JdbcRequest)req0;

        if (!busyLock.enterBusy())
            return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN,
                "Failed to handle JDBC request because node is stopping.");

        try {
            switch (req.type()) {
                case QRY_EXEC:
                    return executeQuery((JdbcQueryExecuteRequest)req);

                case QRY_FETCH:
                    return fetchQuery((JdbcQueryFetchRequest)req);

                case QRY_CLOSE:
                    return closeQuery((JdbcQueryCloseRequest)req);

                case QRY_META:
                    return getQueryMeta((JdbcQueryMetadataRequest)req);

                case BATCH_EXEC:
                    return executeBatch((JdbcBatchExecuteRequest)req);

                case META_TABLES:
                    return getTablesMeta((JdbcMetaTablesRequest)req);

                case META_COLUMNS:
                    return getColumnsMeta((JdbcMetaColumnsRequest)req);

                case META_INDEXES:
                    return getIndexesMeta((JdbcMetaIndexesRequest)req);

                case META_PARAMS:
                    return getParametersMeta((JdbcMetaParamsRequest)req);

                case META_PRIMARY_KEYS:
                    return getPrimaryKeys((JdbcMetaPrimaryKeysRequest)req);

                case META_SCHEMAS:
                    return getSchemas((JdbcMetaSchemasRequest)req);

                case QRY_CANCEL:
                    return cancelQuery((JdbcQueryCancelRequest)req);
            }

            return new JdbcResponse(IgniteQueryErrorCode.UNSUPPORTED_OPERATION,
                "Unsupported JDBC request [req=" + req + ']');
        }
        finally {
            busyLock.leaveBusy();
        }
    }

    /** {@inheritDoc} */
    @Override public ClientListenerResponse handleException(Exception e, ClientListenerRequest req) {
        return exceptionToResult(e);
    }

    /**
     * @param qryId Query ID to cancel.
     */
    public void cancelQuery(long qryId) {
        JdbcQueryCursor cur = qryCursors.get(qryId);

        if (cur == null) {
            log.warning("Cancel query do nothing. Cannot find query [qryId=" + qryId + ']');

            return;
        }

        cur.cancel();
    }

    /**
     * Called whenever client is disconnected due to correct connection close
     * or due to {@code IOException} during network operations.
     */
    public void onDisconnect() {
        if (busyLock.enterBusy())
        {
            try
            {
                for (JdbcQueryCursor cursor : qryCursors.values())
                    cursor.close();
            }
            finally {
                busyLock.leaveBusy();
            }
        }
    }

    /**
     * {@link JdbcQueryExecuteRequest} command handler.
     *
     * @param req Execute query request.
     * @return Response.
     */
    @SuppressWarnings("unchecked")
    private JdbcResponse executeQuery(JdbcQueryExecuteRequest req) {
        int cursorCnt = qryCursors.size();

        if (maxCursors > 0 && cursorCnt >= maxCursors)
            return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN, "Too many open cursors (either close other " +
                "open cursors or increase the limit through " +
                "ClientConnectorConfiguration.maxOpenCursorsPerConnection) [maximum=" + maxCursors +
                ", current=" + cursorCnt + ']');

        long qryId = req.queryId() < 0 ? QRY_ID_GEN.getAndIncrement() : req.queryId();

        try {
            String sql = req.sqlQuery();

            SqlFieldsQuery qry;

            switch(req.expectedStatementType()) {
                case ANY_STATEMENT_TYPE:
                    qry = new SqlFieldsQuery(sql);

                    break;

                case SELECT_STATEMENT_TYPE:
                    qry = new JdbcSqlFieldsQuery(sql, true);

                    break;

                default:
                    assert req.expectedStatementType() == JdbcStatementType.UPDATE_STMT_TYPE;

                    qry = new JdbcSqlFieldsQuery(sql, false);
            }

            qry.setArgs(req.arguments());

            qry.setDistributedJoins(distributedJoins);
            qry.setEnforceJoinOrder(enforceJoinOrder);
            qry.setCollocated(collocated);
            qry.setReplicatedOnly(replicatedOnly);
            qry.setLazy(lazy);

            if (req.pageSize() <= 0)
                return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN, "Invalid fetch size: " + req.pageSize());

            qry.setPageSize(req.pageSize());

            String schemaName = req.schemaName();

            if (F.isEmpty(schemaName))
                schemaName = QueryUtils.DFLT_SCHEMA;

            qry.setSchema(schemaName);

            FieldsQueryCursor<List<?>> qryCur = ctx.query().querySqlFieldsNoCache(qry, true);

            JdbcQueryCursor cur = new JdbcQueryCursor(qryId, req.pageSize(), req.maxRows(), (QueryCursorImpl)qryCur);

            qryCursors.put(qryId, cur);

            cur.open();

            JdbcQueryExecuteResult res;

            if (cur.isQuery())
                res = new JdbcQueryExecuteResult(qryId, cur.fetchRows(), !cur.hasNext());
            else {
                List<List<Object>> items = cur.fetchRows();

                assert items != null && items.size() == 1 && items.get(0).size() == 1
                    && items.get(0).get(0) instanceof Long :
                    "Invalid result set for not-SELECT query. [qry=" + sql +
                        ", res=" + S.toString(List.class, items) + ']';

                res = new JdbcQueryExecuteResult(qryId, (Long)items.get(0).get(0));
            }

            if (res.last() && (!res.isQuery() || autoCloseCursors)) {
                cur.close();

                qryCursors.remove(qryId);
            }

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            qryCursors.remove(qryId);

            U.error(log, "Failed to execute SQL query [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * {@link JdbcQueryCloseRequest} command handler.
     *
     * @param req Execute query request.
     * @return Response.
     */
    private JdbcResponse closeQuery(JdbcQueryCloseRequest req) {
        try {
            JdbcQueryCursor cur = qryCursors.remove(req.queryId());

            if (cur == null)
                return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN,
                    "Failed to find query cursor with ID: " + req.queryId());

            cur.close();

            return new JdbcResponse(null);
        }
        catch (Exception e) {
            qryCursors.remove(req.queryId());

            U.error(log, "Failed to close SQL query [reqId=" + req.requestId() + ", req=" + req.queryId() + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * {@link JdbcQueryFetchRequest} command handler.
     *
     * @param req Execute query request.
     * @return Response.
     */
    private JdbcResponse fetchQuery(JdbcQueryFetchRequest req) {
        try {
            JdbcQueryCursor cur = qryCursors.get(req.queryId());

            if (cur == null)
                return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN,
                    "Failed to find query cursor with ID: " + req.queryId());

            if (req.pageSize() <= 0)
                return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN,
                    "Invalid fetch size : [fetchSize=" + req.pageSize() + ']');

            cur.pageSize(req.pageSize());

            JdbcQueryFetchResult res = new JdbcQueryFetchResult(cur.fetchRows(), !cur.hasNext());

            if (res.last() && (!cur.isQuery() || autoCloseCursors)) {
                qryCursors.remove(req.queryId());

                cur.close();
            }

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            U.error(log, "Failed to fetch SQL query result [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    private JdbcResponse getQueryMeta(JdbcQueryMetadataRequest req) {
        try {
            JdbcQueryCursor cur = qryCursors.get(req.queryId());

            if (cur == null)
                return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN,
                    "Failed to find query with ID: " + req.queryId());

            JdbcQueryMetadataResult res = new JdbcQueryMetadataResult(req.queryId(),
                cur.meta());

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            U.error(log, "Failed to fetch SQL query result [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    @SuppressWarnings("unchecked")
    private ClientListenerResponse executeBatch(JdbcBatchExecuteRequest req) {
        String schemaName = req.schemaName();

        if (F.isEmpty(schemaName))
            schemaName = QueryUtils.DFLT_SCHEMA;

        int successQueries = 0;
        int updCnts[] = new int[req.queries().size()];

        try {
            String sql = null;

            qryCursors.put(req.queryId(), JdbcQueryCursor.idleCursor());

            for (JdbcQuery q : req.queries()) {
                if (q.sql() != null)
                    sql = q.sql();

                SqlFieldsQuery qry = new JdbcSqlFieldsQuery(sql, false);

                qry.setArgs(q.args());

                qry.setDistributedJoins(distributedJoins);
                qry.setEnforceJoinOrder(enforceJoinOrder);
                qry.setCollocated(collocated);
                qry.setReplicatedOnly(replicatedOnly);
                qry.setLazy(lazy);

                qry.setSchema(schemaName);

                QueryCursorImpl<List<?>> qryCur = (QueryCursorImpl<List<?>>)ctx.query()
                    .querySqlFieldsNoCache(qry, true);

                JdbcQueryCursor jdbcCur = new JdbcQueryCursor(req.queryId(), (QueryCursorImpl)qryCur);

                JdbcQueryCursor jdbcCurOld = qryCursors.put(req.queryId(), jdbcCur);

                if (jdbcCurOld.isCanceled())
                    throw new QueryCancelledException();

                assert !qryCur.isQuery();

                List<List<?>> items = qryCur.getAll();

                updCnts[successQueries++] = ((Long)items.get(0).get(0)).intValue();
            }

            return new JdbcResponse(new JdbcBatchExecuteResult(updCnts, ClientListenerResponse.STATUS_SUCCESS, null));
        }
        catch (Exception e) {
            U.error(log, "Failed to execute batch query [reqId=" + req.requestId() + ", req=" + req + ']', e);

            JdbcResponse errResp = exceptionToResult(e);

            return new JdbcResponse(new JdbcBatchExecuteResult(Arrays.copyOf(updCnts, successQueries),
                errResp.status(), errResp.error()));
        }
        finally {
            qryCursors.remove(req.queryId());
        }
    }

    /**
     * @param req Get tables metadata request.
     * @return Response.
     */
    private JdbcResponse getTablesMeta(JdbcMetaTablesRequest req) {
        try {
            List<JdbcTableMeta> meta = new ArrayList<>();

            for (String cacheName : ctx.cache().publicCacheNames()) {
                for (GridQueryTypeDescriptor table : ctx.query().types(cacheName)) {
                    if (!matches(table.schemaName(), req.schemaName()))
                        continue;

                    if (!matches(table.tableName(), req.tableName()))
                        continue;

                    JdbcTableMeta tableMeta = new JdbcTableMeta(table.schemaName(), table.tableName(), "TABLE");

                    if (!meta.contains(tableMeta))
                        meta.add(tableMeta);
                }
            }

            JdbcMetaTablesResult res = new JdbcMetaTablesResult(meta);

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            U.error(log, "Failed to get tables metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * {@link OdbcQueryGetColumnsMetaRequest} command handler.
     *
     * @param req Get columns metadata request.
     * @return Response.
     */
    private JdbcResponse getColumnsMeta(JdbcMetaColumnsRequest req) {
        try {
            Collection<JdbcColumnMeta> meta = new HashSet<>();

            for (String cacheName : ctx.cache().publicCacheNames()) {
                for (GridQueryTypeDescriptor table : ctx.query().types(cacheName)) {
                    if (!matches(table.schemaName(), req.schemaName()))
                        continue;

                    if (!matches(table.tableName(), req.tableName()))
                        continue;

                    for (Map.Entry<String, Class<?>> field : table.fields().entrySet()) {
                        if (!matches(field.getKey(), req.columnName()))
                            continue;

                        JdbcColumnMeta columnMeta = new JdbcColumnMeta(table.schemaName(), table.tableName(),
                            field.getKey(), field.getValue());

                        if (!meta.contains(columnMeta))
                            meta.add(columnMeta);
                    }
                }
            }

            JdbcMetaColumnsResult res = new JdbcMetaColumnsResult(meta);

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            U.error(log, "Failed to get columns metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    private ClientListenerResponse getIndexesMeta(JdbcMetaIndexesRequest req) {
        try {
            Collection<JdbcIndexMeta> meta = new HashSet<>();

            for (String cacheName : ctx.cache().publicCacheNames()) {
                for (GridQueryTypeDescriptor table : ctx.query().types(cacheName)) {
                    if (!matches(table.schemaName(), req.schemaName()))
                        continue;

                    if (!matches(table.tableName(), req.tableName()))
                        continue;

                    for (GridQueryIndexDescriptor idxDesc : table.indexes().values())
                        meta.add(new JdbcIndexMeta(table.schemaName(), table.tableName(), idxDesc));
                }
            }

            return new JdbcResponse(new JdbcMetaIndexesResult(meta));
        }
        catch (Exception e) {
            U.error(log, "Failed to get parameters metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    private ClientListenerResponse getParametersMeta(JdbcMetaParamsRequest req) {
        try {
            ParameterMetaData paramMeta = ctx.query().prepareNativeStatement(req.schemaName(), req.sql())
                .getParameterMetaData();

            int size = paramMeta.getParameterCount();

            List<JdbcParameterMeta> meta = new ArrayList<>(size);

            for (int i = 0; i < size; i++)
                meta.add(new JdbcParameterMeta(paramMeta, i + 1));

            JdbcMetaParamsResult res = new JdbcMetaParamsResult(meta);

            return new JdbcResponse(res);
        }
        catch (Exception e) {
            U.error(log, "Failed to get parameters metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    private ClientListenerResponse getPrimaryKeys(JdbcMetaPrimaryKeysRequest req) {
        try {
            Collection<JdbcPrimaryKeyMeta> meta = new HashSet<>();

            for (String cacheName : ctx.cache().publicCacheNames()) {
                for (GridQueryTypeDescriptor table : ctx.query().types(cacheName)) {
                    if (!matches(table.schemaName(), req.schemaName()))
                        continue;

                    if (!matches(table.tableName(), req.tableName()))
                        continue;

                    List<String> fields = new ArrayList<>();

                    for (String field : table.fields().keySet()) {
                        if (table.property(field).key())
                            fields.add(field);
                    }


                    final String keyName = table.keyFieldName() == null ?
                        "PK_" + table.schemaName() + "_" + table.tableName() :
                        table.keyFieldName();

                    if (fields.isEmpty()) {
                        meta.add(new JdbcPrimaryKeyMeta(table.schemaName(), table.tableName(), keyName,
                            Collections.singletonList("_KEY")));
                    }
                    else
                        meta.add(new JdbcPrimaryKeyMeta(table.schemaName(), table.tableName(), keyName, fields));
                }
            }

            return new JdbcResponse(new JdbcMetaPrimaryKeysResult(meta));
        }
        catch (Exception e) {
            U.error(log, "Failed to get parameters metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Request.
     * @return Response.
     */
    private ClientListenerResponse getSchemas(JdbcMetaSchemasRequest req) {
        try {
            String schemaPtrn = req.schemaName();

            Set<String> schemas = new HashSet<>();

            for (String cacheName : ctx.cache().publicCacheNames()) {
                for (GridQueryTypeDescriptor table : ctx.query().types(cacheName)) {
                    if (matches(table.schemaName(), schemaPtrn))
                        schemas.add(table.schemaName());
                }
            }

            return new JdbcResponse(new JdbcMetaSchemasResult(schemas));
        }
        catch (Exception e) {
            U.error(log, "Failed to get schemas metadata [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * @param req Execute query request.
     * @return Response.
     */
    private JdbcResponse cancelQuery(JdbcQueryCancelRequest req) {
        try {
            JdbcRequestHandler handler = connHnd.handler(req.connectionId());

            handler.cancelQuery(req.queryId());

            return new JdbcResponse(null);
        }
        catch (Exception e) {
            U.error(log, "Failed to cancel query [reqId=" + req.requestId() + ", req=" + req + ']', e);

            return exceptionToResult(e);
        }
    }

    /**
     * Checks whether string matches SQL pattern.
     *
     * @param str String.
     * @param ptrn Pattern.
     * @return Whether string matches pattern.
     */
    private static boolean matches(String str, String ptrn) {
        return str != null && (F.isEmpty(ptrn) ||
            str.matches(ptrn.replace("%", ".*").replace("_", ".")));
    }

    /**
     * Create {@link JdbcResponse} bearing appropriate Ignite specific result code if possible
     *     from given {@link Exception}.
     *
     * @param e Exception to convert.
     * @return resulting {@link JdbcResponse}.
     */
    private JdbcResponse exceptionToResult(Exception e) {
        JdbcResponse sqlEx = null;

        Throwable t = e;

        while (sqlEx == null && t != null) {
            if (t instanceof SQLException)
                return new JdbcResponse(((SQLException) e).getErrorCode(), e.getMessage());
            else if (t instanceof IgniteSQLException)
                return new JdbcResponse(((IgniteSQLException) e).statusCode(), e.getMessage());
            else if (t instanceof QueryCancelledException) {
                return new JdbcResponse(IgniteQueryErrorCode.QUERY_CANCELED,
                    "The query was cancelled while executing");
            }

            t = t.getCause();
        }

        return new JdbcResponse(IgniteQueryErrorCode.UNKNOWN, e.toString());
    }
}
