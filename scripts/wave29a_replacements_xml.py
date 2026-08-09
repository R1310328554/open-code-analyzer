"""Chinese JavaDoc replacements for springframework wave29a XML support classes."""

XML_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SqlXmlFeatureNotImplementedException.java": [
        (
            "/**\n * Exception thrown when the underlying implementation does not support the\n * requested feature of the API.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @deprecated as of 6.2, in favor of direct {@link ResultSet#getSQLXML} and\n * {@link Connection#createSQLXML()} usage, possibly in combination with a\n * custom {@link org.springframework.jdbc.support.SqlValue} implementation\n */",
            "/**\n * 当底层实现不支持 API 所请求的特性时抛出的异常。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link ResultSet#getSQLXML} 和\n * {@link Connection#createSQLXML()}，必要时结合自定义\n * {@link org.springframework.jdbc.support.SqlValue} 实现\n */",
        ),
        (
            "\t/**\n\t * Constructor for SqlXmlFeatureNotImplementedException.\n\t * @param msg the detail message\n\t */",
            "\t/**\n\t * 构造 SqlXmlFeatureNotImplementedException。\n\t * @param msg 详细消息\n\t */",
        ),
        (
            "\t/**\n\t * Constructor for SqlXmlFeatureNotImplementedException.\n\t * @param msg the detail message\n\t * @param cause the root cause from the data access API in use\n\t */",
            "\t/**\n\t * 构造 SqlXmlFeatureNotImplementedException。\n\t * @param msg 详细消息\n\t * @param cause 所用数据访问 API 的根因\n\t */",
        ),
    ],
    "SqlXmlHandler.java": [
        (
            "/**\n * Abstraction for handling XML fields in specific databases. Its main purpose\n * is to isolate database-specific handling of XML stored in the database.\n *\n * <p>JDBC 4.0 introduces the new data type {@code java.sql.SQLXML}\n * but most databases and their drivers currently rely on database-specific\n * data types and features.\n *\n * <p>Provides accessor methods for XML fields and acts as factory for\n * {@link SqlXmlValue} instances.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see Jdbc4SqlXmlHandler\n * @see java.sql.SQLXML\n * @see java.sql.ResultSet#getSQLXML\n * @see java.sql.PreparedStatement#setSQLXML\n * @deprecated as of 6.2, in favor of direct {@link ResultSet#getSQLXML} and\n * {@link Connection#createSQLXML()} usage, possibly in combination with a\n * custom {@link org.springframework.jdbc.support.SqlValue} implementation\n */",
            "/**\n * 处理特定数据库中 XML 字段的抽象层，主要目的是隔离数据库内 XML 的厂商特定处理逻辑。\n *\n * <p>JDBC 4.0 引入了 {@code java.sql.SQLXML} 数据类型，但多数数据库及其驱动\n * 仍依赖厂商特定的数据类型与特性。\n *\n * <p>提供 XML 字段的访问方法，并作为 {@link SqlXmlValue} 实例的工厂。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see Jdbc4SqlXmlHandler\n * @see java.sql.SQLXML\n * @see java.sql.ResultSet#getSQLXML\n * @see java.sql.PreparedStatement#setSQLXML\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link ResultSet#getSQLXML} 和\n * {@link Connection#createSQLXML()}，必要时结合自定义\n * {@link org.springframework.jdbc.support.SqlValue} 实现\n */",
        ),
        (
            "\t//-------------------------------------------------------------------------\n\t// Convenience methods for accessing XML content\n\t//-------------------------------------------------------------------------",
            "\t//-------------------------------------------------------------------------\n\t// 访问 XML 内容的便捷方法\n\t//-------------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Retrieve the given column as String from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getString} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnName the column name to use\n\t * @return the content as String, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getString\n\t * @see java.sql.ResultSet#getSQLXML\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为字符串。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getString}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnName 列名\n\t * @return 字符串形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getString\n\t * @see java.sql.ResultSet#getSQLXML\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as String from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getString} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnIndex the column index to use\n\t * @return the content as String, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getString\n\t * @see java.sql.ResultSet#getSQLXML\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为字符串。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getString}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnIndex 列索引\n\t * @return 字符串形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getString\n\t * @see java.sql.ResultSet#getSQLXML\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as binary stream from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getAsciiStream} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnName the column name to use\n\t * @return the content as a binary stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getBinaryStream\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为二进制流。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getAsciiStream}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnName 列名\n\t * @return 二进制流形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getBinaryStream\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as binary stream from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getAsciiStream} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnIndex the column index to use\n\t * @return the content as binary stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getBinaryStream\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为二进制流。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getAsciiStream}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnIndex 列索引\n\t * @return 二进制流形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getBinaryStream\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as character stream from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getCharacterStream} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnName the column name to use\n\t * @return the content as character stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getCharacterStream\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为字符流。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getCharacterStream}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnName 列名\n\t * @return 字符流形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getCharacterStream\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as character stream from the given ResultSet.\n\t * <p>Might simply invoke {@code ResultSet.getCharacterStream} or work with\n\t * {@code SQLXML} or database-specific classes depending on the\n\t * database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnIndex the column index to use\n\t * @return the content as character stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getCharacterStream\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为字符流。\n\t * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getCharacterStream}，\n\t * 也可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnIndex 列索引\n\t * @return 字符流形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getCharacterStream\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as Source implemented using the specified source class\n\t * from the given ResultSet.\n\t * <p>Might work with {@code SQLXML} or database-specific classes depending\n\t * on the database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnName the column name to use\n\t * @param sourceClass the implementation class to be used\n\t * @return the content as character stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getSource\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为以给定 Source 实现类表示的 {@link Source}。\n\t * <p>视数据库与驱动而定，可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnName 列名\n\t * @param sourceClass 要使用的 Source 实现类\n\t * @return Source 形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getSource\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the given column as Source implemented using the specified source class\n\t * from the given ResultSet.\n\t * <p>Might work with {@code SQLXML} or database-specific classes depending\n\t * on the database and driver.\n\t * @param rs the ResultSet to retrieve the content from\n\t * @param columnIndex the column index to use\n\t * @param sourceClass the implementation class to be used\n\t * @return the content as character stream, or {@code null} in case of SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getSource\n\t */",
            "\t/**\n\t * 从给定 ResultSet 中将指定列读取为以给定 Source 实现类表示的 {@link Source}。\n\t * <p>视数据库与驱动而定，可能通过 {@code SQLXML} 或厂商特定类处理。\n\t * @param rs 待读取内容的 ResultSet\n\t * @param columnIndex 列索引\n\t * @param sourceClass 要使用的 Source 实现类\n\t * @return Source 形式的内容；SQL NULL 时为 {@code null}\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.ResultSet#getSQLXML\n\t * @see java.sql.SQLXML#getSource\n\t */",
        ),
        (
            "\t//-------------------------------------------------------------------------\n\t// Convenience methods for building XML content\n\t//-------------------------------------------------------------------------",
            "\t//-------------------------------------------------------------------------\n\t// 构建 XML 内容的便捷方法\n\t//-------------------------------------------------------------------------",
        ),
        (
            "\t/**\n\t * Create a {@code SqlXmlValue} instance for the given XML data,\n\t * as supported by the underlying JDBC driver.\n\t * @param value the XML String value providing XML data\n\t * @return the implementation specific instance\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setString(String)\n\t */",
            "\t/**\n\t * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。\n\t * @param value 提供 XML 数据的字符串\n\t * @return 与实现相关的具体实例\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setString(String)\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code SqlXmlValue} instance for the given XML data,\n\t * as supported by the underlying JDBC driver.\n\t * @param provider the {@code XmlBinaryStreamProvider} providing XML data\n\t * @return the implementation specific instance\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setBinaryStream()\n\t */",
            "\t/**\n\t * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。\n\t * @param provider 提供 XML 数据的 {@code XmlBinaryStreamProvider}\n\t * @return 与实现相关的具体实例\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setBinaryStream()\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code SqlXmlValue} instance for the given XML data,\n\t * as supported by the underlying JDBC driver.\n\t * @param provider the {@code XmlCharacterStreamProvider} providing XML data\n\t * @return the implementation specific instance\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setCharacterStream()\n\t */",
            "\t/**\n\t * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。\n\t * @param provider 提供 XML 数据的 {@code XmlCharacterStreamProvider}\n\t * @return 与实现相关的具体实例\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setCharacterStream()\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code SqlXmlValue} instance for the given XML data,\n\t * as supported by the underlying JDBC driver.\n\t * @param resultClass the Result implementation class to be used\n\t * @param provider the {@code XmlResultProvider} that will provide the XML data\n\t * @return the implementation specific instance\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setResult(Class)\n\t */",
            "\t/**\n\t * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。\n\t * @param resultClass 要使用的 Result 实现类\n\t * @param provider 提供 XML 数据的 {@code XmlResultProvider}\n\t * @return 与实现相关的具体实例\n\t * @see SqlXmlValue\n\t * @see java.sql.SQLXML#setResult(Class)\n\t */",
        ),
        (
            "\t/**\n\t * Create a {@code SqlXmlValue} instance for the given XML data,\n\t * as supported by the underlying JDBC driver.\n\t * @param doc the XML Document to be used\n\t * @return the implementation specific instance\n\t * @see SqlXmlValue\n\t */",
            "\t/**\n\t * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。\n\t * @param doc 要使用的 XML 文档\n\t * @return 与实现相关的具体实例\n\t * @see SqlXmlValue\n\t */",
        ),
    ],
    "SqlXmlValue.java": [
        (
            "/**\n * Subinterface of {@link org.springframework.jdbc.support.SqlValue}\n * that specifically indicates passing in XML data to a specified column.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see org.springframework.jdbc.support.SqlValue\n * @deprecated as of 6.2, in favor of a direct {@link SqlValue} implementation\n */",
            "/**\n * {@link org.springframework.jdbc.support.SqlValue} 的子接口，\n * 专门用于向指定列传入 XML 数据。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see org.springframework.jdbc.support.SqlValue\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link SqlValue} 实现\n */",
        ),
    ],
}
