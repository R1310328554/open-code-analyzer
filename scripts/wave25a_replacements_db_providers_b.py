"""Chinese JavaDoc replacements for springframework wave25a Oracle/Postgres providers."""

DB_PROVIDERS_B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "OracleCallMetaDataProvider.java": [
        (
            "/**\n * Oracle-specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 的 Oracle 特定实现。\n * 供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
        ),
    ],
    "OracleTableMetaDataProvider.java": [
        (
            "/**\n * Oracle-specific implementation of the {@link org.springframework.jdbc.core.metadata.TableMetaDataProvider}.\n * Supports a feature for including synonyms in the meta-data lookup. Also supports lookup of current schema\n * using the {@code sys_context}.\n *\n * <p>Thanks to Mike Youngstrom and Bruce Campbell for submitting the original suggestion for the Oracle\n * current schema lookup implementation.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 3.0\n */",
            "/**\n * {@link org.springframework.jdbc.core.metadata.TableMetaDataProvider} 的 Oracle 特定实现。\n * 支持在元数据查找中包含同义词，并支持通过 {@code sys_context} 查找当前 schema。\n *\n * <p>感谢 Mike Youngstrom 与 Bruce Campbell 提出 Oracle 当前 schema 查找的原始建议。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 3.0\n */",
        ),
        (
            "\t/**\n\t * Constructor used to initialize with provided database meta-data.\n\t * @param databaseMetaData meta-data to be used\n\t */",
            "\t/**\n\t * 使用提供的数据库元数据初始化。\n\t * @param databaseMetaData 要使用的元数据\n\t */",
        ),
        (
            "\t/**\n\t * Constructor used to initialize with provided database meta-data.\n\t * @param databaseMetaData meta-data to be used\n\t * @param includeSynonyms whether to include synonyms\n\t */",
            "\t/**\n\t * 使用提供的数据库元数据初始化。\n\t * @param databaseMetaData 要使用的元数据\n\t * @param includeSynonyms 是否包含同义词\n\t */",
        ),
        (
            "\t/*\n\t * Oracle-based implementation for detecting the current schema.\n\t */",
            "\t/*\n\t * 基于 Oracle 的当前 schema 检测实现。\n\t */",
        ),
    ],
    "PostgresCallMetaDataProvider.java": [
        (
            "/**\n * Postgres-specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 的 PostgreSQL 特定实现。\n * 供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
    ],
    "PostgresTableMetaDataProvider.java": [
        (
            "/**\n * The PostgreSQL specific implementation of {@link TableMetaDataProvider}.\n * Supports a feature for retrieving generated keys without the JDBC 3.0\n * {@code getGeneratedKeys} support. Also, it processes PostgreSQL-returned\n * catalog and schema names from {@code DatabaseMetaData} in the given case.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * {@link TableMetaDataProvider} 的 PostgreSQL 特定实现。\n * 支持在无 JDBC 3.0 {@code getGeneratedKeys} 时获取生成键。\n * 并按给定大小写处理 {@code DatabaseMetaData} 返回的 catalog 与 schema 名。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
    ],
}
