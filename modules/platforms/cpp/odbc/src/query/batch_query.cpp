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

#include "ignite/odbc/connection.h"
#include "ignite/odbc/message.h"
#include "ignite/odbc/log.h"
#include "ignite/odbc/odbc_error.h"
#include "ignite/odbc/query/batch_query.h"

namespace ignite
{
    namespace odbc
    {
        namespace query
        {
            BatchQuery::BatchQuery(diagnostic::Diagnosable& diag, Connection& connection, const std::string& sql,
                const app::ParameterSet& params, int32_t& timeout) :
                Query(diag, QueryType::BATCH),
                connection(connection),
                sql(sql),
                params(params),
                resultMeta(),
                rowsAffected(),
                rowsAffectedIdx(0),
                executed(false),
                timeout(timeout)
            {
                // No-op.
            }

            BatchQuery::~BatchQuery()
            {
                // No-op.
            }

            SqlResult::Type BatchQuery::Execute()
            {
                if (executed)
                {
                    diag.AddStatusRecord(SqlState::SHY010_SEQUENCE_ERROR, "Query cursor is in open state already.");

                    return SqlResult::AI_ERROR;
                }

                int32_t maxPageSize = connection.GetConfiguration().GetPageSize();
                int32_t rowNum = params.GetParamSetSize();
                SqlResult::Type res;

                int32_t processed = 0;

                rowsAffected.clear();
                rowsAffected.reserve(static_cast<size_t>(params.GetParamSetSize()));

                do {
                    int32_t currentPageSize = std::min(maxPageSize, rowNum - processed);
                    bool lastPage = currentPageSize == rowNum - processed;

                    res = MakeRequestExecuteBatch(processed, processed + currentPageSize, lastPage);

                    processed += currentPageSize;
                } while (res == SqlResult::AI_SUCCESS && processed < rowNum);

                params.SetParamsProcessed(static_cast<SqlUlen>(rowsAffected.size()));

                return res;
            }

            const meta::ColumnMetaVector& BatchQuery::GetMeta() const
            {
                return resultMeta;
            }

            SqlResult::Type BatchQuery::FetchNextRow(app::ColumnBindingMap& columnBindings)
            {
                if (!executed)
                {
                    diag.AddStatusRecord(SqlState::SHY010_SEQUENCE_ERROR, "Query was not executed.");

                    return SqlResult::AI_ERROR;
                }

                return SqlResult::AI_NO_DATA;
            }

            SqlResult::Type BatchQuery::GetColumn(uint16_t columnIdx, app::ApplicationDataBuffer& buffer)
            {
                if (!executed)
                {
                    diag.AddStatusRecord(SqlState::SHY010_SEQUENCE_ERROR, "Query was not executed.");

                    return SqlResult::AI_ERROR;
                }

                diag.AddStatusRecord(SqlState::S24000_INVALID_CURSOR_STATE,
                    "Cursor has reached end of the result set.");

                return SqlResult::AI_ERROR;
            }

            SqlResult::Type BatchQuery::Close()
            {
                executed = false;
                rowsAffected.clear();
                rowsAffectedIdx = 0;

                return SqlResult::AI_SUCCESS;
            }

            bool BatchQuery::DataAvailable() const
            {
                return false;
            }

            int64_t BatchQuery::AffectedRows() const
            {
                int64_t affected = rowsAffectedIdx < rowsAffected.size() ? rowsAffected[rowsAffectedIdx] : 0;
                return affected < 0 ? 0 : affected;
            }

            SqlResult::Type BatchQuery::NextResultSet()
            {
                if (rowsAffectedIdx + 1 >= rowsAffected.size())
                {
                    Close();
                    return SqlResult::AI_NO_DATA;
                }

                ++rowsAffectedIdx;

                return SqlResult::AI_SUCCESS;
            }

            SqlResult::Type BatchQuery::MakeRequestExecuteBatch(SqlUlen begin, SqlUlen end, bool last)
            {
                const std::string& schema = connection.GetSchema();

                QueryExecuteBatchtRequest req(schema, sql, params, begin, end, last, timeout);
                QueryExecuteBatchResponse rsp;

                try
                {
                    // Setting connection timeout to 1 second more than query timeout itself.
                    int32_t connectionTimeout = timeout ? timeout + 1 : 0;

                    bool success = connection.SyncMessage(req, rsp, connectionTimeout);

                    if (!success)
                    {
                        diag.AddStatusRecord(SqlState::SHYT00_TIMEOUT_EXPIRED, "Query timeout expired");

                        return SqlResult::AI_ERROR;
                    }
                }
                catch (const OdbcError& err)
                {
                    diag.AddStatusRecord(err);

                    return SqlResult::AI_ERROR;
                }
                catch (const IgniteError& err)
                {
                    diag.AddStatusRecord(SqlState::SHY000_GENERAL_ERROR, err.GetText());

                    return SqlResult::AI_ERROR;
                }

                if (rsp.GetStatus() != ResponseStatus::SUCCESS)
                {
                    LOG_MSG("Error: " << rsp.GetError());

                    diag.AddStatusRecord(ResponseStatusToSqlState(rsp.GetStatus()), rsp.GetError());

                    return SqlResult::AI_ERROR;
                }

                rowsAffected.insert(rowsAffected.end(), rsp.GetAffectedRows().begin(), rsp.GetAffectedRows().end());
                LOG_MSG("Affected rows list size: " << rowsAffected.size());

                if (!rsp.GetErrorMessage().empty())
                {
                    LOG_MSG("Error: " << rsp.GetErrorMessage());
                    LOG_MSG("Sets Processed: " << rowsAffected.size());

                    diag.AddStatusRecord(ResponseStatusToSqlState(rsp.GetErrorCode()), rsp.GetErrorMessage(),
                        static_cast<int32_t>(rowsAffected.size()), 0);

                    return SqlResult::AI_SUCCESS_WITH_INFO;
                }

                return SqlResult::AI_SUCCESS;
            }
        }
    }
}

