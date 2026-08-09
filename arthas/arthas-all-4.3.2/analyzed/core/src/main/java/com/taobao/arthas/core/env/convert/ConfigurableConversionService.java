/*
 * Copyright 2002-2011 the original author or authors.
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

package com.taobao.arthas.core.env.convert;

import com.taobao.arthas.core.env.ConversionService;

/**
 * 可配置的 {@link ConversionService} 标记接口。
 * <p>
 * 合并 {@link ConversionService} 的只读转换能力与 {@link ConverterRegistry} 的
 * 注册/移除 {@link org.springframework.core.convert.converter.Converter} 能力，
 * 便于在启动阶段向 {@link ConfigurableEnvironment} 动态添加类型转换器。
 *
 * @author Chris Beams
 * @since 3.1
 * @see com.taobao.arthas.core.env.springframework.core.env.ConfigurablePropertyResolver#getConversionService()
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
public interface ConfigurableConversionService extends ConversionService {

}
