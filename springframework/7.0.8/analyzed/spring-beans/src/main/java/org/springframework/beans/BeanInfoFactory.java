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

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

import org.jspecify.annotations.Nullable;

/**
 * 为 Spring bean 创建 {@link BeanInfo} 的策略接口。
 * 可插入自定义的 bean 属性解析策略（例如面向 JVM 上其他语言），
 * 或采用更高效的 {@link BeanInfo} 获取算法。
 *
 * <p>各 BeanInfoFactory 由 {@link CachedIntrospectionResults} 通过
 * {@link org.springframework.core.io.support.SpringFactoriesLoader}
 * 工具类实例化。
 *
 * <p>需要创建 {@link BeanInfo} 时，{@code CachedIntrospectionResults}
 * 会遍历已发现的工厂，依次调用 {@link #getBeanInfo(Class)}。
 * 若返回 {@code null}，则继续询问下一个工厂。
 * 若所有工厂都不支持该类，则回退为创建标准 {@link BeanInfo}。
 *
 * <p>注意：{@link org.springframework.core.io.support.SpringFactoriesLoader}
 * 会按 {@link org.springframework.core.annotation.Order @Order}
 * 对 {@code BeanInfoFactory} 实例排序，优先级更高的排在前面。
 *
 * @author Arjen Poutsma
 * @since 3.2
 * @see CachedIntrospectionResults
 * @see org.springframework.core.io.support.SpringFactoriesLoader
 */
public interface BeanInfoFactory {

	/**
	 * 在支持的前提下，返回给定类的 BeanInfo。
	 * @param beanClass bean 的 Class
	 * @return BeanInfo；若该类不受支持则返回 {@code null}
	 * @throws IntrospectionException 内省过程中出现异常时抛出
	 */
	@Nullable BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;

}
