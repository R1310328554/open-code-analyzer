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
 * 表示 SQL BLOB/CLOB 值参数的对象。 BLOB 可以是输入流或字节数组。 CLOB 可以采用 Reader、InputStream 或 String 的形式。每个 
 * CLOB/BLOB 值将与其长度一起存储。类型基于使用哪个构造函数。此类的实例是有状态且不可变的：使用它们并丢弃它们。
 * <p><b>NOTE：从 6.1.4 开始，此类已被 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}
 * 有效取代，它们能够进行现代 BLOB/CLOB 处理，同时还处理 LONGVARBINARY/LONGVARCHAR.</b> 继续使用此类的唯一原因是自定义 {@link
 * LobHandler}。
 * <p> 该类保存对 {@link LobCreator} 的引用，更新完成后必须关闭该引用。这是通过调用 {@link #cleanup()} 方法来完成的。 {@code L
 * obCreator} 的所有处理均由使用它的框架类完成 - 无需为此类的最终用户设置或关闭 {@code LobCreator}。
 * <p>A使用示例：
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); // 可重用对象
 * LobHandler lobHandler = new DefaultLobHandler(); // 可重用对象
 * jdbcTemplate.update( "INSERT INTO imagedb (image_name, content, description) VALUES (?, 
 * ?, ?)", new Object[] { name, new SqlLobValue(contentStream, contentLength, lobHandler), 
 * new SqlLobValue(description, lobHandler) }, new int[] {Types.VARCHAR, Types.BLOB, Types.
 * CLOB}); OCAJAVA0文档
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @see org.springframework.jdbc.core.JdbcTemplate#update(String, Object[], int[])
 * @see org.springframework.jdbc.object.SqlUpdate#update(Object[])
 * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)
 * @deprecated 6.2，支持 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public class SqlLobValue implements DisposableSqlTypeValue {

	/** `content`：该类的成员状态。 */
	private final @Nullable Object content;

	/** `length`：该类的成员状态。 */
	private final int length;

	/**
	 * 引用 LobCreator - 因此我们可以在更新完成后关闭它。
	 */
	private final LobCreator lobCreator;


	/**
	 * 使用 DefaultLobHandler 用给定的字节数组创建一个新的 BLOB 值。
	 * @param bytes 包含 BLOB 值的字节数组
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(byte @Nullable [] bytes) {
		this(bytes, new DefaultLobHandler());
	}

	/**
	 * 使用给定的字节数组创建一个新的 BLOB 值。
	 * @param bytes 包含 BLOB 值的字节数组
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(byte @Nullable [] bytes, LobHandler lobHandler) {
		this.content = bytes;
		this.length = (bytes != null ? bytes.length : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用 DefaultLobHandler 用给定的内容字符串创建一个新的 CLOB 值。
	 * @param content 包含 CLOB 值的字符串
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(@Nullable String content) {
		this(content, new DefaultLobHandler());
	}

	/**
	 * 使用给定的内容字符串创建一个新的 CLOB 值。
	 * @param content 包含 CLOB 值的字符串
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(@Nullable String content, LobHandler lobHandler) {
		this.content = content;
		this.length = (content != null ? content.length() : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用 DefaultLobHandler 使用给定流创建新的 BLOB/CLOB 值。
	 * @param stream 包含 LOB 值的流
	 * @param length LOB 值的长度
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(InputStream stream, int length) {
		this(stream, length, new DefaultLobHandler());
	}

	/**
	 * 使用给定流创建新的 BLOB/CLOB 值。
	 * @param stream 包含 LOB 值的流
	 * @param length LOB 值的长度
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(InputStream stream, int length, LobHandler lobHandler) {
		this.content = stream;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * 使用 DefaultLobHandler 用给定的字符流创建一个新的 CLOB 值。
	 * @param reader 包含 CLOB 值的字符流
	 * @param length CLOB 值的长度
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(Reader reader, int length) {
		this(reader, length, new DefaultLobHandler());
	}

	/**
	 * 使用给定的字符流创建一个新的 CLOB 值。
	 * @param reader 包含 CLOB 值的字符流
	 * @param length CLOB 值的长度
	 * @param lobHandler 要使用的 LobHandler
	 */
	public SqlLobValue(Reader reader, int length, LobHandler lobHandler) {
		this.content = reader;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}


	/**
	 * 通过LobCreator设置指定的内容。
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
