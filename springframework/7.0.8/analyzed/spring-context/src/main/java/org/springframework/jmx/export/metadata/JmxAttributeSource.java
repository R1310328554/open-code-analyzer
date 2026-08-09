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

package org.springframework.jmx.export.metadata;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * 供 {@code MetadataMBeanInfoAssembler} 从受管资源类读取源码级元数据的接口。
 *
 * @author Rob Harrop
 * @author Jennifer Hickey
 * @since 1.2
 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler#setAttributeSource
 * @see org.springframework.jmx.export.MBeanExporter#setAssembler
 */
public interface JmxAttributeSource {

	/**
	 * 若给定 {@code Class} 具有相应元数据，实现应返回 {@link ManagedResource} 实例。
	 * @param clazz 读取资源数据的类
	 * @return 资源元数据，未找到时返回 {@code null}
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedResource getManagedResource(Class<?> clazz) throws InvalidMetadataException;

	/**
	 * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedAttribute} 实例。
	 * @param method 读取属性数据的方法
	 * @return 属性元数据，未找到时返回 {@code null}
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedAttribute getManagedAttribute(Method method) throws InvalidMetadataException;

	/**
	 * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedMetric} 实例。
	 * @param method 读取指标数据的方法
	 * @return 指标元数据，未找到时返回 {@code null}
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedMetric getManagedMetric(Method method) throws InvalidMetadataException;

	/**
	 * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedOperation} 实例。
	 * @param method 读取操作数据的方法
	 * @return 操作元数据，未找到时返回 {@code null}
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedOperation getManagedOperation(Method method) throws InvalidMetadataException;

	/**
	 * 若给定 {@code Method} 具有相应元数据，实现应返回 {@link ManagedOperationParameter
	 * ManagedOperationParameters} 数组。
	 * @param method 读取元数据的 {@code Method}
	 * @return 参数信息，未找到元数据时返回空数组
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedOperationParameter[] getManagedOperationParameters(Method method) throws InvalidMetadataException;

	/**
	 * 若给定 {@code Class} 具有相应元数据，实现应返回 {@link ManagedNotification ManagedNotifications} 数组。
	 * @param clazz 读取元数据的 {@code Class}
	 * @return 通知信息，未找到元数据时返回空数组
	 * @throws InvalidMetadataException 元数据无效时
	 */
	@Nullable ManagedNotification[] getManagedNotifications(Class<?> clazz) throws InvalidMetadataException;

}
