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
import java.lang.reflect.Method;

import org.springframework.core.Ordered;

/**
 * {@link StandardBeanInfoFactory} 的扩展：通过 Spring（包可见）的
 * {@code ExtendedBeanInfo} 实现对“非标准”JavaBeans setter 方法的内省支持。
 *
 * <p>通过 {@code META-INF/spring.factories} 配置，内容如下：
 *
 * <p>{@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.ExtendedBeanInfoFactory}
 *
 * <p>顺序为 {@link Ordered#LOWEST_PRECEDENCE}，以便用户自定义的
 * {@link BeanInfoFactory} 可以优先生效。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.2
 * @see StandardBeanInfoFactory
 * @see CachedIntrospectionResults
 */
public class ExtendedBeanInfoFactory extends StandardBeanInfoFactory {

	/**
	 * 获取 bean 类的 BeanInfo；若存在非标准写方法则包装为 ExtendedBeanInfo。
	 */
	@Override
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = super.getBeanInfo(beanClass);
		return (supports(beanClass) ? new ExtendedBeanInfo(beanInfo) : beanInfo);
	}

	/**
	 * 判断给定 bean 类是否声明或继承了任何返回非 void 的
	 * bean 属性 / 索引属性 setter 方法。
	 */
	private boolean supports(Class<?> beanClass) {
		for (Method method : beanClass.getMethods()) {
			if (ExtendedBeanInfo.isCandidateWriteMethod(method)) {
				return true;
			}
		}
		return false;
	}

}
