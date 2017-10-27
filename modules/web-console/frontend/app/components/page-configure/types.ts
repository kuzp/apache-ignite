namespace ig.config.model {
    type QueryMetadataTypes = 'Annotations' | 'Configuration'
    type DomainModelKinds = 'query' | 'store' | 'both'
    interface KeyField {
        databaseFieldName: string,
        databaseFieldType: string,
        javaFieldName: string,
        javaFieldType: string
    }
    interface ValueField {
        databaseFieldName: string,
        databaseFieldType: string,
        javaFieldName: string,
        javaFieldType: string
    }
    interface Field {
        name: string,
        className: string
    }
    interface Alias {
        field: string,
        alias: string
    }
    type IndexTypes = 'SORTED' | 'FULLTEXT' | 'GEOSPATIAL'
    export interface IndexField {
        name?: string,
        direction?: boolean
    }
    interface Index {
        _id: string,
        name: string,
        indexType: IndexTypes,
        fields: Array<IndexField>
    }

    export interface DomainModel {
        _id: string,
        space?: string,
        clusters?: Array<string>,
        caches?: Array<string>,
        queryMetadata?: QueryMetadataTypes,
        kind?: DomainModelKinds,
        tableName?: string,
        keyFieldName?: string,
        valueFieldName?: string,
        databaseSchema?: string,
        databaseTable?: string,
        keyType?: string,
        valueType?: string,
        keyFields?: Array<KeyField>,
        valueFields?: Array<ValueField>,
        queryKeyFields?: Array<string>,
        fields?: Array<Field>,
        aliases?: Array<Alias>,
        indexes?: Array<Index>,
        generatePojo?: boolean
    }
}