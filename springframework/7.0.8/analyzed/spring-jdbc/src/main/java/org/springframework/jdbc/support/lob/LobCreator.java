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

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * 用于抽象大型二进制字段和大型文本字段的潜在数据库特定创建的接口。不适用于 API 中的 {@code java.sql.Blob} 和 {@code java.sql.Clob
 * } 实例，因为某些 JDBC 驱动程序本身不支持这些类型。
 * <p> LOB 创建部分是 {@link LobHandler} 实现通常不同的地方。可能的策略包括使用 {@code
 * PreparedStatement.setBinaryStream/setCharacterStream} 以及带有流参数或 {@code
 * java.sql.Blob/Clob} 包装对象的 {@code PreparedStatement.setBlob/setClob}。
 * <p>A LobCreator 表示用于创建 BLOB 的会话：它是 <i>not</i> 线程安全的，需要为每个语句执行或每个事务实例化。每个LobCreator完成后都需要
 * 关闭。
 * <p>为了方便地使用PreparedStatement和LobCreator，请考虑将{@link org.springframework.jdbc.core.JdbcTemp
 * late}与{@link org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementC
 * allback}实现一起使用。有关详细信息，请参阅后者的 javadoc。
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #close()
 * @see LobHandler#getLobCreator()
 * @see DefaultLobHandler.DefaultLobCreator
 * @see java.sql.PreparedStatement#setBlob
 * @see java.sql.PreparedStatement#setClob
 * @see java.sql.PreparedStatement#setBytes
 * @see java.sql.PreparedStatement#setBinaryStream
 * @see java.sql.PreparedStatement#setString
 * @see java.sql.PreparedStatement#setAsciiStream
 * @see java.sql.PreparedStatement#setCharacterStream
 * @deprecated 6.2，支持 {@link org.springframework.jdbc.core.support.SqlBinaryValue} 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public interface LobCreator extends Closeable {

	/**
	 * 使用给定参数索引将给定内容设置为给定语句上的字节。可能只是调用 {@code PreparedStatement.setBytes} 或为其创建一个 Blob 实例，具体取决于
	 * 数据库和驱动程序。
	 * @param ps 用于设置内容的PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param content 内容为字节数组，或 {@code null} for SQL NULL
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte @Nullable [] content)
			throws SQLException;

	/**
	 * 使用给定参数索引将给定内容设置为给定语句上的二进制流。可能只是调用 {@code PreparedStatement.setBinaryStream} 或为其创建一个 Blob
	 *  实例，具体取决于数据库和驱动程序。
	 * @param ps 用于设置内容的PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param contentStream 内容为二进制流，或 {@code null} for SQL NULL
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setBlobAsBinaryStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream contentStream, int contentLength)
			throws SQLException;

	/**
	 * 使用给定的参数索引，将给定的内容设置为给定语句上的字符串。可能只是调用 {@code PreparedStatement.setString} 或为其创建一个 Clob 实例，
	 * 具体取决于数据库和驱动程序。
	 * @param ps 用于设置内容的PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param content 内容为字符串，或 {@code null}（对于 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setClobAsString(PreparedStatement ps, int paramIndex, @Nullable String content)
			throws SQLException;

	/**
	 * 使用给定的参数索引，将给定的内容设置为给定语句上的 ASCII 流。可能只是调用 {@code PreparedStatement.setAsciiStream} 或为其创建一
	 * 个 Clob 实例，具体取决于数据库和驱动程序。
	 * @param ps 用于设置内容的PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param asciiStream 内容为 ASCII 流，或 {@code null} for SQL NULL
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.PreparedStatement#setAsciiStream
	 */
	void setClobAsAsciiStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream asciiStream, int contentLength)
			throws SQLException;

	/**
	 * 使用给定的参数索引将给定的内容设置为给定语句上的字符流。可能只是调用 {@code PreparedStatement.setCharacterStream} 或为其创建一个 
	 * Clob 实例，具体取决于数据库和驱动程序。
	 * @param ps 用于设置内容的PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param characterStream 内容为字符流，或 {@code null} for SQL NULL
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.PreparedStatement#setCharacterStream
	 */
	void setClobAsCharacterStream(
			PreparedStatement ps, int paramIndex, @Nullable Reader characterStream, int contentLength)
			throws SQLException;

	/**
	 * 关闭此 LobCreator 会话并释放其临时创建的 BLOB 和 CLOB。如果使用PreparedStatement的标准方法，则不需要执行任何操作，但如果使用专有方法，则
	 * 可能需要释放数据库资源。 <p><b>NOTE</b>：需要在执行涉及的PreparedStatement或刷新受影响的O/R映射会话后调用。否则，临时 BLOB 的数据库资源
	 * 可能会保持分配状态。
	 */
	@Override
	void close();

}
