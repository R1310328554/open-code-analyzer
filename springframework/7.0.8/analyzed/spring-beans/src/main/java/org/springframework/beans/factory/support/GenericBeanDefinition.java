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

package org.springframework.beans.factory.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ObjectUtils;

/**
 * GenericBeanDefinition 是声明式 Bean 定义的一站式方案。
 * 与所有常见 Bean 定义一样，可指定类以及可选的构造器参数值和属性值。
 * 此外，可通过 {@code parentName} 属性灵活配置从父 Bean 定义继承。
 *
 * <p>通常使用本 {@code GenericBeanDefinition} 类注册声明式 Bean 定义
 * （例如 Bean 后处理器可能操作的 XML 定义，甚至可重新配置父名称）。
 * 在父子关系已预先确定时使用 {@code RootBeanDefinition}/{@code ChildBeanDefinition}，
 * 对于由工厂方法/供应商派生的编程式定义，优先使用 {@link RootBeanDefinition}。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #setParentName
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class
GenericBeanDefinition extends AbstractBeanDefinition {

	/** 父 Bean 的名称。 */
	private @Nullable String parentName;


	/**
	 * 创建新的 GenericBeanDefinition，通过 Bean 属性和配置方法进行配置。
	 * @see #setBeanClass
	 * @see #setScope
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public GenericBeanDefinition() {
		super();
	}

	/**
	 * 将给定 Bean 定义深拷贝为新的 GenericBeanDefinition。
	 * @param original 要拷贝的原始 Bean 定义
	 */
	public GenericBeanDefinition(BeanDefinition original) {
		super(original);
	}


	@Override
	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Override
	public @Nullable String getParentName() {
		return this.parentName;
	}


	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new GenericBeanDefinition(this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof GenericBeanDefinition that &&
				ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other)));
	}

	@Override
	public String toString() {
		if (this.parentName != null) {
			return "Generic bean with parent '" + this.parentName + "': " + super.toString();
		}
		return "Generic bean: " + super.toString();
	}

}
