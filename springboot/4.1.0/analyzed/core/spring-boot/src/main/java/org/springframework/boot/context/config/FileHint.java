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

package org.springframework.boot.context.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * 用户提供的文件属性提示，如扩展名或编码。
 *
 * @author Phillip Webb
 * @author Moritz Halbritter
 */
final class FileHint {

	private static final FileHint NONE = new FileHint(Collections.emptyMap());

	private final Map<String, String> attributes;

	private FileHint(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * 若存在提示则返回 {@code true}。
	 *
	 * @return 是否存在提示
	 */
	boolean isPresent() {
		return getExtension() != null || getEncoding() != null;
	}

	/**
	 * 返回扩展名。
	 *
	 * @return 扩展名
	 */
	@Nullable String getExtension() {
		String extension = this.attributes.get("extension");
		if (extension == null) {
			return null;
		}
		if (!extension.startsWith(".")) {
			return null;
		}
		return extension;
	}

	/**
	 * 返回提示中的扩展名；若不存在则返回 {@code fallback}。
	 *
	 * @param fallback 回退扩展名
	 * @return 提示或回退扩展名
	 */
	String getExtensionOrElse(String fallback) {
		String extension = getExtension();
		return (extension != null) ? extension : fallback;
	}

	/**
	 * 返回编码。
	 *
	 * @return 编码
	 */
	@Nullable String getEncoding() {
		return this.attributes.get("encoding");
	}

	/**
	 * 以 {@link Charset} 形式返回编码。
	 *
	 * @return 编码对应的 {@link Charset}
	 */
	@Nullable Charset getEncodingAsCharset() {
		String encoding = getEncoding();
		return (encoding != null) ? Charset.forName(encoding) : null;
	}

	@Override
	public String toString() {
		return "FileHint{attributes=" + this.attributes + '}';
	}

	/**
	 * 从给定值解析 {@link FileHint}。
	 *
	 * @param value 源值
	 * @return {@link FileHint}
	 */
	static FileHint from(String value) {
		if (!hasBrackets(value)) {
			return NONE;
		}
		List<String> betweenBrackets = findBetweenBrackets(value);
		Map<String, String> attributes = new HashMap<>();
		for (String entry : betweenBrackets) {
			String cleaned = entry.trim();
			if (cleaned.isEmpty()) {
				continue;
			}
			int equals = cleaned.indexOf('=');
			if (equals == -1) {
				attributes.put("extension", cleaned);
			}
			else {
				attributes.put(cleaned.substring(0, equals).trim(), cleaned.substring(equals + 1).trim());
			}
		}
		return new FileHint(Collections.unmodifiableMap(attributes));
	}

	private static List<String> findBetweenBrackets(String value) {
		List<String> collected = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int brackets = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '[') {
				if (brackets > 0) {
					buffer.append(c);
				}
				brackets++;
			}
			else if (c == ']') {
				brackets--;
				if (brackets > 0) {
					buffer.append(c);
				}
				else if (brackets == 0 && !buffer.isEmpty()) {
					collected.add(buffer.toString());
					buffer.setLength(0);
				}
			}
			else if (brackets > 0) {
				buffer.append(c);
			}
		}
		return collected;
	}

	/**
	 * 从给定值中移除所有提示。
	 *
	 * @param value 源值
	 * @return 不含提示的值
	 */
	static String removeFrom(String value) {
		if (!hasBrackets(value)) {
			return value;
		}
		StringBuilder result = new StringBuilder();
		int brackets = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '[') {
				brackets++;
				continue;
			}
			if (c == ']') {
				brackets--;
				continue;
			}
			if (brackets <= 0) {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static boolean hasBrackets(String value) {
		return value.indexOf('[') != -1 && value.indexOf(']') != -1;
	}

}
