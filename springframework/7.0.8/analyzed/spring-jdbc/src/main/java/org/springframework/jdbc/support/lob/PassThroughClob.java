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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.util.FileCopyUtils;

/**
 * 公开给定字符串或字符流的简单 JDBC {@link Clob} 适配器。可选地由 {@link DefaultLobHandler} 使用。
 * @author Juergen Hoeller
 * @since 2.5.3
 */
@Deprecated(since = "6.2")
class PassThroughClob implements Clob {

	/** `content`：该类的成员状态。 */
	private @Nullable String content;

	/** `characterStream`：该类的成员状态。 */
	private @Nullable Reader characterStream;

	/** `asciiStream`：该类的成员状态。 */
	private @Nullable InputStream asciiStream;

	/** `contentLength`：该类的成员状态。 */
	private final long contentLength;


	/**
	 * 创建 `PassThroughClob` 的新实例。
	 */
	public PassThroughClob(String content) {
		this.content = content;
		this.contentLength = content.length();
	}

	/**
	 * 创建 `PassThroughClob` 的新实例。
	 */
	public PassThroughClob(Reader characterStream, long contentLength) {
		this.characterStream = characterStream;
		this.contentLength = contentLength;
	}

	/**
	 * 创建 `PassThroughClob` 的新实例。
	 */
	public PassThroughClob(InputStream asciiStream, long contentLength) {
		this.asciiStream = asciiStream;
		this.contentLength = contentLength;
	}


	/**
	 * 方法 `length`：完成本类中与「length」相关的职责。
	 */
	@Override
	public long length() throws SQLException {
		return this.contentLength;
	}

	/**
	 * 获取 Character Stream（`CharacterStream`）。
	 */
	@Override
	public Reader getCharacterStream() throws SQLException {
		if (this.content != null) {
			return new StringReader(this.content);
		}
		else if (this.characterStream != null) {
			return this.characterStream;
		}
		else {
			return new InputStreamReader(
					(this.asciiStream != null ? this.asciiStream : InputStream.nullInputStream()),
					StandardCharsets.US_ASCII);
		}
	}

	/**
	 * 获取 Ascii Stream（`AsciiStream`）。
	 */
	@Override
	public InputStream getAsciiStream() throws SQLException {
		try {
			if (this.content != null) {
				return new ByteArrayInputStream(this.content.getBytes(StandardCharsets.US_ASCII));
			}
			else if (this.characterStream != null) {
				String tempContent = FileCopyUtils.copyToString(this.characterStream);
				return new ByteArrayInputStream(tempContent.getBytes(StandardCharsets.US_ASCII));
			}
			else {
				return (this.asciiStream != null ? this.asciiStream : InputStream.nullInputStream());
			}
		}
		catch (IOException ex) {
			throw new SQLException("Failed to read stream content: " + ex);
		}
	}


	/**
	 * 获取 Character Stream（`CharacterStream`）。
	 */
	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 Character Stream（`CharacterStream`）。
	 */
	@Override
	public Writer setCharacterStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 Ascii Stream（`AsciiStream`）。
	 */
	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取 Sub String（`SubString`）。
	 */
	@Override
	public String getSubString(long pos, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 String（`String`）。
	 */
	@Override
	public int setString(long pos, String str) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 String（`String`）。
	 */
	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `position`：完成本类中与「position」相关的职责。
	 */
	@Override
	public long position(String searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `position`：完成本类中与「position」相关的职责。
	 */
	@Override
	public long position(Clob searchstr, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `truncate`：完成本类中与「truncate」相关的职责。
	 */
	@Override
	public void truncate(long len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `free`：完成本类中与「free」相关的职责。
	 */
	@Override
	public void free() throws SQLException {
		// no-op
	}

}
