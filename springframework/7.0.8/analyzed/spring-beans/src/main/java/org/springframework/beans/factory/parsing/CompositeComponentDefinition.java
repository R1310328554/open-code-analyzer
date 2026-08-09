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

package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 持有若干嵌套 {@link ComponentDefinition} 实例的 {@link ComponentDefinition} 实现，
 * 将它们聚合为命名的组件组。
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see #getNestedComponents()
 */
public class CompositeComponentDefinition extends AbstractComponentDefinition {

	/** 复合组件名称。 */
	private final String name;

	/** 定义复合组件根的来源元素。 */
	private final @Nullable Object source;

	/** 嵌套的组件定义列表。 */
	private final List<ComponentDefinition> nestedComponents = new ArrayList<>();


	/**
	 * 创建新的 {@link CompositeComponentDefinition}。
	 * @param name 复合组件的名称
	 * @param source 定义复合组件根的来源元素
	 */
	public CompositeComponentDefinition(String name, @Nullable Object source) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.source = source;
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}


	/**
	 * 将给定组件作为嵌套元素添加到本复合组件。
	 * @param component 要添加的嵌套组件
	 */
	public void addNestedComponent(ComponentDefinition component) {
		Assert.notNull(component, "ComponentDefinition must not be null");
		this.nestedComponents.add(component);
	}

	/**
	 * 返回本复合组件持有的嵌套组件。
	 * @return 嵌套组件数组，若无则返回空数组
	 */
	public ComponentDefinition[] getNestedComponents() {
		return this.nestedComponents.toArray(new ComponentDefinition[0]);
	}

}
