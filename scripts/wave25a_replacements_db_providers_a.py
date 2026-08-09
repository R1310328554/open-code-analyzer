"""Chinese JavaDoc replacements for springframework wave25a DB-specific providers (part A)."""

DB_PROVIDERS_A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Db2CallMetaDataProvider.java": [
        (
            "/**\n * DB2 specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 的 DB2 特定实现。\n * 供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
    ],
    "DerbyCallMetaDataProvider.java": [
        (
            "/**\n * Derby specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
            "/**\n * {@link CallMetaDataProvider} 的 Derby 特定实现。\n * 供 Simple JDBC 类内部使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5\n */",
        ),
    ],
    "DerbyTableMetaDataProvider.java": [
        (
            "/**\n * The Derby specific implementation of {@link TableMetaDataProvider}.\n * Overrides the Derby meta-data info regarding retrieving generated keys.\n *\n * @author Thomas Risberg\n * @since 3.0\n */",
            "/**\n * {@link TableMetaDataProvider} 的 Derby 特定实现。\n * 覆盖 Derby 关于获取生成键的元数据信息。\n *\n * @author Thomas Risberg\n * @since 3.0\n */",
        ),
    ],
    "HanaCallMetaDataProvider.java": [
        (
            "/**\n * SAP HANA specific implementation for the {@link CallMetaDataProvider} interface.\n * This class is intended for internal use by the Simple JDBC classes.\n *\n * @author Subhobrata Dey\n * @author Juergen Hoeller\n * @since 4.2.1\n */",
            "/**\n * {@link CallMetaDataProvider} 的 SAP HANA 特定实现。\n * 供 Simple JDBC 类内部使用。\n *\n * @author Subhobrata Dey\n * @author Juergen Hoeller\n * @since 4.2.1\n */",
        ),
    ],
    "HsqlTableMetaDataProvider.java": [
        (
            "/**\n * The HSQL specific implementation of {@link TableMetaDataProvider}.\n * Supports a feature for retrieving generated keys without the JDBC 3.0\n * {@code getGeneratedKeys} support.\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
            "/**\n * {@link TableMetaDataProvider} 的 HSQL 特定实现。\n * 支持在无 JDBC 3.0 {@code getGeneratedKeys} 时\n * 获取生成键的特性。\n *\n * @author Thomas Risberg\n * @since 2.5\n */",
        ),
    ],
    "MySQLTableMetaDataProvider.java": [
        (
            "/**\n * The MySQL/MariaDB specific implementation of {@link TableMetaDataProvider}.\n * Sets {@link #setGeneratedKeysColumnNameArraySupported} to {@code false}.\n *\n * @author Juergen Hoeller\n * @since 6.2.12\n */",
            "/**\n * {@link TableMetaDataProvider} 的 MySQL/MariaDB 特定实现。\n * MySQL 与 MariaDB 驱动在通过列名数组获取自增主键时行为不一致，\n * 因此本实现将 {@link #setGeneratedKeysColumnNameArraySupported} 设为 {@code false}，\n * 避免 SimpleJdbcInsert 等组件依赖不可靠的列名数组特性。\n *\n * @author Juergen Hoeller\n * @since 6.2.12\n */",
        ),
    ],
}
