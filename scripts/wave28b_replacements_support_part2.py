"""Additional wave28b support replacements (LobCreator, LobHandler, SqlRowSetMetaData)."""

SUPPORT_PART2: dict[str, list[tuple[str, str]]] = {
    "LobCreator.java": [
        (
            "/**\n * Interface that abstracts potentially database-specific creation of large binary\n * fields and large text fields. Does not work with {@code java.sql.Blob}\n * and {@code java.sql.Clob} instances in the API, as some JDBC drivers\n * do not support these types as such.\n *\n * <p>The LOB creation part is where {@link LobHandler} implementations usually\n * differ. Possible strategies include usage of\n * {@code PreparedStatement.setBinaryStream/setCharacterStream} but also\n * {@code PreparedStatement.setBlob/setClob} with either a stream argument or\n * {@code java.sql.Blob/Clob} wrapper objects.\n *\n * <p>A LobCreator represents a session for creating BLOBs: It is <i>not</i>\n * thread-safe and needs to be instantiated for each statement execution or for\n * each transaction. Each LobCreator needs to be closed after completion.\n *\n * <p>For convenient working with a PreparedStatement and a LobCreator,\n * consider using {@link org.springframework.jdbc.core.JdbcTemplate} with an\n *{@link org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback}\n * implementation. See the latter's javadoc for details.\n *\n * @author Juergen Hoeller\n * @since 04.12.2003\n * @see #close()\n * @see LobHandler#getLobCreator()\n * @see DefaultLobHandler.DefaultLobCreator\n * @see java.sql.PreparedStatement#setBlob\n * @see java.sql.PreparedStatement#setClob\n * @see java.sql.PreparedStatement#setBytes\n * @see java.sql.PreparedStatement#setBinaryStream\n * @see java.sql.PreparedStatement#setString\n * @see java.sql.PreparedStatement#setAsciiStream\n * @see java.sql.PreparedStatement#setCharacterStream\n * @deprecated as of 6.2, in favor of {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * and {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
            "/**\n * 抽象可能因数据库而异的大二进制字段与大文本字段创建的接口。\n * API 中不使用 {@code java.sql.Blob} 和 {@code java.sql.Clob} 实例，\n * 因为部分 JDBC 驱动并不支持这些类型。\n *\n * <p>LOB 创建是 {@link LobHandler} 实现通常存在差异的部分。\n * 可能的策略包括使用 {@code PreparedStatement.setBinaryStream/setCharacterStream}，\n * 或使用 {@code PreparedStatement.setBlob/setClob} 配合流参数或\n * {@code java.sql.Blob/Clob} 包装对象。\n *\n * <p>LobCreator 表示创建 BLOB 的会话：<i>非</i>线程安全，\n * 每次语句执行或每个事务都需新建实例，完成后必须关闭。\n *\n * <p>若需便捷地配合 PreparedStatement 与 LobCreator 使用，\n * 可考虑将 {@link org.springframework.jdbc.core.JdbcTemplate} 与\n * {@link org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback}\n * 实现配合使用，详见后者的 javadoc。\n *\n * @author Juergen Hoeller\n * @since 04.12.2003\n * @see #close()\n * @see LobHandler#getLobCreator()\n * @see DefaultLobHandler.DefaultLobCreator\n * @see java.sql.PreparedStatement#setBlob\n * @see java.sql.PreparedStatement#setClob\n * @see java.sql.PreparedStatement#setBytes\n * @see java.sql.PreparedStatement#setBinaryStream\n * @see java.sql.PreparedStatement#setString\n * @see java.sql.PreparedStatement#setAsciiStream\n * @see java.sql.PreparedStatement#setCharacterStream\n * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}\n * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}\n */",
        ),
        (
            "\t/**\n\t * Set the given content as bytes on the given statement, using the given\n\t * parameter index. Might simply invoke {@code PreparedStatement.setBytes}\n\t * or create a Blob instance for it, depending on the database and driver.\n\t * @param ps the PreparedStatement to the set the content on\n\t * @param paramIndex the parameter index to use\n\t * @param content the content as byte array, or {@code null} for SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.PreparedStatement#setBytes\n\t */",
            "\t/**\n\t * 使用给定参数索引，将内容作为字节设置到给定语句上。\n\t * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setBytes}\n\t * 或为其创建 Blob 实例。\n\t * @param ps 要设置内容的 PreparedStatement\n\t * @param paramIndex 要使用的参数索引\n\t * @param content 字节数组形式的内容，或 {@code null} 表示 SQL NULL\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.PreparedStatement#setBytes\n\t */",
        ),
        (
            "\t/**\n\t * Set the given content as binary stream on the given statement, using the given\n\t * parameter index. Might simply invoke {@code PreparedStatement.setBinaryStream}\n\t * or create a Blob instance for it, depending on the database and driver.\n\t * @param ps the PreparedStatement to the set the content on\n\t * @param paramIndex the parameter index to use\n\t * @param contentStream the content as binary stream, or {@code null} for SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.PreparedStatement#setBinaryStream\n\t */",
            "\t/**\n\t * 使用给定参数索引，将内容作为二进制流设置到给定语句上。\n\t * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setBinaryStream}\n\t * 或为其创建 Blob 实例。\n\t * @param ps 要设置内容的 PreparedStatement\n\t * @param paramIndex 要使用的参数索引\n\t * @param contentStream 二进制流形式的内容，或 {@code null} 表示 SQL NULL\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.PreparedStatement#setBinaryStream\n\t */",
        ),
        (
            "\t/**\n\t * Set the given content as String on the given statement, using the given\n\t * parameter index. Might simply invoke {@code PreparedStatement.setString}\n\t * or create a Clob instance for it, depending on the database and driver.\n\t * @param ps the PreparedStatement to the set the content on\n\t * @param paramIndex the parameter index to use\n\t * @param content the content as String, or {@code null} for SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.PreparedStatement#setBytes\n\t */",
            "\t/**\n\t * 使用给定参数索引，将内容作为 String 设置到给定语句上。\n\t * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setString}\n\t * 或为其创建 Clob 实例。\n\t * @param ps 要设置内容的 PreparedStatement\n\t * @param paramIndex 要使用的参数索引\n\t * @param content String 形式的内容，或 {@code null} 表示 SQL NULL\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.PreparedStatement#setBytes\n\t */",
        ),
        (
            "\t/**\n\t * Set the given content as ASCII stream on the given statement, using the given\n\t * parameter index. Might simply invoke {@code PreparedStatement.setAsciiStream}\n\t * or create a Clob instance for it, depending on the database and driver.\n\t * @param ps the PreparedStatement to the set the content on\n\t * @param paramIndex the parameter index to use\n\t * @param asciiStream the content as ASCII stream, or {@code null} for SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.PreparedStatement#setAsciiStream\n\t */",
            "\t/**\n\t * 使用给定参数索引，将内容作为 ASCII 流设置到给定语句上。\n\t * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setAsciiStream}\n\t * 或为其创建 Clob 实例。\n\t * @param ps 要设置内容的 PreparedStatement\n\t * @param paramIndex 要使用的参数索引\n\t * @param asciiStream ASCII 流形式的内容，或 {@code null} 表示 SQL NULL\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.PreparedStatement#setAsciiStream\n\t */",
        ),
        (
            "\t/**\n\t * Set the given content as character stream on the given statement, using the given\n\t * parameter index. Might simply invoke {@code PreparedStatement.setCharacterStream}\n\t * or create a Clob instance for it, depending on the database and driver.\n\t * @param ps the PreparedStatement to the set the content on\n\t * @param paramIndex the parameter index to use\n\t * @param characterStream the content as character stream, or {@code null} for SQL NULL\n\t * @throws SQLException if thrown by JDBC methods\n\t * @see java.sql.PreparedStatement#setCharacterStream\n\t */",
            "\t/**\n\t * 使用给定参数索引，将内容作为字符流设置到给定语句上。\n\t * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setCharacterStream}\n\t * 或为其创建 Clob 实例。\n\t * @param ps 要设置内容的 PreparedStatement\n\t * @param paramIndex 要使用的参数索引\n\t * @param characterStream 字符流形式的内容，或 {@code null} 表示 SQL NULL\n\t * @throws SQLException JDBC 方法抛出时\n\t * @see java.sql.PreparedStatement#setCharacterStream\n\t */",
        ),
        (
            "\t/**\n\t * Close this LobCreator session and free its temporarily created BLOBs and CLOBs.\n\t * Will not need to do anything if using PreparedStatement's standard methods,\n\t * but might be necessary to free database resources if using proprietary means.\n\t * <p><b>NOTE</b>: Needs to be invoked after the involved PreparedStatements have\n\t * been executed or the affected O/R mapping sessions have been flushed.\n\t * Otherwise, the database resources for the temporary BLOBs might stay allocated.\n\t */",
            "\t/**\n\t * 关闭本 LobCreator 会话并释放临时创建的 BLOB 与 CLOB。\n\t * 若使用 PreparedStatement 标准方法则通常无需操作，\n\t * 但若使用专有方式则可能需要释放数据库资源。\n\t * <p><b>NOTE</b>：须在相关 PreparedStatement 执行完毕\n\t * 或受影响的 O/R 映射会话 flush 之后调用。\n\t * 否则临时 BLOB 的数据库资源可能持续占用。\n\t */",
        ),
    ],
    "SqlRowSetMetaData.java": [
        (
            "/**\n * Metadata interface for Spring's {@link SqlRowSet}, analogous to JDBC's\n * {@link java.sql.ResultSetMetaData}.\n *\n * <p>The main difference to the standard JDBC ResultSetMetaData is that a\n * {@link java.sql.SQLException} is never thrown here. This allows\n * SqlRowSetMetaData to be used without having to deal with checked exceptions.\n * SqlRowSetMetaData will throw Spring's {@link InvalidResultSetAccessException}\n * instead (when appropriate).\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.2\n * @see SqlRowSet#getMetaData()\n * @see java.sql.ResultSetMetaData\n * @see org.springframework.jdbc.InvalidResultSetAccessException\n */",
            "/**\n * Spring {@link SqlRowSet} 的元数据接口，类似于 JDBC 的\n * {@link java.sql.ResultSetMetaData}。\n *\n * <p>与标准 JDBC ResultSetMetaData 的主要区别在于此处从不抛出\n * {@link java.sql.SQLException}，因此使用 SqlRowSetMetaData 时\n * 无需处理受检异常；在适当时机会抛出 Spring 的\n * {@link InvalidResultSetAccessException}。\n *\n * @author Thomas Risberg\n * @author Juergen Hoeller\n * @since 1.2\n * @see SqlRowSet#getMetaData()\n * @see java.sql.ResultSetMetaData\n * @see org.springframework.jdbc.InvalidResultSetAccessException\n */",
        ),
        (
            "\t/**\n\t * Retrieve the catalog name of the table that served as the source for the\n\t * specified column.\n\t * @param columnIndex the index of the column\n\t * @return the catalog name\n\t * @see java.sql.ResultSetMetaData#getCatalogName(int)\n\t */",
            "\t/**\n\t * 检索指定列来源表的 catalog 名称。\n\t * @param columnIndex 列索引\n\t * @return catalog 名称\n\t * @see java.sql.ResultSetMetaData#getCatalogName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the fully qualified class that the specified column will be mapped to.\n\t * @param columnIndex the index of the column\n\t * @return the class name as a String\n\t * @see java.sql.ResultSetMetaData#getColumnClassName(int)\n\t */",
            "\t/**\n\t * 检索指定列将映射到的完全限定类名。\n\t * @param columnIndex 列索引\n\t * @return 类名字符串\n\t * @see java.sql.ResultSetMetaData#getColumnClassName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the number of columns in the RowSet.\n\t * @return the number of columns\n\t * @see java.sql.ResultSetMetaData#getColumnCount()\n\t */",
            "\t/**\n\t * 检索 RowSet 中的列数。\n\t * @return 列数\n\t * @see java.sql.ResultSetMetaData#getColumnCount()\n\t */",
        ),
        (
            "\t/**\n\t * Return the column names of the table that the result set represents.\n\t * @return the column names\n\t */",
            "\t/**\n\t * 返回结果集所代表表的列名。\n\t * @return 列名数组\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the maximum width of the designated column.\n\t * @param columnIndex the index of the column\n\t * @return the width of the column\n\t * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)\n\t */",
            "\t/**\n\t * 检索指定列的最大显示宽度。\n\t * @param columnIndex 列索引\n\t * @return 列宽度\n\t * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the suggested column title for the column specified.\n\t * @param columnIndex the index of the column\n\t * @return the column title\n\t * @see java.sql.ResultSetMetaData#getColumnLabel(int)\n\t */",
            "\t/**\n\t * 检索指定列的建议列标题。\n\t * @param columnIndex 列索引\n\t * @return 列标题\n\t * @see java.sql.ResultSetMetaData#getColumnLabel(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the column name for the indicated column.\n\t * @param columnIndex the index of the column\n\t * @return the column name\n\t * @see java.sql.ResultSetMetaData#getColumnName(int)\n\t */",
            "\t/**\n\t * 检索指定列的列名。\n\t * @param columnIndex 列索引\n\t * @return 列名\n\t * @see java.sql.ResultSetMetaData#getColumnName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the SQL type code for the indicated column.\n\t * @param columnIndex the index of the column\n\t * @return the SQL type code\n\t * @see java.sql.ResultSetMetaData#getColumnType(int)\n\t * @see java.sql.Types\n\t */",
            "\t/**\n\t * 检索指定列的 SQL 类型代码。\n\t * @param columnIndex 列索引\n\t * @return SQL 类型代码\n\t * @see java.sql.ResultSetMetaData#getColumnType(int)\n\t * @see java.sql.Types\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the DBMS-specific type name for the indicated column.\n\t * @param columnIndex the index of the column\n\t * @return the type name\n\t * @see java.sql.ResultSetMetaData#getColumnTypeName(int)\n\t */",
            "\t/**\n\t * 检索指定列的 DBMS 特定类型名。\n\t * @param columnIndex 列索引\n\t * @return 类型名\n\t * @see java.sql.ResultSetMetaData#getColumnTypeName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the precision for the indicated column.\n\t * @param columnIndex the index of the column\n\t * @return the precision\n\t * @see java.sql.ResultSetMetaData#getPrecision(int)\n\t */",
            "\t/**\n\t * 检索指定列的精度。\n\t * @param columnIndex 列索引\n\t * @return 精度\n\t * @see java.sql.ResultSetMetaData#getPrecision(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the scale of the indicated column.\n\t * @param columnIndex the index of the column\n\t * @return the scale\n\t * @see java.sql.ResultSetMetaData#getScale(int)\n\t */",
            "\t/**\n\t * 检索指定列的小数位数。\n\t * @param columnIndex 列索引\n\t * @return 小数位数\n\t * @see java.sql.ResultSetMetaData#getScale(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the schema name of the table that served as the source for the\n\t * specified column.\n\t * @param columnIndex the index of the column\n\t * @return the schema name\n\t * @see java.sql.ResultSetMetaData#getSchemaName(int)\n\t */",
            "\t/**\n\t * 检索指定列来源表的 schema 名称。\n\t * @param columnIndex 列索引\n\t * @return schema 名称\n\t * @see java.sql.ResultSetMetaData#getSchemaName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Retrieve the name of the table that served as the source for the\n\t * specified column.\n\t * @param columnIndex the index of the column\n\t * @return the name of the table\n\t * @see java.sql.ResultSetMetaData#getTableName(int)\n\t */",
            "\t/**\n\t * 检索指定列来源表的名称。\n\t * @param columnIndex 列索引\n\t * @return 表名\n\t * @see java.sql.ResultSetMetaData#getTableName(int)\n\t */",
        ),
        (
            "\t/**\n\t * Indicate whether the case of the designated column is significant.\n\t * @param columnIndex the index of the column\n\t * @return true if the column is case-sensitive, false otherwise\n\t * @see java.sql.ResultSetMetaData#isCaseSensitive(int)\n\t */",
            "\t/**\n\t * 指示指定列的大小写是否敏感。\n\t * @param columnIndex 列索引\n\t * @return 若列大小写敏感则为 true，否则为 false\n\t * @see java.sql.ResultSetMetaData#isCaseSensitive(int)\n\t */",
        ),
        (
            "\t/**\n\t * Indicate whether the designated column contains a currency value.\n\t * @param columnIndex the index of the column\n\t * @return true if the value is a currency value, false otherwise\n\t * @see java.sql.ResultSetMetaData#isCurrency(int)\n\t */",
            "\t/**\n\t * 指示指定列是否包含货币值。\n\t * @param columnIndex 列索引\n\t * @return 若为货币值则为 true，否则为 false\n\t * @see java.sql.ResultSetMetaData#isCurrency(int)\n\t */",
        ),
        (
            "\t/**\n\t * Indicate whether the designated column contains a signed number.\n\t * @param columnIndex the index of the column\n\t * @return true if the column contains a signed number, false otherwise\n\t * @see java.sql.ResultSetMetaData#isSigned(int)\n\t */",
            "\t/**\n\t * 指示指定列是否包含有符号数。\n\t * @param columnIndex 列索引\n\t * @return 若列包含有符号数则为 true，否则为 false\n\t * @see java.sql.ResultSetMetaData#isSigned(int)\n\t */",
        ),
    ],
}
