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

#include <cstring>
#include <algorithm>

#include "ignite/odbc/system/odbc_constants.h"
#include "ignite/odbc/utility.h"
#include "ignite/odbc/config/connection_info.h"

 // Temporary workaround.
#ifndef SQL_ASYNC_NOTIFICATION
#define SQL_ASYNC_NOTIFICATION                  10025
#endif

#ifndef SQL_ASYNC_NOTIFICATION_NOT_CAPABLE
#define SQL_ASYNC_NOTIFICATION_NOT_CAPABLE      0x00000000L
#endif

#ifndef SQL_ASYNC_NOTIFICATION_CAPABLE
#define SQL_ASYNC_NOTIFICATION_CAPABLE          0x00000001L
#endif

namespace ignite
{
    namespace odbc
    {
        namespace config
        {

#define DBG_STR_CASE(x) case x: return #x

            const char * ConnectionInfo::InfoTypeToString(InfoType type)
            {
                switch (type)
                {
#ifdef SQL_ACCESSIBLE_PROCEDURES
                    DBG_STR_CASE(SQL_ACCESSIBLE_PROCEDURES);
#endif // SQL_ACCESSIBLE_PROCEDURES
#ifdef SQL_ACCESSIBLE_TABLES
                    DBG_STR_CASE(SQL_ACCESSIBLE_TABLES);
#endif // SQL_ACCESSIBLE_TABLES
#ifdef SQL_ACTIVE_ENVIRONMENTS
                    DBG_STR_CASE(SQL_ACTIVE_ENVIRONMENTS);
#endif // SQL_ACTIVE_ENVIRONMENTS
#ifdef SQL_DRIVER_NAME
                    DBG_STR_CASE(SQL_DRIVER_NAME);
#endif // SQL_DRIVER_NAME
#ifdef SQL_DBMS_NAME
                    DBG_STR_CASE(SQL_DBMS_NAME);
#endif // SQL_DBMS_NAME
#ifdef SQL_DRIVER_ODBC_VER
                    DBG_STR_CASE(SQL_DRIVER_ODBC_VER);
#endif // SQL_DRIVER_ODBC_VER
#ifdef SQL_DBMS_VER
                    DBG_STR_CASE(SQL_DBMS_VER);
#endif // SQL_DBMS_VER
#ifdef SQL_DRIVER_VER
                    DBG_STR_CASE(SQL_DRIVER_VER);
#endif // SQL_DRIVER_VER
#ifdef SQL_COLUMN_ALIAS
                    DBG_STR_CASE(SQL_COLUMN_ALIAS);
#endif // SQL_COLUMN_ALIAS
#ifdef SQL_IDENTIFIER_QUOTE_CHAR
                    DBG_STR_CASE(SQL_IDENTIFIER_QUOTE_CHAR);
#endif // SQL_IDENTIFIER_QUOTE_CHAR
#ifdef SQL_CATALOG_NAME_SEPARATOR
                    DBG_STR_CASE(SQL_CATALOG_NAME_SEPARATOR);
#endif // SQL_CATALOG_NAME_SEPARATOR
#ifdef SQL_SPECIAL_CHARACTERS
                    DBG_STR_CASE(SQL_SPECIAL_CHARACTERS);
#endif // SQL_SPECIAL_CHARACTERS
#ifdef SQL_CATALOG_TERM
                    DBG_STR_CASE(SQL_CATALOG_TERM);
#endif // SQL_CATALOG_TERM
#ifdef SQL_TABLE_TERM
                    DBG_STR_CASE(SQL_TABLE_TERM);
#endif // SQL_TABLE_TERM
#ifdef SQL_SCHEMA_TERM
                    DBG_STR_CASE(SQL_SCHEMA_TERM);
#endif // SQL_SCHEMA_TERM
#ifdef SQL_NEED_LONG_DATA_LEN
                    DBG_STR_CASE(SQL_NEED_LONG_DATA_LEN);
#endif // SQL_NEED_LONG_DATA_LEN
#ifdef SQL_ASYNC_DBC_FUNCTIONS
                    DBG_STR_CASE(SQL_ASYNC_DBC_FUNCTIONS);
#endif // SQL_ASYNC_DBC_FUNCTIONS
#ifdef SQL_ASYNC_NOTIFICATION
                    DBG_STR_CASE(SQL_ASYNC_NOTIFICATION);
#endif // SQL_ASYNC_NOTIFICATION
#ifdef SQL_GETDATA_EXTENSIONS
                    DBG_STR_CASE(SQL_GETDATA_EXTENSIONS);
#endif // SQL_GETDATA_EXTENSIONS
#ifdef SQL_ODBC_INTERFACE_CONFORMANCE
                    DBG_STR_CASE(SQL_ODBC_INTERFACE_CONFORMANCE);
#endif // SQL_ODBC_INTERFACE_CONFORMANCE
#ifdef SQL_SQL_CONFORMANCE
                    DBG_STR_CASE(SQL_SQL_CONFORMANCE);
#endif // SQL_SQL_CONFORMANCE
#ifdef SQL_CATALOG_USAGE
                    DBG_STR_CASE(SQL_CATALOG_USAGE);
#endif // SQL_CATALOG_USAGE
#ifdef SQL_SCHEMA_USAGE
                    DBG_STR_CASE(SQL_SCHEMA_USAGE);
#endif // SQL_SCHEMA_USAGE
#ifdef SQL_MAX_IDENTIFIER_LEN
                    DBG_STR_CASE(SQL_MAX_IDENTIFIER_LEN);
#endif // SQL_MAX_IDENTIFIER_LEN
#ifdef SQL_AGGREGATE_FUNCTIONS
                    DBG_STR_CASE(SQL_AGGREGATE_FUNCTIONS);
#endif // SQL_AGGREGATE_FUNCTIONS
#ifdef SQL_NUMERIC_FUNCTIONS
                    DBG_STR_CASE(SQL_NUMERIC_FUNCTIONS);
#endif // SQL_NUMERIC_FUNCTIONS
#ifdef SQL_STRING_FUNCTIONS
                    DBG_STR_CASE(SQL_STRING_FUNCTIONS);
#endif // SQL_STRING_FUNCTIONS
#ifdef SQL_TIMEDATE_FUNCTIONS
                    DBG_STR_CASE(SQL_TIMEDATE_FUNCTIONS);
#endif // SQL_TIMEDATE_FUNCTIONS
#ifdef SQL_TIMEDATE_ADD_INTERVALS
                    DBG_STR_CASE(SQL_TIMEDATE_ADD_INTERVALS);
#endif // SQL_TIMEDATE_ADD_INTERVALS
#ifdef SQL_TIMEDATE_DIFF_INTERVALS
                    DBG_STR_CASE(SQL_TIMEDATE_DIFF_INTERVALS);
#endif // SQL_TIMEDATE_DIFF_INTERVALS
#ifdef SQL_DATETIME_LITERALS
                    DBG_STR_CASE(SQL_DATETIME_LITERALS);
#endif // SQL_DATETIME_LITERALS
#ifdef SQL_SYSTEM_FUNCTIONS
                    DBG_STR_CASE(SQL_SYSTEM_FUNCTIONS);
#endif // SQL_SYSTEM_FUNCTIONS
#ifdef SQL_CONVERT_FUNCTIONS
                    DBG_STR_CASE(SQL_CONVERT_FUNCTIONS);
#endif // SQL_CONVERT_FUNCTIONS
#ifdef SQL_OJ_CAPABILITIES
                    DBG_STR_CASE(SQL_OJ_CAPABILITIES);
#endif // SQL_OJ_CAPABILITIES
#ifdef SQL_POS_OPERATIONS
                    DBG_STR_CASE(SQL_POS_OPERATIONS);
#endif // SQL_POS_OPERATIONS
#ifdef SQL_MAX_CONCURRENT_ACTIVITIES
                    DBG_STR_CASE(SQL_MAX_CONCURRENT_ACTIVITIES);
#endif // SQL_MAX_CONCURRENT_ACTIVITIES
#ifdef SQL_CURSOR_COMMIT_BEHAVIOR
                    DBG_STR_CASE(SQL_CURSOR_COMMIT_BEHAVIOR);
#endif // SQL_CURSOR_COMMIT_BEHAVIOR
#ifdef SQL_CURSOR_ROLLBACK_BEHAVIOR
                    DBG_STR_CASE(SQL_CURSOR_ROLLBACK_BEHAVIOR);
#endif // SQL_CURSOR_ROLLBACK_BEHAVIOR
#ifdef SQL_TXN_CAPABLE
                    DBG_STR_CASE(SQL_TXN_CAPABLE);
#endif // SQL_TXN_CAPABLE
#ifdef SQL_QUOTED_IDENTIFIER_CASE
                    DBG_STR_CASE(SQL_QUOTED_IDENTIFIER_CASE);
#endif // SQL_QUOTED_IDENTIFIER_CASE
#ifdef SQL_SQL92_NUMERIC_VALUE_FUNCTIONS
                    DBG_STR_CASE(SQL_SQL92_NUMERIC_VALUE_FUNCTIONS);
#endif // SQL_SQL92_NUMERIC_VALUE_FUNCTIONS
#ifdef SQL_SQL92_STRING_FUNCTIONS
                    DBG_STR_CASE(SQL_SQL92_STRING_FUNCTIONS);
#endif // SQL_SQL92_STRING_FUNCTIONS
#ifdef SQL_SQL92_DATETIME_FUNCTIONS
                    DBG_STR_CASE(SQL_SQL92_DATETIME_FUNCTIONS);
#endif // SQL_SQL92_DATETIME_FUNCTIONS
#ifdef SQL_SQL92_PREDICATES
                    DBG_STR_CASE(SQL_SQL92_PREDICATES);
#endif // SQL_SQL92_PREDICATES
#ifdef SQL_SQL92_RELATIONAL_JOIN_OPERATORS
                    DBG_STR_CASE(SQL_SQL92_RELATIONAL_JOIN_OPERATORS);
#endif // SQL_SQL92_RELATIONAL_JOIN_OPERATORS
#ifdef SQL_SQL92_VALUE_EXPRESSIONS
                    DBG_STR_CASE(SQL_SQL92_VALUE_EXPRESSIONS);
#endif // SQL_SQL92_VALUE_EXPRESSIONS
#ifdef SQL_STATIC_CURSOR_ATTRIBUTES1
                    DBG_STR_CASE(SQL_STATIC_CURSOR_ATTRIBUTES1);
#endif // SQL_STATIC_CURSOR_ATTRIBUTES1
#ifdef SQL_STATIC_CURSOR_ATTRIBUTES2
                    DBG_STR_CASE(SQL_STATIC_CURSOR_ATTRIBUTES2);
#endif // SQL_STATIC_CURSOR_ATTRIBUTES2
#ifdef SQL_CONVERT_BIGINT
                    DBG_STR_CASE(SQL_CONVERT_BIGINT);
#endif // SQL_CONVERT_BIGINT
#ifdef SQL_CONVERT_BINARY
                    DBG_STR_CASE(SQL_CONVERT_BINARY);
#endif // SQL_CONVERT_BINARY
#ifdef SQL_CONVERT_BIT
                    DBG_STR_CASE(SQL_CONVERT_BIT);
#endif // SQL_CONVERT_BIT
#ifdef SQL_CONVERT_CHAR
                    DBG_STR_CASE(SQL_CONVERT_CHAR);
#endif // SQL_CONVERT_CHAR
#ifdef SQL_CONVERT_DATE
                    DBG_STR_CASE(SQL_CONVERT_DATE);
#endif // SQL_CONVERT_DATE
#ifdef SQL_CONVERT_DECIMAL
                    DBG_STR_CASE(SQL_CONVERT_DECIMAL);
#endif // SQL_CONVERT_DECIMAL
#ifdef SQL_CONVERT_DOUBLE
                    DBG_STR_CASE(SQL_CONVERT_DOUBLE);
#endif // SQL_CONVERT_DOUBLE
#ifdef SQL_CONVERT_FLOAT
                    DBG_STR_CASE(SQL_CONVERT_FLOAT);
#endif // SQL_CONVERT_FLOAT
#ifdef SQL_CONVERT_INTEGER
                    DBG_STR_CASE(SQL_CONVERT_INTEGER);
#endif // SQL_CONVERT_INTEGER
#ifdef SQL_CONVERT_LONGVARCHAR
                    DBG_STR_CASE(SQL_CONVERT_LONGVARCHAR);
#endif // SQL_CONVERT_LONGVARCHAR
#ifdef SQL_CONVERT_NUMERIC
                    DBG_STR_CASE(SQL_CONVERT_NUMERIC);
#endif // SQL_CONVERT_NUMERIC
#ifdef SQL_CONVERT_REAL
                    DBG_STR_CASE(SQL_CONVERT_REAL);
#endif // SQL_CONVERT_REAL
#ifdef SQL_CONVERT_SMALLINT
                    DBG_STR_CASE(SQL_CONVERT_SMALLINT);
#endif // SQL_CONVERT_SMALLINT
#ifdef SQL_CONVERT_TIME
                    DBG_STR_CASE(SQL_CONVERT_TIME);
#endif // SQL_CONVERT_TIME
#ifdef SQL_CONVERT_TIMESTAMP
                    DBG_STR_CASE(SQL_CONVERT_TIMESTAMP);
#endif // SQL_CONVERT_TIMESTAMP
#ifdef SQL_CONVERT_TINYINT
                    DBG_STR_CASE(SQL_CONVERT_TINYINT);
#endif // SQL_CONVERT_TINYINT
#ifdef SQL_CONVERT_VARBINARY
                    DBG_STR_CASE(SQL_CONVERT_VARBINARY);
#endif // SQL_CONVERT_VARBINARY
#ifdef SQL_CONVERT_VARCHAR
                    DBG_STR_CASE(SQL_CONVERT_VARCHAR);
#endif // SQL_CONVERT_VARCHAR
#ifdef SQL_CONVERT_LONGVARBINARY
                    DBG_STR_CASE(SQL_CONVERT_LONGVARBINARY);
#endif // SQL_CONVERT_LONGVARBINARY
#ifdef SQL_CONVERT_WCHAR
                    DBG_STR_CASE(SQL_CONVERT_WCHAR);
#endif // SQL_CONVERT_WCHAR
#ifdef SQL_CONVERT_INTERVAL_DAY_TIME
                    DBG_STR_CASE(SQL_CONVERT_INTERVAL_DAY_TIME);
#endif // SQL_CONVERT_INTERVAL_DAY_TIME
#ifdef SQL_CONVERT_INTERVAL_YEAR_MONTH
                    DBG_STR_CASE(SQL_CONVERT_INTERVAL_YEAR_MONTH);
#endif // SQL_CONVERT_INTERVAL_YEAR_MONTH
#ifdef SQL_CONVERT_WLONGVARCHAR
                    DBG_STR_CASE(SQL_CONVERT_WLONGVARCHAR);
#endif // SQL_CONVERT_WLONGVARCHAR
#ifdef SQL_CONVERT_WVARCHAR
                    DBG_STR_CASE(SQL_CONVERT_WVARCHAR);
#endif // SQL_CONVERT_WVARCHAR
#ifdef SQL_CONVERT_GUID
                    DBG_STR_CASE(SQL_CONVERT_GUID);
#endif // SQL_CONVERT_GUID
#ifdef SQL_SCROLL_OPTIONS
                    DBG_STR_CASE(SQL_SCROLL_OPTIONS);
#endif // SQL_SCROLL_OPTIONS
#ifdef SQL_PARAM_ARRAY_ROW_COUNTS
                    DBG_STR_CASE(SQL_PARAM_ARRAY_ROW_COUNTS);
#endif // SQL_PARAM_ARRAY_ROW_COUNTS
#ifdef SQL_PARAM_ARRAY_SELECTS
                    DBG_STR_CASE(SQL_PARAM_ARRAY_SELECTS);
#endif // SQL_PARAM_ARRAY_SELECTS
#ifdef SQL_ALTER_DOMAIN
                    DBG_STR_CASE(SQL_ALTER_DOMAIN);
#endif // SQL_ALTER_DOMAIN
#ifdef SQL_ASYNC_MODE
                    DBG_STR_CASE(SQL_ASYNC_MODE);
#endif // SQL_ASYNC_MODE
#ifdef SQL_BATCH_ROW_COUNT
                    DBG_STR_CASE(SQL_BATCH_ROW_COUNT);
#endif // SQL_BATCH_ROW_COUNT
#ifdef SQL_BATCH_SUPPORT
                    DBG_STR_CASE(SQL_BATCH_SUPPORT);
#endif // SQL_BATCH_SUPPORT
#ifdef SQL_BOOKMARK_PERSISTENCE
                    DBG_STR_CASE(SQL_BOOKMARK_PERSISTENCE);
#endif // SQL_BOOKMARK_PERSISTENCE
#ifdef SQL_CATALOG_LOCATION
                    DBG_STR_CASE(SQL_CATALOG_LOCATION);
#endif // SQL_CATALOG_LOCATION
#ifdef SQL_CATALOG_NAME
                    DBG_STR_CASE(SQL_CATALOG_NAME);
#endif // SQL_CATALOG_NAME
#ifdef SQL_COLLATION_SEQ
                    DBG_STR_CASE(SQL_COLLATION_SEQ);
#endif // SQL_COLLATION_SEQ
#ifdef SQL_CONCAT_NULL_BEHAVIOR
                    DBG_STR_CASE(SQL_CONCAT_NULL_BEHAVIOR);
#endif // SQL_CONCAT_NULL_BEHAVIOR
#ifdef SQL_CORRELATION_NAME
                    DBG_STR_CASE(SQL_CORRELATION_NAME);
#endif // SQL_CORRELATION_NAME
                    default:
                        break;
                }
                return "<< UNKNOWN TYPE >>";
            }

#undef DBG_STR_CASE

