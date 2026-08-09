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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.AbstractResource;
import org.springframework.util.Assert;

/**
 * 对 {@link org.springframework.beans.factory.config.BeanDefinition} 进行描述的
 * {@link org.springframework.core.io.Resource} 包装器。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see org.springframework.core.io.DescriptiveResource
 */
class BeanDefinitionResource extends AbstractResource {

	/** 被包装的 Bean 定义对象。 */
	private final BeanDefinition beanDefinition;


	/**
	 * 创建新的 BeanDefinitionResource。
	 * @param beanDefinition 要包装的 BeanDefinition 对象
	 */
	public BeanDefinitionResource(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinition = beanDefinition;
	}

	/**
	 * 返回被包装的 BeanDefinition 对象。
	 */
	public final BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}


	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new FileNotFoundException(
				"Resource cannot be opened because it points to " + getDescription());
	}

	@Override
	public String getDescription() {
		return "BeanDefinition defined in " + this.beanDefinition.getResourceDescription();
	}


	/**
	 * 本实现比较底层 BeanDefinition 是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BeanDefinitionResource that &&
				this.beanDefinition.equals(that.beanDefinition)));
	}

	/**
	 * 本实现返回底层 BeanDefinition 的哈希码。
	 */
	@Override
	public int hashCode() {
		return this.beanDefinition.hashCode();
	}

}
