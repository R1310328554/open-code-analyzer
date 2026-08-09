/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;

/**
 * 可写出 JSON 内容的接口。
 * 提供写入 {@link Appendable}、字符串、字节数组及资源等多种输出方式。
 *
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 3.4.0
 * @see JsonWriter
 */
@FunctionalInterface
public interface WritableJson {

	/**
	 * 将 JSON 写入给定 {@link Appendable}。
	 *
	 * @param out 接收 JSON 的 {@link Appendable}
	 * @throws IOException on IO error IO 错误时
	 */
	void to(Appendable out) throws IOException;

	/**
	 * 将 JSON 写入 {@link String}。
	 *
	 * @return the JSON string JSON 字符串
	 */
	default String toJsonString() {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			to(stringBuilder);
			return stringBuilder.toString();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * 将 JSON 写入 UTF-8 编码字节数组。
	 *
	 * @return the JSON bytes JSON 字节
	 */
	default byte[] toByteArray() {
		return toByteArray(StandardCharsets.UTF_8);
	}

	/**
	 * 将 JSON 写入字节数组。
	 *
	 * @param charset 字符集
	 * @return the JSON bytes JSON 字节
	 */
	default byte[] toByteArray(Charset charset) {
		Assert.notNull(charset, "'charset' must not be null");
		try {
			AppendableByteArray appendable = AppendableByteArray.get(charset);
			to(appendable);
			return appendable.toByteArray();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * 使用 {@link StandardCharsets#UTF_8 UTF8} 编码将 JSON 写入给定 {@link WritableResource}。
	 *
	 * @param out 接收 JSON 的 {@link OutputStream}
	 * @throws IOException on IO error IO 错误时
	 */
	default void toResource(WritableResource out) throws IOException {
		Assert.notNull(out, "'out' must not be null");
		try (OutputStream outputStream = out.getOutputStream()) {
			toOutputStream(outputStream);
		}
	}

	/**
	 * 使用给定 {@link Charset} 将 JSON 写入 {@link WritableResource}。
	 *
	 * @param out 接收 JSON 的 {@link OutputStream}
	 * @param charset 要使用的字符集
	 * @throws IOException on IO error IO 错误时
	 */
	default void toResource(WritableResource out, Charset charset) throws IOException {
		Assert.notNull(out, "'out' must not be null");
		Assert.notNull(charset, "'charset' must not be null");
		try (OutputStream outputStream = out.getOutputStream()) {
			toOutputStream(outputStream, charset);
		}
	}

	/**
	 * 使用 {@link StandardCharsets#UTF_8 UTF8} 编码将 JSON 写入 {@link OutputStream}。
	 * 输出流不会被关闭。
	 *
	 * @param out 接收 JSON 的 {@link OutputStream}
	 * @throws IOException on IO error IO 错误时
	 * @see #toOutputStream(OutputStream, Charset)
	 */
	default void toOutputStream(OutputStream out) throws IOException {
		toOutputStream(out, StandardCharsets.UTF_8);
	}

	/**
	 * 使用给定 {@link Charset} 将 JSON 写入 {@link OutputStream}。
	 * 输出流不会被关闭。
	 *
	 * @param out 接收 JSON 的 {@link OutputStream}
	 * @param charset 要使用的字符集
	 * @throws IOException on IO error IO 错误时
	 */
	default void toOutputStream(OutputStream out, Charset charset) throws IOException {
		Assert.notNull(out, "'out' must not be null");
		Assert.notNull(charset, "'charset' must not be null");
		toWriter(new OutputStreamWriter(out, charset));
	}

	/**
	 * 将 JSON 写入给定 {@link Writer}。
	 * Writer 会被刷新但不会关闭。
	 *
	 * @param out 接收 JSON 的 {@link Writer}
	 * @throws IOException on IO error IO 错误时
	 * @see #toOutputStream(OutputStream, Charset)
	 */
	default void toWriter(Writer out) throws IOException {
		Assert.notNull(out, "'out' must not be null");
		to(out);
		out.flush();
	}

	/**
	 * 创建 {@link WritableJson} 的工厂方法，其 {@link Object#toString()} 委托给
	 * {@link WritableJson#toJsonString()}。
	 *
	 * @param writableJson 源 {@link WritableJson}
	 * @return a new {@link WritableJson} with a sensible {@link Object#toString()} 带合理 toString 的新实例
	 */
	static WritableJson of(WritableJson writableJson) {
		return new WritableJson() {

			@Override
			public void to(Appendable out) throws IOException {
				writableJson.to(out);
			}

			@Override
			public String toString() {
				return toJsonString();
			}

		};
	}

}
