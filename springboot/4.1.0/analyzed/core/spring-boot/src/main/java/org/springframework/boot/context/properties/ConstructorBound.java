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

package org.springframework.boot.context.properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * 用于以编程方式绑定采用构造器注入的配置属性的辅助类。
 *
 * @author Stephane Nicoll
 * @since 3.0.0
 * @see ConstructorBinding
 */
public abstract class ConstructorBound {

	/**
	 * 使用指定 {@link BeanFactory} 为给定 {@code beanName} 与 {@code beanType}
	 * 创建不可变的 {@link ConfigurationProperties} 实例。
	 *
	 * @param beanFactory 要使用的 Bean 工厂
	 * @param beanName Bean 名称
	 * @param beanType Bean 类型
	 * @return 指定 Bean 的实例
	 */
	public static Object from(BeanFactory beanFactory, String beanName, Class<?> beanType) {
		ConfigurationPropertiesBean bean = ConfigurationPropertiesBean.forValueObject(beanType, beanName);
		ConfigurationPropertiesBinder binder = ConfigurationPropertiesBinder.get(beanFactory);
		try {
			return binder.bindOrCreate(bean);
		}
		catch (Exception ex) {
			throw new ConfigurationPropertiesBindException(bean, ex);
		}
	}

}
