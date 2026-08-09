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

/**
 * 用于获取 {@link PropertyAccessor} 实例的简易工厂门面，
 * 尤其便于获取 {@link BeanWrapper}。
 * 对外隐藏了具体实现类及其扩展的公开签名。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public final class PropertyAccessorFactory {

	/**
	 * 私有构造函数，防止实例化。
	 */
	private PropertyAccessorFactory() {
	}


	/**
	 * 为给定目标对象获取一个 {@link BeanWrapper}，
	 * 以 JavaBeans 方式访问属性。
	 * @param target 要包装的目标对象
	 * @return 属性访问器
	 * @see BeanWrapperImpl
	 */
	public static BeanWrapper forBeanPropertyAccess(Object target) {
		return new BeanWrapperImpl(target);
	}

	/**
	 * 为给定目标对象获取一个 {@link PropertyAccessor}，
	 * 以直接字段访问方式读写属性。
	 * @param target 要包装的目标对象
	 * @return 属性访问器
	 * @see DirectFieldAccessor
	 */
	public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
		return new DirectFieldAccessor(target);
	}

}
