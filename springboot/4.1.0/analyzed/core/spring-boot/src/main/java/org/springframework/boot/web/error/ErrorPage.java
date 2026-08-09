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

package org.springframework.boot.web.error;

import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

/**
 * 与服务器无关的错误页面简单抽象，大致等价于 web.xml 中传统的
 * {@literal &lt;error-page&gt;} 元素。
 *
 * @author Dave Syer
 * @since 4.0.0
 */
public class ErrorPage {

	private final @Nullable HttpStatus status;

	private final @Nullable Class<? extends Throwable> exception;

	private final String path;

	public ErrorPage(String path) {
		this.status = null;
		this.exception = null;
		this.path = path;
	}

	public ErrorPage(HttpStatus status, String path) {
		this.status = status;
		this.exception = null;
		this.path = path;
	}

	public ErrorPage(Class<? extends Throwable> exception, String path) {
		this.status = null;
		this.exception = exception;
		this.path = path;
	}

	/**
	 * 用于渲染的路径（通常实现为 forward），以 "/" 开头。
	 * 可使用自定义控制器或 Servlet 路径，若服务器支持也可使用模板路径（例如 "/error.jsp"）。
	 *
	 * @return the path that will be rendered for this error 此错误将渲染的路径
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * 返回异常类型（按状态码匹配的页面则返回 {@code null}）。
	 *
	 * @return the exception type or {@code null} 异常类型或 {@code null}
	 */
	public @Nullable Class<? extends Throwable> getException() {
		return this.exception;
	}

	/**
	 * 此错误页面匹配的 HTTP 状态值（按异常匹配的页面则返回 {@code null}）。
	 *
	 * @return the status or {@code null} 状态或 {@code null}
	 */
	public @Nullable HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * 此错误页面匹配的 HTTP 状态值。
	 *
	 * @return the status value (or 0 for a page that matches any status) 状态值（匹配任意状态的页面返回 0）
	 */
	public int getStatusCode() {
		return (this.status != null) ? this.status.value() : 0;
	}

	/**
	 * 异常类型名称。
	 *
	 * @return the exception type name (or {@code null} if there is none) 异常类型名称（若无则为 {@code null}）
	 */
	public @Nullable String getExceptionName() {
		return (this.exception != null) ? this.exception.getName() : null;
	}

	/**
	 * 返回此错误页面是否为全局页面（匹配所有未匹配的状态码与异常类型）。
	 *
	 * @return if this is a global error page 是否为全局错误页面
	 */
	public boolean isGlobal() {
		return (this.status == null && this.exception == null);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof ErrorPage other) {
			return ObjectUtils.nullSafeEquals(getExceptionName(), other.getExceptionName())
					&& ObjectUtils.nullSafeEquals(this.path, other.path) && this.status == other.status;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ObjectUtils.nullSafeHashCode(getExceptionName());
		result = prime * result + ObjectUtils.nullSafeHashCode(this.path);
		result = prime * result + getStatusCode();
		return result;
	}

}
