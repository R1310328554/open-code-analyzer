/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * 用于处理特定数据库中的大型二进制字段和大型文本字段的抽象，无论表示为简单类型还是大型对象。
 * <p> 提供 BLOB 和 CLOB 的访问器方法，并充当 LobCreator 实例的工厂，用作创建 BLOB 或 CLOB 的会话。 LobCreators 通常为每个语句
 * 执行或每个事务实例化；它们不是线程安全的，因为它们可能会跟踪分配的数据库资源以便在执行后释放它们。
 * <p> 大多数数据库/驱动程序应该能够使用 {@link DefaultLobHandler}，它默认委托给 JDBC 的直接访问器方法，完全避免 {@code
 * java.sql.Blob} 和 {@code java.sql.Clob} API。通过设置 {@link DefaultLobHandler#setWrapAsLob
 * "wrapAsLob"} 属性，还可以将 {@link DefaultLobHandler} 配置为使用 {@code
 * PreparedStatement.setBlob/setClob}（例如，对于 PostgreSQL）访问 LOB。
 * <p>O当然需要为每个数据库声明不同的字段类型。在Oracle中，任何二进制内容都需要放入BLOB中，所有超过4000字节的字符内容都需要放入CLOB中。在 MySQL 中，没
 * 有 CLOB 类型的概念，而是有行为类似于 VARCHAR 的 LONGTEXT 类型。为了获得完整的可移植性，请对由于字段大小而通常需要某些数据库上的 LOB 的字段使用 L
 * obHandler（以 Oracle 的数字作为指导）。
 * <p><b>总结推荐选项（针对实际的 LOB 字段）：</b> <ul> <li><b>JDBC 4.0 驱动程序（包括 Oracle 11g 驱动程序）：</b> 使用
 * {@link DefaultLobHandler}，如果您的数据库驱动程序在填充 LOB 时需要该提示，则可以使用 {@code streamAsLob=true}场。如果您的
 * (Oracle) 数据库设置碰巧遇到 LOB 大小限制，请回退到 {@code createTemporaryLob=true}。 <li><b>Oracle 10g
 * 驱动程序：</b> 通过标准设置使用 {@link DefaultLobHandler}。在 Oracle 10.1
 * 上，设置“SetBigStringTryClob”连接属性；从 Oracle 10.2 开始，DefaultLobHandler 应该可以使用开箱即用的标准设置。
 * <li><b>PostgreSQL:</b> 使用 {@code wrapAsLob=true} 配置 {@link DefaultLobHandler}，并使用该
 * LobHandler 访问数据库表中的 OID 列（但不是 BYTEA）。 <li> 对于所有其他数据库驱动程序（以及对于在某些数据库上可能会变成 LOB 的非 LOB
 * 字段）：只需使用普通的 {@link DefaultLobHandler}。 OCAJAVA21文档
 * @author Juergen Hoeller
 * @since 23.12.2003
 * @see DefaultLobHandler
 * @see java.sql.ResultSet#getBlob
 * @see java.sql.ResultSet#getClob
 * @see java.sql.ResultSet#getBytes
 * @see java.sql.ResultSet#getBinaryStream
 * @see java.sql.ResultSet#getString
 * @see java.sql.ResultSet#getAsciiStream
 * @see java.sql.ResultSet#getCharacterStream
 * @deprecated 6.2，支持 {@link org.springframework.jdbc.core.support.SqlBinaryValue} 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public interface LobHandler {

	/**
	 * 从给定 ResultSet 中以字节形式检索给定列。可能只是调用 {@code ResultSet.getBytes} 或使用 {@code
	 * ResultSet.getBlob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容为字节数组，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getBytes
	 */
	byte @Nullable [] getBlobAsBytes(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 中以字节形式检索给定列。可能只是调用 {@code ResultSet.getBytes} 或使用 {@code
	 * ResultSet.getBlob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为字节数组，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getBytes
	 */
	byte @Nullable [] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以二进制流形式检索给定的列。可能只是调用 {@code ResultSet.getBinaryStream} 或使用 {@code
	 * ResultSet.getBlob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容为二进制流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getBinaryStream
	 */
	@Nullable InputStream getBlobAsBinaryStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以二进制流形式检索给定的列。可能只是调用 {@code ResultSet.getBinaryStream} 或使用 {@code
	 * ResultSet.getBlob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为二进制流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getBinaryStream
	 */
	@Nullable InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的结果集中以字符串形式检索给定的列。可能只是调用 {@code ResultSet.getString} 或使用 {@code ResultSet.getClob}，具体
	 * 取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容为字符串，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getString
	 */
	@Nullable String getClobAsString(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的结果集中以字符串形式检索给定的列。可能只是调用 {@code ResultSet.getString} 或使用 {@code ResultSet.getClob}，具体
	 * 取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为字符串，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getString
	 */
	@Nullable String getClobAsString(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以 ASCII 流形式检索给定的列。可能只是调用 {@code ResultSet.getAsciiStream} 或使用 {@code
	 * ResultSet.getClob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容为 ASCII 流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getAsciiStream
	 */
	@Nullable InputStream getClobAsAsciiStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以 ASCII 流形式检索给定的列。可能只是调用 {@code ResultSet.getAsciiStream} 或使用 {@code
	 * ResultSet.getClob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为 ASCII 流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getAsciiStream
	 */
	@Nullable InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的结果集中检索给定的列作为字符流。可能只是调用 {@code ResultSet.getCharacterStream} 或使用 {@code ResultSet.get
	 * Clob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容作为字符流
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的结果集中检索给定的列作为字符流。可能只是调用 {@code ResultSet.getCharacterStream} 或使用 {@code ResultSet.get
	 * Clob}，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容作为字符流
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 创建一个新的 {@link LobCreator} 实例，即用于创建 BLOB 和 CLOB 的会话。不再需要创建的 LOB 后需要关闭 - 通常是在语句执行或事务完成后。
	 * @return 新的 LobCreator 实例
	 * @see LobCreator#close()
	 */
	LobCreator getLobCreator();

}
