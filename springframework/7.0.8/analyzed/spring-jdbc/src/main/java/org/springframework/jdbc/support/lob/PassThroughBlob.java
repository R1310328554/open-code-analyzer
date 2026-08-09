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
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * 简单的 JDBC {@link Blob} 适配器，公开给定的字节数组或二进制流。可选地由 {@link DefaultLobHandler} 使用。
 * @author Juergen Hoeller
 * @since 2.5.3
 */
@Deprecated(since = "6.2")
class PassThroughBlob implements Blob {

	/** `content`：该类的成员状态。 */
	private byte @Nullable [] content;

	/** `binaryStream`：该类的成员状态。 */
	private @Nullable InputStream binaryStream;

	/** `contentLength`：该类的成员状态。 */
	private final long contentLength;


	/**
	 * 创建 `PassThroughBlob` 的新实例。
	 */
	public PassThroughBlob(byte[] content) {
		this.content = content;
		this.contentLength = content.length;
	}

	/**
	 * 创建 `PassThroughBlob` 的新实例。
	 */
	public PassThroughBlob(InputStream binaryStream, long contentLength) {
		this.binaryStream = binaryStream;
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
	 * 获取 Binary Stream（`BinaryStream`）。
	 */
	@Override
	public InputStream getBinaryStream() throws SQLException {
		if (this.content != null) {
			return new ByteArrayInputStream(this.content);
		}
		else {
			return (this.binaryStream != null ? this.binaryStream : InputStream.nullInputStream());
		}
	}


	/**
	 * 获取 Binary Stream（`BinaryStream`）。
	 */
	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 Binary Stream（`BinaryStream`）。
	 */
	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取 Bytes（`Bytes`）。
	 */
	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 Bytes（`Bytes`）。
	 */
	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置 Bytes（`Bytes`）。
	 */
	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `position`：完成本类中与「position」相关的职责。
	 */
	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `position`：完成本类中与「position」相关的职责。
	 */
	@Override
	public long position(Blob pattern, long start) throws SQLException {
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
