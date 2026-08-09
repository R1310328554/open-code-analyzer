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
 * 抽象可能因数据库而异的大二进制字段与大文本字段创建的接口。
 * API 中不使用 {@code java.sql.Blob} 和 {@code java.sql.Clob} 实例，
 * 因为部分 JDBC 驱动并不支持这些类型。
 *
 * <p>LOB 创建是 {@link LobHandler} 实现通常存在差异的部分。
 * 可能的策略包括使用 {@code PreparedStatement.setBinaryStream/setCharacterStream}，
 * 或使用 {@code PreparedStatement.setBlob/setClob} 配合流参数或
 * {@code java.sql.Blob/Clob} 包装对象。
 *
 * <p>LobCreator 表示创建 BLOB 的会话：<i>非</i>线程安全，
 * 每次语句执行或每个事务都需新建实例，完成后必须关闭。
 *
 * <p>若需便捷地配合 PreparedStatement 与 LobCreator 使用，
 * 可考虑将 {@link org.springframework.jdbc.core.JdbcTemplate} 与
 * {@link org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback}
 * 实现配合使用，详见后者的 javadoc。
 *
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
 * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}
 * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public interface LobCreator extends Closeable {

	/**
	 * 使用给定参数索引，将内容作为字节设置到给定语句上。
	 * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setBytes}
	 * 或为其创建 Blob 实例。
	 * @param ps 要设置内容的 PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param content 字节数组形式的内容，或 {@code null} 表示 SQL NULL
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte @Nullable [] content)
			throws SQLException;

	/**
	 * 使用给定参数索引，将内容作为二进制流设置到给定语句上。
	 * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setBinaryStream}
	 * 或为其创建 Blob 实例。
	 * @param ps 要设置内容的 PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param contentStream 二进制流形式的内容，或 {@code null} 表示 SQL NULL
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setBlobAsBinaryStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream contentStream, int contentLength)
			throws SQLException;

	/**
	 * 使用给定参数索引，将内容作为 String 设置到给定语句上。
	 * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setString}
	 * 或为其创建 Clob 实例。
	 * @param ps 要设置内容的 PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param content String 形式的内容，或 {@code null} 表示 SQL NULL
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setClobAsString(PreparedStatement ps, int paramIndex, @Nullable String content)
			throws SQLException;

	/**
	 * 使用给定参数索引，将内容作为 ASCII 流设置到给定语句上。
	 * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setAsciiStream}
	 * 或为其创建 Clob 实例。
	 * @param ps 要设置内容的 PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param asciiStream ASCII 流形式的内容，或 {@code null} 表示 SQL NULL
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.PreparedStatement#setAsciiStream
	 */
	void setClobAsAsciiStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream asciiStream, int contentLength)
			throws SQLException;

	/**
	 * 使用给定参数索引，将内容作为字符流设置到给定语句上。
	 * 根据数据库和驱动，可能直接调用 {@code PreparedStatement.setCharacterStream}
	 * 或为其创建 Clob 实例。
	 * @param ps 要设置内容的 PreparedStatement
	 * @param paramIndex 要使用的参数索引
	 * @param characterStream 字符流形式的内容，或 {@code null} 表示 SQL NULL
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.PreparedStatement#setCharacterStream
	 */
	void setClobAsCharacterStream(
			PreparedStatement ps, int paramIndex, @Nullable Reader characterStream, int contentLength)
			throws SQLException;

	/**
	 * 关闭本 LobCreator 会话并释放临时创建的 BLOB 与 CLOB。
	 * 若使用 PreparedStatement 标准方法则通常无需操作，
	 * 但若使用专有方式则可能需要释放数据库资源。
	 * <p><b>NOTE</b>：须在相关 PreparedStatement 执行完毕
	 * 或受影响的 O/R 映射会话 flush 之后调用。
	 * 否则临时 BLOB 的数据库资源可能持续占用。
	 */
	@Override
	void close();

}
