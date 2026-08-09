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

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 对 {@link Resource} 中任意位置的建模类。
 *
 * <p>通常用于追踪 XML 配置文件中存在问题的或错误的元数据位置。例如，
 * 某个 {@link #getSource() 来源} 位置可能是「beans.properties 第 76 行定义的
 * Bean 具有无效的 Class」；另一来源可能是来自已解析 XML
 * {@link org.w3c.dom.Document} 的实际 DOM 元素；或者来源对象可能仅为 {@code null}。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class Location {

	/** 关联的资源。 */
	private final Resource resource;

	/** 资源内的具体位置。 */
	private final @Nullable Object source;


	/**
	 * 创建新的 {@link Location} 实例。
	 * @param resource 与本位置关联的资源
	 */
	public Location(Resource resource) {
		this(resource, null);
	}

	/**
	 * 创建新的 {@link Location} 实例。
	 * @param resource 与本位置关联的资源
	 * @param source 关联资源内的实际位置（可为 {@code null}）
	 */
	public Location(Resource resource, @Nullable Object source) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.source = source;
	}


	/**
	 * 获取与本位置关联的资源。
	 */
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * 获取关联 {@link #getResource() 资源} 内的实际位置（可为 {@code null}）。
	 * <p>返回对象的实际类型示例参见本类的类级 JavaDoc。
	 */
	public @Nullable Object getSource() {
		return this.source;
	}

}
