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
 * 表示 SQL 语句字符型参数值的对象，例如 CLOB/NCLOB 或 LONGVARCHAR 列的字符流。
 *
 * <p>设计用于 {@link org.springframework.jdbc.core.JdbcTemplate} 和
 * {@link org.springframework.jdbc.core.simple.JdbcClient}，
 * 作为包装目标内容的参数值传入。
 *
 * <p>可与 {@link org.springframework.jdbc.core.SqlParameterValue} 组合以指定 SQL 类型，例如
 * {@code new SqlParameterValue(Types.CLOB, new SqlCharacterValue(myContent))}。
 * 多数数据库驱动实际上不需要类型提示。
 *
 * <p>注意：仅在实际 CLOB 时使用 {@code Types.CLOB}，否则优先 {@code Types.LONGVARCHAR}。
 * 这与 {@link SqlLobValue} 对字符序列的宽松处理不同。
 *
 * @author Juergen Hoeller
 * @since 6.1.4
 * @see SqlBinaryValue
 * @see org.springframework.jdbc.core.SqlParameterValue
 */
public class SqlCharacterValue implements SqlTypeValue {

	private final Object content;

	private final long length;


	/**
	 * 使用给定内容字符串创建新的 CLOB 值。
	 * @param string String 或其他 CharSequence 形式的内容
	 */
	public SqlCharacterValue(CharSequence string) {
		this.content = string;
		this.length = string.length();
	}

	/**
	 * 为给定内容创建新的 {@code SqlCharacterValue}。
	 * @param characters 字符数组形式的内容
	 */
	public SqlCharacterValue(char[] characters) {
		this.content = characters;
		this.length = characters.length;
	}

	/**
	 * 为给定内容创建新的 {@code SqlCharacterValue}。
	 * @param reader 内容 Reader
	 * @param length 内容长度（未知时为 -1）
	 */
	public SqlCharacterValue(Reader reader, long length) {
		this.content = reader;
		this.length = length;
	}

	/**
	 * 为给定内容创建新的 {@code SqlCharacterValue}。
	 * @param asciiStream ASCII 流形式的内容
	 * @param length 内容长度（未知时为 -1）
	 */
	public SqlCharacterValue(InputStream asciiStream, long length) {
		this.content = asciiStream;
		this.length = length;
	}


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
