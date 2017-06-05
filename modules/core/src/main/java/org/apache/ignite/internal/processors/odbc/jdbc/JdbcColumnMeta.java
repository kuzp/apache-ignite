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

import org.apache.ignite.internal.binary.BinaryReaderExImpl;
import org.apache.ignite.internal.binary.BinaryWriterExImpl;
import org.apache.ignite.internal.jdbc.thin.JdbcThinUtils;
import org.apache.ignite.internal.processors.query.GridQueryFieldMetadata;

/**
 * JDBC column metadata.
 */
public class JdbcColumnMeta implements JdbcRawBinarylizable {
    /** Cache name. */
    private String schemaName;

    /** Table name. */
    private String tableName;

    /** Column name. */
    private String columnName;

    /** Data type. */
    private int dataType;

    /** Data type. */
    private String dataTypeName;

    /** Data type. */
    private String dataTypeClass;

    /**
     * Default constructor is used for serialization.
     */
    JdbcColumnMeta() {
    }

    /**
     * @param info Field metadata.
     */
    JdbcColumnMeta(GridQueryFieldMetadata info) {
        this.schemaName = info.schemaName();
        this.tableName = info.typeName();
        this.columnName = info.fieldName();

        dataType = JdbcThinUtils.type(info.fieldTypeName());
        dataTypeName = JdbcThinUtils.typeName(info.fieldTypeName());
        dataTypeClass = info.fieldTypeName();
    }

    /**
     * @param schemaName Schema.
     * @param tableName Table.
     * @param columnName Column.
     * @param cls Type.
     */
    public JdbcColumnMeta(String schemaName, String tableName, String columnName, Class<?> cls) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;

        String type = cls.getName();
        dataType = JdbcThinUtils.type(type);
        dataTypeName = JdbcThinUtils.typeName(type);
        dataTypeClass = type;
    }


        /**
         * @return Schema name.
         */
    public String schemaName() {
        return schemaName;
    }

    /**
     * @return Table name.
     */
    public String tableName() {
        return tableName;
    }

    /**
     * @return Column name.
     */
    public String columnName() {
        return columnName;
    }

    /**
     * @return Column's data type.
     */
    public int dataType() {
        return dataType;
    }

    /**
     * @return Column's data type name.
     */
    public String dataTypeName() {
        return dataTypeName;
    }

    /**
     * @return Column's data type class.
     */
    public String dataTypeClass() {
        return dataTypeClass;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryWriterExImpl writer) {
        writer.writeString(schemaName);
        writer.writeString(tableName);
        writer.writeString(columnName);

        writer.writeInt(dataType);
        writer.writeString(dataTypeName);
        writer.writeString(dataTypeClass);
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryReaderExImpl reader) {
        schemaName = reader.readString();
        tableName = reader.readString();
        columnName = reader.readString();

        dataType = reader.readInt();
        dataTypeName = reader.readString();
        dataTypeClass = reader.readString();
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        JdbcColumnMeta meta = (JdbcColumnMeta)o;

        if (schemaName != null ? !schemaName.equals(meta.schemaName) : meta.schemaName != null)
            return false;

        if (tableName != null ? !tableName.equals(meta.tableName) : meta.tableName != null)
            return false;

        return columnName.equals(meta.columnName);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        int result = schemaName != null ? schemaName.hashCode() : 0;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + columnName.hashCode();
        return result;
    }
}
