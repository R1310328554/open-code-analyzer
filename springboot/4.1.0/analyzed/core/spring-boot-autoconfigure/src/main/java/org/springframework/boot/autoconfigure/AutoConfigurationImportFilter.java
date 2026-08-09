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

package org.springframework.boot.autoconfigure;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * 可在 {@code spring.factories} 中注册的过滤器，用于在读取字节码之前
 * 快速剔除不需要考虑的自动配置类。
 * <p>
 * {@link AutoConfigurationImportFilter} 可实现以下任意
 * {@link org.springframework.beans.factory.Aware Aware} 接口，
 * 相应方法会在调用 {@link #match} 之前被调用：
 * <ul>
 * <li>{@link EnvironmentAware}</li>
 * <li>{@link BeanFactoryAware}</li>
 * <li>{@link BeanClassLoaderAware}</li>
 * <li>{@link ResourceLoaderAware}</li>
 * </ul>
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
@FunctionalInterface
public interface AutoConfigurationImportFilter {

	/**
	 * 对给定的自动配置类候选集应用过滤。
	 * @param autoConfigurationClasses 正在考虑的自动配置类，数组中可能包含 {@code null} 元素；
	 * 实现不应修改此数组中的值
	 * @param autoConfigurationMetadata 访问自动配置注解处理器生成的元数据
	 * @return 布尔数组，指示哪些自动配置类应被导入；返回数组大小必须与
	 * {@code autoConfigurationClasses} 相同，值为 {@code false} 的项不会被导入
	 */
	boolean[] match(@Nullable String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata);

}
