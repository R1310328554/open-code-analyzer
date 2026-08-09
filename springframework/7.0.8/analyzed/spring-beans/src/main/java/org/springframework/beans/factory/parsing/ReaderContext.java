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

/**
 * 在 Bean 定义读取过程中传递的上下文，封装所有相关配置及状态。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ReaderContext {

	/** 当前读取的 Bean 定义资源。 */
	private final Resource resource;

	/** 问题报告器。 */
	private final ProblemReporter problemReporter;

	/** 事件监听器。 */
	private final ReaderEventListener eventListener;

	/** 来源提取器。 */
	private final SourceExtractor sourceExtractor;


	/**
	 * 构造新的 {@code ReaderContext}。
	 * @param resource XML Bean 定义资源
	 * @param problemReporter 使用中的问题报告器
	 * @param eventListener 使用中的事件监听器
	 * @param sourceExtractor 使用中的来源提取器
	 */
	public ReaderContext(Resource resource, ProblemReporter problemReporter,
			ReaderEventListener eventListener, SourceExtractor sourceExtractor) {

		this.resource = resource;
		this.problemReporter = problemReporter;
		this.eventListener = eventListener;
		this.sourceExtractor = sourceExtractor;
	}

	/**
	 * 返回当前读取的资源。
	 */
	public final Resource getResource() {
		return this.resource;
	}


	// 错误与警告

	/**
	 * 引发致命错误。
	 */
	public void fatal(String message, @Nullable Object source) {
		fatal(message, source, null, null);
	}

	/**
	 * 引发致命错误。
	 */
	public void fatal(String message, @Nullable Object source, @Nullable Throwable cause) {
		fatal(message, source, null, cause);
	}

	/**
	 * 引发致命错误。
	 */
	public void fatal(String message, @Nullable Object source, @Nullable ParseState parseState) {
		fatal(message, source, parseState, null);
	}

	/**
	 * 引发致命错误。
	 */
	public void fatal(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.fatal(new Problem(message, location, parseState, cause));
	}

	/**
	 * 引发普通错误。
	 */
	public void error(String message, @Nullable Object source) {
		error(message, source, null, null);
	}

	/**
	 * 引发普通错误。
	 */
	public void error(String message, @Nullable Object source, @Nullable Throwable cause) {
		error(message, source, null, cause);
	}

	/**
	 * 引发普通错误。
	 */
	public void error(String message, @Nullable Object source, @Nullable ParseState parseState) {
		error(message, source, parseState, null);
	}

	/**
	 * 引发普通错误。
	 */
	public void error(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.error(new Problem(message, location, parseState, cause));
	}

	/**
	 * 引发非致命警告。
	 */
	public void warning(String message, @Nullable Object source) {
		warning(message, source, null, null);
	}

	/**
	 * 引发非致命警告。
	 */
	public void warning(String message, @Nullable Object source, @Nullable Throwable cause) {
		warning(message, source, null, cause);
	}

	/**
	 * 引发非致命警告。
	 */
	public void warning(String message, @Nullable Object source, @Nullable ParseState parseState) {
		warning(message, source, parseState, null);
	}

	/**
	 * 引发非致命警告。
	 */
	public void warning(String message, @Nullable Object source, @Nullable ParseState parseState, @Nullable Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.warning(new Problem(message, location, parseState, cause));
	}


	// 显式解析事件

	/**
	 * 触发默认值已注册事件。
	 */
	public void fireDefaultsRegistered(DefaultsDefinition defaultsDefinition) {
		this.eventListener.defaultsRegistered(defaultsDefinition);
	}

	/**
	 * 触发组件已注册事件。
	 */
	public void fireComponentRegistered(ComponentDefinition componentDefinition) {
		this.eventListener.componentRegistered(componentDefinition);
	}

	/**
	 * 触发别名已注册事件。
	 */
	public void fireAliasRegistered(String beanName, String alias, @Nullable Object source) {
		this.eventListener.aliasRegistered(new AliasDefinition(beanName, alias, source));
	}

	/**
	 * 触发导入已处理事件。
	 */
	public void fireImportProcessed(String importedResource, @Nullable Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, source));
	}

	/**
	 * 触发导入已处理事件。
	 */
	public void fireImportProcessed(String importedResource, Resource[] actualResources, @Nullable Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, actualResources, source));
	}


	// 来源提取

	/**
	 * 返回使用中的来源提取器。
	 */
	public SourceExtractor getSourceExtractor() {
		return this.sourceExtractor;
	}

	/**
	 * 对给定来源对象调用来源提取器。
	 * @param sourceCandidate 原始来源对象
	 * @return 要存储的来源对象，若无则为 {@code null}
	 * @see #getSourceExtractor()
	 * @see SourceExtractor#extractSource
	 */
	public @Nullable Object extractSource(Object sourceCandidate) {
		return this.sourceExtractor.extractSource(sourceCandidate, this.resource);
	}

}
