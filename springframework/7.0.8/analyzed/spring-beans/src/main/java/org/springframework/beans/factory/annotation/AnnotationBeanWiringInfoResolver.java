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

package org.springframework.beans.factory.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.beans.factory.wiring.BeanWiringInfoResolver} 实现：
 * 通过 {@link Configurable} 注解识别需要自动装配的类。
 * <p>查找用的 Bean 名称取自 {@code @Configurable}（若已指定）；
 * 否则默认为被配置类的全限定名。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see Configurable
 * @see org.springframework.beans.factory.wiring.ClassNameBeanWiringInfoResolver
 */
public class AnnotationBeanWiringInfoResolver implements BeanWiringInfoResolver {

	@Override
	public @Nullable BeanWiringInfo resolveWiringInfo(Object beanInstance) {
		Assert.notNull(beanInstance, "Bean instance must not be null");
		Configurable annotation = beanInstance.getClass().getAnnotation(Configurable.class);
		return (annotation != null ? buildWiringInfo(beanInstance, annotation) : null);
	}

	/**
	 * 根据给定的 {@link Configurable} 注解构建 {@link BeanWiringInfo}。
	 * @param beanInstance Bean 实例
	 * @param annotation Bean 类上找到的 {@code Configurable} 注解
	 * @return 解析得到的 {@code BeanWiringInfo}
	 */
	protected BeanWiringInfo buildWiringInfo(Object beanInstance, Configurable annotation) {
		if (!Autowire.NO.equals(annotation.autowire())) {
			// 按名称或按类型自动装配
			return new BeanWiringInfo(annotation.autowire().value(), annotation.dependencyCheck());
		}
		else if (!annotation.value().isEmpty()) {
			// 显式指定了作为属性值模板的 Bean 定义名称
			return new BeanWiringInfo(annotation.value(), false);
		}
		else {
			// 使用默认 Bean 名称作为属性值模板
			return new BeanWiringInfo(getDefaultBeanName(beanInstance), true);
		}
	}

	/**
	 * 为指定 Bean 实例确定默认 Bean 名称。
	 * <p>默认实现：若为 CGLIB 代理则返回超类名，否则返回普通 Bean 类名。
	 * @param beanInstance 要为其生成默认名称的 Bean 实例
	 * @return 要使用的默认 Bean 名称
	 * @see org.springframework.util.ClassUtils#getUserClass(Class)
	 */
	protected String getDefaultBeanName(Object beanInstance) {
		return ClassUtils.getUserClass(beanInstance).getName();
	}

}
