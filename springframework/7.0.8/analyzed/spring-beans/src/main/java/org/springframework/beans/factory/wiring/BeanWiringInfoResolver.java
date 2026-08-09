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

package org.springframework.beans.factory.wiring;

import org.jspecify.annotations.Nullable;

/**
 * 策略接口：根据新实例化的 Bean 对象解析 Bean 名称/装配元数据。
 * 相关具体切面中的 AspectJ 切点会驱动对本接口 {@link #resolveWiringInfo} 的调用。
 *
 * <p>元数据解析策略可插拔。良好的默认实现是 {@link ClassNameBeanWiringInfoResolver}，
 * 以全限定类名作为 Bean 名。
 *
 * @author Rod Johnson
 * @since 2.0
 * @see BeanWiringInfo
 * @see ClassNameBeanWiringInfoResolver
 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
 */
public interface BeanWiringInfoResolver {

	/**
	 * 为给定 Bean 实例解析 {@link BeanWiringInfo}。
	 * @param beanInstance 待解析的 Bean 实例
	 * @return BeanWiringInfo，未找到时返回 {@code null}
	 */
	@Nullable BeanWiringInfo resolveWiringInfo(Object beanInstance);

}