            ConnectionInfo::ConnectionInfo() : strParams(), intParams(),
                shortParams()
            {
                //
                //======================= String Params =======================
                //

                // Driver name.
#ifdef SQL_DRIVER_NAME
                strParams[SQL_DRIVER_NAME] = "Apache Ignite";
#endif // SQL_DRIVER_NAME
#ifdef SQL_DBMS_NAME
                strParams[SQL_DBMS_NAME]   = "Apache Ignite";
#endif // SQL_DBMS_NAME

                // ODBC version.
#ifdef SQL_DRIVER_ODBC_VER
                strParams[SQL_DRIVER_ODBC_VER] = "03.00";
#endif // SQL_DRIVER_ODBC_VER
#ifdef SQL_DBMS_VER
                strParams[SQL_DBMS_VER]        = "03.00";
#endif // SQL_DBMS_VER

#ifdef SQL_DRIVER_VER
                // Driver version. At a minimum, the version is of the form ##.##.####, where the first two digits are
                // the major version, the next two digits are the minor version, and the last four digits are the
                // release version.
                strParams[SQL_DRIVER_VER] = "01.05.0000";
#endif // SQL_DRIVER_VER

#ifdef SQL_COLUMN_ALIAS
                // A character string: "Y" if the data source supports column aliases; otherwise, "N".
                strParams[SQL_COLUMN_ALIAS] = "Y";
#endif // SQL_COLUMN_ALIAS

#ifdef SQL_IDENTIFIER_QUOTE_CHAR
                // The character string that is used as the starting and ending delimiter of a quoted (delimited)
                // identifier in SQL statements. Identifiers passed as arguments to ODBC functions do not have to be
                // quoted. If the data source does not support quoted identifiers, a blank is returned.
                strParams[SQL_IDENTIFIER_QUOTE_CHAR] = "";
#endif // SQL_IDENTIFIER_QUOTE_CHAR

#ifdef SQL_CATALOG_NAME_SEPARATOR
                // A character string: the character or characters that the data source defines as the separator between
                // a catalog name and the qualified name element that follows or precedes it.
                strParams[SQL_CATALOG_NAME_SEPARATOR] = ".";
#endif // SQL_CATALOG_NAME_SEPARATOR

#ifdef SQL_SPECIAL_CHARACTERS
                // A character string that contains all special characters (that is, all characters except a through z,
                // A through Z, 0 through 9, and underscore) that can be used in an identifier name, such as a table
                // name, column name, or index name, on the data source.
                strParams[SQL_SPECIAL_CHARACTERS] = "";
#endif // SQL_SPECIAL_CHARACTERS

#ifdef SQL_CATALOG_TERM
                // A character string with the data source vendor's name for a catalog; for example, "database" or
                // "directory". This string can be in upper, lower, or mixed case. This InfoType has been renamed for
                // ODBC 3.0 from the ODBC 2.0 InfoType SQL_QUALIFIER_TERM.
                strParams[SQL_CATALOG_TERM] = "";
#endif // SQL_CATALOG_TERM

#ifdef SQL_QUALIFIER_TERM
                strParams[SQL_QUALIFIER_TERM] = "";
#endif // SQL_QUALIFIER_TERM

#ifdef SQL_TABLE_TERM
                // A character string with the data source vendor's name for a table; for example, "table" or "file".
                strParams[SQL_TABLE_TERM] = "table";
#endif // SQL_TABLE_TERM

#ifdef SQL_SCHEMA_TERM
                // A character string with the data source vendor's name for a schema; for example, "owner",
                // "Authorization ID", or "Schema".
                strParams[SQL_SCHEMA_TERM] = "schema";
#endif // SQL_SCHEMA_TERM

#ifdef SQL_NEED_LONG_DATA_LEN
                // A character string: "Y" if the data source needs the length of a long data value (the data type is
                // SQL_LONGVARCHAR, SQL_LONGVARBINARY) before that value is sent to the data source, "N" if it does not.
                strParams[SQL_NEED_LONG_DATA_LEN ] = "Y";
#endif // SQL_NEED_LONG_DATA_LEN

#ifdef SQL_ACCESSIBLE_PROCEDURES
                // A character string: "Y" if the user can execute all procedures returned by SQLProcedures; "N" if
                // there may be procedures returned that the user cannot execute.
                strParams[SQL_ACCESSIBLE_PROCEDURES] = "Y";
#endif // SQL_ACCESSIBLE_PROCEDURES

#ifdef SQL_ACCESSIBLE_TABLES
                // A character string: "Y" if the user is guaranteed SELECT privileges to all tables returned by
                // SQLTables; "N" if there may be tables returned that the user cannot access.
                strParams[SQL_ACCESSIBLE_TABLES] = "Y";
#endif // SQL_ACCESSIBLE_TABLES

#ifdef SQL_CATALOG_NAME
                // A character string: "Y" if the server supports catalog names, or "N" if it does not.
                // An SQL - 92 Full level-conformant driver will always return "Y".
                strParams[SQL_CATALOG_NAME] = "N";
#endif // SQL_CATALOG_NAME

#ifdef SQL_COLLATION_SEQ
                // The name of the collation sequence. This is a character string that indicates the name of the default
                // collation for the default character set for this server (for example, 'ISO 8859-1' or EBCDIC). If
                // this is unknown, an empty string will be returned. An SQL-92 Full level-conformant driver will always
                // return a non-empty string.
                strParams[SQL_COLLATION_SEQ] = "UTF-8";
#endif // SQL_COLLATION_SEQ

                //
                //====================== Integer Params =======================
                //

#ifdef SQL_ASYNC_DBC_FUNCTIONS
                // Indicates if the driver can execute functions asynchronously on the connection handle.
                // SQL_ASYNC_DBC_CAPABLE = The driver can execute connection functions asynchronously.
                // SQL_ASYNC_DBC_NOT_CAPABLE = The driver can not execute connection functions asynchronously.
                intParams[SQL_ASYNC_DBC_FUNCTIONS] = SQL_ASYNC_DBC_NOT_CAPABLE;
#endif // SQL_ASYNC_DBC_FUNCTIONS

#ifdef SQL_ASYNC_MODE
                // Indicates the level of asynchronous support in the driver:
                // SQL_AM_CONNECTION = Connection level asynchronous execution is supported.Either all statement handles
                //    associated with a given connection handle are in asynchronous mode or all are in synchronous mode.
                //    A statement handle on a connection cannot be in asynchronous mode while another statement handle
                //    on the same connection is in synchronous mode, and vice versa.
                // SQL_AM_STATEMENT = Statement level asynchronous execution is supported.Some statement handles
                //    associated with a connection handle can be in asynchronous mode, while other statement handles on
                //    the same connection are in synchronous mode.
                // SQL_AM_NONE = Asynchronous mode is not supported.
                intParams[SQL_ASYNC_MODE] = SQL_AM_NONE;
#endif // SQL_ASYNC_MODE

#ifdef SQL_ASYNC_NOTIFICATION
                // Indicates if the driver supports asynchronous notification:
                // SQL_ASYNC_NOTIFICATION_CAPABLE Asynchronous execution notification is supported by the driver.
                // SQL_ASYNC_NOTIFICATION_NOT_CAPABLE Asynchronous execution notification is not supported by the
                //     driver.
                //
                // There are two categories of ODBC asynchronous operations: connection level asynchronous operations
                // and statement level asynchronous operations. If a driver returns SQL_ASYNC_NOTIFICATION_CAPABLE, it
                // must support notification for all APIs that it can execute asynchronously.
                intParams[SQL_ASYNC_NOTIFICATION] = SQL_ASYNC_NOTIFICATION_NOT_CAPABLE;
#endif // SQL_ASYNC_NOTIFICATION

#ifdef SQL_BATCH_ROW_COUNT
                // Enumerates the behavior of the driver with respect to the availability of row counts. The following
                // bitmasks are used together with the information type:
                // SQL_BRC_ROLLED_UP = Row counts for consecutive INSERT, DELETE, or UPDATE statements are rolled up
                //     into one. If this bit is not set, row counts are available for each statement.
                // SQL_BRC_PROCEDURES = Row counts, if any, are available when a batch is executed in a stored
                //     procedure. If row counts are available, they can be rolled up or individually available,
                //     depending on the SQL_BRC_ROLLED_UP bit.
                // SQL_BRC_EXPLICIT = Row counts, if any, are available when a batch is executed directly by calling
                //     SQLExecute or SQLExecDirect. If row counts are available, they can be rolled up or individually
                //     available, depending on the SQL_BRC_ROLLED_UP bit.
                intParams[SQL_BATCH_ROW_COUNT] = SQL_BRC_ROLLED_UP | SQL_BRC_EXPLICIT;
#endif // SQL_BATCH_ROW_COUNT

#ifdef SQL_BATCH_SUPPORT
                // Bitmask enumerating the driver's support for batches. The following bitmasks are used to determine
                // which level is supported:
                // SQL_BS_SELECT_EXPLICIT = The driver supports explicit batches that can have result - set generating
                //     statements.
                // SQL_BS_ROW_COUNT_EXPLICIT = The driver supports explicit batches that can have row - count generating
                //     statements.
                // SQL_BS_SELECT_PROC = The driver supports explicit procedures that can have result - set generating
                //     statements.
                // SQL_BS_ROW_COUNT_PROC = The driver supports explicit procedures that can have row - count generating
                //     statements.
                intParams[SQL_BATCH_SUPPORT] = SQL_BS_ROW_COUNT_EXPLICIT;
#endif // SQL_BATCH_SUPPORT

#ifdef SQL_BOOKMARK_PERSISTENCE
                // Bitmask enumerating the operations through which bookmarks persist. The following bitmasks are used
                // together with the flag to determine through which options bookmarks persist:
                // SQL_BP_CLOSE = Bookmarks are valid after an application calls SQLFreeStmt with the SQL_CLOSE option,
                //     or SQLCloseCursor to close the cursor associated with a statement.
                // SQL_BP_DELETE = The bookmark for a row is valid after that row has been deleted.
                // SQL_BP_DROP = Bookmarks are valid after an application calls SQLFreeHandle with a HandleType of
                //     SQL_HANDLE_STMT to drop a statement.
                // SQL_BP_TRANSACTION = Bookmarks are valid after an application commits or rolls back a transaction.
                // SQL_BP_UPDATE = The bookmark for a row is valid after any column in that row has been updated,
                //     including key columns.
                // SQL_BP_OTHER_HSTMT = A bookmark associated with one statement can be used with another statement.
                //     Unless SQL_BP_CLOSE or SQL_BP_DROP is specified, the cursor on the first statement must be open.
                intParams[SQL_BOOKMARK_PERSISTENCE] = 0;
#endif // SQL_BOOKMARK_PERSISTENCE

#ifdef SQL_CATALOG_LOCATION
                // Value that indicates the position of the catalog in a qualified table name: SQL_CL_START, SQL_CL_END
                //
                // An SQL - 92 Full level-conformant driver will always return SQL_CL_START.A value of 0 is returned if
                // catalogs are not supported by the data source. This InfoType has been renamed for ODBC 3.0 from the
                // ODBC 2.0 InfoType SQL_QUALIFIER_LOCATION.
                intParams[SQL_CATALOG_LOCATION] = 0;
#endif // SQL_CATALOG_LOCATION

#ifdef SQL_QUALIFIER_LOCATION
                intParams[SQL_QUALIFIER_LOCATION] = 0;
#endif // SQL_QUALIFIER_LOCATION

#ifdef SQL_GETDATA_EXTENSIONS
                // Bitmask enumerating extensions to SQLGetData.
                intParams[SQL_GETDATA_EXTENSIONS] = SQL_GD_ANY_COLUMN | SQL_GD_ANY_ORDER | SQL_GD_BOUND;
#endif // SQL_GETDATA_EXTENSIONS

#ifdef SQL_ODBC_INTERFACE_CONFORMANCE
                // Indicates the level of the ODBC 3.x interface that the driver
                // complies with.
                intParams[SQL_ODBC_INTERFACE_CONFORMANCE] = SQL_OIC_CORE;
#endif // SQL_ODBC_INTERFACE_CONFORMANCE

#ifdef SQL_SQL_CONFORMANCE
                // Indicates the level of SQL-92 supported by the driver.
                intParams[SQL_SQL_CONFORMANCE] = SQL_SC_SQL92_ENTRY;
#endif // SQL_SQL_CONFORMANCE

#ifdef SQL_CATALOG_USAGE
                // Bitmask enumerating the statements in which catalogs can be used.
                // The following bitmasks are used to determine where catalogs can be used:
                // SQL_CU_DML_STATEMENTS = Catalogs are supported in all Data Manipulation Language statements :
                //     SELECT, INSERT, UPDATE, DELETE, and if supported, SELECT FOR UPDATE and positioned update and
                //     delete statements.
                // SQL_CU_PROCEDURE_INVOCATION = Catalogs are supported in the ODBC procedure invocation statement.
                // SQL_CU_TABLE_DEFINITION = Catalogs are supported in all table definition statements : CREATE TABLE,
                //     CREATE VIEW, ALTER TABLE, DROP TABLE, and DROP VIEW.
                // SQL_CU_INDEX_DEFINITION = Catalogs are supported in all index definition statements : CREATE INDEX
                //     and DROP INDEX.
                // SQL_CU_PRIVILEGE_DEFINITION = Catalogs are supported in all privilege definition statements : GRANT
                //     and REVOKE.
                //
                // A value of 0 is returned if catalogs are not supported by the data source.To determine whether
                // catalogs are supported, an application calls SQLGetInfo with the SQL_CATALOG_NAME information type.
                // An SQL - 92 Full level-conformant driver will always return a bitmask with all of these bits set.
                // This InfoType has been renamed for ODBC 3.0 from the ODBC 2.0 InfoType SQL_QUALIFIER_USAGE.
                intParams[SQL_CATALOG_USAGE] = 0;
#endif // SQL_CATALOG_USAGE

#ifdef SQL_QUALIFIER_USAGE
                intParams[SQL_QUALIFIER_USAGE] = 0;
#endif // SQL_QUALIFIER_USAGE

#ifdef SQL_SCHEMA_USAGE
                // Bitmask enumerating the statements in which schemas can be used.
                intParams[SQL_SCHEMA_USAGE] = SQL_SU_DML_STATEMENTS | SQL_SU_TABLE_DEFINITION |
                    SQL_SU_PRIVILEGE_DEFINITION;
#endif // SQL_SCHEMA_USAGE

#ifdef SQL_MAX_IDENTIFIER_LEN
                // Indicates the maximum size in characters that the data source supports for user-defined names.
                intParams[SQL_MAX_IDENTIFIER_LEN] = 128;
#endif // SQL_MAX_IDENTIFIER_LEN

#ifdef SQL_AGGREGATE_FUNCTIONS
                // Bitmask enumerating support for aggregation functions.
                intParams[SQL_AGGREGATE_FUNCTIONS] = SQL_AF_AVG | SQL_AF_COUNT | SQL_AF_MAX | SQL_AF_MIN | SQL_AF_SUM |
                    SQL_AF_DISTINCT;
#endif // SQL_AGGREGATE_FUNCTIONS

#ifdef SQL_NUMERIC_FUNCTIONS
                // Bitmask enumerating the scalar numeric functions supported by the driver and associated data source.
                intParams[SQL_NUMERIC_FUNCTIONS] = SQL_FN_NUM_ABS | SQL_FN_NUM_ACOS | SQL_FN_NUM_ASIN | SQL_FN_NUM_EXP |
                    SQL_FN_NUM_ATAN | SQL_FN_NUM_ATAN2 | SQL_FN_NUM_CEILING | SQL_FN_NUM_COS | SQL_FN_NUM_TRUNCATE |
                    SQL_FN_NUM_FLOOR | SQL_FN_NUM_DEGREES | SQL_FN_NUM_POWER | SQL_FN_NUM_RADIANS | SQL_FN_NUM_SIGN |
                    SQL_FN_NUM_SIN | SQL_FN_NUM_LOG | SQL_FN_NUM_TAN | SQL_FN_NUM_PI | SQL_FN_NUM_MOD | SQL_FN_NUM_COT |
                    SQL_FN_NUM_LOG10 | SQL_FN_NUM_ROUND | SQL_FN_NUM_SQRT | SQL_FN_NUM_RAND;
#endif // SQL_NUMERIC_FUNCTIONS

#ifdef SQL_STRING_FUNCTIONS
                // Bitmask enumerating the scalar string functions supported by the driver and associated data source.
                intParams[SQL_STRING_FUNCTIONS] = SQL_FN_STR_ASCII | SQL_FN_STR_BIT_LENGTH | SQL_FN_STR_CHAR_LENGTH |
                    SQL_FN_STR_CHAR | SQL_FN_STR_CONCAT | SQL_FN_STR_DIFFERENCE | SQL_FN_STR_INSERT | SQL_FN_STR_LEFT |
                    SQL_FN_STR_LENGTH | SQL_FN_STR_CHARACTER_LENGTH | SQL_FN_STR_LTRIM | SQL_FN_STR_OCTET_LENGTH |
                    SQL_FN_STR_POSITION | SQL_FN_STR_REPEAT | SQL_FN_STR_REPLACE | SQL_FN_STR_RIGHT | SQL_FN_STR_RTRIM |
                    SQL_FN_STR_SOUNDEX | SQL_FN_STR_SPACE | SQL_FN_STR_SUBSTRING | SQL_FN_STR_LCASE | SQL_FN_STR_UCASE |
                    SQL_FN_STR_LOCATE_2 | SQL_FN_STR_LOCATE;
#endif // SQL_STRING_FUNCTIONS

#ifdef SQL_TIMEDATE_FUNCTIONS
                // Bitmask enumerating the scalar date and time functions supported by the driver and associated data
                // source.
                intParams[SQL_TIMEDATE_FUNCTIONS] = SQL_FN_TD_CURRENT_DATE | SQL_FN_TD_CURRENT_TIME | SQL_FN_TD_WEEK |
                    SQL_FN_TD_QUARTER | SQL_FN_TD_SECOND | SQL_FN_TD_CURDATE | SQL_FN_TD_CURTIME | SQL_FN_TD_DAYNAME |
                    SQL_FN_TD_MINUTE | SQL_FN_TD_DAYOFWEEK | SQL_FN_TD_DAYOFYEAR | SQL_FN_TD_EXTRACT | SQL_FN_TD_HOUR |
                    SQL_FN_TD_DAYOFMONTH | SQL_FN_TD_MONTH | SQL_FN_TD_MONTHNAME | SQL_FN_TD_NOW | SQL_FN_TD_YEAR |
                    SQL_FN_TD_CURRENT_TIMESTAMP;
#endif // SQL_TIMEDATE_FUNCTIONS

#ifdef SQL_TIMEDATE_ADD_INTERVALS
                // Bitmask enumerating timestamp intervals supported by the driver and associated data source for the
                // TIMESTAMPADD scalar function.
                intParams[SQL_TIMEDATE_ADD_INTERVALS] = 0;
#endif // SQL_TIMEDATE_ADD_INTERVALS

#ifdef SQL_TIMEDATE_DIFF_INTERVALS
                // Bitmask enumerating timestamp intervals supported by the driver and associated data source for the
                // TIMESTAMPDIFF scalar function.
                intParams[SQL_TIMEDATE_DIFF_INTERVALS] = 0;
#endif // SQL_TIMEDATE_DIFF_INTERVALS

#ifdef SQL_DATETIME_LITERALS
                // Bitmask enumerating the SQL-92 datetime literals supported by the data source.
                intParams[SQL_DATETIME_LITERALS] =  SQL_DL_SQL92_DATE | SQL_DL_SQL92_TIME | SQL_DL_SQL92_TIMESTAMP;
#endif // SQL_DATETIME_LITERALS

#ifdef SQL_SYSTEM_FUNCTIONS
                // Bitmask enumerating the scalar system functions supported by the driver and associated data source.
                intParams[SQL_SYSTEM_FUNCTIONS] = SQL_FN_SYS_USERNAME | SQL_FN_SYS_DBNAME | SQL_FN_SYS_IFNULL;
#endif // SQL_SYSTEM_FUNCTIONS

#ifdef SQL_CONVERT_FUNCTIONS
                // Bitmask enumerating the scalar conversion functions supported by the driver and associated data
                // source.
                intParams[SQL_CONVERT_FUNCTIONS] = SQL_FN_CVT_CONVERT | SQL_FN_CVT_CAST;
#endif // SQL_CONVERT_FUNCTIONS

#ifdef SQL_OJ_CAPABILITIES
                // Bitmask enumerating the types of outer joins supported by the driver and data source.
                intParams[SQL_OJ_CAPABILITIES] = SQL_OJ_LEFT | SQL_OJ_NOT_ORDERED | SQL_OJ_ALL_COMPARISON_OPS;
#endif // SQL_OJ_CAPABILITIES

#ifdef SQL_POS_OPERATIONS
                // Bitmask enumerating the support operations in SQLSetPos.
                intParams[SQL_POS_OPERATIONS] = 0;
#endif // SQL_POS_OPERATIONS

#ifdef SQL_SQL92_NUMERIC_VALUE_FUNCTIONS
                // Bitmask enumerating the numeric value scalar functions.
                intParams[SQL_SQL92_NUMERIC_VALUE_FUNCTIONS] = 0;
#endif // SQL_SQL92_NUMERIC_VALUE_FUNCTIONS

#ifdef SQL_SQL92_STRING_FUNCTIONS
                // Bitmask enumerating the string scalar functions.
                intParams[SQL_SQL92_STRING_FUNCTIONS] = SQL_SSF_LOWER | SQL_SSF_UPPER | SQL_SSF_TRIM_TRAILING |
                    SQL_SSF_SUBSTRING | SQL_SSF_TRIM_BOTH | SQL_SSF_TRIM_LEADING;
#endif // SQL_SQL92_STRING_FUNCTIONS

#ifdef SQL_SQL92_DATETIME_FUNCTIONS
                // Bitmask enumerating the datetime scalar functions.
                intParams[SQL_SQL92_DATETIME_FUNCTIONS] = SQL_SDF_CURRENT_DATE |
                    SQL_SDF_CURRENT_TIMESTAMP;
#endif // SQL_SQL92_DATETIME_FUNCTIONS

#ifdef SQL_SQL92_VALUE_EXPRESSIONS
                // Bitmask enumerating the value expressions supported, as defined in SQL-92.
                intParams[SQL_SQL92_VALUE_EXPRESSIONS] = SQL_SVE_CASE |
                    SQL_SVE_CAST | SQL_SVE_COALESCE | SQL_SVE_NULLIF;
#endif // SQL_SQL92_VALUE_EXPRESSIONS

#ifdef SQL_SQL92_PREDICATES
                // Bitmask enumerating the datetime scalar functions.
                intParams[SQL_SQL92_PREDICATES] = SQL_SP_BETWEEN | SQL_SP_COMPARISON | SQL_SP_EXISTS | SQL_SP_IN |
                    SQL_SP_ISNOTNULL | SQL_SP_ISNULL | SQL_SP_LIKE | SQL_SP_MATCH_FULL | SQL_SP_MATCH_PARTIAL |
                    SQL_SP_MATCH_UNIQUE_FULL | SQL_SP_MATCH_UNIQUE_PARTIAL | SQL_SP_OVERLAPS | SQL_SP_UNIQUE |
                    SQL_SP_QUANTIFIED_COMPARISON;
#endif // SQL_SQL92_PREDICATES

#ifdef SQL_SQL92_RELATIONAL_JOIN_OPERATORS
                // Bitmask enumerating the relational join operators supported in a SELECT statement, as defined
                // in SQL-92.
                intParams[SQL_SQL92_RELATIONAL_JOIN_OPERATORS] = SQL_SRJO_CORRESPONDING_CLAUSE | SQL_SRJO_CROSS_JOIN |
                    SQL_SRJO_EXCEPT_JOIN | SQL_SRJO_INNER_JOIN | SQL_SRJO_LEFT_OUTER_JOIN| SQL_SRJO_RIGHT_OUTER_JOIN |
                    SQL_SRJO_NATURAL_JOIN | SQL_SRJO_INTERSECT_JOIN | SQL_SRJO_UNION_JOIN;
#endif // SQL_SQL92_RELATIONAL_JOIN_OPERATORS

#ifdef SQL_STATIC_CURSOR_ATTRIBUTES1
                // Bitmask that describes the attributes of a static cursor that are supported by the driver. This
                // bitmask contains the first subset of attributes; for the second subset, see
                // SQL_STATIC_CURSOR_ATTRIBUTES2.
                intParams[SQL_STATIC_CURSOR_ATTRIBUTES1] = SQL_CA1_NEXT;
#endif // SQL_STATIC_CURSOR_ATTRIBUTES1

#ifdef SQL_STATIC_CURSOR_ATTRIBUTES2
                // Bitmask that describes the attributes of a static cursor that are supported by the driver. This
                // bitmask contains the second subset of attributes; for the first subset, see
                // SQL_STATIC_CURSOR_ATTRIBUTES1.
                intParams[SQL_STATIC_CURSOR_ATTRIBUTES2] = 0;
#endif // SQL_STATIC_CURSOR_ATTRIBUTES2

#ifdef SQL_CONVERT_BIGINT
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type BIGINT
                intParams[SQL_CONVERT_BIGINT] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_TIMESTAMP | SQL_CVT_TINYINT |
                    SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_BIT;
#endif // SQL_CONVERT_BIGINT

#ifdef SQL_CONVERT_BINARY
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type BINARY
                intParams[SQL_CONVERT_BINARY] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_BIT |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_FLOAT | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_DATE |
                    SQL_CVT_TINYINT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_DECIMAL | SQL_CVT_TIME | SQL_CVT_GUID |
                    SQL_CVT_TIMESTAMP | SQL_CVT_VARBINARY;
#endif // SQL_CONVERT_BINARY

#ifdef SQL_CONVERT_BIT
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type BIT
                intParams[SQL_CONVERT_BIT] = SQL_CVT_BIGINT | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WVARCHAR |
                    SQL_CVT_NUMERIC | SQL_CVT_BIT | SQL_CVT_CHAR | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_TINYINT;
#endif // SQL_CONVERT_BIT

#ifdef SQL_CONVERT_CHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type CHAR
                intParams[SQL_CONVERT_CHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_VARBINARY |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL |
                    SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL |
                    SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_TIMESTAMP | SQL_CVT_DATE | SQL_CVT_TIME |
                    SQL_CVT_LONGVARBINARY | SQL_CVT_GUID;
#endif // SQL_CONVERT_CHAR

#ifdef SQL_CONVERT_VARCHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type VARCHAR
                intParams[SQL_CONVERT_VARCHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TINYINT |
                    SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_FLOAT | SQL_CVT_BIT |
                    SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_GUID | SQL_CVT_DATE | SQL_CVT_TIME |
                    SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_VARCHAR

#ifdef SQL_CONVERT_LONGVARCHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type
                // LONGVARCHAR
                intParams[SQL_CONVERT_LONGVARCHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_DATE | SQL_CVT_TIME |
                    SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC |
                    SQL_CVT_DECIMAL | SQL_CVT_BIT | SQL_CVT_REAL | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_GUID |
                    SQL_CVT_BIGINT | SQL_CVT_LONGVARBINARY | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY |
                    SQL_CVT_TINYINT | SQL_CVT_FLOAT | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_LONGVARCHAR

#ifdef SQL_CONVERT_WCHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type WCHAR
                intParams[SQL_CONVERT_WCHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_VARBINARY |
                    SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL |
                    SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP |
                    SQL_CVT_DATE | SQL_CVT_TIME | SQL_CVT_GUID;
#endif // SQL_CONVERT_WCHAR

#ifdef SQL_CONVERT_WVARCHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type WVARCHAR
                intParams[SQL_CONVERT_WVARCHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_REAL |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_DECIMAL | SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_DATE | SQL_CVT_TIME |
                    SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_GUID |
                    SQL_CVT_VARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_WVARCHAR

#ifdef SQL_CONVERT_WLONGVARCHAR
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type
                // WLONGVARCHAR
                intParams[SQL_CONVERT_WLONGVARCHAR] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_BIGINT | SQL_CVT_REAL |
                    SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_VARBINARY |
                    SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_DATE | SQL_CVT_FLOAT |
                    SQL_CVT_INTEGER | SQL_CVT_SMALLINT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_TIME | SQL_CVT_TIMESTAMP | SQL_CVT_GUID;
#endif // SQL_CONVERT_WLONGVARCHAR

#ifdef SQL_CONVERT_GUID
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type GUID
                intParams[SQL_CONVERT_GUID] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WVARCHAR |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_GUID;
#endif // SQL_CONVERT_GUID

#ifdef SQL_CONVERT_DATE
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type DATE
                intParams[SQL_CONVERT_DATE] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_INTEGER | SQL_CVT_BIGINT |
                    SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_DATE | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_DATE

#ifdef SQL_CONVERT_DECIMAL
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type DECIMAL
                intParams[SQL_CONVERT_DECIMAL] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TIMESTAMP |
                    SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL |
                    SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY;
#endif // SQL_CONVERT_DECIMAL

#ifdef SQL_CONVERT_DOUBLE
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type DOUBLE
                intParams[SQL_CONVERT_DOUBLE] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TIMESTAMP |
                    SQL_CVT_TINYINT | SQL_CVT_BIGINT | SQL_CVT_INTEGER | SQL_CVT_FLOAT | SQL_CVT_REAL | SQL_CVT_DOUBLE |
                    SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_SMALLINT | SQL_CVT_LONGVARBINARY;
#endif // SQL_CONVERT_DOUBLE

#ifdef SQL_CONVERT_FLOAT
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type FLOAT
                intParams[SQL_CONVERT_FLOAT] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WVARCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TINYINT | SQL_CVT_SMALLINT |
                    SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY |
                    SQL_CVT_VARBINARY | SQL_CVT_WCHAR | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP | SQL_CVT_BIT;
#endif // SQL_CONVERT_FLOAT

#ifdef SQL_CONVERT_REAL
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type REAL
                intParams[SQL_CONVERT_REAL] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WVARCHAR |
                    SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_BIT | SQL_CVT_FLOAT | SQL_CVT_SMALLINT | SQL_CVT_REAL |
                    SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_DOUBLE | SQL_CVT_TINYINT | SQL_CVT_WLONGVARCHAR |
                    SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP | SQL_CVT_WCHAR;
#endif // SQL_CONVERT_REAL

#ifdef SQL_CONVERT_INTEGER
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type INTEGER
                intParams[SQL_CONVERT_INTEGER] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TINYINT |
                    SQL_CVT_SMALLINT | SQL_CVT_BIT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_FLOAT |
                    SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_INTEGER

#ifdef SQL_CONVERT_NUMERIC
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type NUMERIC
                intParams[SQL_CONVERT_NUMERIC] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_SMALLINT |
                    SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_FLOAT |
                    SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_NUMERIC

#ifdef SQL_CONVERT_SMALLINT
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type SMALLINT
                intParams[SQL_CONVERT_SMALLINT] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_VARBINARY |
                    SQL_CVT_BIT | SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL |
                    SQL_CVT_FLOAT | SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_SMALLINT

#ifdef SQL_CONVERT_TINYINT
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type TINYINT
                intParams[SQL_CONVERT_TINYINT] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WCHAR |
                    SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL | SQL_CVT_TINYINT |
                    SQL_CVT_SMALLINT | SQL_CVT_BIT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_FLOAT |
                    SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_TINYINT

#ifdef SQL_CONVERT_TIME
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type TIME
                intParams[SQL_CONVERT_TIME] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_WVARCHAR |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_TIME | SQL_CVT_TIMESTAMP;
#endif // SQL_CONVERT_TIME

#ifdef SQL_CONVERT_TIMESTAMP
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type TIMESTAMP
                intParams[SQL_CONVERT_TIMESTAMP] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_DATE |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_DECIMAL | SQL_CVT_INTEGER | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_TIMESTAMP |
                    SQL_CVT_BIGINT | SQL_CVT_TIME;
#endif // SQL_CONVERT_TIMESTAMP

#ifdef SQL_CONVERT_VARBINARY
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type VARBINARY
                intParams[SQL_CONVERT_VARBINARY] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_BIT |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_NUMERIC | SQL_CVT_DECIMAL |
                    SQL_CVT_TINYINT | SQL_CVT_SMALLINT | SQL_CVT_DATE | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_FLOAT |
                    SQL_CVT_DOUBLE | SQL_CVT_INTEGER | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY |
                    SQL_CVT_TIME | SQL_CVT_TIMESTAMP | SQL_CVT_GUID;
#endif // SQL_CONVERT_VARBINARY

#ifdef SQL_CONVERT_LONGVARBINARY
                // Bitmask indicates the conversions supported by the CONVERT scalar function for target type
                // LONGVARBINARY
                intParams[SQL_CONVERT_LONGVARBINARY] = SQL_CVT_CHAR | SQL_CVT_VARCHAR | SQL_CVT_BIT | SQL_CVT_TINYINT |
                    SQL_CVT_WCHAR | SQL_CVT_WLONGVARCHAR | SQL_CVT_WVARCHAR | SQL_CVT_LONGVARCHAR | SQL_CVT_NUMERIC |
                    SQL_CVT_DECIMAL | SQL_CVT_FLOAT | SQL_CVT_INTEGER | SQL_CVT_BIGINT | SQL_CVT_REAL | SQL_CVT_DATE |
                    SQL_CVT_DOUBLE | SQL_CVT_BINARY | SQL_CVT_VARBINARY | SQL_CVT_LONGVARBINARY | SQL_CVT_TIMESTAMP |
                    SQL_CVT_SMALLINT | SQL_CVT_TIME | SQL_CVT_GUID;
#endif // SQL_CONVERT_LONGVARBINARY

#ifdef SQL_PARAM_ARRAY_ROW_COUNTS
                // Enumerating the driver's properties regarding the availability of row counts in a parameterized
                // execution. Has the following values:
                //
                // SQL_PARC_BATCH = Individual row counts are available for each set of parameters. This is conceptually
                //     equivalent to the driver generating a batch of SQL statements, one for each parameter set in the
                //     array. Extended error information can be retrieved by using the SQL_PARAM_STATUS_PTR descriptor
                //     field.
                //
                // SQL_PARC_NO_BATCH = There is only one row count available, which is the cumulative row count
                //     resulting from the execution of the statement for the entire array of parameters. This is
                //     conceptually equivalent to treating the statement together with the complete parameter array as
                //     one atomic unit. Errors are handled the same as if one statement were executed.
                intParams[SQL_PARAM_ARRAY_ROW_COUNTS] = SQL_PARC_NO_BATCH;
#endif // SQL_PARAM_ARRAY_ROW_COUNTS

#ifdef SQL_PARAM_ARRAY_SELECTS
                // Enumerating the driver's properties regarding the availability of result sets in a parameterized
                // execution. Has the following values:
                //
                // SQL_PAS_BATCH = There is one result set available per set of parameters. This is conceptually
                //     equivalent to the driver generating a batch of SQL statements, one for each parameter set in
                //     the array.
                //
                // SQL_PAS_NO_BATCH = There is only one result set available, which represents the cumulative result set
                //     resulting from the execution of the statement for the complete array of parameters. This is
                //     conceptually equivalent to treating the statement together with the complete parameter array as
                //     one atomic unit.
                //
                // SQL_PAS_NO_SELECT = A driver does not allow a result - set generating statement to be executed with
                //     an array of parameters.
                intParams[SQL_PARAM_ARRAY_SELECTS] = SQL_PAS_NO_SELECT;
#endif // SQL_PARAM_ARRAY_SELECTS

#ifdef SQL_SCROLL_OPTIONS
                // Bitmask enumerating the scroll options supported for scrollable cursors
                // SQL_SO_FORWARD_ONLY = The cursor only scrolls forward. (ODBC 1.0)
                // SQL_SO_STATIC = The data in the result set is static. (ODBC 2.0)
                // SQL_SO_KEYSET_DRIVEN = The driver saves and uses the keys for every row in the result set. (ODBC 1.0)
                // SQL_SO_DYNAMIC = The driver keeps the keys for every row in the rowset(the keyset size is the same
                //     as the rowset size). (ODBC 1.0)
                // SQL_SO_MIXED = The driver keeps the keys for every row in the keyset, and the keyset size is greater
                //     than the rowset size.The cursor is keyset - driven inside the keyset and dynamic outside the
                //     keyset. (ODBC 1.0)
                intParams[SQL_SCROLL_OPTIONS] = SQL_SO_FORWARD_ONLY | SQL_SO_STATIC;
#endif // SQL_SCROLL_OPTIONS

#ifdef SQL_ALTER_DOMAIN
                // Bitmask enumerating the clauses in the ALTER DOMAIN statement, as defined in SQL-92, supported by the
                // data source. An SQL-92 Full level-compliant driver will always return all the bitmasks. A return
                // value of "0" means that the ALTER DOMAIN statement is not supported.
                //
                // The SQL - 92 or FIPS conformance level at which this feature must be supported is shown in
                // parentheses next to each bitmask.
                //
                // The following bitmasks are used to determine which clauses are supported :
                // SQL_AD_ADD_DOMAIN_CONSTRAINT = Adding a domain constraint is supported (Full level).
                // SQL_AD_ADD_DOMAIN_DEFAULT = <alter domain> <set domain default clause> is supported (Full level).
                // SQL_AD_CONSTRAINT_NAME_DEFINITION = <constraint name definition clause> is supported for naming
                //     domain constraint (Intermediate level).
                // SQL_AD_DROP_DOMAIN_CONSTRAINT = <drop domain constraint clause> is supported (Full level).
                // SQL_AD_DROP_DOMAIN_DEFAULT = <alter domain> <drop domain default clause> is supported (Full level).
                //
                // The following bits specify the supported <constraint attributes> if <add domain constraint> is
                // supported (the SQL_AD_ADD_DOMAIN_CONSTRAINT bit is set) :
                // SQL_AD_ADD_CONSTRAINT_DEFERRABLE (Full level)
                // SQL_AD_ADD_CONSTRAINT_NON_DEFERRABLE (Full level)
                // SQL_AD_ADD_CONSTRAINT_INITIALLY_DEFERRED (Full level)
                // SQL_AD_ADD_CONSTRAINT_INITIALLY_IMMEDIATE (Full level)
                intParams[SQL_ALTER_DOMAIN] = 0;
#endif // SQL_ALTER_DOMAIN

#ifdef SQL_ALTER_TABLE
                // Bitmask enumerating the clauses in the ALTER TABLE statement supported by the data source.
                //
                // The SQL - 92 or FIPS conformance level at which this feature must be supported is shown in
                // parentheses next to each bitmask. The following bitmasks are used to determine which clauses are
                // supported :
                // SQL_AT_ADD_COLUMN_COLLATION = <add column> clause is supported, with facility to specify column
                //     collation (Full level) (ODBC 3.0)
                // SQL_AT_ADD_COLUMN_DEFAULT = <add column> clause is supported, with facility to specify column
                //     defaults (FIPS Transitional level) (ODBC 3.0)
                // SQL_AT_ADD_COLUMN_SINGLE = <add column> is supported (FIPS Transitional level) (ODBC 3.0)
                // SQL_AT_ADD_CONSTRAINT = <add column> clause is supported, with facility to specify column
                //     constraints (FIPS Transitional level) (ODBC 3.0)
                // SQL_AT_ADD_TABLE_CONSTRAINT = <add table constraint> clause is supported(FIPS Transitional level)
                //     (ODBC 3.0)
                // SQL_AT_CONSTRAINT_NAME_DEFINITION = <constraint name definition> is supported for naming column and
                //     table constraints(Intermediate level) (ODBC 3.0)
                // SQL_AT_DROP_COLUMN_CASCADE = <drop column> CASCADE is supported (FIPS Transitional level) (ODBC 3.0)
                // SQL_AT_DROP_COLUMN_DEFAULT = <alter column> <drop column default clause> is supported (Intermediate
                //     level) (ODBC 3.0)
                // SQL_AT_DROP_COLUMN_RESTRICT = <drop column> RESTRICT is supported (FIPS Transitional level)
                //     (ODBC 3.0)
                // SQL_AT_DROP_TABLE_CONSTRAINT_CASCADE (ODBC 3.0)
                // SQL_AT_DROP_TABLE_CONSTRAINT_RESTRICT = <drop column> RESTRICT is supported(FIPS Transitional level)
                //     (ODBC 3.0)
                // SQL_AT_SET_COLUMN_DEFAULT = <alter column> <set column default clause> is supported (Intermediate
                //     level) (ODBC 3.0)
                //
                // The following bits specify the support <constraint attributes> if specifying column or table
                // constraints is supported (the SQL_AT_ADD_CONSTRAINT bit is set) :
                // SQL_AT_CONSTRAINT_INITIALLY_DEFERRED (Full level) (ODBC 3.0)
                // SQL_AT_CONSTRAINT_INITIALLY_IMMEDIATE (Full level) (ODBC 3.0)
                // SQL_AT_CONSTRAINT_DEFERRABLE (Full level) (ODBC 3.0)
                // SQL_AT_CONSTRAINT_NON_DEFERRABLE (Full level) (ODBC 3.0)
                intParams[SQL_ALTER_TABLE] = 0;
#endif // SQL_ALTER_TABLE

                //
                //======================= Short Params ========================
                //

#ifdef SQL_MAX_CONCURRENT_ACTIVITIES
                // The maximum number of active statements that the driver can  support for a connection. Zero mean no
                // limit.
                shortParams[SQL_MAX_CONCURRENT_ACTIVITIES] = 32;
#endif // SQL_MAX_CONCURRENT_ACTIVITIES

#ifdef SQL_CURSOR_COMMIT_BEHAVIOR
                // Indicates how a COMMIT operation affects cursors and prepared statements in the data source.
                shortParams[SQL_CURSOR_COMMIT_BEHAVIOR] = SQL_CB_PRESERVE;
#endif // SQL_CURSOR_COMMIT_BEHAVIOR

#ifdef SQL_CURSOR_ROLLBACK_BEHAVIOR
                // Indicates how a ROLLBACK  operation affects cursors and prepared statements in the data source.
                shortParams[SQL_CURSOR_ROLLBACK_BEHAVIOR] = SQL_CB_PRESERVE;
#endif // SQL_CURSOR_ROLLBACK_BEHAVIOR

#ifdef SQL_TXN_CAPABLE
                // Describs the transaction support in the driver or data source.
                shortParams[SQL_TXN_CAPABLE] = SQL_TC_NONE;
#endif // SQL_TXN_CAPABLE

#ifdef SQL_QUOTED_IDENTIFIER_CASE
                // Case-sensitiveness of the quoted identifiers in SQL.
                shortParams[SQL_QUOTED_IDENTIFIER_CASE] = SQL_IC_SENSITIVE;
#endif // SQL_QUOTED_IDENTIFIER_CASE

#ifdef SQL_ACTIVE_ENVIRONMENTS
                // The maximum number of active environments that the driver can support. If there is no specified limit
                // or the limit is unknown, this value is set to zero.
                shortParams[SQL_ACTIVE_ENVIRONMENTS] = 0;
#endif // SQL_ACTIVE_ENVIRONMENTS

#ifdef SQL_CONCAT_NULL_BEHAVIOR
                // Indicates how the data source handles the concatenation of NULL valued character data type columns
                // with non-NULL valued character data type columns:
                // SQL_CB_NULL = Result is NULL valued.
                // SQL_CB_NON_NULL = Result is concatenation of non - NULL valued column or columns.
                // An SQL - 92 Entry level-conformant driver will always return SQL_CB_NULL.
                shortParams[SQL_CONCAT_NULL_BEHAVIOR] = SQL_CB_NULL;
#endif // SQL_CONCAT_NULL_BEHAVIOR

#ifdef SQL_CORRELATION_NAME
                // Value that indicates whether table correlation names are supported:
                // SQL_CN_NONE = Correlation names are not supported.
                // SQL_CN_DIFFERENT = Correlation names are supported but must differ from the names of the tables they
                //     represent.
                // SQL_CN_ANY = Correlation names are supported and can be any valid user - defined name.
                // An SQL - 92 Entry level-conformant driver will always return SQL_CN_ANY.
                shortParams[SQL_CORRELATION_NAME] = SQL_CB_NULL;
#endif // SQL_CORRELATION_NAME
            }

            ConnectionInfo::~ConnectionInfo()
            {
                // No-op.
            }

            SqlResult::Type ConnectionInfo::GetInfo(InfoType type, void* buf,
                short buflen, short* reslen) const
            {
                if (!buf)
                    return SqlResult::AI_ERROR;

                StringInfoMap::const_iterator itStr = strParams.find(type);

                if (itStr != strParams.end())
                {
                    if (!buflen)
                        return SqlResult::AI_ERROR;

                    unsigned short strlen = static_cast<short>(
                        utility::CopyStringToBuffer(itStr->second,
                            reinterpret_cast<char*>(buf), buflen));

                    if (reslen)
                        *reslen = strlen;

                    return SqlResult::AI_SUCCESS;
                }

                UintInfoMap::const_iterator itInt = intParams.find(type);

                if (itInt != intParams.end())
                {
                    unsigned int *res = reinterpret_cast<unsigned int*>(buf);

                    *res = itInt->second;

                    return SqlResult::AI_SUCCESS;
                }

                UshortInfoMap::const_iterator itShort = shortParams.find(type);

                if (itShort != shortParams.end())
                {
                    unsigned short *res = reinterpret_cast<unsigned short*>(buf);

                    *res = itShort->second;

                    return SqlResult::AI_SUCCESS;
                }

                return SqlResult::AI_ERROR;
            }
        }
    }
}

