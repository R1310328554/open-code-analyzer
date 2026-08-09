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

package org.springframework.beans.factory.xml;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.parsing.DefaultsDefinition;

/**
 * 简单 JavaBean，保存标准 Spring XML bean 定义文档中 {@code <beans>} 级别指定的默认值：
 * {@code default-lazy-init}、{@code default-autowire} 等。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 */
public class DocumentDefaultsDefinition implements DefaultsDefinition {

	private @Nullable String lazyInit;

	private @Nullable String merge;

	private @Nullable String autowire;

	private @Nullable String autowireCandidates;

	private @Nullable String initMethod;

	private @Nullable String destroyMethod;

	private @Nullable Object source;


	/**
	 * 设置当前正在解析的文档的默认 lazy-init 标志。
	 */
	public void setLazyInit(@Nullable String lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * 返回当前正在解析的文档的默认 lazy-init 标志。
	 */
	public @Nullable String getLazyInit() {
		return this.lazyInit;
	}

	/**
	 * 设置当前正在解析的文档的默认 merge 设置。
	 */
	public void setMerge(@Nullable String merge) {
		this.merge = merge;
	}

	/**
	 * 返回当前正在解析的文档的默认 merge 设置。
	 */
	public @Nullable String getMerge() {
		return this.merge;
	}

	/**
	 * 设置当前正在解析的文档的默认 autowire 设置。
	 */
	public void setAutowire(@Nullable String autowire) {
		this.autowire = autowire;
	}

	/**
	 * 返回当前正在解析的文档的默认 autowire 设置。
	 */
	public @Nullable String getAutowire() {
		return this.autowire;
	}

	/**
	 * 设置当前正在解析的文档的默认 autowire-candidate 模式。
	 * 也接受逗号分隔的模式列表。
	 */
	public void setAutowireCandidates(@Nullable String autowireCandidates) {
		this.autowireCandidates = autowireCandidates;
	}

	/**
	 * 返回当前正在解析的文档的默认 autowire-candidate 模式。
	 * 也可能返回逗号分隔的模式列表。
	 */
	public @Nullable String getAutowireCandidates() {
		return this.autowireCandidates;
	}

	/**
	 * 设置当前正在解析的文档的默认 init-method 设置。
	 */
	public void setInitMethod(@Nullable String initMethod) {
		this.initMethod = initMethod;
	}

	/**
	 * 返回当前正在解析的文档的默认 init-method 设置。
	 */
	public @Nullable String getInitMethod() {
		return this.initMethod;
	}

	/**
	 * 设置当前正在解析的文档的默认 destroy-method 设置。
	 */
	public void setDestroyMethod(@Nullable String destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	/**
	 * 返回当前正在解析的文档的默认 destroy-method 设置。
	 */
	public @Nullable String getDestroyMethod() {
		return this.destroyMethod;
	}

	/**
	 * 设置此元数据元素的配置源 {@code Object}。
	 * <p>对象的确切类型取决于所使用的配置机制。
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	public @Nullable Object getSource() {
		return this.source;
	}

}
