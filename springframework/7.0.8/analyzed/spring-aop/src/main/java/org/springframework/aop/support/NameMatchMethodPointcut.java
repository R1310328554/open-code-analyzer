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
 * 用于简单方法名称匹配的切入点 bean，作为正则表达式模式的替代方案。
 * <p>E每个配置的方法名称可以是精确的方法名称或方法名称模式（有关支持的模式样式的详细信息，请参阅 {@link #isMatch(String, String)}）。
 * <p>不处理重载方法：具有给定名称的所有方法都符合条件。
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
	 * 配置单个方法名称模式的便捷方法。 <p> 使用此方法或 {@link #setMappedNames(String...)}，但不能同时使用两者。
	 * @see #setMappedNames
	 */
	public void setMappedName(String mappedNamePattern) {
		setMappedNames(mappedNamePattern);
	}

	/**
	 * 设置定义要匹配的方法的方法名称模式。 <p>Matching 将是所有这些的并集；如果有匹配，则切入点匹配。
	 * @see #setMappedName(String)
	 */
	public void setMappedNames(String... mappedNamePatterns) {
		this.mappedNamePatterns = new ArrayList<>(Arrays.asList(mappedNamePatterns));
	}

	/**
	 * 除了已配置的方法名称模式之外，添加另一个方法名称模式。 <p>与“set”方法一样，此方法用于在使用代理之前配置代理时使用。 <p><b>NOTE:</b> 此方法在使用代理后
	 * 不起作用，因为建议链将被缓存。
	 * @param mappedNamePattern 附加方法名称模式
	 * @return 切入点以允许方法链接
	 * @see #setMappedNames(String...)
	 * @see #setMappedName(String)
	 */
	public NameMatchMethodPointcut addMethodName(String mappedNamePattern) {
		this.mappedNamePatterns.add(mappedNamePattern);
		return this;
	}


	/**
	 * 匹配：es（方法 `matches`）。
	 */
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
	 * 确定给定的方法名称是否与映射的名称模式匹配。 <p> 默认实现检查 {@code xxx*}、{@code *xxx}、{@code *xxx*} 和 {@code xxx*y
	 * yy} 匹配以及直接相等。 <p>可以在子类中重写。
	 * @param methodName 要检查的方法名称
	 * @param mappedNamePattern 方法名称模式
	 * @return true} 如果方法名称与模式匹配
	 * @see PatternMatchUtils#simpleMatch(String, String)
	 */
	protected boolean isMatch(String methodName, String mappedNamePattern) {
		return PatternMatchUtils.simpleMatch(mappedNamePattern, methodName);
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof NameMatchMethodPointcut that &&
				this.mappedNamePatterns.equals(that.mappedNamePatterns)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return this.mappedNamePatterns.hashCode();
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.mappedNamePatterns;
	}

}
