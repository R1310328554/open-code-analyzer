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

import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;

/**
 * 标记类，表示由 Spring 管理的 {@link Properties} 实例，
 * 并支持父子 Bean 定义之间的合并。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class ManagedProperties extends Properties implements Mergeable, BeanMetadataElement {

	/** 该元数据元素对应的配置来源对象。 */
	private @Nullable Object source;

	/** 是否允许与父级集合值合并。 */
	private boolean mergeEnabled;


	/**
	 * 设置该元数据元素的配置来源 {@code Object}。
	 * <p>对象的具体类型取决于所使用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}

	/**
	 * 设置是否允许合并本集合；
	 * 当存在「父级」集合值时可与父级合并。
	 */
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}


	@Override
	public Object merge(@Nullable Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Properties properties)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		// 先复制父级属性，再叠加当前属性（同名键由当前值覆盖）
		Properties merged = new ManagedProperties();
		merged.putAll(properties);
		merged.putAll(this);
		return merged;
	}

}
