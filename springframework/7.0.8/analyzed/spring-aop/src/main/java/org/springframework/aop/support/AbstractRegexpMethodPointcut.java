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
 * 抽象基础正则表达式切入点 bean。 JavaBean 属性有： <ul> <li>pattern：要匹配的完全限定方法名称的正则表达式。确切的正则表达式语法将取决于子类（例如
 * ，Perl5 正则表达式） <li>patterns：采用字符串模式数组的替代属性。结果将是这些模式的联合。 </ul>
 * <p>注意：正则表达式必须匹配。例如，{@code .*get.*} 将匹配 com.mycom.Foo.getBar()。 {@code get.*} 不会。
 * <p>这个基类是可序列化的。子类应将所有字段声明为瞬态的； {@link #initPatternRepresentation} 方法将在反序列化时再次调用。
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
	 * 要匹配的正则表达式。
	 */
	private String[] patterns = new String[0];

	/**
	 * 正则表达式 <strong>not</strong> 进行匹配。
	 */
	private String[] excludedPatterns = new String[0];


	/**
	 * 当我们只有一个模式时的便捷方法。使用此方法或 {@link #setPatterns}，而不是同时使用两者。
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(pattern);
	}

	/**
	 * 设置要匹配的定义方法的正则表达式。匹配将是所有这些的并集；如果有匹配，则切入点匹配。
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
	 * 返回方法匹配的正则表达式。
	 */
	public String[] getPatterns() {
		return this.patterns;
	}

	/**
	 * 当我们只有一个排除模式时的便捷方法。使用此方法或 {@link #setExcludedPatterns}，而不是同时使用两者。
	 * @see #setExcludedPatterns
	 */
	public void setExcludedPattern(String excludedPattern) {
		setExcludedPatterns(excludedPattern);
	}

	/**
	 * 设置正则表达式定义方法以匹配排除。匹配将是所有这些的并集；如果有匹配，则切入点匹配。
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
	 * 返回排除匹配的正则表达式。
	 */
	public String[] getExcludedPatterns() {
		return this.excludedPatterns;
	}


	/**
	 * 尝试将正则表达式与目标类的完全限定名称以及方法的声明类以及方法的名称进行匹配。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return (matchesPattern(ClassUtils.getQualifiedMethodName(method, targetClass)) ||
				(targetClass != method.getDeclaringClass() &&
						matchesPattern(ClassUtils.getQualifiedMethodName(method, method.getDeclaringClass()))));
	}

	/**
	 * 将指定的候选者与配置的模式进行匹配。
	 * @param signatureString “java.lang.Object.hashCode”样式签名
	 * @return 候选者至少匹配指定模式之一
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
	 * 子类必须实现它来初始化正则表达式切入点。可以多次调用。 <p> 该方法将从 {@link #setPatterns} 方法以及反序列化时调用。
	 * @param patterns 要初始化的模式
	 * @throws IllegalArgumentException 如果模式无效
	 */
	protected abstract void initPatternRepresentation(String[] patterns) throws IllegalArgumentException;

	/**
	 * 子类必须实现它来初始化正则表达式切入点。可以多次调用。 <p> 该方法将从 {@link #setExcludedPatterns} 方法以及反序列化时调用。
	 * @param patterns 要初始化的模式
	 * @throws IllegalArgumentException 如果模式无效
	 */
	protected abstract void initExcludedPatternRepresentation(String[] patterns) throws IllegalArgumentException;

	/**
	 * 给定索引处的模式是否与给定字符串匹配？
	 * @param pattern 要匹配的 {@code String} 模式
	 * @param patternIndex 模式索引（从0开始）
	 * @return true} 如果存在匹配，否则为 {@code false}
	 */
	protected abstract boolean matches(String pattern, int patternIndex);

	/**
	 * 给定索引处的排除模式是否与给定字符串匹配？
	 * @param pattern 要匹配的 {@code String} 模式
	 * @param patternIndex 模式索引（从0开始）
	 * @return true} 如果存在匹配，否则为 {@code false}
	 */
	protected abstract boolean matchesExclusion(String pattern, int patternIndex);


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AbstractRegexpMethodPointcut otherPointcut &&
				Arrays.equals(this.patterns, otherPointcut.patterns) &&
				Arrays.equals(this.excludedPatterns, otherPointcut.excludedPatterns)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
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

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": patterns " + ObjectUtils.nullSafeToString(this.patterns) +
				", excluded patterns " + ObjectUtils.nullSafeToString(this.excludedPatterns);
	}

}
