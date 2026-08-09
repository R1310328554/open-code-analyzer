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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

/**
 * {@link LobHandler} 接口的默认实现。调用 {@code java.sql.ResultSet} 和 {@code
 * java.sql.PreparedStatement} 提供的直接访问器方法。
 * <p> 默认情况下，传入流将传递到 JDBC 驱动程序的 {@link PreparedStatement} 上相应的 {@code setBinary/Ascii/Chara
 * cterStream} 方法。如果指定的内容长度为负数，则此处理程序将使用不带长度参数的 set-stream 方法的 JDBC 4.0 变体；否则，它将把指定的长度传递给驱动
 * 程序。
 * <p>此 LobHandler 应该适用于根据有关简单 BLOB 和 CLOB 处理的规范建议兼容 JDBC 的任何 JDBC 驱动程序。这根本不适用于 Oracle 9i 的
 * 驱动程序；从 Oracle 10g 开始，它确实可以工作，但可能仍然存在 LOB 大小限制。即使在使用较旧的数据库服务器时，也请考虑使用最新的 Oracle 驱动程序。有关完整
 * 的建议，请参阅 {@link LobHandler} javadoc。
 * <p> 某些 JDBC 驱动程序要求通过 JDBC {@code setBlob} / {@code setClob} API 显式设置具有 BLOB/CLOB
 * 目标列的值：例如 PostgreSQL 的驱动程序。针对此类驱动程序进行操作时，将 {@link #setWrapAsLob "wrapAsLob"} 属性切换为“true”。
 * <p> 在 JDBC 4.0 上，此 LobHandler 还支持通过直接采用流参数的 {@code setBlob} / {@code setClob} 变体流式传输
 * BLOB/CLOB 内容。当针对完全兼容的 JDBC 4.0 驱动程序进行操作时，请考虑将 {@link #setStreamAsLob "streamAsLob"}
 * 属性切换为“true”。
 * <p>最后，这个LobHandler还支持临时BLOB/CLOB对象的创建。当“streamAsLob”碰巧遇到 LOB 大小限制时，请考虑将 {@link #setCreat
 * eTemporaryLob "createTemporaryLob"} 属性切换为“true”。
 * <p> 有关建议摘要，请参阅 {@link LobHandler} 接口 javadoc。
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see java.sql.ResultSet#getBytes
 * @see java.sql.ResultSet#getBinaryStream
 * @see java.sql.ResultSet#getString
 * @see java.sql.ResultSet#getAsciiStream
 * @see java.sql.ResultSet#getCharacterStream
 * @see java.sql.PreparedStatement#setBytes
 * @see java.sql.PreparedStatement#setBinaryStream
 * @see java.sql.PreparedStatement#setString
 * @see java.sql.PreparedStatement#setAsciiStream
 * @see java.sql.PreparedStatement#setCharacterStream
 * @deprecated 6.2，支持 {@link org.springframework.jdbc.core.support.SqlBinaryValue} 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public class DefaultLobHandler extends AbstractLobHandler {

	/** 日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 是否将字节数组/String 包装为 JDBC Blob/Clob 提交。 */
	private boolean wrapAsLob = false;

	/** 是否以 JDBC 4.0 LOB 流方式提交二进制/字符流。 */
	private boolean streamAsLob = false;

	/** 是否通过 JDBC 4.0 创建临时 Blob/Clob。 */
	private boolean createTemporaryLob = false;


	/**
	 * 指定是否使用带有 Blob / Clob 参数的 JDBC {@code setBlob} / {@code setClob} 方法将字节数组 / 字符串提交到包装在
	 * JDBC Blob / Clob 对象中的 JDBC 驱动程序。 <p>Default 为“false”，使用常见的 JDBC 2.0 {@code
	 * setBinaryStream} / {@code setCharacterStream} 方法设置内容。将其切换为“true”，以针对已知需要此类包装的 JDBC
	 * 驱动程序进行显式 Blob / Clob 包装（例如，PostgreSQL 用于访问 OID 列，而 BYTEA 列需要以标准方式访问）。 <p>
	 * 此设置影响字节数组/字符串参数以及流参数，除非 {@link #setStreamAsLob "streamAsLob"} 覆盖此处理以使用 JDBC 4.0
	 * 的新显式流支持（如果可用）。
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	public void setWrapAsLob(boolean wrapAsLob) {
		this.wrapAsLob = wrapAsLob;
	}

	/**
	 * 使用带有流参数的 JDBC 4.0 {@code setBlob} / {@code setClob} 方法指定是否将二进制流/字符流作为显式 LOB 内容提交给 JDBC
	 * 驱动程序。 <p>Default 为“false”，使用常见的 JDBC 2.0 {@code setBinaryStream} / {@code
	 * setCharacterStream} 方法设置内容。对于显式 JDBC 4.0 流，将此选项切换为“true”，前提是您的 JDBC 驱动程序实际上支持这些 JDBC 4.0
	 * 操作（例如，Derby 的操作）。 <p>此设置影响流参数以及字节数组/字符串参数，需要 JDBC 4.0 支持。要支持 JDBC 3.0 的 LOB 内容，请查看
	 * {@link #setWrapAsLob "wrapAsLob"} 设置。
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	public void setStreamAsLob(boolean streamAsLob) {
		this.streamAsLob = streamAsLob;
	}

	/**
	 * 指定是否将字节数组/字符串复制到通过 JDBC 4.0 {@code createBlob} / {@code createClob} 方法创建的临时 JDBC Blob /
	 * Clob 对象中。 <p>Default 为“false”，使用常见的 JDBC 2.0 {@code setBinaryStream} / {@code
	 * setCharacterStream} 方法设置内容。将其切换为“true”以使用 JDBC 4.0 显式创建 Blob/Clob。
	 * <p>此设置影响流参数以及字节数组/字符串参数，需要 JDBC 4.0 支持。要支持 JDBC 3.0 的 LOB 内容，请查看 {@link #setWrapAsLob
	 * "wrapAsLob"} 设置。
	 * @see java.sql.Connection#createBlob()
	 * @see java.sql.Connection#createClob()
	 */
	public void setCreateTemporaryLob(boolean createTemporaryLob) {
		this.createTemporaryLob = createTemporaryLob;
	}


	/**
	 * 获取 Blob As Bytes（`BlobAsBytes`）。
	 */
	@Override
	public byte @Nullable [] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as bytes");
		if (this.wrapAsLob) {
			Blob blob = rs.getBlob(columnIndex);
			return blob.getBytes(1, (int) blob.length());
		}
		else {
			return rs.getBytes(columnIndex);
		}
	}

	/**
	 * 获取 Blob As Binary Stream（`BlobAsBinaryStream`）。
	 */
	@Override
	public @Nullable InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as binary stream");
		if (this.wrapAsLob) {
			Blob blob = rs.getBlob(columnIndex);
			return blob.getBinaryStream();
		}
		else {
			return rs.getBinaryStream(columnIndex);
		}
	}

	/**
	 * 获取 Clob As String（`ClobAsString`）。
	 */
	@Override
	public @Nullable String getClobAsString(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as string");
		if (this.wrapAsLob) {
			Clob clob = rs.getClob(columnIndex);
			return clob.getSubString(1, (int) clob.length());
		}
		else {
			return rs.getString(columnIndex);
		}
	}

	/**
	 * 获取 Clob As Ascii Stream（`ClobAsAsciiStream`）。
	 */
	@Override
	public InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as ASCII stream");
		if (this.wrapAsLob) {
			Clob clob = rs.getClob(columnIndex);
			return clob.getAsciiStream();
		}
		else {
			return rs.getAsciiStream(columnIndex);
		}
	}

	/**
	 * 获取 Clob As Character Stream（`ClobAsCharacterStream`）。
	 */
	@Override
	public Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as character stream");
		if (this.wrapAsLob) {
			Clob clob = rs.getClob(columnIndex);
			return clob.getCharacterStream();
		}
		else {
			return rs.getCharacterStream(columnIndex);
		}
	}

	/**
	 * 获取 Lob Creator（`LobCreator`）。
	 */
	@Override
	public LobCreator getLobCreator() {
		return (this.createTemporaryLob ? new TemporaryLobCreator() : new DefaultLobCreator());
	}


	/**
	 * 默认 LobCreator 实现为内部类。可以在 DefaultLobHandler 扩展中进行子类化。
	 */
	protected class DefaultLobCreator implements LobCreator {

		@Override
		public void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte @Nullable [] content)
				throws SQLException {

			if (streamAsLob) {
				if (content != null) {
					ps.setBlob(paramIndex, new ByteArrayInputStream(content), content.length);
				}
				else {
					ps.setBlob(paramIndex, (Blob) null);
				}
			}
			else if (wrapAsLob) {
				if (content != null) {
					ps.setBlob(paramIndex, new PassThroughBlob(content));
				}
				else {
					ps.setBlob(paramIndex, (Blob) null);
				}
			}
			else {
				ps.setBytes(paramIndex, content);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(content != null ? "Set bytes for BLOB with length " + content.length :
						"Set BLOB to null");
			}
		}

		@Override
		public void setBlobAsBinaryStream(
				PreparedStatement ps, int paramIndex, @Nullable InputStream binaryStream, int contentLength)
				throws SQLException {

			if (streamAsLob) {
				if (binaryStream != null) {
					if (contentLength >= 0) {
						ps.setBlob(paramIndex, binaryStream, contentLength);
					}
					else {
						ps.setBlob(paramIndex, binaryStream);
					}
				}
				else {
					ps.setBlob(paramIndex, (Blob) null);
				}
			}
			else if (wrapAsLob) {
				if (binaryStream != null) {
					ps.setBlob(paramIndex, new PassThroughBlob(binaryStream, contentLength));
				}
				else {
					ps.setBlob(paramIndex, (Blob) null);
				}
			}
			else if (contentLength >= 0) {
				ps.setBinaryStream(paramIndex, binaryStream, contentLength);
			}
			else {
				ps.setBinaryStream(paramIndex, binaryStream);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(binaryStream != null ? "Set binary stream for BLOB with length " + contentLength :
						"Set BLOB to null");
			}
		}

		@Override
		public void setClobAsString(PreparedStatement ps, int paramIndex, @Nullable String content)
				throws SQLException {

			if (streamAsLob) {
				if (content != null) {
					ps.setClob(paramIndex, new StringReader(content), content.length());
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else if (wrapAsLob) {
				if (content != null) {
					ps.setClob(paramIndex, new PassThroughClob(content));
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else {
				ps.setString(paramIndex, content);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(content != null ? "Set string for CLOB with length " + content.length() :
						"Set CLOB to null");
			}
		}

		@Override
		public void setClobAsAsciiStream(
				PreparedStatement ps, int paramIndex, @Nullable InputStream asciiStream, int contentLength)
				throws SQLException {

			if (streamAsLob) {
				if (asciiStream != null) {
					Reader reader = new InputStreamReader(asciiStream, StandardCharsets.US_ASCII);
					if (contentLength >= 0) {
						ps.setClob(paramIndex, reader, contentLength);
					}
					else {
						ps.setClob(paramIndex, reader);
					}
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else if (wrapAsLob) {
				if (asciiStream != null) {
					ps.setClob(paramIndex, new PassThroughClob(asciiStream, contentLength));
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else if (contentLength >= 0) {
				ps.setAsciiStream(paramIndex, asciiStream, contentLength);
			}
			else {
				ps.setAsciiStream(paramIndex, asciiStream);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(asciiStream != null ? "Set ASCII stream for CLOB with length " + contentLength :
						"Set CLOB to null");
			}
		}

		@Override
		public void setClobAsCharacterStream(
				PreparedStatement ps, int paramIndex, @Nullable Reader characterStream, int contentLength)
				throws SQLException {

			if (streamAsLob) {
				if (characterStream != null) {
					if (contentLength >= 0) {
						ps.setClob(paramIndex, characterStream, contentLength);
					}
					else {
						ps.setClob(paramIndex, characterStream);
					}
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else if (wrapAsLob) {
				if (characterStream != null) {
					ps.setClob(paramIndex, new PassThroughClob(characterStream, contentLength));
				}
				else {
					ps.setClob(paramIndex, (Clob) null);
				}
			}
			else if (contentLength >= 0) {
				ps.setCharacterStream(paramIndex, characterStream, contentLength);
			}
			else {
				ps.setCharacterStream(paramIndex, characterStream);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(characterStream != null ? "Set character stream for CLOB with length " + contentLength :
						"Set CLOB to null");
			}
		}

		@Override
		public void close() {
			// 不创建临时 LOB 时无需执行任何操作
		}
	}

}
