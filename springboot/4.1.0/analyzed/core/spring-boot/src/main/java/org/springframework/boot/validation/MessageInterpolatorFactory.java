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

package org.springframework.boot.validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.ClassUtils;

/**
 * 用于创建 {@link MessageInterpolator} 的 {@link ObjectFactory}。
 * 根据类路径选择最合适的 {@link MessageInterpolator}，失败时尝试 Hibernate Validator 回退实现。
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
public class MessageInterpolatorFactory implements ObjectFactory<MessageInterpolator> {

	private static final Set<String> FALLBACKS;

	static {
		Set<String> fallbacks = new LinkedHashSet<>();
		fallbacks.add("org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator");
		FALLBACKS = Collections.unmodifiableSet(fallbacks);
	}

	private final @Nullable MessageSource messageSource;

	public MessageInterpolatorFactory() {
		this(null);
	}

	/**
	 * 创建新的 {@link MessageInterpolatorFactory}，
	 * 生成的 {@link MessageInterpolator} 会在最终插值前通过 {@code messageSource} 解析消息参数。
	 *
	 * @param messageSource message source to be used by the interpolator 插值器使用的消息源
	 * @since 2.6.0
	 */
	public MessageInterpolatorFactory(@Nullable MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public MessageInterpolator getObject() throws BeansException {
		MessageInterpolator messageInterpolator = getMessageInterpolator();
		if (this.messageSource != null) {
			return new MessageSourceMessageInterpolator(this.messageSource, messageInterpolator);
		}
		return messageInterpolator;
	}

	private MessageInterpolator getMessageInterpolator() {
		try {
			return Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();
		}
		catch (ValidationException ex) {
			MessageInterpolator fallback = getFallback();
			if (fallback != null) {
				return fallback;
			}
			throw ex;
		}
	}

	private @Nullable MessageInterpolator getFallback() {
		for (String fallback : FALLBACKS) {
			try {
				return getFallback(fallback);
			}
			catch (Exception ex) {
				// 吞掉异常并继续
			}
		}
		return null;
	}

	private MessageInterpolator getFallback(String fallback) {
		Class<?> interpolatorClass = ClassUtils.resolveClassName(fallback, null);
		Object interpolator = BeanUtils.instantiateClass(interpolatorClass);
		return (MessageInterpolator) interpolator;
	}

}
