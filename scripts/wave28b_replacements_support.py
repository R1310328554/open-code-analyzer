"""Chinese JavaDoc replacements for springframework wave28b lob/rowset/xml classes."""

SUPPORT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "AbstractLobHandler.java": [
        (
            "/**\n * Abstract base class for {@link LobHandler} implementations.\n *\n * <p>Implements all accessor methods for column names through a column lookup\n * and delegating to the corresponding accessor that takes a column index.\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see java.sql.ResultSet#findColumn\n * @deprecated as of 6.2, in favor of {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * and {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
            "/**\n * {@link LobHandler} 实现的抽象基类。\n *\n * <p>通过列名查找并委托给接受列索引的对应访问器，实现所有按列名访问的方法。\n *\n * @author Juergen Hoeller\n * @since 1.2\n * @see java.sql.ResultSet#findColumn\n * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
        ),
    ],
    "PassThroughBlob.java": [
        (
            "/**\n * Simple JDBC {@link Blob} adapter that exposes a given byte array or binary stream.\n * Optionally used by {@link DefaultLobHandler}.\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
            "/**\n * 简单的 JDBC {@link Blob} 适配器，暴露给定的字节数组或二进制流。\n * 可选地由 {@link DefaultLobHandler} 使用。\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
        ),
    ],
    "PassThroughClob.java": [
        (
            "/**\n * Simple JDBC {@link Clob} adapter that exposes a given String or character stream.\n * Optionally used by {@link DefaultLobHandler}.\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
            "/**\n * 简单的 JDBC {@link Clob} 适配器，暴露给定的 String 或字符流。\n * 可选地由 {@link DefaultLobHandler} 使用。\n *\n * @author Juergen Hoeller\n * @since 2.5.3\n */",
        ),
    ],
    "TemporaryLobCreator.java": [
        (
            "/**\n * {@link LobCreator} implementation based on temporary LOBs, using JDBC's\n * {@link java.sql.Connection#createBlob()} /\n * {@link java.sql.Connection#createClob()} mechanism.\n *\n * <p>Used by DefaultLobHandler's {@link DefaultLobHandler#setCreateTemporaryLob} mode.\n * Can also be used directly to reuse the tracking and freeing of temporary LOBs.\n *\n * @author Juergen Hoeller\n * @since 3.2.2\n * @see DefaultLobHandler#setCreateTemporaryLob\n * @see java.sql.Connection#createBlob()\n * @see java.sql.Connection#createClob()\n * @deprecated as of 6.2, in favor of {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * and {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
            "/**\n * 基于临时 LOB 的 {@link LobCreator} 实现，使用 JDBC 的\n * {@link java.sql.Connection#createBlob()} /\n * {@link java.sql.Connection#createClob()} 机制。\n *\n * <p>用于 DefaultLobHandler 的 {@link DefaultLobHandler#setCreateTemporaryLob} 模式。\n * 也可直接使用以复用临时 LOB 的跟踪与释放逻辑。\n *\n * @author Juergen Hoeller\n * @since 3.2.2\n * @see DefaultLobHandler#setCreateTemporaryLob\n * @see java.sql.Connection#createBlob()\n * @see java.sql.Connection#createClob()\n * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
        ),
    ],
    "Jdbc4SqlXmlHandler.java": [
        (
            "/**\n * Default implementation of the {@link SqlXmlHandler} interface.\n * Provides database-specific implementations for storing and\n * retrieving XML documents to and from fields in a database,\n * relying on the JDBC 4.0 {@code java.sql.SQLXML} facility.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see java.sql.SQLXML\n * @see java.sql.ResultSet#getSQLXML\n * @see java.sql.PreparedStatement#setSQLXML\n * @deprecated as of 6.2, in favor of direct {@link ResultSet#getSQLXML} and\n * {@link Connection#createSQLXML()} usage, possibly in combination with a\n * custom {@link org.springframework.jdbc.support.SqlValue} implementation\n */",
            "/**\n * {@link SqlXmlHandler} 接口的默认实现。\n * 依赖 JDBC 4.0 {@code java.sql.SQLXML} 设施，\n * 提供将 XML 文档存入和读出数据库字段的数据库特定实现。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 2.5.6\n * @see java.sql.SQLXML\n * @see java.sql.ResultSet#getSQLXML\n * @see java.sql.PreparedStatement#setSQLXML\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link ResultSet#getSQLXML} 和\n * {@link Connection#createSQLXML()}，必要时结合自定义\n * {@link org.springframework.jdbc.support.SqlValue} 实现\n */",
        ),
        (
            "\t/**\n\t * Internal base class for {@link SqlXmlValue} implementations.\n\t */",
            "\t/**\n\t * {@link SqlXmlValue} 实现的内部基类。\n\t */",
        ),
    ],
    "ResultSetWrappingSqlRowSetMetaData.java": [
        (
            "/**\n * The default implementation of Spring's {@link SqlRowSetMetaData} interface, wrapping a\n * {@link java.sql.ResultSetMetaData} instance, catching any {@link SQLException SQLExceptions}\n * and translating them to a corresponding Spring {@link InvalidResultSetAccessException}.\n *\n * <p>Used by {@link ResultSetWrappingSqlRowSet}.\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.2\n * @see ResultSetWrappingSqlRowSet#getMetaData()\n */",
            "/**\n * Spring {@link SqlRowSetMetaData} 接口的默认实现，包装\n * {@link java.sql.ResultSetMetaData} 实例，捕获 {@link SQLException SQLExceptions}\n * 并转换为对应的 Spring {@link InvalidResultSetAccessException}。\n *\n * <p>由 {@link ResultSetWrappingSqlRowSet} 使用。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.2\n * @see ResultSetWrappingSqlRowSet#getMetaData()\n */",
        ),
        (
            "\t/**\n\t * Create a new ResultSetWrappingSqlRowSetMetaData object\n\t * for the given ResultSetMetaData instance.\n\t * @param resultSetMetaData a disconnected ResultSetMetaData instance\n\t * to wrap (usually a {@code javax.sql.RowSetMetaData} instance)\n\t * @see java.sql.ResultSet#getMetaData\n\t * @see javax.sql.RowSetMetaData\n\t * @see ResultSetWrappingSqlRowSet#getMetaData\n\t */",
            "\t/**\n\t * 为给定的 ResultSetMetaData 实例创建 ResultSetWrappingSqlRowSetMetaData 对象。\n\t * @param resultSetMetaData 要包装的已断开 ResultSetMetaData 实例\n\t * （通常为 {@code javax.sql.RowSetMetaData} 实例）\n\t * @see java.sql.ResultSet#getMetaData\n\t * @see javax.sql.RowSetMetaData\n\t * @see ResultSetWrappingSqlRowSet#getMetaData\n\t */",
        ),
    ],
}
