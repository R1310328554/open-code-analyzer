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

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.util.StringValueResolver;

/**
 * 提供便捷访问已配置常用类型（如数字、日期、时间）转换器与格式化器的
 * {@link FormattingConversionService} 的工厂。
 *
 * <p>可通过 {@link #setConverters(Set)} 与 {@link #setFormatters(Set)}
 * 以声明方式注册额外的转换器与格式化器。另一种方式是通过实现
 * {@link FormatterRegistrar} 接口在代码中注册；随后可通过
 * {@link #setFormatterRegistrars(Set)} 提供要使用的注册器集合。
 *
 * <p>与所有 {@code FactoryBean} 实现一样，本类适用于使用 Spring
 * {@code <beans>} XML 配置文件配置 Spring 应用上下文的场景。
 * 使用 {@link org.springframework.context.annotation.Configuration @Configuration}
 * 类配置容器时，只需在
 * {@link org.springframework.context.annotation.Bean @Bean} 方法中
 * 实例化、配置并返回相应的 {@code FormattingConversionService} 对象即可。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @author Chris Beams
 * @since 3.0
 */
public class FormattingConversionServiceFactoryBean
		implements FactoryBean<FormattingConversionService>, EmbeddedValueResolverAware, InitializingBean {

	private @Nullable Set<?> converters;

	private @Nullable Set<?> formatters;

	private @Nullable Set<FormatterRegistrar> formatterRegistrars;

	private boolean registerDefaultFormatters = true;

	private @Nullable StringValueResolver embeddedValueResolver;

	private @Nullable FormattingConversionService conversionService;


	/**
	 * 配置要添加的自定义转换器对象集合。
	 * @param converters 以下任意类型的实例：
	 * {@link org.springframework.core.convert.converter.Converter}、
	 * {@link org.springframework.core.convert.converter.ConverterFactory}、
	 * {@link org.springframework.core.convert.converter.GenericConverter}
	 */
	public void setConverters(Set<?> converters) {
		this.converters = converters;
	}

	/**
	 * 配置要添加的自定义格式化器对象集合。
	 * @param formatters {@link Formatter} 或 {@link AnnotationFormatterFactory} 的实例
	 */
	public void setFormatters(Set<?> formatters) {
		this.formatters = formatters;
	}

	/**
	 * <p>配置要调用的 FormatterRegistrar 集合，用于在通过
	 * {@link #setConverters(Set)} 与 {@link #setFormatters(Set)}
	 * 以声明方式添加的转换器与格式化器之外，额外注册转换器与格式化器。
	 * <p>FormatterRegistrar 适用于为某一格式化类别（如日期格式化）
	 * 注册多个相关转换器与格式化器；支持该类别所需的全部类型
	 * 可在一处统一注册。
	 * <p>FormatterRegistrar 也可用于注册索引在与其自身 &lt;T&gt; 不同的
	 * 特定字段类型下的 Formatter，或从 Printer/Parser 对注册 Formatter。
	 * @see FormatterRegistry#addFormatterForFieldType(Class, Formatter)
	 * @see FormatterRegistry#addFormatterForFieldType(Class, Printer, Parser)
	 */
	public void setFormatterRegistrars(Set<FormatterRegistrar> formatterRegistrars) {
		this.formatterRegistrars = formatterRegistrars;
	}

	/**
	 * 指示是否应注册默认格式化器。
	 * <p>默认会注册内置格式化器。此标志可用于关闭该行为，
	 * 仅依赖显式注册的格式化器。
	 * @see #setFormatters(Set)
	 * @see #setFormatterRegistrars(Set)
	 */
	public void setRegisterDefaultFormatters(boolean registerDefaultFormatters) {
		this.registerDefaultFormatters = registerDefaultFormatters;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver embeddedValueResolver) {
		this.embeddedValueResolver = embeddedValueResolver;
	}


	@Override
	public void afterPropertiesSet() {
		this.conversionService = new DefaultFormattingConversionService(this.embeddedValueResolver, this.registerDefaultFormatters);
		ConversionServiceFactory.registerConverters(this.converters, this.conversionService);
		registerFormatters(this.conversionService);
	}

	private void registerFormatters(FormattingConversionService conversionService) {
		if (this.formatters != null) {
			for (Object candidate : this.formatters) {
				if (candidate instanceof Formatter<?> formatter) {
					conversionService.addFormatter(formatter);
				}
				else if (candidate instanceof AnnotationFormatterFactory<?> factory) {
					conversionService.addFormatterForFieldAnnotation(factory);
				}
				else {
					throw new IllegalArgumentException(
							"Custom formatters must be implementations of Formatter or AnnotationFormatterFactory");
				}
			}
		}
		if (this.formatterRegistrars != null) {
			for (FormatterRegistrar registrar : this.formatterRegistrars) {
				registrar.registerFormatters(conversionService);
			}
		}
	}


	@Override
	public @Nullable FormattingConversionService getObject() {
		return this.conversionService;
	}

	@Override
	public Class<? extends FormattingConversionService> getObjectType() {
		return FormattingConversionService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
