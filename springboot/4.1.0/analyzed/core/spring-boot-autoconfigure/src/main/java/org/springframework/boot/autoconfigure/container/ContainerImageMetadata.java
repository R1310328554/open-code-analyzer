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

package org.springframework.boot.autoconfigure.container;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AttributeAccessor;

/**
 * 可附加到 {@link AttributeAccessor} 的容器镜像元数据。
 * <p>
 * 主要用于标记 Testcontainers 或 Docker Compose 支持场景下创建的
 * {@link BeanDefinition BeanDefinitions}。
 *
 * @param imageName 容器镜像名称；若尚未确定则为 {@code null}
 * @author Phillip Webb
 * @since 3.4.0
 */
public record ContainerImageMetadata(@Nullable String imageName) {

	static final String NAME = ContainerImageMetadata.class.getName();

	/**
	 * 将此容器镜像元数据添加到给定属性访问器。
	 *
	 * @param attributes 要添加元数据的属性访问器
	 */
	public void addTo(@Nullable AttributeAccessor attributes) {
		if (attributes != null) {
			attributes.setAttribute(NAME, this);
		}
	}

	/**
	 * 若给定属性访问器中已添加 {@link ContainerImageMetadata} 则返回 {@code true}。
	 *
	 * @param attributes 要检查的属性访问器
	 * @return 元数据是否存在
	 */
	public static boolean isPresent(@Nullable AttributeAccessor attributes) {
		return getFrom(attributes) != null;
	}

	/**
	 * 从给定属性访问器获取 {@link ContainerImageMetadata}；
	 * 若未添加元数据则返回 {@code null}。
	 *
	 * @param attributes 属性访问器
	 * @return 元数据或 {@code null}
	 */
	public static @Nullable ContainerImageMetadata getFrom(@Nullable AttributeAccessor attributes) {
		return (attributes != null) ? (ContainerImageMetadata) attributes.getAttribute(NAME) : null;
	}

}
