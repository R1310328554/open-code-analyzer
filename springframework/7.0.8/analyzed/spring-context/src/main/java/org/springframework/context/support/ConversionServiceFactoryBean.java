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

package org.springframework.context.support;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * 提供便捷访问已配置 ConversionService 的工厂，该服务预置了适用于大多数环境的转换器。
 * 可通过 {@link #setConverters "converters"} 属性补充默认转换器。
 *
 * <p>本实现创建 {@link DefaultConversionService}。
 * 子类可覆盖 {@link #createConversionService()} 以返回自选的
 * {@link GenericConversionService} 实例。
 *
 * <p>与所有 {@code FactoryBean} 实现一样，本类适用于使用 Spring {@code <beans>}
 * XML 配置应用上下文。若使用
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * 类配置容器，只需在 {@link org.springframework.context.annotation.Bean @Bean}
 * 方法中实例化、配置并返回相应的 {@code ConversionService} 对象即可。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 */
public class ConversionServiceFactoryBean implements FactoryBean<ConversionService>, InitializingBean {

	private @Nullable Set<?> converters;

	private @Nullable GenericConversionService conversionService;


	/**
	 * 配置要添加的自定义转换器对象集合：实现
	 * {@link org.springframework.core.convert.converter.Converter}、
	 * {@link org.springframework.core.convert.converter.ConverterFactory} 或
	 * {@link org.springframework.core.convert.converter.GenericConverter} 之一。
	 */
	public void setConverters(Set<?> converters) {
		this.converters = converters;
	}

	@Override
	public void afterPropertiesSet() {
		this.conversionService = createConversionService();
		ConversionServiceFactory.registerConverters(this.converters, this.conversionService);
	}

	/**
	 * 创建本工厂 Bean 返回的 ConversionService 实例。
	 * <p>默认创建简单的 {@link GenericConversionService} 实例。
	 * 子类可覆盖以自定义创建的 ConversionService 实例。
	 */
	protected GenericConversionService createConversionService() {
		return new DefaultConversionService();
	}


	// implementing FactoryBean

	@Override
	public @Nullable ConversionService getObject() {
		return this.conversionService;
	}

	@Override
	public Class<? extends ConversionService> getObjectType() {
		return GenericConversionService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
