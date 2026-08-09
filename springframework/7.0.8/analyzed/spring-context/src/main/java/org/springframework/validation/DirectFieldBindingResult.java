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

package org.springframework.validation;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Errors 与 BindingResult 接口的特殊实现，
 * 支持在值对象上注册并评估绑定错误。
 * 直接访问字段，而非通过 JavaBean getter。
 *
 * <p>自 Spring 4.1 起，本实现可遍历嵌套字段。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder#getBindingResult()
 * @see DataBinder#initDirectFieldAccess()
 * @see BeanPropertyBindingResult
 */
@SuppressWarnings("serial")
public class DirectFieldBindingResult extends AbstractPropertyBindingResult {

	private final @Nullable Object target;

	private final boolean autoGrowNestedPaths;

	private transient @Nullable ConfigurablePropertyAccessor directFieldAccessor;


	/**
	 * 为给定目标创建新的 {@code DirectFieldBindingResult}。
	 * @param target 要绑定到的目标对象
	 * @param objectName 目标对象名称
	 */
	public DirectFieldBindingResult(@Nullable Object target, String objectName) {
		this(target, objectName, true);
	}

	/**
	 * 为给定目标创建新的 {@code DirectFieldBindingResult}。
	 * @param target 要绑定到的目标对象
	 * @param objectName 目标对象名称
	 * @param autoGrowNestedPaths 是否对含 null 值的嵌套路径进行“自动扩展”
	 */
	public DirectFieldBindingResult(@Nullable Object target, String objectName, boolean autoGrowNestedPaths) {
		super(objectName);
		this.target = target;
		this.autoGrowNestedPaths = autoGrowNestedPaths;
	}


	@Override
	public final @Nullable Object getTarget() {
		return this.target;
	}

	/**
	 * 返回本实例使用的 DirectFieldAccessor。
	 * 若此前不存在则创建新实例。
	 * @see #createDirectFieldAccessor()
	 */
	@Override
	public final ConfigurablePropertyAccessor getPropertyAccessor() {
		if (this.directFieldAccessor == null) {
			this.directFieldAccessor = createDirectFieldAccessor();
			this.directFieldAccessor.setExtractOldValueForEditor(true);
			this.directFieldAccessor.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
		}
		return this.directFieldAccessor;
	}

	/**
	 * 为底层目标对象创建新的 DirectFieldAccessor。
	 * @see #getTarget()
	 */
	protected ConfigurablePropertyAccessor createDirectFieldAccessor() {
		if (this.target == null) {
			throw new IllegalStateException("Cannot access fields on null target instance '" + getObjectName() + "'");
		}
		return PropertyAccessorFactory.forDirectFieldAccess(this.target);
	}

}
