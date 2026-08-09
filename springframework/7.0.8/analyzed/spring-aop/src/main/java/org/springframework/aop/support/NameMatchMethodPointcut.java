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

package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.PatternMatchUtils;

/**
 * 简单方法名匹配的切入点 bean，作为正则表达式模式的替代。
 *
 * <p>每个配置的方法名可为精确方法名或方法名模式
 * （支持的模式风格见 {@link #isMatch(String, String)}）。
 *
 * <p>不处理重载方法：给定名称的所有方法均符合条件。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 11.02.2004
 * @see #isMatch
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public class NameMatchMethodPointcut extends StaticMethodMatcherPointcut implements Serializable {

	private List<String> mappedNamePatterns = new ArrayList<>();


	/**
	 * 配置单个方法名模式的便捷方法。
	 * <p>使用本方法或 {@link #setMappedNames(String...)} 之一，不可同时使用。
	 * @see #setMappedNames
	 */
	public void setMappedName(String mappedNamePattern) {
		setMappedNames(mappedNamePattern);
	}

	/**
	 * 设置定义要匹配方法的方法名模式。
	 * <p>匹配为所有模式的并集；任一匹配则切入点匹配。
	 * @see #setMappedName(String)
	 */
	public void setMappedNames(String... mappedNamePatterns) {
		this.mappedNamePatterns = new ArrayList<>(Arrays.asList(mappedNamePatterns));
	}

	/**
	 * 在已配置模式之外再添加一个方法名模式。
	 * <p>与 "set" 方法类似，本方法用于配置代理、代理使用前。
	 * <p><b>注意：</b>代理使用后本方法无效，因 advice 链会被缓存。
	 * @param mappedNamePattern 额外的方法名模式
	 * @return 本切入点，支持方法链式调用
	 * @see #setMappedNames(String...)
	 * @see #setMappedName(String)
	 */
	public NameMatchMethodPointcut addMethodName(String mappedNamePattern) {
		this.mappedNamePatterns.add(mappedNamePattern);
		return this;
	}


	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		for (String mappedNamePattern : this.mappedNamePatterns) {
			if (mappedNamePattern.equals(method.getName()) || isMatch(method.getName(), mappedNamePattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断给定方法名是否匹配映射名模式。
	 * <p>默认实现检查 {@code xxx*}、{@code *xxx}、
	 * {@code *xxx*}、{@code xxx*yyy} 匹配及直接相等。
	 * <p>子类可覆盖。
	 * @param methodName 待检查的方法名
	 * @param mappedNamePattern 方法名模式
	 * @return 方法名是否匹配模式
	 * @see PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String methodName, String mappedNamePattern) {
		return PatternMatchUtils.simpleMatch(mappedNamePattern, methodName);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof NameMatchMethodPointcut that &&
				this.mappedNamePatterns.equals(that.mappedNamePatterns)));
	}

	@Override
	public int hashCode() {
		return this.mappedNamePatterns.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.mappedNamePatterns;
	}

}
