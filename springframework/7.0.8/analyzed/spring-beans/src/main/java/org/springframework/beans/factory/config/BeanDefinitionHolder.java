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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 持有 BeanDefinition 及其名称和别名的容器。
 * 可作为内部 bean 的占位符注册。
 *
 * <p>也可用于以编程方式注册内部 bean 定义。
 * 若不在意 BeanNameAware 等回调，直接注册 RootBeanDefinition 或 ChildBeanDefinition 即可。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public class BeanDefinitionHolder implements BeanMetadataElement {

	/** 包装的 BeanDefinition。 */
	private final BeanDefinition beanDefinition;

	/** bean 的主名称。 */
	private final String beanName;

	/** bean 的别名数组。 */
	private final String @Nullable [] aliases;


	/**
	 * 创建新的 BeanDefinitionHolder。
	 * @param beanDefinition 要包装的 BeanDefinition
	 * @param beanName bean 的名称，与 bean 定义中指定的一致
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	/**
	 * 创建新的 BeanDefinitionHolder。
	 * @param beanDefinition 要包装的 BeanDefinition
	 * @param beanName bean 的名称，与 bean 定义中指定的一致
	 * @param aliases bean 的别名，无则为 {@code null}
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String @Nullable [] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}

	/**
	 * 拷贝构造器：创建与给定 BeanDefinitionHolder 实例内容相同的新实例。
	 * <p>注意：包装的 BeanDefinition 引用原样取用，<i>不会</i>深拷贝。
	 * @param beanDefinitionHolder 要拷贝的 BeanDefinitionHolder
	 */
	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}


	/**
	 * 返回包装的 BeanDefinition。
	 */
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * 返回 bean 的主名称，与 bean 定义中指定的一致。
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * 返回 bean 的别名，与 bean 定义中直接指定的一致。
	 * @return 别名数组，无则为 {@code null}
	 */
	public String @Nullable [] getAliases() {
		return this.aliases;
	}

	/**
	 * 暴露 bean 定义的源对象。
	 * @see BeanDefinition#getSource()
	 */
	@Override
	public @Nullable Object getSource() {
		return this.beanDefinition.getSource();
	}

	/**
	 * 判断给定候选名称是否与存储的 bean 名称或别名匹配。
	 */
	public boolean matchesName(@Nullable String candidateName) {
		return (candidateName != null && (candidateName.equals(this.beanName) ||
				candidateName.equals(BeanFactoryUtils.transformedBeanName(this.beanName)) ||
				ObjectUtils.containsElement(this.aliases, candidateName)));
	}


	/**
	 * 返回 bean 的友好简短描述，包含名称和别名。
	 * @see #getBeanName()
	 * @see #getAliases()
	 */
	public String getShortDescription() {
		if (this.aliases == null) {
			return "Bean definition with name '" + this.beanName + "'";
		}
		return "Bean definition with name '" + this.beanName + "' and aliases [" + StringUtils.arrayToCommaDelimitedString(this.aliases) + ']';
	}

	/**
	 * 返回 bean 的详细描述，包含名称、别名以及所含 {@link BeanDefinition} 的描述。
	 * @see #getShortDescription()
	 * @see #getBeanDefinition()
	 */
	public String getLongDescription() {
		return getShortDescription() + ": " + this.beanDefinition;
	}

	/**
	 * 默认返回详细描述。子类可覆盖以返回简短描述或自定义描述。
	 * @see #getLongDescription()
	 * @see #getShortDescription()
	 */
	@Override
	public String toString() {
		return getLongDescription();
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BeanDefinitionHolder that &&
				this.beanDefinition.equals(that.beanDefinition) &&
				this.beanName.equals(that.beanName) &&
				ObjectUtils.nullSafeEquals(this.aliases, that.aliases)));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.beanDefinition, this.beanName, this.aliases);
	}

}
