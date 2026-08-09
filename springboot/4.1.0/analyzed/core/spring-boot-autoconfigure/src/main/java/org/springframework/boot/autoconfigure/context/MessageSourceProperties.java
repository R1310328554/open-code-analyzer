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

package org.springframework.boot.autoconfigure.context;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.core.io.Resource;

/**
 * 消息源（Message Source）的配置属性。
 *
 * @author Stephane Nicoll
 * @author Kedar Joshi
 * @author Misagh Moayyed
 * @since 2.0.0
 */
@ConfigurationProperties("spring.messages")
public class MessageSourceProperties {

	/**
	 * 基名列表（本质上是完全限定的类路径位置），
	 * 遵循 ResourceBundle 约定，并放宽对斜杠分隔位置的支持。
	 * 若不包含包限定符（如 {@code org.mypackage}），则从类路径根解析。
	 */
	private List<String> basename = new ArrayList<>(List.of("messages"));

	/**
	 * 包含通用消息、与区域设置无关的属性文件资源列表。
	 */
	private @Nullable List<Resource> commonMessages;

	/**
	 * 消息资源包的字符编码。
	 */
	private Charset encoding = StandardCharsets.UTF_8;

	/**
	 * 已加载资源包文件的缓存时长。未设置时永久缓存。
	 * 若未指定时长后缀，默认以秒为单位。
	 */
	@DurationUnit(ChronoUnit.SECONDS)
	private @Nullable Duration cacheDuration;

	/**
	 * 未找到特定区域设置的文件时，是否回退到系统 {@code Locale}。
	 * 关闭后，唯一回退为默认文件（如基名为 {@code messages} 时的 {@code messages.properties}）。
	 */
	private boolean fallbackToSystemLocale = true;

	/**
	 * 是否始终应用 MessageFormat 规则，即使消息不含参数也进行解析。
	 */
	private boolean alwaysUseMessageFormat;

	/**
	 * 是否将消息代码作为默认消息，而非抛出 {@code NoSuchMessageException}。
	 * 建议仅在开发阶段启用。
	 */
	private boolean useCodeAsDefaultMessage;

	public List<String> getBasename() {
		return this.basename;
	}

	public void setBasename(List<String> basename) {
		this.basename = basename;
	}

	public Charset getEncoding() {
		return this.encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public @Nullable Duration getCacheDuration() {
		return this.cacheDuration;
	}

	public void setCacheDuration(@Nullable Duration cacheDuration) {
		this.cacheDuration = cacheDuration;
	}

	public boolean isFallbackToSystemLocale() {
		return this.fallbackToSystemLocale;
	}

	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	public boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}

	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}

	public boolean isUseCodeAsDefaultMessage() {
		return this.useCodeAsDefaultMessage;
	}

	public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
	}

	public @Nullable List<Resource> getCommonMessages() {
		return this.commonMessages;
	}

	public void setCommonMessages(@Nullable List<Resource> commonMessages) {
		this.commonMessages = commonMessages;
	}

}
