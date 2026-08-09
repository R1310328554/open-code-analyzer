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

package org.springframework.boot.web.servlet.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.error.ErrorPageRegistry;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 为非嵌入式应用（即部署的 WAR 包）提供 {@link ErrorPageRegistry} 的 Servlet {@link Filter}。
 * 它注册错误页，并通过过滤请求并转发到错误页来处理应用错误，而非交由服务器处理。
 * 错误页是 Servlet 规范的功能，但规范中并无注册错误页的 Java API。
 * 本过滤器通过接受 Spring Boot {@link ErrorPageRegistrar} 的错误页注册来绕过此限制
 * （上下文中所有该类型的 Bean 都会应用到此服务器）。
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class ErrorPageFilter implements Filter, ErrorPageRegistry, Ordered {

	private static final Log logger = LogFactory.getLog(ErrorPageFilter.class);

	// 来自 RequestDispatcher，但不直接引用以保持与 Servlet 2.5 的兼容性

	private static final String ERROR_EXCEPTION = "jakarta.servlet.error.exception";

	private static final String ERROR_EXCEPTION_TYPE = "jakarta.servlet.error.exception_type";

	private static final String ERROR_MESSAGE = "jakarta.servlet.error.message";

	/**
	 * 包含请求 URI 的 Servlet 属性名。
	 */
	public static final String ERROR_REQUEST_URI = "jakarta.servlet.error.request_uri";

	private static final String ERROR_STATUS_CODE = "jakarta.servlet.error.status_code";

	private static final Set<Class<?>> CLIENT_ABORT_EXCEPTIONS;
	static {
		Set<Class<?>> clientAbortExceptions = new HashSet<>();
		addClassIfPresent(clientAbortExceptions, "org.apache.catalina.connector.ClientAbortException");
		CLIENT_ABORT_EXCEPTIONS = Collections.unmodifiableSet(clientAbortExceptions);
	}

	private @Nullable String global;

	private final Map<Integer, String> statuses = new HashMap<>();

	private final Map<@Nullable Class<?>, String> exceptions = new HashMap<>();

	private final OncePerRequestFilter delegate = new OncePerRequestFilter() {

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws ServletException, IOException {
			ErrorPageFilter.this.doFilter(request, response, chain);
		}

		@Override
		protected boolean shouldNotFilterAsyncDispatch() {
			return false;
		}

	};

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.delegate.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		this.delegate.doFilter(request, response, chain);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ErrorWrapperResponse wrapped = new ErrorWrapperResponse(response);
		try {
			chain.doFilter(request, wrapped);
			if (wrapped.hasErrorToSend()) {
				handleErrorStatus(request, response, wrapped.getStatus(), wrapped.getMessage());
				response.flushBuffer();
			}
			else if (!request.isAsyncStarted() && !response.isCommitted()) {
				response.flushBuffer();
			}
		}
		catch (Throwable ex) {
			Throwable exceptionToHandle = ex;
			if (ex instanceof ServletException servletException) {
				Throwable rootCause = servletException.getRootCause();
				if (rootCause != null) {
					exceptionToHandle = rootCause;
				}
			}
			handleException(request, response, wrapped, exceptionToHandle);
			response.flushBuffer();
		}
	}

	private void handleErrorStatus(HttpServletRequest request, HttpServletResponse response, int status,
			@Nullable String message) throws ServletException, IOException {
		if (response.isCommitted()) {
			handleCommittedResponse(request, null);
			return;
		}
		String errorPath = getErrorPath(this.statuses, status);
		if (errorPath == null) {
			response.sendError(status, message);
			return;
		}
		response.setStatus(status);
		setErrorAttributes(request, status, message);
		request.getRequestDispatcher(errorPath).forward(request, response);
	}

	private void handleException(HttpServletRequest request, HttpServletResponse response, ErrorWrapperResponse wrapped,
			Throwable ex) throws IOException, ServletException {
		Class<?> type = ex.getClass();
		String errorPath = getErrorPath(type);
		if (errorPath == null) {
			rethrow(ex);
			return;
		}
		if (response.isCommitted()) {
			handleCommittedResponse(request, ex);
			return;
		}
		forwardToErrorPage(errorPath, request, wrapped, ex);
	}

	private void forwardToErrorPage(String path, HttpServletRequest request, HttpServletResponse response, Throwable ex)
			throws ServletException, IOException {
		if (logger.isErrorEnabled()) {
			String message = "Forwarding to error page from request " + getDescription(request) + " due to exception ["
					+ ex.getMessage() + "]";
			logger.error(message, ex);
		}
		setErrorAttributes(request, 500, ex.getMessage());
		request.setAttribute(ERROR_EXCEPTION, ex);
		request.setAttribute(ERROR_EXCEPTION_TYPE, ex.getClass());
		response.reset();
		response.setStatus(500);
		request.getRequestDispatcher(path).forward(request, response);
		request.removeAttribute(ERROR_EXCEPTION);
		request.removeAttribute(ERROR_EXCEPTION_TYPE);
	}

	/**
	 * 返回给定请求的描述。默认基于请求的 {@code servletPath} 与 {@code pathInfo} 生成描述。
	 *
	 * @param request the source request 源请求
	 * @return the description 描述信息
	 */
	protected String getDescription(HttpServletRequest request) {
		String pathInfo = (request.getPathInfo() != null) ? request.getPathInfo() : "";
		return "[" + request.getServletPath() + pathInfo + "]";
	}

	private void handleCommittedResponse(HttpServletRequest request, @Nullable Throwable ex) {
		if (isClientAbortException(ex)) {
			return;
		}
		String message = "Cannot forward to error page for request " + getDescription(request)
				+ " as the response has already been"
				+ " committed. As a result, the response may have the wrong status"
				+ " code. If your application is running on WebSphere Application"
				+ " Server you may be able to resolve this problem by setting"
				+ " com.ibm.ws.webcontainer.invokeFlushAfterService to false";
		if (ex == null) {
			logger.error(message);
		}
		else {
			// 用户可能看到缺少部分数据的错误页，但抛出异常也无济于事（为稳妥起见仍记录日志）
			logger.error(message, ex);
		}
	}

	private boolean isClientAbortException(@Nullable Throwable ex) {
		if (ex == null) {
			return false;
		}
		for (Class<?> candidate : CLIENT_ABORT_EXCEPTIONS) {
			if (candidate.isInstance(ex)) {
				return true;
			}
		}
		return isClientAbortException(ex.getCause());
	}

	private @Nullable String getErrorPath(Map<Integer, String> map, Integer status) {
		if (map.containsKey(status)) {
			return map.get(status);
		}
		return this.global;
	}

	private @Nullable String getErrorPath(Class<?> type) {
		while (type != Object.class) {
			String path = this.exceptions.get(type);
			if (path != null) {
				return path;
			}
			type = type.getSuperclass();
		}
		return this.global;
	}

	private void setErrorAttributes(HttpServletRequest request, int status, @Nullable String message) {
		request.setAttribute(ERROR_STATUS_CODE, status);
		request.setAttribute(ERROR_MESSAGE, message);
		request.setAttribute(ERROR_REQUEST_URI, request.getRequestURI());
	}

	private void rethrow(Throwable ex) throws IOException, ServletException {
		if (ex instanceof RuntimeException runtimeException) {
			throw runtimeException;
		}
		if (ex instanceof Error error) {
			throw error;
		}
		if (ex instanceof IOException ioException) {
			throw ioException;
		}
		if (ex instanceof ServletException servletException) {
			throw servletException;
		}
		throw new IllegalStateException(ex);
	}

	@Override
	public void addErrorPages(ErrorPage... errorPages) {
		for (ErrorPage errorPage : errorPages) {
			if (errorPage.isGlobal()) {
				this.global = errorPage.getPath();
			}
			else if (errorPage.getStatus() != null) {
				this.statuses.put(errorPage.getStatus().value(), errorPage.getPath());
			}
			else {
				this.exceptions.put(errorPage.getException(), errorPage.getPath());
			}
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	private static void addClassIfPresent(Collection<Class<?>> collection, String className) {
		try {
			collection.add(ClassUtils.forName(className, null));
		}
		catch (Throwable ex) {
			// 忽略
		}
	}

	private static class ErrorWrapperResponse extends HttpServletResponseWrapper {

		private int status;

		private @Nullable String message;

		private boolean hasErrorToSend;

		ErrorWrapperResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int status) {
			sendError(status, null);
		}

		@Override
		public void sendError(int status, @Nullable String message) {
			this.status = status;
			this.message = message;
			this.hasErrorToSend = true;
			// 不调用 super，因为容器可能阻止我们自行处理错误
		}

		@Override
		public int getStatus() {
			if (this.hasErrorToSend) {
				return this.status;
			}
			// 若无错误则需信任被包装的响应
			return super.getStatus();
		}

		@Override
		public void flushBuffer() throws IOException {
			sendErrorIfNecessary();
			super.flushBuffer();
		}

		private void sendErrorIfNecessary() throws IOException {
			if (this.hasErrorToSend && !isCommitted()) {
				((HttpServletResponse) getResponse()).sendError(this.status, this.message);
			}
		}

		@Nullable String getMessage() {
			return this.message;
		}

		boolean hasErrorToSend() {
			return this.hasErrorToSend;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			sendErrorIfNecessary();
			return super.getWriter();
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			sendErrorIfNecessary();
			return super.getOutputStream();
		}

	}

}
