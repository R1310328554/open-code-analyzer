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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlTypeValue;

/**
 * 表示 SQL 语句二进制参数值的对象，例如 BLOB、LONGVARBINARY
 * 或 PostgreSQL BYTEA 列的二进制流。
 *
 * <p>设计用于 {@link org.springframework.jdbc.core.JdbcTemplate} 和
 * {@link org.springframework.jdbc.core.simple.JdbcClient}，
 * 作为包装目标内容的参数值传入。
 *
 * <p>可与 {@link org.springframework.jdbc.core.SqlParameterValue} 组合以指定 SQL 类型，例如
 * {@code new SqlParameterValue(Types.BLOB, new SqlBinaryValue(myContent))}。
 * 多数数据库驱动实际上不需要类型提示。
 *
 * <p>注意：仅在实际 BLOB 时使用 {@code Types.BLOB}，否则优先 {@code Types.LONGVARBINARY}。
 * PostgreSQL 的 BYTEA 列应指定 {@code Types.ARRAY} 而非 {@code Types.BLOB}。
 * 这与 {@link SqlLobValue} 对字节数组的宽松处理不同。
 *
 * @author Juergen Hoeller
 * @since 6.1.4
 * @see SqlCharacterValue
 * @see org.springframework.jdbc.core.SqlParameterValue
 */
public class SqlBinaryValue implements SqlTypeValue {

	private final Object content;

	private final long length;


	/**
	 * 为给定内容创建新的 {@code SqlBinaryValue}。
	 * @param bytes 字节数组形式的内容
	 */
	public SqlBinaryValue(byte[] bytes) {
		this.content = bytes;
		this.length = bytes.length;
	}

	/**
	 * 为给定内容创建新的 {@code SqlBinaryValue}。
	 * @param stream 内容流
	 * @param length 内容长度（未知时为 -1）
	 */
	public SqlBinaryValue(InputStream stream, long length) {
		this.content = stream;
		this.length = length;
	}

	/**
	 * 为给定内容创建新的 {@code SqlBinaryValue}。
	 * <p>若可用，建议使用支持 contentLength 的 {@link Resource}：
	 * {@link SqlBinaryValue#SqlBinaryValue(Resource)}。
	 * @param resource 用于获取内容流的资源
	 * @param length 内容长度（未知时为 -1）
	 */
	public SqlBinaryValue(InputStreamSource resource, long length) {
		this.content = resource;
		this.length = length;
	}

	/**
	 * 为给定内容创建新的 {@code SqlBinaryValue}。
	 * <p>长度将从 {@link Resource#contentLength()} 推导。
	 * @param resource 用于获取内容流的资源
	 */
	public SqlBinaryValue(Resource resource) {
		this.content = resource;
		this.length = -1;
	}


	@Override
	public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException {

		if (this.content instanceof byte[] bytes) {
			setByteArray(ps, paramIndex, sqlType, bytes);
		}
		else if (this.content instanceof InputStream inputStream) {
			setInputStream(ps, paramIndex, sqlType, inputStream, this.length);
		}
		else if (this.content instanceof Resource resource) {
			try {
				setInputStream(ps, paramIndex, sqlType, resource.getInputStream(), resource.contentLength());
			}
			catch (IOException ex) {
				throw new IllegalArgumentException("Cannot open binary stream for JDBC value: " + resource, ex);
			}
		}
		else if (this.content instanceof InputStreamSource resource) {
			try {
				setInputStream(ps, paramIndex, sqlType, resource.getInputStream(), this.length);
			}
			catch (IOException ex) {
				throw new IllegalArgumentException("Cannot open binary stream for JDBC value: " + resource, ex);
			}
		}
		else {
			throw new IllegalArgumentException("Illegal content type: " + this.content.getClass().getName());
		}
	}

	private void setByteArray(PreparedStatement ps, int paramIndex, int sqlType, byte[] bytes)
			throws SQLException {

		if (sqlType == Types.BLOB) {
			ps.setBlob(paramIndex, new ByteArrayInputStream(bytes), bytes.length);
		}
		else {
			ps.setBytes(paramIndex, bytes);
		}
	}

	private void setInputStream(PreparedStatement ps, int paramIndex, int sqlType, InputStream is, long length)
			throws SQLException {

		if (sqlType == Types.BLOB) {
			if (length >= 0) {
				ps.setBlob(paramIndex, is, length);
			}
			else {
				ps.setBlob(paramIndex, is);
			}
		}
		else {
			if (length >= 0) {
				ps.setBinaryStream(paramIndex, is, length);
			}
			else {
				ps.setBinaryStream(paramIndex, is);
			}
		}
	}

}
