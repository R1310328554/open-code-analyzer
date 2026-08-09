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

package org.springframework.web.reactive.result.method.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Hints;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.HandlerResultHandlerSupport;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;

/* ===== [OCA 中文解析] =====
class AbstractMessageWriterResultHandler — 意图说明

class `AbstractMessageWriterResultHandler`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-webflux/src/main/java/org/springframework/web/reactive/result/method/annotation/AbstractMessageWriterResultHandler.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Abstract base class for result handlers that handle return values by writing
 * to the response with {@link HttpMessageWriter}.
 *
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @since 5.0
 */
public abstract class AbstractMessageWriterResultHandler extends HandlerResultHandlerSupport {

	// [OCA] 字段 `COROUTINES_FLOW_CLASS_NAME`：类成员状态。
	protected static final String COROUTINES_FLOW_CLASS_NAME = "kotlinx.coroutines.flow.Flow";

	private final List<HttpMessageWriter<?>> messageWriters;

	// [OCA] 字段 `errorResponseInterceptors`：类成员状态。
	private final List<ErrorResponse.Interceptor> errorResponseInterceptors = new ArrayList<>();

	// [OCA] 字段 `problemMediaTypes`：类成员状态。
	private final List<MediaType> problemMediaTypes =
			Arrays.asList(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_PROBLEM_XML);


	/**
	 * Constructor with {@link HttpMessageWriter HttpMessageWriters} and a
	 * {@code RequestedContentTypeResolver}.
	 * @param messageWriters for serializing Objects to the response body stream
	 * @param contentTypeResolver for resolving the requested content type
	 */
	protected AbstractMessageWriterResultHandler(List<HttpMessageWriter<?>> messageWriters,
			RequestedContentTypeResolver contentTypeResolver) {

		this(messageWriters, contentTypeResolver, ReactiveAdapterRegistry.getSharedInstance());
	}

	/**
	 * Constructor with an additional {@link ReactiveAdapterRegistry}.
	 * @param messageWriters for serializing Objects to the response body stream
	 * @param contentTypeResolver for resolving the requested content type
	 * @param adapterRegistry for adapting other reactive types (for example, rx.Observable,
	 * rx.Single, etc.) to Flux or Mono
	 */
	protected AbstractMessageWriterResultHandler(List<HttpMessageWriter<?>> messageWriters,
			RequestedContentTypeResolver contentTypeResolver, ReactiveAdapterRegistry adapterRegistry) {

		this(messageWriters, contentTypeResolver, adapterRegistry, Collections.emptyList());
	}

	/**
	 * Variant of
	 * {@link #AbstractMessageWriterResultHandler(List, RequestedContentTypeResolver, ReactiveAdapterRegistry)}
	 * with additional list of {@link ErrorResponse.Interceptor}s for return
	 * value handling.
	 * @since 6.2
	 */
	protected AbstractMessageWriterResultHandler(List<HttpMessageWriter<?>> messageWriters,
			RequestedContentTypeResolver contentTypeResolver, ReactiveAdapterRegistry adapterRegistry,
			List<ErrorResponse.Interceptor> interceptors) {

		super(contentTypeResolver, adapterRegistry);
		Assert.notEmpty(messageWriters, "At least one message writer is required");
		this.messageWriters = messageWriters;
		this.errorResponseInterceptors.addAll(interceptors);
	}


	/**
	 * Return the configured message converters.
	 */
	public List<HttpMessageWriter<?>> getMessageWriters() {
		return this.messageWriters;
	}

	/**
	 * Return the configured {@link ErrorResponse.Interceptor}'s.
	 * @since 6.2
	 */
	public List<ErrorResponse.Interceptor> getErrorResponseInterceptors() {
		return this.errorResponseInterceptors;
	}


	/**
	 * Invoke the configured {@link ErrorResponse.Interceptor}'s.
	 * @since 6.2
	 */
	protected void invokeErrorResponseInterceptors(ProblemDetail detail, @Nullable ErrorResponse errorResponse) {
		try {
			for (ErrorResponse.Interceptor handler : this.errorResponseInterceptors) {
				handler.handleError(detail, errorResponse);
			}
		}
		catch (Throwable ex) {
			// ignore
		}
	}

	/* ===== [OCA 中文解析] =====
方法 writeBody — 意图与阅读要点

方法 `writeBody` 复杂度较高（CCN≈27, NLOC≈76）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Write a given body to the response with {@link HttpMessageWriter}.
	 * @param body the object to write
	 * @param bodyParameter the {@link MethodParameter} of the body to write
	 * @param exchange the current exchange
	 * @return indicates completion or error
	 * @see #writeBody(Object, MethodParameter, MethodParameter, ServerWebExchange)
	 */
	protected Mono<Void> writeBody(@Nullable Object body, MethodParameter bodyParameter, ServerWebExchange exchange) {
		return this.writeBody(body, bodyParameter, null, exchange);
	}

