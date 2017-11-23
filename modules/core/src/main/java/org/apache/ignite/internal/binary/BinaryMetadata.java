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

package org.apache.ignite.internal.binary;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.jetbrains.annotations.Nullable;

/**
 * Binary metadata which is passed over a wire.
 */
public class BinaryMetadata implements Externalizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private static final int VERSION = 2;

    /** Type ID. */
    @GridToStringInclude(sensitive = true)
    private int typeId;

    /** Type name. */
    @GridToStringInclude(sensitive = true)
    private String typeName;

    /** Recorded object fields. */
    @GridToStringInclude(sensitive = true)
    private Map<String, BinaryFieldMetadata> fields;

    /** Affinity key field name. */
    @GridToStringInclude(sensitive = true)
    private String affKeyFieldName;

    /** Schemas associated with type. */
    @GridToStringInclude
    private Collection<BinarySchema> schemas;

    /** Schema IDs registered for this type */
    private Set<Integer> schemaIds;

    /** Whether this is enum type. */
    private boolean isEnum;

    /** Enum name to ordinal mapping. */
    private Map<String, Integer> nameToOrdinal;

    /** Enum ordinal to name mapping. */
    private Map<Integer, String> ordinalToName;

    /** Explicit changes. */
    private boolean explicit;

    /** Versioned part of meta. */
    private CacheSpecificMetadata changes = null;

    /**
     * For {@link Externalizable}.
     */
    public BinaryMetadata() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param typeId Type ID.
     * @param typeName Type name.
     * @param fields Fields map.
     * @param affKeyFieldName Affinity key field name.
     * @param schemas Schemas.
     * @param isEnum Enum flag.
     * @param enumMap Enum name to ordinal mapping.
     */
    public BinaryMetadata(int typeId, String typeName, @Nullable Map<String, BinaryFieldMetadata> fields,
        @Nullable String affKeyFieldName, @Nullable Collection<BinarySchema> schemas, boolean isEnum,
        @Nullable Map<String, Integer> enumMap) {
        assert typeName != null;

        this.typeId = typeId;
        this.typeName = typeName;
        this.fields = fields;
        this.affKeyFieldName = affKeyFieldName;
        this.schemas = schemas;

        if (schemas != null) {
            schemaIds = U.newHashSet(schemas.size());

            for (BinarySchema schema : schemas)
                schemaIds.add(schema.schemaId());
        }
        else
            schemaIds = Collections.emptySet();

        this.isEnum = isEnum;

        if (enumMap != null) {
            this.nameToOrdinal = new LinkedHashMap<>(enumMap);

            this.ordinalToName = new LinkedHashMap<>(enumMap.size());

            for (Map.Entry<String, Integer> e: nameToOrdinal.entrySet())
                this.ordinalToName.put(e.getValue(), e.getKey());
        }
    }

    /**
     * @return Type ID.
     */
    public int typeId() {
        return typeId;
    }

    /**
     * @return Type name.
     */
    public String typeName() {
        return typeName;
    }

    /**
     * @return Fields.
     */
    public Collection<String> fields() {
        return fields != null ? fields.keySet() : Collections.<String>emptyList();
    }

    /**
     * @return Fields.
     */
    public Map<String, BinaryFieldMetadata> fieldsMap() {
        return fields != null ? fields : Collections.<String, BinaryFieldMetadata>emptyMap();
    }

    /**
     * @param fieldName Field name.
     * @return Field type name.
     */
    @Nullable public String fieldTypeName(String fieldName) {
        BinaryFieldMetadata meta = fields != null ? fields.get(fieldName) : null;

        return meta != null ? BinaryUtils.fieldTypeName(meta.typeId()) : null;
    }

    /**
     * @return Affinity key field name.
     */
    @Nullable public String affinityKeyFieldName() {
        return affKeyFieldName;
    }

    /**
     * @return Schemas.
     */
    public Collection<BinarySchema> schemas() {
        return schemas != null ? schemas : Collections.<BinarySchema>emptyList();
    }

    /**
     * @param schemaId Schema ID.
     * @return {@code true} if <b>BinaryMetadata</b> instance has schema with ID specified, {@code false} otherwise.
     */
    public boolean hasSchema(int schemaId) {
        if (schemaIds == null)
            return false;
        
        return schemaIds.contains(schemaId);
    }

    /**
     * @return {@code True} if this is enum type.
     */
    public boolean isEnum() {
        return isEnum;
    }

    /**
     * Wrap metadata into binary type.
     *
     * @param ctx Binary context.
     * @return Binary type.
     */
    public BinaryTypeImpl wrap(BinaryContext ctx) {
        return new BinaryTypeImpl(ctx, this);
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        writeTo(out);
    }

    /**
     * The object implements the writeTo method to save its contents
     * by calling the methods of DataOutput for its primitive values and strings or
     * calling the writeTo method for other objects.
     *
     * @param out the stream to write the object to.
     * @exception IOException Includes any I/O exceptions that may occur.
     */
    public void writeTo(DataOutput out) throws IOException {
        out.writeByte(VERSION);
        out.writeInt(typeId);

        U.writeString(out, typeName);

        if (fields == null)
            out.writeInt(-1);
        else {
            out.writeInt(fields.size());

            for (Map.Entry<String, BinaryFieldMetadata> fieldEntry : fields.entrySet()) {
                U.writeString(out, fieldEntry.getKey());
                fieldEntry.getValue().writeTo(out);
            }
        }

        U.writeString(out, affKeyFieldName);

        if (schemas == null)
            out.writeInt(-1);
        else {
            out.writeInt(schemas.size());

            for (BinarySchema schema : schemas)
                schema.writeTo(out);
        }

        out.writeBoolean(isEnum);

        if (isEnum) {
            Map<String, Integer> map = enumMap();

            out.writeInt(map.size());

            for (Map.Entry<String, Integer> e : map.entrySet()) {
                U.writeString(out, e.getKey());

                out.writeInt(e.getValue());
            }
        }

        // Version 2 starting here
        out.writeBoolean(explicit);

        if (explicit) {
            if (changes == null)
                out.writeBoolean(false);
            else {
                out.writeBoolean(true);

                changes.writeTo(out);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readFrom(in);
    }

    /**
     * The object implements the readFrom method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and strings or calling readExternal for other objects.  The
     * readFrom method must read the values in the same sequence
     * and with the same types as were written by writeTo.
     *
     * @param in the stream to read data from in order to restore the object.
     * @exception IOException if I/O errors occur.
     */
    public void readFrom(DataInput in) throws IOException {
        in.readByte(); //skip version

        typeId = in.readInt();
        typeName = U.readString(in);

        int fieldsSize = in.readInt();

        if (fieldsSize == -1)
            fields = null;
        else {
            fields = new HashMap<>();

            for (int i = 0; i < fieldsSize; i++) {
                String fieldName = U.readString(in);

                BinaryFieldMetadata fieldMeta = new BinaryFieldMetadata();
                fieldMeta.readFrom(in);

                fields.put(fieldName, fieldMeta);
            }
        }

        affKeyFieldName = U.readString(in);

        int schemasSize = in.readInt();

        if (schemasSize == -1) {
            schemas = null;

            schemaIds = Collections.emptySet();
        }
        else {
            schemas = new ArrayList<>();

            schemaIds = U.newHashSet(schemasSize);

            for (int i = 0; i < schemasSize; i++) {
                BinarySchema schema = new BinarySchema();

                schema.readFrom(in);

                schemas.add(schema);

                schemaIds.add(schema.schemaId());
            }
        }

        isEnum = in.readBoolean();

        if (isEnum) {
            int size = in.readInt();

            if (size >= 0) {
                ordinalToName = new LinkedHashMap<>(size);
                nameToOrdinal = new LinkedHashMap<>(size);

                for (int idx = 0; idx < size; idx++) {
                    String name = U.readString(in);

                    int ord = in.readInt();

                    ordinalToName.put(ord, name);
                    nameToOrdinal.put(name, ord);
                }
            }
        }

        if (VERSION < 2)
            return;

        explicit = in.readBoolean();

        if (explicit) {
            if (in.readBoolean()) {
                changes = new CacheSpecificMetadata();

                changes.readFrom(in);
            }
        }
    }

    /**
     * Gets enum constant name given its ordinal value.
     *
     * @param ord Enum constant ordinal value.
     * @return Enum constant name.
     */
    public String getEnumNameByOrdinal(int ord) {
        if (ordinalToName == null)
            return null;

        return ordinalToName.get(ord);
    }

    /**
     * Gets enum constant ordinal value given its name.
     *
     * @param name Enum constant name.
     * @return Enum constant ordinal value.
     */
    public Integer getEnumOrdinalByName(String name) {
        assert name != null;

        return nameToOrdinal.get(name);
    }

    /**
     * @return Name to ordinal mapping.
     */
    public Map<String, Integer> enumMap() {
        if (nameToOrdinal == null)
            return Collections.emptyMap();

        return nameToOrdinal;
    }

    /**
     *
     * @return Explicit flag.
     */
    public boolean explicit() {
        return explicit;
    }

    /**
     *
     * @param explicit Explicit flag.
     */
    public void explicit(boolean explicit) {
        this.explicit = explicit;
    }

    /**
     *
     * @return
     */
    public CacheSpecificMetadata cache() {
        return changes;
    }

    /**
     *
     * @param fieldName
     */
    public void removeField(String fieldName) {
        assert explicit;

        CacheSpecificMetadata cacheMeta = changes;

        if (cacheMeta == null) {
            cacheMeta = new CacheSpecificMetadata();

            changes = cacheMeta;
        }

        cacheMeta.removeField(fieldName);
    }

    /**
     *
     * @param fieldName
     * @param fieldMeta
     */
    public void addField(String fieldName, BinaryFieldMetadata fieldMeta) {
        assert explicit;

        CacheSpecificMetadata cacheMeta = changes;

        if (cacheMeta == null) {
            cacheMeta = new CacheSpecificMetadata();

            changes = cacheMeta;
        }

        cacheMeta.addField(fieldName, fieldMeta);
    }

    public int mapSchemaVersion(int schemaId) {
        CacheSpecificMetadata cacheMeta = changes;

        if (cacheMeta == null)
            return schemaId;

        return cacheMeta.mapSchemaVersion(schemaId);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(BinaryMetadata.class, this);
    }

    /** Cache specific metadata. */
    public class CacheSpecificMetadata implements Externalizable {
        /** */
        private static final long serialVersionUID = 0L;

        /** Current version. */
        private int version = 0;

        /** Version-Schema-Id to version info. */
        private Map<Integer, VersionInfo> versionSchemas = new HashMap<>();

        /** Version to field map. */
        private Map<Integer, Map<String, BinaryFieldMetadata>> fieldsMaps = new HashMap<>();

        /**
         *
         * @return Current version.
         */
        public int version() {
            return version;
        }

        /**
         *
         * @param fields
         */
        public void addVersion(Map<String, BinaryFieldMetadata> fields) {
            version++;

            fieldsMaps.put(version, fields);

            BinarySchema.Builder scmBldr = BinarySchema.Builder.newBuilder();

            for (Map.Entry<String, BinaryFieldMetadata> entry : fields.entrySet()) {
                scmBldr.addField(entry.getValue().fieldId());
            }

            BinarySchema schema = scmBldr.build();

            schema.schemaId();

            versionSchemas.put(schema.schemaId() ^ version, new VersionInfo(version, schema));
        }

        /**
         *
         * @param version Current version.
         */
        public void version(int version) {
            this.version = version;
        }

        /**
         *
         * @param schemaId
         * @return
         */
        public int mapSchemaVersion(int schemaId) {
            if (version == 0)
                return schemaId;

            return version ^ schemaId;
        }

        /**
         *
         * @param fieldName
         */
        public void removeField(String fieldName) {
            Map<String, BinaryFieldMetadata> f = new LinkedHashMap<>(
                (version == 0) ? fields : fieldsMaps.get(version));

            if (f.remove(fieldName) == null)
                throw new IgniteException("Cannot find field to remove [fieldName=" + fieldName + "].");

            addVersion(f);
        }

        /**
         *
         * @param fieldName
         * @param fldMeta
         */
        public void addField(String fieldName, BinaryFieldMetadata fldMeta) {
            Map<String, BinaryFieldMetadata> f = new LinkedHashMap<>(
                (version == 0) ? fields : fieldsMaps.get(version));

            if (f.put(fieldName, fldMeta) != null)
                throw new IgniteException("Field already exists [fieldName=" + fieldName + "].");

            addVersion(f);
        }

        /**
         *
         * @return Metadata view for current version.
         */
        public BinaryMetadata metadata() {
            Map<String, BinaryFieldMetadata> f = new LinkedHashMap<>(
                (version == 0) ? fields : fieldsMaps.get(version));

            return new BinaryMetadata(typeId, typeName, f, affKeyFieldName, null, false, null);
        }

        /** */
        public Map<String, BinarySchemaFieldState> getFieldStates(int schemaId) {
            VersionInfo verInfo = versionSchemas.get(schemaId);

            int v = (verInfo == null)? 0 : verInfo.version;

            // start with fields for current version, all marked as 'OK'
            // go down from current version to target version
            // mark fields that are deleted
            Map<String, BinarySchemaFieldState> result = null;

            for (int ver = version; ver > v; ver--) {
                Map<String, BinaryFieldMetadata> fields = (ver == 0) ? fieldsMap() : fieldsMaps.get(ver);

                if (result == null) {
                    result = new HashMap<>(fields.size());

                    for (String name : fields.keySet()) {
                        result.put(name, BinarySchemaFieldState.OK);
                    }
                }
                else {
                    for (Object name : result.keySet().toArray()) {
                        if (result.get(name) == BinarySchemaFieldState.OK && !fields.containsKey(name))
                            result.put((String)name, BinarySchemaFieldState.DELETED);
                    }
                }
            }

            return result;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void writeTo(DataOutput out) throws IOException {
            out.writeInt(version);

            // write schemas
            out.writeInt(versionSchemas.size());

            for (Map.Entry<Integer, VersionInfo> e : versionSchemas.entrySet()) {
                out.writeInt(e.getKey());

                VersionInfo vInfo = e.getValue();
                out.writeInt(vInfo.version);
                vInfo.schema.writeTo(out);
            }

            // write version fields maps
            out.writeInt(fieldsMaps.size());

            for (Map.Entry<Integer, Map<String, BinaryFieldMetadata>> e : fieldsMaps.entrySet()) {
                out.writeInt(e.getKey());

                Map<String, BinaryFieldMetadata> map = e.getValue();

                out.writeInt(map.size());

                for (Map.Entry<String, BinaryFieldMetadata> e2 : map.entrySet()) {
                    U.writeString(out, e2.getKey());

                    e2.getValue().writeTo(out);
                }
            }
        }

        /**
         *
         * @param in
         * @throws IOException
         */
        public void readFrom(DataInput in) throws IOException {
            version = in.readInt();

            // read schemas
            int size = in.readInt();

            versionSchemas = new HashMap<>(size);

            for (int idx = 0; idx < size; idx++) {
                int id = in.readInt();

                int v = in.readInt();

                BinarySchema schema = new BinarySchema();
                schema.readFrom(in);

                versionSchemas.put(id, new VersionInfo(v, schema));
            }

            // read version fields maps
            size = in.readInt();

            fieldsMaps = new HashMap<>(size);

            for (int idx = 0; idx < size; idx++) {
                int id = in.readInt();

                int size2 = in.readInt();

                Map<String, BinaryFieldMetadata> map = new HashMap<>(size2);

                fieldsMaps.put(id, map);

                for (int idx2 = 0; idx2 < size2; idx2++) {
                    String id2 = U.readString(in);

                    BinaryFieldMetadata fieldMetadata = new BinaryFieldMetadata();
                    fieldMetadata.readFrom(in);

                    map.put(id2, fieldMetadata);
                }
            }
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            writeTo(out);
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            readFrom(in);
        }
    }

    /** */
    public static class VersionInfo {
        /** Version. */
        private final int version;

        /** Schema. */
        private final BinarySchema schema;

        /**
         *
         * @param version Version.
         * @param schema Schema.
         */
        public VersionInfo(int version, BinarySchema schema) {
            this.version = version;
            this.schema = schema;
        }

        /**
         *
         * @return Version.
         */
        public int version() {
            return version;
        }

        /**
         *
         * @return Schema.
         */
        public BinarySchema schema() {
            return schema;
        }
    }

}
