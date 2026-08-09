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

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Bean 定义中键值对形式属性的持有者。
 *
 * <p>除键值对本身外，还会记录该定义的来源信息。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class BeanMetadataAttribute implements BeanMetadataElement {

	/** 属性名称 */
	private final String name;

	/** 属性值（可能尚未完成类型转换） */
	private final @Nullable Object value;

	/** 配置来源对象 */
	private @Nullable Object source;


	/**
	 * 创建一个新的属性值实例。
	 * @param name 属性名称（不得为 {@code null}）
	 * @param value 属性值（可能尚未完成类型转换）
	 */
	public BeanMetadataAttribute(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}


	/**
	 * 返回属性名称。
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回属性值。
	 */
	public @Nullable Object getValue() {
		return this.value;
	}

	/**
	 * 设置该元数据元素的配置来源 {@code Object}。
	 * <p>对象的具体类型取决于所用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	/**
	 * 返回该元数据元素的配置来源。
	 */
	@Override
	public @Nullable Object getSource() {
		return this.source;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other ||(other instanceof BeanMetadataAttribute that &&
				this.name.equals(that.name) &&
				ObjectUtils.nullSafeEquals(this.value, that.value) &&
				ObjectUtils.nullSafeEquals(this.source, that.source)));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.name, this.value);
	}

	@Override
	public String toString() {
		return "metadata attribute: name='" + this.name + "'; value=" + this.value;
	}

}
