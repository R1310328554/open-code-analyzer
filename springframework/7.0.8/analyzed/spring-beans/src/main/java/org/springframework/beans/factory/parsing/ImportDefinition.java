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

import org.springframework.beans.BeanMetadataElement;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 解析过程中已处理导入的表示。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderEventListener#importProcessed(ImportDefinition)
 */
public class ImportDefinition implements BeanMetadataElement {

	/** 被导入资源的位置。 */
	private final String importedResource;

	/** 实际解析得到的资源数组。 */
	private final Resource @Nullable [] actualResources;

	/** 来源对象。 */
	private final @Nullable Object source;


	/**
	 * 创建新的 {@link ImportDefinition}。
	 * @param importedResource 被导入资源的位置
	 */
	public ImportDefinition(String importedResource) {
		this(importedResource, null, null);
	}

	/**
	 * 创建新的 {@link ImportDefinition}。
	 * @param importedResource 被导入资源的位置
	 * @param source 来源对象（可为 {@code null}）
	 */
	public ImportDefinition(String importedResource, @Nullable Object source) {
		this(importedResource, null, source);
	}

	/**
	 * 创建新的 {@link ImportDefinition}。
	 * @param importedResource 被导入资源的位置
	 * @param source 来源对象（可为 {@code null}）
	 */
	public ImportDefinition(String importedResource, Resource @Nullable [] actualResources, @Nullable Object source) {
		Assert.notNull(importedResource, "Imported resource must not be null");
		this.importedResource = importedResource;
		this.actualResources = actualResources;
		this.source = source;
	}


	/**
	 * 返回被导入资源的位置。
	 */
	public final String getImportedResource() {
		return this.importedResource;
	}

	/**
	 * 返回实际解析得到的资源数组。
	 */
	public final Resource @Nullable [] getActualResources() {
		return this.actualResources;
	}

	@Override
	public final @Nullable Object getSource() {
		return this.source;
	}

}
