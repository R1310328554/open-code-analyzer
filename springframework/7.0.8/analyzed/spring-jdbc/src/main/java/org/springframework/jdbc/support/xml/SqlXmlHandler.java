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

package org.springframework.jdbc.support.xml;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

/**
 * 处理特定数据库中 XML 字段的抽象层，主要目的是隔离数据库内 XML 的厂商特定处理逻辑。
 *
 * <p>JDBC 4.0 引入了 {@code java.sql.SQLXML} 数据类型，但多数数据库及其驱动
 * 仍依赖厂商特定的数据类型与特性。
 *
 * <p>提供 XML 字段的访问方法，并作为 {@link SqlXmlValue} 实例的工厂。
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see Jdbc4SqlXmlHandler
 * @see java.sql.SQLXML
 * @see java.sql.ResultSet#getSQLXML
 * @see java.sql.PreparedStatement#setSQLXML
 * @deprecated 自 6.2 起弃用，推荐直接使用 {@link ResultSet#getSQLXML} 和
 * {@link Connection#createSQLXML()}，必要时结合自定义
 * {@link org.springframework.jdbc.support.SqlValue} 实现
 */
@Deprecated(since = "6.2")
public interface SqlXmlHandler {

	//-------------------------------------------------------------------------
	// 访问 XML 内容的便捷方法
	//-------------------------------------------------------------------------

	/**
	 * 从给定 ResultSet 中将指定列读取为字符串。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getString}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnName 列名
	 * @return 字符串形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getString
	 * @see java.sql.ResultSet#getSQLXML
	 */
	@Nullable String getXmlAsString(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为字符串。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getString}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnIndex 列索引
	 * @return 字符串形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getString
	 * @see java.sql.ResultSet#getSQLXML
	 */
	@Nullable String getXmlAsString(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为二进制流。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getAsciiStream}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnName 列名
	 * @return 二进制流形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getBinaryStream
	 */
	@Nullable InputStream getXmlAsBinaryStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为二进制流。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getAsciiStream}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnIndex 列索引
	 * @return 二进制流形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getBinaryStream
	 */
	@Nullable InputStream getXmlAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为字符流。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getCharacterStream}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnName 列名
	 * @return 字符流形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getCharacterStream
	 */
	@Nullable Reader getXmlAsCharacterStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为字符流。
	 * <p>视数据库与驱动而定，可能直接调用 {@code ResultSet.getCharacterStream}，
	 * 也可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnIndex 列索引
	 * @return 字符流形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getCharacterStream
	 */
	@Nullable Reader getXmlAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为以给定 Source 实现类表示的 {@link Source}。
	 * <p>视数据库与驱动而定，可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnName 列名
	 * @param sourceClass 要使用的 Source 实现类
	 * @return Source 形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getSource
	 */
	@Nullable Source getXmlAsSource(ResultSet rs, String columnName, @Nullable Class<? extends Source> sourceClass) throws SQLException;

	/**
	 * 从给定 ResultSet 中将指定列读取为以给定 Source 实现类表示的 {@link Source}。
	 * <p>视数据库与驱动而定，可能通过 {@code SQLXML} 或厂商特定类处理。
	 * @param rs 待读取内容的 ResultSet
	 * @param columnIndex 列索引
	 * @param sourceClass 要使用的 Source 实现类
	 * @return Source 形式的内容；SQL NULL 时为 {@code null}
	 * @throws SQLException JDBC 方法抛出时
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getSource
	 */
	@Nullable Source getXmlAsSource(ResultSet rs, int columnIndex, @Nullable Class<? extends Source> sourceClass) throws SQLException;


	//-------------------------------------------------------------------------
	// 构建 XML 内容的便捷方法
	//-------------------------------------------------------------------------

	/**
	 * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。
	 * @param value 提供 XML 数据的字符串
	 * @return 与实现相关的具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setString(String)
	 */
	SqlXmlValue newSqlXmlValue(String value);

	/**
	 * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。
	 * @param provider 提供 XML 数据的 {@code XmlBinaryStreamProvider}
	 * @return 与实现相关的具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setBinaryStream()
	 */
	SqlXmlValue newSqlXmlValue(XmlBinaryStreamProvider provider);

	/**
	 * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。
	 * @param provider 提供 XML 数据的 {@code XmlCharacterStreamProvider}
	 * @return 与实现相关的具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setCharacterStream()
	 */
	SqlXmlValue newSqlXmlValue(XmlCharacterStreamProvider provider);

	/**
	 * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。
	 * @param resultClass 要使用的 Result 实现类
	 * @param provider 提供 XML 数据的 {@code XmlResultProvider}
	 * @return 与实现相关的具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setResult(Class)
	 */
	SqlXmlValue newSqlXmlValue(Class<? extends Result> resultClass, XmlResultProvider provider);

	/**
	 * 为给定 XML 数据创建 {@code SqlXmlValue} 实例，具体形式由底层 JDBC 驱动支持。
	 * @param doc 要使用的 XML 文档
	 * @return 与实现相关的具体实例
	 * @see SqlXmlValue
	 */
	SqlXmlValue newSqlXmlValue(Document doc);

}
