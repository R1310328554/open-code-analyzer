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

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * {@link Errors} 与 {@link BindingResult} 接口的默认实现，
 * 用于在 JavaBean 对象上注册并评估绑定错误。
 *
 * <p>执行标准 JavaBean 属性访问，并支持嵌套属性。
 * 通常应用代码使用 {@code Errors} 或 {@code BindingResult} 接口。
 * {@link DataBinder} 通过 {@link DataBinder#getBindingResult()} 返回其 BindingResult。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder#getBindingResult()
 * @see DataBinder#initBeanPropertyAccess()
 * @see DirectFieldBindingResult
 */
@SuppressWarnings("serial")
public class BeanPropertyBindingResult extends AbstractPropertyBindingResult implements Serializable {

	private final @Nullable Object target;

	private final boolean autoGrowNestedPaths;

	private final int autoGrowCollectionLimit;

	private transient @Nullable BeanWrapper beanWrapper;


	/**
	 * 为给定目标创建新的 {@code BeanPropertyBindingResult}。
	 * @param target 要绑定到的目标 bean
	 * @param objectName 目标对象名称
	 */
	public BeanPropertyBindingResult(@Nullable Object target, String objectName) {
		this(target, objectName, true, Integer.MAX_VALUE);
	}

	/**
	 * 为给定目标创建新的 {@code BeanPropertyBindingResult}。
	 * @param target 要绑定到的目标 bean
	 * @param objectName 目标对象名称
	 * @param autoGrowNestedPaths 是否对含 null 值的嵌套路径进行“自动扩展”
	 * @param autoGrowCollectionLimit 数组与集合自动扩展的上限
	 */
	public BeanPropertyBindingResult(@Nullable Object target, String objectName,
			boolean autoGrowNestedPaths, int autoGrowCollectionLimit) {

		super(objectName);
		this.target = target;
		this.autoGrowNestedPaths = autoGrowNestedPaths;
		this.autoGrowCollectionLimit = autoGrowCollectionLimit;
	}


	@Override
	public final @Nullable Object getTarget() {
		return this.target;
	}

	/**
	 * 返回本实例使用的 {@link BeanWrapper}。
	 * 若此前不存在则创建新实例。
	 * @see #createBeanWrapper()
	 */
	@Override
	public final ConfigurablePropertyAccessor getPropertyAccessor() {
		if (this.beanWrapper == null) {
			this.beanWrapper = createBeanWrapper();
			this.beanWrapper.setExtractOldValueForEditor(true);
			this.beanWrapper.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
			this.beanWrapper.setAutoGrowCollectionLimit(this.autoGrowCollectionLimit);
		}
		return this.beanWrapper;
	}

	/**
	 * 为底层目标对象创建新的 {@link BeanWrapper}。
	 * @see #getTarget()
	 */
	protected BeanWrapper createBeanWrapper() {
		if (this.target == null) {
			throw new IllegalStateException("Cannot access properties on null bean instance '" + getObjectName() + "'");
		}
		return PropertyAccessorFactory.forBeanPropertyAccess(this.target);
	}

}
