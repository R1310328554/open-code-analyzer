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

package org.springframework.beans.factory.config;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

/**
 * 单个自动装配属性值的简单标记类，用于添加到
 * {@link BeanDefinition#getPropertyValues()} 中，标识某个 bean 属性需要自动装配。
 *
 * <p>运行时，该标记会被替换为对应 bean 属性写方法的 {@link DependencyDescriptor}，
 * 最终通过 {@link AutowireCapableBeanFactory#resolveDependency} 步骤完成解析。
 *
 * @author Juergen Hoeller
 * @since 5.2
 * @see AutowireCapableBeanFactory#resolveDependency
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#addAutowiredProperty
 */
@SuppressWarnings("serial")
public final class AutowiredPropertyMarker implements Serializable {

	/**
	 * 自动装配标记值的标准单例实例。
	 */
	public static final Object INSTANCE = new AutowiredPropertyMarker();


	private AutowiredPropertyMarker() {
	}

	/**
	 * 反序列化时返回标准单例实例。
	 */
	private Object readResolve() {
		return INSTANCE;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other);
	}

	@Override
	public int hashCode() {
		return AutowiredPropertyMarker.class.hashCode();
	}

	@Override
	public String toString() {
		return "(autowired)";
	}

}
