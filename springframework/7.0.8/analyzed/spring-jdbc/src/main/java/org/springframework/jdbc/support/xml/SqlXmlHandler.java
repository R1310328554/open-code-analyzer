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
 * 用于处理特定数据库中的 XML 字段的抽象。其主要目的是隔离数据库中存储的 XML 的特定于数据库的处理。
 * <p>JDBC 4.0 引入了新的数据类型 {@code java.sql.SQLXML}，但大多数数据库及其驱动程序当前依赖于数据库特定的数据类型和功能。
 * <p> 提供 XML 字段的访问器方法并充当 {@link SqlXmlValue} 实例的工厂。
 * @author Thomas Risberg
 * @since 2.5.5
 * @see Jdbc4SqlXmlHandler
 * @see java.sql.SQLXML
 * @see java.sql.ResultSet#getSQLXML
 * @see java.sql.PreparedStatement#setSQLXML
 * @deprecated 6.2，支持直接使用 {@link ResultSet#getSQLXML} 和 {@link Connection#createSQLXML()}，可能与自定义 {@link org.springframework.jdbc.support.SqlValue} 实现结合使用
 */
@Deprecated(since = "6.2")
public interface SqlXmlHandler {

	//-------------------------------------------------------------------------
	// 访问 XML 内容的便捷方法
	//-------------------------------------------------------------------------

	/**
	 * 从给定的结果集中以字符串形式检索给定的列。 <p>M 可以简单地调用 {@code ResultSet.getString} 或使用 {@code SQLXML} 或特定于数据
	 * 库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容为字符串，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getString
	 * @see java.sql.ResultSet#getSQLXML
	 */
	@Nullable String getXmlAsString(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的结果集中以字符串形式检索给定的列。 <p>M 可以简单地调用 {@code ResultSet.getString} 或使用 {@code SQLXML} 或特定于数据
	 * 库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为字符串，如果 SQL NULL，则为 {@code null}
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getString
	 * @see java.sql.ResultSet#getSQLXML
	 */
	@Nullable String getXmlAsString(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以二进制流形式检索给定的列。 <p>M 可以简单地调用 {@code ResultSet.getAsciiStream} 或使用 {@code 
	 * SQLXML} 或特定于数据库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容作为二进制流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getBinaryStream
	 */
	@Nullable InputStream getXmlAsBinaryStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的 ResultSet 中以二进制流形式检索给定的列。 <p>M 可以简单地调用 {@code ResultSet.getAsciiStream} 或使用 {@code 
	 * SQLXML} 或特定于数据库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容为二进制流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getBinaryStream
	 */
	@Nullable InputStream getXmlAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 从给定的结果集中检索给定的列作为字符流。 <p>M 可以简单地调用 {@code ResultSet.getCharacterStream} 或使用 {@code SQLXML
	 * } 或特定于数据库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @return 内容作为字符流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getCharacterStream
	 */
	@Nullable Reader getXmlAsCharacterStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * 从给定的结果集中检索给定的列作为字符流。 <p>M 可以简单地调用 {@code ResultSet.getCharacterStream} 或使用 {@code SQLXML
	 * } 或特定于数据库的类，具体取决于数据库和驱动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @return 内容作为字符流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getCharacterStream
	 */
	@Nullable Reader getXmlAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * 使用给定 ResultSet 中的指定源类将给定列检索为 Source 实现。 <p>M 可能与 {@code SQLXML} 或特定于数据库的类一起使用，具体取决于数据库和驱
	 * 动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnName 要使用的列名称
	 * @param sourceClass 要使用的实现类
	 * @return 内容作为字符流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getSource
	 */
	@Nullable Source getXmlAsSource(ResultSet rs, String columnName, @Nullable Class<? extends Source> sourceClass) throws SQLException;

	/**
	 * 使用给定 ResultSet 中的指定源类将给定列检索为 Source 实现。 <p>M 可能与 {@code SQLXML} 或特定于数据库的类一起使用，具体取决于数据库和驱
	 * 动程序。
	 * @param rs 从中检索内容的 ResultSet
	 * @param columnIndex 要使用的列索引
	 * @param sourceClass 要使用的实现类
	 * @return 内容作为字符流，或 {@code null}（如果 SQL NULL）
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @see java.sql.ResultSet#getSQLXML
	 * @see java.sql.SQLXML#getSource
	 */
	@Nullable Source getXmlAsSource(ResultSet rs, int columnIndex, @Nullable Class<? extends Source> sourceClass) throws SQLException;


	//-------------------------------------------------------------------------
	// 构建 XML 内容的便捷方法
	//-------------------------------------------------------------------------

	/**
	 * 为给定的 XML 数据创建一个 {@code SqlXmlValue} 实例，由底层 JDBC 驱动程序支持。
	 * @param value 提供 XML 数据的 XML 字符串值
	 * @return 实施具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setString(String)
	 */
	SqlXmlValue newSqlXmlValue(String value);

	/**
	 * 为给定的 XML 数据创建一个 {@code SqlXmlValue} 实例，由底层 JDBC 驱动程序支持。
	 * @param provider 提供 XML 数据的 {@code XmlBinaryStreamProvider}
	 * @return 实施具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setBinaryStream()
	 */
	SqlXmlValue newSqlXmlValue(XmlBinaryStreamProvider provider);

	/**
	 * 为给定的 XML 数据创建一个 {@code SqlXmlValue} 实例，由底层 JDBC 驱动程序支持。
	 * @param provider 提供 XML 数据的 {@code XmlCharacterStreamProvider}
	 * @return 实施具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setCharacterStream()
	 */
	SqlXmlValue newSqlXmlValue(XmlCharacterStreamProvider provider);

	/**
	 * 为给定的 XML 数据创建一个 {@code SqlXmlValue} 实例，由底层 JDBC 驱动程序支持。
	 * @param resultClass 要使用的 Result 实现类
	 * @param provider 将提供 XML 数据的 {@code XmlResultProvider}
	 * @return 实施具体实例
	 * @see SqlXmlValue
	 * @see java.sql.SQLXML#setResult(Class)
	 */
	SqlXmlValue newSqlXmlValue(Class<? extends Result> resultClass, XmlResultProvider provider);

	/**
	 * 为给定的 XML 数据创建一个 {@code SqlXmlValue} 实例，由底层 JDBC 驱动程序支持。
	 * @param doc 要使用的 XML 文档
	 * @return 实施具体实例
	 * @see SqlXmlValue
	 */
	SqlXmlValue newSqlXmlValue(Document doc);

}
