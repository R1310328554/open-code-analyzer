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
 * 在特定数据库中处理大二进制字段与大文本字段的抽象，
 * 无论其表示为简单类型还是大对象（LOB）。
 *
 * <p>提供 BLOB 与 CLOB 的访问方法，并作为 LobCreator 实例的工厂，
 * 用于创建 BLOB 或 CLOB 的会话。LobCreator 通常每次语句执行或每个事务实例化一次；
 * 非线程安全，因为可能跟踪已分配的数据库资源以便执行后释放。
 *
 * <p>大多数数据库/驱动可与 {@link DefaultLobHandler} 配合使用，
 * 其默认委托 JDBC 直接访问方法，完全避开 {@code java.sql.Blob} 和 {@code java.sql.Clob} API。
 * 也可通过设置 {@link DefaultLobHandler#setWrapAsLob "wrapAsLob"} 属性，
 * 配置 {@link DefaultLobHandler} 使用 {@code PreparedStatement.setBlob/setClob}
 * 访问 LOB（例如 PostgreSQL）。
 *
 * <p>当然，不同数据库需声明不同的字段类型。
 * Oracle 中二进制内容须存入 BLOB，超过 4000 字节的字符内容须存入 CLOB。
 * MySQL 没有 CLOB 概念，而是行为类似 VARCHAR 的 LONGTEXT。
 * 为完全可移植，对可能因字段大小而需要 LOB 的字段使用 LobHandler
 * （以 Oracle 的数字为参考）。
 *
 * <p><b>推荐选项摘要（针对实际 LOB 字段）：</b>
 * <ul>
 * <li><b>JDBC 4.0 驱动（含 Oracle 11g 驱动）：</b> 使用 {@link DefaultLobHandler}，
 * 若驱动填充 LOB 字段时需要该提示，可设 {@code streamAsLob=true}。
 * 若 Oracle 数据库配置遇到 LOB 大小限制，可回退到 {@code createTemporaryLob=true}。
 * <li><b>Oracle 10g 驱动：</b> 使用标准配置的 {@link DefaultLobHandler}。
 * Oracle 10.1 上需设置 "SetBigStringTryClob" 连接属性；Oracle 10.2 起
 * DefaultLobHandler 标准配置即可开箱即用。
 * <li><b>PostgreSQL：</b> 将 {@link DefaultLobHandler} 配置为 {@code wrapAsLob=true}，
 * 并用该 LobHandler 访问数据库表中的 OID 列（非 BYTEA）。
 * <li>其他所有数据库驱动（以及可能在某些数据库上变为 LOB 的非 LOB 字段）：
 * 直接使用普通 {@link DefaultLobHandler}。
 * </ul>
 *
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
 * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}
 * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public interface LobHandler {

	/**
	 * 从给定 ResultSet 检索指定列的字节内容。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getBytes}
	 * 或使用 {@code ResultSet.getBlob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnName 要使用的列名
	 * @return 字节数组形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getBytes
	 */
	byte @Nullable [] getBlobAsBytes(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的字节内容。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getBytes}
	 * 或使用 {@code ResultSet.getBlob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 字节数组形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getBytes
	 */
	byte @Nullable [] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的二进制流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getBinaryStream}
	 * 或使用 {@code ResultSet.getBlob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnName 要使用的列名
	 * @return 二进制流形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getBinaryStream
	 */
	@Nullable InputStream getBlobAsBinaryStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的二进制流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getBinaryStream}
	 * 或使用 {@code ResultSet.getBlob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 二进制流形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getBinaryStream
	 */
	@Nullable InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的 String 内容。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getString}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnName 要使用的列名
	 * @return String 形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getString
	 */
	@Nullable String getClobAsString(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的 String 内容。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getString}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return String 形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getString
	 */
	@Nullable String getClobAsString(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的 ASCII 流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getAsciiStream}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnName 要使用的列名
	 * @return ASCII 流形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getAsciiStream
	 */
	@Nullable InputStream getClobAsAsciiStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的 ASCII 流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getAsciiStream}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return ASCII 流形式的内容，SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getAsciiStream
	 */
	@Nullable InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的字符流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getCharacterStream}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnName 要使用的列名
	 * @return 字符流形式的内容
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 检索指定列的字符流。
	 * 根据数据库和驱动，可能直接调用 {@code ResultSet.getCharacterStream}
	 * 或使用 {@code ResultSet.getClob}。
	 * @param rs 要检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 字符流形式的内容
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 创建新的 {@link LobCreator} 实例，即创建 BLOB 与 CLOB 的会话。
	 * 创建的 LOB 不再需要后须关闭——通常在语句执行或事务完成后。
	 * @return 新的 LobCreator 实例
	 * @see LobCreator#close()
	 */
	LobCreator getLobCreator();

}
