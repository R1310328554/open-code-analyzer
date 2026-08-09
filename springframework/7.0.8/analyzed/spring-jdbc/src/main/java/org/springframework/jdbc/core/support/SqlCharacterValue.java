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

package org.springframework.jdbc.core.support;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.SqlTypeValue;

/**
 * 表示 SQL 语句的基于字符的参数值的对象，例如 CLOB/NCLOB 或 LONGVARCHAR 列的字符流。
 * <p> 设计用于与 {@link org.springframework.jdbc.core.JdbcTemplate} 以及 {@link
 * org.springframework.jdbc.core.simple.JdbcClient} 一起使用，作为包装目标内容值的参数值传入。
 * <p>可以与{@link org.springframework.jdbc.core.SqlParameterValue}组合用于指定SQL类型，例如{@code new
 * SqlParameterValue(Types.CLOB, new
 * SqlCharacterValue(myContent))}。对于大多数数据库驱动程序，类型提示实际上并不是必需的。
 * <p>注意：仅在实际 CLOB 的情况下指定 {@code Types.CLOB}，否则首选 {@code Types.LONGVARCHAR}。这与 {@link
 * SqlLobValue} 形成鲜明对比，其中字符序列处理较为宽松。
 * @author Juergen Hoeller
 * @since 6.1.4
 * @see SqlBinaryValue
 * @see org.springframework.jdbc.core.SqlParameterValue
 */
public class SqlCharacterValue implements SqlTypeValue {

	/** `content`：该类的成员状态。 */
	private final Object content;

	/** `length`：该类的成员状态。 */
	private final long length;


	/**
	 * 使用给定的内容字符串创建一个新的 CLOB 值。
	 * @param string String 或其他 CharSequence 形式的内容
	 */
	public SqlCharacterValue(CharSequence string) {
		this.content = string;
		this.length = string.length();
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlCharacterValue}。
	 * @param characters 内容为字符数组
	 */
	public SqlCharacterValue(char[] characters) {
		this.content = characters;
		this.length = characters.length;
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlCharacterValue}。
	 * @param reader 内容阅读器
	 * @param length 内容的长度（如果未确定则为-1）
	 */
	public SqlCharacterValue(Reader reader, long length) {
		this.content = reader;
		this.length = length;
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlCharacterValue}。
	 * @param asciiStream 内容为 ASCII 流
	 * @param length 内容的长度（如果未确定则为-1）
	 */
	public SqlCharacterValue(InputStream asciiStream, long length) {
		this.content = asciiStream;
		this.length = length;
	}


	/**
	 * 设置 Type Value（`TypeValue`）。
	 */
	@Override
	public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException {

		if (this.content instanceof CharSequence) {
			setString(ps, paramIndex, sqlType, this.content.toString());
		}
		else if (this.content instanceof char[] chars) {
			setReader(ps, paramIndex, sqlType, new CharArrayReader(chars), this.length);
		}
		else if (this.content instanceof Reader reader) {
			setReader(ps, paramIndex, sqlType, reader, this.length);
		}
		else if (this.content instanceof InputStream inputStream) {
			setInputStream(ps, paramIndex, inputStream, this.length);
		}
		else {
			throw new IllegalArgumentException("Illegal content type: " + this.content.getClass().getName());
		}
	}

	/**
	 * 设置 String（`String`）。
	 */
	private void setString(PreparedStatement ps, int paramIndex, int sqlType, String string)
			throws SQLException {

		if (sqlType == Types.CLOB) {
			ps.setClob(paramIndex, new StringReader(string), string.length());
		}
		else if (sqlType == Types.NCLOB) {
			ps.setNClob(paramIndex, new StringReader(string), string.length());
		}
		else {
			ps.setString(paramIndex, string);
		}
	}

	/**
	 * 设置 Reader（`Reader`）。
	 */
	private void setReader(PreparedStatement ps, int paramIndex, int sqlType, Reader reader, long length)
			throws SQLException {

		if (sqlType == Types.CLOB) {
			if (length >= 0) {
				ps.setClob(paramIndex, reader, length);
			}
			else {
				ps.setClob(paramIndex, reader);
			}
		}
		else if (sqlType == Types.NCLOB) {
			if (length >= 0) {
				ps.setNClob(paramIndex, reader, length);
			}
			else {
				ps.setNClob(paramIndex, reader);
			}
		}
		else {
			if (length >= 0) {
				ps.setCharacterStream(paramIndex, reader, length);
			}
			else {
				ps.setCharacterStream(paramIndex, reader);
			}
		}
	}

	/**
	 * 设置 Input Stream（`InputStream`）。
	 */
	private void setInputStream(PreparedStatement ps, int paramIndex, InputStream is, long length)
			throws SQLException {

		if (length >= 0) {
			ps.setAsciiStream(paramIndex, is, length);
		}
		else {
			ps.setAsciiStream(paramIndex, is);
		}
	}

}
