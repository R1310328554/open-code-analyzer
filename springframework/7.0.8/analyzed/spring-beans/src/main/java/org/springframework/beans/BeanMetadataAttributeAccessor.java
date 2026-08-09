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

import org.springframework.core.AttributeAccessorSupport;

/**
 * {@link org.springframework.core.AttributeAccessorSupport} 的扩展：
 * 将属性以 {@link BeanMetadataAttribute} 形式保存，以便追踪定义来源。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

	/** 该元数据元素的配置来源（具体类型取决于所用配置机制） */
	private @Nullable Object source;


	/**
	 * 设置该元数据元素的配置来源对象。
	 * <p>对象的具体类型取决于实际使用的配置机制。
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


	/**
	 * 将给定的 BeanMetadataAttribute 注册到本访问器的属性集合中。
	 * @param attribute 要注册的 BeanMetadataAttribute 对象
	 */
	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}

	/**
	 * 在本访问器的属性集合中查找指定的 BeanMetadataAttribute。
	 * @param name 属性名称
	 * @return 对应的 BeanMetadataAttribute；若不存在则返回 {@code null}
	 */
	public @Nullable BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}

	/**
	 * 设置属性：将值包装为 BeanMetadataAttribute 后再存入父类属性集合。
	 */
	@Override
	public void setAttribute(String name, @Nullable Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}

	/**
	 * 获取属性值：取出包装后的 BeanMetadataAttribute，再返回其内部 value。
	 */
	@Override
	public @Nullable Object getAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

	/**
	 * 移除属性：删除包装后的 BeanMetadataAttribute，并返回其内部 value。
	 */
	@Override
	public @Nullable Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
