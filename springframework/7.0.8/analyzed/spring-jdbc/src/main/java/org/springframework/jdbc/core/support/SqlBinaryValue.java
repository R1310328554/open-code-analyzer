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
 * 表示 SQL 语句的二进制参数值的对象，例如 BLOB 或 LONGVARBINARY 或 PostgreSQL BYTEA 列的二进制流。
 * <p> 设计用于与 {@link org.springframework.jdbc.core.JdbcTemplate} 以及 {@link
 * org.springframework.jdbc.core.simple.JdbcClient} 一起使用，作为包装目标内容值的参数值传入。
 * <p>可以与{@link org.springframework.jdbc.core.SqlParameterValue}组合用于指定SQL类型，例如{@code new
 * SqlParameterValue(Types.BLOB, new
 * SqlBinaryValue(myContent))}。对于大多数数据库驱动程序，类型提示实际上并不是必需的。
 * <p>注意：仅在实际 BLOB 的情况下指定 {@code Types.BLOB}，否则首选 {@code Types.LONGVARBINARY}。对于
 * PostgreSQL，必须为 BYTEA 列指定 {@code Types.ARRAY}，而不是 {@code Types.BLOB}。这与 {@link
 * SqlLobValue} 形成鲜明对比，其中字节数组处理较为宽松。
 * @author Juergen Hoeller
 * @since 6.1.4
 * @see SqlCharacterValue
 * @see org.springframework.jdbc.core.SqlParameterValue
 */
public class SqlBinaryValue implements SqlTypeValue {

	/** `content`：该类的成员状态。 */
	private final Object content;

	/** `length`：该类的成员状态。 */
	private final long length;


	/**
	 * 为给定内容创建一个新的 {@code SqlBinaryValue}。
	 * @param bytes 内容作为字节数组
	 */
	public SqlBinaryValue(byte[] bytes) {
		this.content = bytes;
		this.length = bytes.length;
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlBinaryValue}。
	 * @param stream 内容流
	 * @param length 内容的长度（如果未确定则为-1）
	 */
	public SqlBinaryValue(InputStream stream, long length) {
		this.content = stream;
		this.length = length;
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlBinaryValue}。 <p>考虑指定具有内容长度支持的 {@link Resource}（如果可用）：{@link
	 * SqlBinaryValue#SqlBinaryValue(Resource)}。
	 * @param resource 从中获取内容流的资源
	 * @param length 内容的长度（如果未确定则为-1）
	 */
	public SqlBinaryValue(InputStreamSource resource, long length) {
		this.content = resource;
		this.length = length;
	}

	/**
	 * 为给定内容创建一个新的 {@code SqlBinaryValue}。 <p> 的长度将从 {@link Resource#contentLength()} 中获得。
	 * @param resource 从中获取内容流的资源
	 */
	public SqlBinaryValue(Resource resource) {
		this.content = resource;
		this.length = -1;
	}


	/**
	 * 设置 Type Value（`TypeValue`）。
	 */
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

	/**
	 * 设置 Byte Array（`ByteArray`）。
	 */
	private void setByteArray(PreparedStatement ps, int paramIndex, int sqlType, byte[] bytes)
			throws SQLException {

		if (sqlType == Types.BLOB) {
			ps.setBlob(paramIndex, new ByteArrayInputStream(bytes), bytes.length);
		}
		else {
			ps.setBytes(paramIndex, bytes);
		}
	}

	/**
	 * 设置 Input Stream（`InputStream`）。
	 */
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