	/**
	 * Write a given body to the response with {@link HttpMessageWriter}.
	 * @param body the object to write
	 * @param bodyParameter the {@link MethodParameter} of the body to write
	 * @param actualParam the actual return type of the method that returned the value;
	 * could be different from {@code bodyParameter} when processing {@code HttpEntity}
	 * for example
	 * @param exchange the current exchange
	 * @return indicates completion or error
	 * @since 5.0.2
	 */
	@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions", "NullAway"})
	/* ===== [OCA 中文解析] =====
方法 writeBody — 意图与阅读要点

方法 `writeBody` 复杂度较高（CCN≈27, NLOC≈76）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	protected Mono<Void> writeBody(@Nullable Object body, MethodParameter bodyParameter,
			@Nullable MethodParameter actualParam, ServerWebExchange exchange) {

		ResolvableType bodyType = ResolvableType.forMethodParameter(bodyParameter);
		ResolvableType actualType = (actualParam != null ? ResolvableType.forMethodParameter(actualParam) : bodyType);
		ReactiveAdapter adapter = getAdapterRegistry().getAdapter(bodyType.resolve(), body);

		Publisher<?> publisher;
		ResolvableType elementType;
		ResolvableType actualElementType;
		if (adapter != null) {
			publisher = adapter.toPublisher(body);
			boolean isUnwrapped = KotlinDetector.isSuspendingFunction(bodyParameter.getMethod()) &&
					!COROUTINES_FLOW_CLASS_NAME.equals(bodyType.toClass().getName()) &&
					!Flux.class.equals(bodyType.toClass());
			ResolvableType genericType = isUnwrapped ? bodyType : bodyType.getGeneric();
			elementType = getElementType(adapter, genericType);
			actualElementType = elementType;
		}
		else {
			publisher = Mono.justOrEmpty(body);
			ResolvableType bodyInstanceType = ResolvableType.forInstance(body);
			if (bodyType.toClass() == Object.class && body != null) {
				actualElementType = bodyInstanceType;
				elementType = bodyInstanceType;
			}
			else {
				actualElementType = (body == null || bodyInstanceType.hasUnresolvableGenerics()) ? bodyType : bodyInstanceType;
				elementType = bodyType;
			}
		}

		if (ClassUtils.isVoidType(elementType.resolve())) {
			return Mono.from((Publisher<Void>) publisher);
		}

		MediaType bestMediaType;
		try {
			bestMediaType = selectMediaType(exchange, () -> getMediaTypesFor(elementType));
		}
		catch (NotAcceptableStatusException ex) {
			HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
			if (statusCode != null && statusCode.isError()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring error response content (if any). " + ex.getReason());
				}
				return Mono.empty();
			}
			throw ex;
		}

		// For ProblemDetail, fall back on RFC 9457 format
		if (bestMediaType == null && ProblemDetail.class.isAssignableFrom(elementType.toClass())) {
			bestMediaType = selectMediaType(exchange, () -> getMediaTypesFor(elementType), this.problemMediaTypes);
		}

		if (bestMediaType != null) {
			String logPrefix = exchange.getLogPrefix();
			if (logger.isDebugEnabled()) {
				logger.debug(logPrefix +
						(publisher instanceof Mono ? "0..1" : "0..N") + " [" + elementType + "]");
			}
			for (HttpMessageWriter<?> writer : getMessageWriters()) {
				if (writer.canWrite(actualElementType, bestMediaType)) {
					return writer.write((Publisher) publisher, actualType, elementType,
							bestMediaType, exchange.getRequest(), exchange.getResponse(),
							Hints.from(Hints.LOG_PREFIX_HINT, logPrefix));
				}
			}
		}

		MediaType contentType = exchange.getResponse().getHeaders().getContentType();
		boolean isPresentMediaType = (contentType != null && contentType.equals(bestMediaType));
		Set<MediaType> producibleTypes = exchange.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		if (isPresentMediaType || !CollectionUtils.isEmpty(producibleTypes)) {
			return Mono.error(new HttpMessageNotWritableException(
					"No Encoder for [" + elementType + "] with preset Content-Type '" + contentType + "'"));
		}

		List<MediaType> mediaTypes = getMediaTypesFor(elementType);
		if (bestMediaType == null && mediaTypes.isEmpty()) {
			return Mono.error(new IllegalStateException("No HttpMessageWriter for " + elementType));
		}

		return Mono.error(new NotAcceptableStatusException(mediaTypes));
	}

	private ResolvableType getElementType(ReactiveAdapter adapter, ResolvableType genericType) {
		if (adapter.isNoValue()) {
			return ResolvableType.forClass(Void.class);
		}
		else if (genericType != ResolvableType.NONE) {
			return genericType;
		}
		else {
			return ResolvableType.forClass(Object.class);
		}
	}

	private List<MediaType> getMediaTypesFor(ResolvableType elementType) {
		List<MediaType> writableMediaTypes = new ArrayList<>();
		for (HttpMessageWriter<?> messageWriter : getMessageWriters()) {
			if (messageWriter.canWrite(elementType, null)) {
				writableMediaTypes.addAll(messageWriter.getWritableMediaTypes(elementType));
			}
		}
		return writableMediaTypes;
	}

}
