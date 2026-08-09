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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.FileCopyUtils;

/**
 * {@link LobCreator}基于临时LOB的实现，使用JDBC的{@link java.sql.Connection#createBlob()} / {@link
 * java.sql.Connection#createClob()}机制。
 * <p>由DefaultLobHandler的{@link DefaultLobHandler#setCreateTemporaryLob}模式使用。也可直接用于重用临时 LOB
 *  的跟踪和释放。
 * @author Juergen Hoeller
 * @since 3.2.2
 * @see DefaultLobHandler#setCreateTemporaryLob
 * @see java.sql.Connection#createBlob()
 * @see java.sql.Connection#createClob()
 * @deprecated 6.2，支持 {@link org.springframework.jdbc.core.support.SqlBinaryValue} 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public class TemporaryLobCreator implements LobCreator {

	/**
	 * 获取 Log（`Log`）。
	 */
	protected static final Log logger = LogFactory.getLog(TemporaryLobCreator.class);

	private final Set<Blob> temporaryBlobs = new LinkedHashSet<>(1);

	private final Set<Clob> temporaryClobs = new LinkedHashSet<>(1);


	/**
	 * 设置 Blob As Bytes（`BlobAsBytes`）。
	 */
	@Override
	public void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte @Nullable [] content)
			throws SQLException {

		if (content != null) {
			Blob blob = ps.getConnection().createBlob();
			blob.setBytes(1, content);
			this.temporaryBlobs.add(blob);
			ps.setBlob(paramIndex, blob);
		}
		else {
			ps.setBlob(paramIndex, (Blob) null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(content != null ? "Copied bytes into temporary BLOB with length " + content.length :
					"Set BLOB to null");
		}
	}

	/**
	 * 设置 Blob As Binary Stream（`BlobAsBinaryStream`）。
	 */
	@Override
	public void setBlobAsBinaryStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream binaryStream, int contentLength)
			throws SQLException {

		if (binaryStream != null) {
			Blob blob = ps.getConnection().createBlob();
			try {
				FileCopyUtils.copy(binaryStream, blob.setBinaryStream(1));
			}
			catch (IOException ex) {
				throw new DataAccessResourceFailureException("Could not copy into LOB stream", ex);
			}
			this.temporaryBlobs.add(blob);
			ps.setBlob(paramIndex, blob);
		}
		else {
			ps.setBlob(paramIndex, (Blob) null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(binaryStream != null ?
					"Copied binary stream into temporary BLOB with length " + contentLength :
					"Set BLOB to null");
		}
	}

	/**
	 * 设置 Clob As String（`ClobAsString`）。
	 */
	@Override
	public void setClobAsString(PreparedStatement ps, int paramIndex, @Nullable String content)
			throws SQLException {

		if (content != null) {
			Clob clob = ps.getConnection().createClob();
			clob.setString(1, content);
			this.temporaryClobs.add(clob);
			ps.setClob(paramIndex, clob);
		}
		else {
			ps.setClob(paramIndex, (Clob) null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(content != null ? "Copied string into temporary CLOB with length " + content.length() :
					"Set CLOB to null");
		}
	}

	/**
	 * 设置 Clob As Ascii Stream（`ClobAsAsciiStream`）。
	 */
	@Override
	public void setClobAsAsciiStream(
			PreparedStatement ps, int paramIndex, @Nullable InputStream asciiStream, int contentLength)
			throws SQLException {

		if (asciiStream != null) {
			Clob clob = ps.getConnection().createClob();
			try {
				FileCopyUtils.copy(asciiStream, clob.setAsciiStream(1));
			}
			catch (IOException ex) {
				throw new DataAccessResourceFailureException("Could not copy into LOB stream", ex);
			}
			this.temporaryClobs.add(clob);
			ps.setClob(paramIndex, clob);
		}
		else {
			ps.setClob(paramIndex, (Clob) null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(asciiStream != null ?
					"Copied ASCII stream into temporary CLOB with length " + contentLength :
					"Set CLOB to null");
		}
	}

	/**
	 * 设置 Clob As Character Stream（`ClobAsCharacterStream`）。
	 */
	@Override
	public void setClobAsCharacterStream(
			PreparedStatement ps, int paramIndex, @Nullable Reader characterStream, int contentLength)
			throws SQLException {

		if (characterStream != null) {
			Clob clob = ps.getConnection().createClob();
			try {
				FileCopyUtils.copy(characterStream, clob.setCharacterStream(1));
			}
			catch (IOException ex) {
				throw new DataAccessResourceFailureException("Could not copy into LOB stream", ex);
			}
			this.temporaryClobs.add(clob);
			ps.setClob(paramIndex, clob);
		}
		else {
			ps.setClob(paramIndex, (Clob) null);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(characterStream != null ?
					"Copied character stream into temporary CLOB with length " + contentLength :
					"Set CLOB to null");
		}
	}

	/**
	 * 关闭（方法 `close`）。
	 */
	@Override
	public void close() {
		for (Blob blob : this.temporaryBlobs) {
			try {
				blob.free();
			}
			catch (SQLException ex) {
				logger.warn("Could not free BLOB", ex);
			}
		}
		for (Clob clob : this.temporaryClobs) {
			try {
				clob.free();
			}
			catch (SQLException ex) {
				logger.warn("Could not free CLOB", ex);
			}
		}
	}

}
