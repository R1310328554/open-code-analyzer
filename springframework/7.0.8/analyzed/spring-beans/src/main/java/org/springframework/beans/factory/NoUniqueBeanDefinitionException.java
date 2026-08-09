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

package org.springframework.beans.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.util.StringUtils;

/**
 * 当向 {@code BeanFactory} 请求某个 Bean 实例，期望仅有一个匹配，
 * 却发现多个匹配候选时抛出。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.2.1
 * @see BeanFactory#getBean(Class)
 */
@SuppressWarnings("serial")
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

	/** 期望唯一匹配时实际找到的 Bean 数量 */
	private final int numberOfBeansFound;

	/** 所有匹配 Bean 的名称集合；构造时未指定则为 {@code null} */
	private final @Nullable Collection<String> beanNamesFound;


	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param beanNamesFound 所有匹配 Bean 的名称（作为 Collection）
	 * @param message 描述问题的详细消息
	 * @since 6.2
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound, String message) {
		super(type, message);
		this.numberOfBeansFound = beanNamesFound.size();
		this.beanNamesFound = new ArrayList<>(beanNamesFound);
	}

	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param numberOfBeansFound 匹配 Bean 的数量
	 * @param message 描述问题的详细消息
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
		super(type, message);
		this.numberOfBeansFound = numberOfBeansFound;
		this.beanNamesFound = null;
	}

	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param beanNamesFound 所有匹配 Bean 的名称（作为 Collection）
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
		this(type, beanNamesFound, "expected single matching bean but found " + beanNamesFound.size() + ": " +
				StringUtils.collectionToCommaDelimitedString(beanNamesFound));
	}

	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param beanNamesFound 所有匹配 Bean 的名称（作为数组）
	 */
	public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
		this(type, Arrays.asList(beanNamesFound));
	}

	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param beanNamesFound 所有匹配 Bean 的名称（作为 Collection）
	 * @since 5.1
	 */
	public NoUniqueBeanDefinitionException(ResolvableType type, Collection<String> beanNamesFound) {
		super(type, "expected single matching bean but found " + beanNamesFound.size() + ": " +
				StringUtils.collectionToCommaDelimitedString(beanNamesFound));
		this.numberOfBeansFound = beanNamesFound.size();
		this.beanNamesFound = new ArrayList<>(beanNamesFound);
	}

	/**
	 * 创建新的 {@code NoUniqueBeanDefinitionException}。
	 * @param type 非唯一 Bean 的所需类型
	 * @param beanNamesFound 所有匹配 Bean 的名称（作为数组）
	 * @since 5.1
	 */
	public NoUniqueBeanDefinitionException(ResolvableType type, String... beanNamesFound) {
		this(type, Arrays.asList(beanNamesFound));
	}


	/**
	 * 返回在期望仅有一个匹配 Bean 时实际找到的 Bean 数量。
	 * 对于 NoUniqueBeanDefinitionException，该值通常大于 1。
	 * @see #getBeanType()
	 */
	@Override
	public int getNumberOfBeansFound() {
		return this.numberOfBeansFound;
	}

	/**
	 * 返回在期望仅有一个匹配 Bean 时找到的所有 Bean 名称。
	 * 注意：若构造时未指定，可能为 {@code null}。
	 * @since 4.3
	 * @see #getBeanType()
	 */
	public @Nullable Collection<String> getBeanNamesFound() {
		return this.beanNamesFound;
	}

}
