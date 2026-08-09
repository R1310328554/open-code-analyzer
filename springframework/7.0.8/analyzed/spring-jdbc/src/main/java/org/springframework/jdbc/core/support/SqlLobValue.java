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

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.DisposableSqlTypeValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * 表示 SQL BLOB/CLOB 参数值的对象。BLOB 可为 InputStream 或字节数组；
 * CLOB 可为 Reader、InputStream 或 String。每个 CLOB/BLOB 值与其长度一并存储。
 * 类型取决于使用的构造函数。本类实例有状态且不可变：用完即弃。
 *
 * <p><b>注意：自 6.1.4 起，本类实质上已被 {@link SqlBinaryValue} 和 {@link SqlCharacterValue} 取代，
 * 后者支持现代 BLOB/CLOB 处理，同时兼容 LONGVARBINARY/LONGVARCHAR。</b>
 * 继续使用本类的唯一理由是自定义 {@link LobHandler}。
 *
 * <p>本类持有 {@link LobCreator} 引用，更新完成后须通过 {@link #cleanup()} 关闭。
 * 框架类会处理 {@code LobCreator} 的全部生命周期，
 * 本类使用者无需手动设置或关闭 {@code LobCreator}。
 *
 * <p>使用示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象
 * LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象
 *
 * jdbcTemplate.update(
 *     "INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)",
 *     new Object[] {
 *       name,
 *       new SqlLobValue(contentStream, contentLength, lobHandler),
 *       new SqlLobValue(description, lobHandler)
 *     },
 *     new int[] {Types.VARCHAR, Types.BLOB, Types.CLOB});
 * </pre>
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @see org.springframework.jdbc.core.JdbcTemplate#update(String, Object[], int[])
 * @see org.springframework.jdbc.object.SqlUpdate#update(Object[])
 * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)
 * @deprecated 自 6.2 起弃用，建议使用 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public class SqlLobValue implements DisposableSqlTypeValue {

	private final @Nullable Object content;

	private final int length;

	/**
	 * LobCreator 引用，以便更新完成后关闭。
	 */
	private final LobCreator lobCreator;


	/**
	 * 使用给定字节数组创建新的 BLOB 值，底层使用 DefaultLobHandler。
	 * @param bytes 包含 BLOB 值的字节数组
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(byte @Nullable [] bytes) {
		this(bytes, new DefaultLobHandler());
	}

	/**
	 * 使用给定字节数组创建新的 BLOB 值。
	 * @param bytes 包含 BLOB 值的字节数组
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(byte @Nullable [] bytes, LobHandler lobHandler) {
		this.content = bytes;
		this.length = (bytes != null ? bytes.length : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用给定内容字符串创建新的 CLOB 值，底层使用 DefaultLobHandler。
	 * @param content 包含 CLOB 值的 String
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(@Nullable String content) {
		this(content, new DefaultLobHandler());
	}

	/**
	 * 使用给定内容字符串创建新的 CLOB 值。
	 * @param content 包含 CLOB 值的 String
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(@Nullable String content, LobHandler lobHandler) {
		this.content = content;
		this.length = (content != null ? content.length() : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用给定流创建新的 BLOB/CLOB 值，底层使用 DefaultLobHandler。
	 * @param stream 包含 LOB 值的流
	 * @param length LOB 值长度
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(InputStream stream, int length) {
		this(stream, length, new DefaultLobHandler());
	}

	/**
	 * 使用给定流创建新的 BLOB/CLOB 值。
	 * @param stream 包含 LOB 值的流
	 * @param length LOB 值长度
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(InputStream stream, int length, LobHandler lobHandler) {
		this.content = stream;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用给定字符流创建新的 CLOB 值，底层使用 DefaultLobHandler。
	 * @param reader 包含 CLOB 值的字符流
	 * @param length CLOB 值长度
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(Reader reader, int length) {
		this(reader, length, new DefaultLobHandler());
	}

	/**
	 * 使用给定字符流创建新的 CLOB 值。
	 * @param reader 包含 CLOB 值的字符流
	 * @param length CLOB 值长度
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(Reader reader, int length, LobHandler lobHandler) {
		this.content = reader;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}


	/**
	 * 通过 LobCreator 设置指定内容。
	 */
	@Override
	public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, @Nullable String typeName)
			throws SQLException {

		if (sqlType == Types.BLOB) {
			if (this.content instanceof byte[] || this.content == null) {
				this.lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) this.content);
			}
			else if (this.content instanceof String string) {
				this.lobCreator.setBlobAsBytes(ps, paramIndex, string.getBytes());
			}
			else if (this.content instanceof InputStream inputStream) {
				this.lobCreator.setBlobAsBinaryStream(ps, paramIndex, inputStream, this.length);
			}
			else {
				throw new IllegalArgumentException(
						"Content type [" + this.content.getClass().getName() + "] not supported for BLOB columns");
			}
		}
		else if (sqlType == Types.CLOB) {
			if (this.content instanceof String || this.content == null) {
				this.lobCreator.setClobAsString(ps, paramIndex, (String) this.content);
			}
			else if (this.content instanceof InputStream inputStream) {
				this.lobCreator.setClobAsAsciiStream(ps, paramIndex, inputStream, this.length);
			}
			else if (this.content instanceof Reader reader) {
				this.lobCreator.setClobAsCharacterStream(ps, paramIndex, reader, this.length);
			}
			else {
				throw new IllegalArgumentException(
						"Content type [" + this.content.getClass().getName() + "] not supported for CLOB columns");
			}
		}
		else {
			throw new IllegalArgumentException("SqlLobValue only supports SQL types BLOB and CLOB");
		}
	}

	/**
	 * 关闭 LobCreator。
	 */
	@Override
	public void cleanup() {
		this.lobCreator.close();
	}

}
