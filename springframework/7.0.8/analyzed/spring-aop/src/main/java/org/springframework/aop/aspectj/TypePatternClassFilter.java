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

package org.springframework.aop.aspectj;

import java.util.Objects;

import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 使用 AspectJ 类型匹配的 Spring AOP {@link ClassFilter} 实现。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
public class TypePatternClassFilter implements ClassFilter {

	private String typePattern = "";

	/** 类型相关状态（`aspectJTypePatternMatcher`）。 */
	private @Nullable TypePatternMatcher aspectJTypePatternMatcher;


	/**
	 * 创建 {@link TypePatternClassFilter} 类的新实例。 <p>这是JavaBean构造函数；请务必设置 {@link
	 * #setTypePattern(String) typePattern} 属性，否则首次调用 {@link #matches(Class)} 方法时无疑会抛出致命的
	 * {@link IllegalStateException}。
	 */
	public TypePatternClassFilter() {
	}

	/**
	 * 使用给定的类型模式创建完全配置的 {@link TypePatternClassFilter}。
	 * @param typePattern AspectJ weaver 应该解析的类型模式
	 */
	public TypePatternClassFilter(String typePattern) {
		setTypePattern(typePattern);
	}


	/**
	 * 设置要匹配的 AspectJ 类型模式。 <p>示例包括： <code class="code"> org.springframework.beans.* </code>
	 * 这将匹配给定包中的任何类或接口。 <code class="code"> org.springframework.beans.ITestBean+ </code> 这将匹配
	 * {@code ITestBean} 接口和实现它的任何类。 <p>这些约定是由AspectJ建立的，而不是Spring AOP。
	 * @param typePattern AspectJ weaver 应该解析的类型模式
	 */
	public void setTypePattern(String typePattern) {
		Assert.notNull(typePattern, "Type pattern must not be null");
		this.typePattern = typePattern;
		this.aspectJTypePatternMatcher =
				PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().
				parseTypePattern(replaceBooleanOperators(typePattern));
	}

	/**
	 * 返回要匹配的 AspectJ 类型模式。
	 */
	public String getTypePattern() {
		return this.typePattern;
	}


	/**
	 * 切入点是否应该应用于给定的接口或目标类？
	 * @param clazz 候选目标类别
	 * @return 该建议应适用于该候选目标类别
	 * @throws IllegalStateException 如果未设置 {@link #setTypePattern(String)}
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		Assert.state(this.aspectJTypePatternMatcher != null, "No type pattern has been set");
		return this.aspectJTypePatternMatcher.matches(clazz);
	}

	/**
	 * 如果已在 XML 中指定类型模式，则用户无法将 {@code and} 写为“&&”（尽管 &amp;&amp; 可以工作）。我们还允许两个子表达式之间存在 {@code an
	 * d}。 <p>此方法转换回 AspectJ 切入点解析器的 {@code &&}。
	 */
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr," and "," && ");
		result = StringUtils.replace(result, " or ", " || ");
		return StringUtils.replace(result, " not ", " ! ");
	}

	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TypePatternClassFilter that &&
				ObjectUtils.nullSafeEquals(this.typePattern, that.typePattern)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(this.typePattern);
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.typePattern;
	}

}
