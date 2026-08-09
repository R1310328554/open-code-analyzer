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

package org.springframework.format.support;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.number.money.CurrencyUnitFormatter;
import org.springframework.format.number.money.Jsr354NumberFormatAnnotationFormatterFactory;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;

/**
 * {@link FormattingConversionService} 的特化实现，默认配置了
 * 适用于大多数应用的转换器与格式化器。
 *
 * <p>设计为可直接实例化，同时也公开静态 {@link #addDefaultFormatters}
 * 工具方法，可针对任意 {@code FormatterRegistry} 实例临时使用，
 * 正如 {@code DefaultConversionService} 公开其自身的
 * {@link DefaultConversionService#addDefaultConverters addDefaultConverters} 方法一样。
 *
 * <p>根据类路径上是否存在对应 API，自动注册 JSR-354 货币与金额
 * 以及 JSR-310 日期时间的格式化器。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public class DefaultFormattingConversionService extends FormattingConversionService {

	private static final boolean JSR_354_PRESENT;

	static {
		ClassLoader classLoader = DefaultFormattingConversionService.class.getClassLoader();
		JSR_354_PRESENT = ClassUtils.isPresent("javax.money.MonetaryAmount", classLoader);
	}

	/**
	 * 创建新的 {@code DefaultFormattingConversionService}，包含
	 * {@linkplain DefaultConversionService#addDefaultConverters 默认转换器}与
	 * {@linkplain #addDefaultFormatters 默认格式化器}。
	 */
	public DefaultFormattingConversionService() {
		this(null, true);
	}

	/**
	 * 创建新的 {@code DefaultFormattingConversionService}，包含
	 * {@linkplain DefaultConversionService#addDefaultConverters 默认转换器}，
	 * 并根据 {@code registerDefaultFormatters} 的值决定是否注册
	 * {@linkplain #addDefaultFormatters 默认格式化器}。
	 * @param registerDefaultFormatters 是否注册默认格式化器
	 */
	public DefaultFormattingConversionService(boolean registerDefaultFormatters) {
		this(null, registerDefaultFormatters);
	}

	/**
	 * 创建新的 {@code DefaultFormattingConversionService}，包含
	 * {@linkplain DefaultConversionService#addDefaultConverters 默认转换器}，
	 * 并根据 {@code registerDefaultFormatters} 的值决定是否注册
	 * {@linkplain #addDefaultFormatters 默认格式化器}。
	 * @param embeddedValueResolver 委托给 {@link #setEmbeddedValueResolver(StringValueResolver)}，
	 * 在调用 {@link #addDefaultFormatters} 之前执行。
	 * @param registerDefaultFormatters 是否注册默认格式化器
	 */
	public DefaultFormattingConversionService(
			@Nullable StringValueResolver embeddedValueResolver, boolean registerDefaultFormatters) {

		if (embeddedValueResolver != null) {
			setEmbeddedValueResolver(embeddedValueResolver);
		}
		DefaultConversionService.addDefaultConverters(this);
		if (registerDefaultFormatters) {
			addDefaultFormatters(this);
		}
	}


	/**
	 * 添加适用于大多数环境的格式化器：包括数字格式化器、
	 * JSR-354 货币与金额格式化器，以及 JSR-310 日期时间格式化器，
	 * 具体取决于类路径上是否存在对应 API。
	 * @param formatterRegistry 要注册默认格式化器的服务
	 */
	public static void addDefaultFormatters(FormatterRegistry formatterRegistry) {
		// 数值的默认处理
		formatterRegistry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

		// 货币金额的默认处理
		if (JSR_354_PRESENT) {
			formatterRegistry.addFormatter(new CurrencyUnitFormatter());
			formatterRegistry.addFormatter(new MonetaryAmountFormatter());
			formatterRegistry.addFormatterForFieldAnnotation(new Jsr354NumberFormatAnnotationFormatterFactory());
		}

		// 日期时间值的默认处理

		// 仅处理 JSR-310 特有的日期与时间类型
		new DateTimeFormatterRegistrar().registerFormatters(formatterRegistry);

		// 基于常规 DateFormat 的 Date、Calendar、Long 转换器
		new DateFormatterRegistrar().registerFormatters(formatterRegistry);
	}

}
