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
import java.util.Arrays;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 正则表达式切入点 Bean 的抽象基类。JavaBean 属性包括：
 * <ul>
 * <li>pattern：匹配全限定方法名的正则表达式。
 * 具体 regexp 语法取决于子类（例如 Perl5 正则）
 * <li>patterns：接受 String 数组的替代属性。
 * 结果为这些模式的并集。
 * </ul>
 *
 * <p>注意：正则表达式必须完全匹配。例如
 * {@code .*get.*} 可匹配 com.mycom.Foo.getBar()，
 * 而 {@code get.*} 不行。
 *
 * <p>本基类可序列化。子类应将所有字段声明为 transient；
 * 反序列化时会再次调用 {@link #initPatternRepresentation}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public abstract class AbstractRegexpMethodPointcut extends StaticMethodMatcherPointcut
		implements Serializable {

	/**
	 * 用于匹配的正则表达式。
	 */
	private String[] patterns = new String[0];

	/**
	 * <strong>不</strong>匹配的正则表达式。
	 */
	private String[] excludedPatterns = new String[0];


	/**
	 * 仅有一个模式时的便捷方法。
	 * 使用本方法或 {@link #setPatterns} 之一，不可同时使用。
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(pattern);
	}

	/**
	 * 设置定义待匹配方法的正则表达式。
	 * 匹配结果为所有模式的并集；任一匹配则切入点匹配。
	 * @see #setPattern
	 */
	public void setPatterns(String... patterns) {
		Assert.notEmpty(patterns, "'patterns' must not be empty");
		this.patterns = new String[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			this.patterns[i] = patterns[i].strip();
		}
		initPatternRepresentation(this.patterns);
	}

	/**
	 * 返回用于方法匹配的正则表达式。
	 */
	public String[] getPatterns() {
		return this.patterns;
	}

	/**
	 * 仅有一个排除模式时的便捷方法。
	 * 使用本方法或 {@link #setExcludedPatterns} 之一，不可同时使用。
	 * @see #setExcludedPatterns
	 */
	public void setExcludedPattern(String excludedPattern) {
		setExcludedPatterns(excludedPattern);
	}

	/**
	 * 设置用于排除匹配的方法正则表达式。
	 * 匹配结果为所有模式的并集；任一匹配则切入点匹配。
	 * @see #setExcludedPattern
	 */
	public void setExcludedPatterns(String... excludedPatterns) {
		Assert.notEmpty(excludedPatterns, "'excludedPatterns' must not be empty");
		this.excludedPatterns = new String[excludedPatterns.length];
		for (int i = 0; i < excludedPatterns.length; i++) {
			this.excludedPatterns[i] = excludedPatterns[i].strip();
		}
		initExcludedPatternRepresentation(this.excludedPatterns);
	}

	/**
	 * 返回用于排除匹配的正则表达式。
	 */
	public String[] getExcludedPatterns() {
		return this.excludedPatterns;
	}


	/**
	 * 尝试将正则表达式与目标类的全限定名、
	 * 方法声明类及方法名进行匹配。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return (matchesPattern(ClassUtils.getQualifiedMethodName(method, targetClass)) ||
				(targetClass != method.getDeclaringClass() &&
						matchesPattern(ClassUtils.getQualifiedMethodName(method, method.getDeclaringClass()))));
	}

	/**
	 * 将指定候选与已配置的模式匹配。
	 * @param signatureString "java.lang.Object.hashCode" 风格的签名
	 * @return 候选是否匹配至少一个指定模式
	 */
	protected boolean matchesPattern(String signatureString) {
		for (int i = 0; i < this.patterns.length; i++) {
			boolean matched = matches(signatureString, i);
			if (matched) {
				for (int j = 0; j < this.excludedPatterns.length; j++) {
					boolean excluded = matchesExclusion(signatureString, j);
					if (excluded) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}


	/**
	 * 子类必须实现此方法以初始化 regexp 切入点。
	 * 可被多次调用。
	 * <p>由 {@link #setPatterns} 调用，反序列化时也会调用。
	 * @param patterns 要初始化的模式
	 * @throws IllegalArgumentException 若模式无效
	 */
	protected abstract void initPatternRepresentation(String[] patterns) throws IllegalArgumentException;

	/**
	 * 子类必须实现此方法以初始化 regexp 切入点。
	 * 可被多次调用。
	 * <p>由 {@link #setExcludedPatterns} 调用，反序列化时也会调用。
	 * @param patterns 要初始化的模式
	 * @throws IllegalArgumentException 若模式无效
	 */
	protected abstract void initExcludedPatternRepresentation(String[] patterns) throws IllegalArgumentException;

	/**
	 * 给定索引处的模式是否匹配给定字符串？
	 * @param pattern 要匹配的 {@code String} 模式
	 * @param patternIndex 模式索引（从 0 起）
	 * @return 匹配则 {@code true}，否则 {@code false}
	 */
	protected abstract boolean matches(String pattern, int patternIndex);

	/**
	 * 给定索引处的排除模式是否匹配给定字符串？
	 * @param pattern 要匹配的 {@code String} 模式
	 * @param patternIndex 模式索引（从 0 起）
	 * @return 匹配则 {@code true}，否则 {@code false}
	 */
	protected abstract boolean matchesExclusion(String pattern, int patternIndex);


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AbstractRegexpMethodPointcut otherPointcut &&
				Arrays.equals(this.patterns, otherPointcut.patterns) &&
				Arrays.equals(this.excludedPatterns, otherPointcut.excludedPatterns)));
	}

	@Override
	public int hashCode() {
		int result = 27;
		for (String pattern : this.patterns) {
			result = 13 * result + pattern.hashCode();
		}
		for (String excludedPattern : this.excludedPatterns) {
			result = 13 * result + excludedPattern.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": patterns " + ObjectUtils.nullSafeToString(this.patterns) +
				", excluded patterns " + ObjectUtils.nullSafeToString(this.excludedPatterns);
	}

}
