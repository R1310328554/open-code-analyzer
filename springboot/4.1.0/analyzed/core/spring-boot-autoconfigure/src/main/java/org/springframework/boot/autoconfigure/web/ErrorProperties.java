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

package org.springframework.boot.autoconfigure.web;

import org.springframework.beans.factory.annotation.Value;

/**
 * Web 错误处理的配置属性。
 *
 * @author Michael Stummvoll
 * @author Stephane Nicoll
 * @author Vedran Pavic
 * @author Scott Frederick
 * @since 1.3.0
 */
public class ErrorProperties {

	/**
	 * 错误控制器的路径。
	 */
	@Value("${error.path:/error}")
	private String path = "/error";

	/**
	 * 是否包含 {@code exception} 属性。
	 */
	private boolean includeException;

	/**
	 * 何时包含 {@code trace} 属性。
	 */
	private IncludeAttribute includeStacktrace = IncludeAttribute.NEVER;

	/**
	 * 何时包含 {@code message} 属性。
	 */
	private IncludeAttribute includeMessage = IncludeAttribute.NEVER;

	/**
	 * 何时包含 {@code errors} 属性。
	 */
	private IncludeAttribute includeBindingErrors = IncludeAttribute.NEVER;

	/**
	 * 何时包含 {@code path} 属性。
	 */
	private IncludeAttribute includePath = IncludeAttribute.ALWAYS;

	private final Whitelabel whitelabel = new Whitelabel();

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isIncludeException() {
		return this.includeException;
	}

	public void setIncludeException(boolean includeException) {
		this.includeException = includeException;
	}

	public IncludeAttribute getIncludeStacktrace() {
		return this.includeStacktrace;
	}

	public void setIncludeStacktrace(IncludeAttribute includeStacktrace) {
		this.includeStacktrace = includeStacktrace;
	}

	public IncludeAttribute getIncludeMessage() {
		return this.includeMessage;
	}

	public void setIncludeMessage(IncludeAttribute includeMessage) {
		this.includeMessage = includeMessage;
	}

	public IncludeAttribute getIncludeBindingErrors() {
		return this.includeBindingErrors;
	}

	public void setIncludeBindingErrors(IncludeAttribute includeBindingErrors) {
		this.includeBindingErrors = includeBindingErrors;
	}

	public IncludeAttribute getIncludePath() {
		return this.includePath;
	}

	public void setIncludePath(IncludeAttribute includePath) {
		this.includePath = includePath;
	}

	public Whitelabel getWhitelabel() {
		return this.whitelabel;
	}

	/**
	 * 错误属性包含选项。
	 */
	public enum IncludeAttribute {

		/**
		 * 从不添加错误属性。
		 */
		NEVER,

		/**
		 * 始终添加错误属性。
		 */
		ALWAYS,

		/**
		 * 当相应请求参数不为 {@code false} 时添加错误属性。
		 */
		ON_PARAM

	}

	public static class Whitelabel {

		/**
		 * 是否在发生服务器错误时在浏览器中启用默认错误页。
		 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

}
