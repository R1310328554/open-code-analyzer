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

package org.springframework.transaction.interceptor;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 判定给定异常是否应导致回滚的规则。
 *
 * <p>可应用多条此类规则，以在抛出异常后决定事务应提交还是回滚。
 *
 * <p>每条规则基于异常类型或异常模式，分别通过
 * {@link #RollbackRuleAttribute(Class)} 或 {@link #RollbackRuleAttribute(String)} 提供。
 *
 * <p>以异常类型定义回滚规则时，该类型用于匹配抛出异常的类型及其超类型，
 * 提供类型安全并避免使用模式时可能出现的意外匹配。
 * 例如 {@code jakarta.servlet.ServletException.class} 仅匹配
 * {@code jakarta.servlet.ServletException} 及其子类的抛出异常。
 *
 * <p>以异常模式定义回滚规则时，模式可为全限定类名或全限定类名的子串
 * （异常类型必须是 {@code Throwable} 的子类），目前不支持通配符。
 * 例如 {@code "jakarta.servlet.ServletException"} 或 {@code "ServletException"}
 * 将匹配 {@code jakarta.servlet.ServletException} 及其子类。
 *
 * <p>有关回滚规则语义、模式及基于模式规则可能意外匹配的警告，
 * 请参阅 {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 09.04.2003
 * @see NoRollbackRuleAttribute
 */
@SuppressWarnings("serial")
public class RollbackRuleAttribute implements Serializable{

	/**
	 * 针对 {@link RuntimeException 运行时异常} 的
	 * {@linkplain RollbackRuleAttribute 回滚规则}。
	 */
	public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS =
			new RollbackRuleAttribute(RuntimeException.class);

	/**
	 * 针对所有 {@link Exception 异常}（含受检异常）的
	 * {@linkplain RollbackRuleAttribute 回滚规则}。
	 * @since 6.2
	 */
	public static final RollbackRuleAttribute ROLLBACK_ON_ALL_EXCEPTIONS =
			new RollbackRuleAttribute(Exception.class);


	/**
	 * 异常模式：基于异常名称在抛出异常的类层次中搜索匹配时使用，
	 * 无类型安全，可能对名称相似的异常类型和嵌套异常类型产生意外匹配。
	 */
	private final String exceptionPattern;

	/**
	 * 异常类型：在抛出异常的类层次中搜索匹配时用于保证类型安全。
	 * @since 6.0
	 */
	private final @Nullable Class<? extends Throwable> exceptionType;


	/**
	 * 为给定 {@code exceptionType} 创建新的 {@code RollbackRuleAttribute} 实例。
	 * <p>这是构造以类型安全方式匹配所供异常类型及其子类的回滚规则的首选方式。
	 * <p>有关回滚规则语义的更多细节，请参阅
	 * {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。
	 * @param exceptionType 异常类型；必须是 {@link Throwable} 或其子类
	 * @throws IllegalArgumentException 若 {@code exceptionType} 不是 {@code Throwable} 类型或为 {@code null}
	 */
	@SuppressWarnings("unchecked")
	public RollbackRuleAttribute(Class<?> exceptionType) {
		Assert.notNull(exceptionType, "'exceptionType' cannot be null");
		if (!Throwable.class.isAssignableFrom(exceptionType)) {
			throw new IllegalArgumentException(
					"Cannot construct rollback rule from [" + exceptionType.getName() + "]: it's not a Throwable");
		}
		this.exceptionPattern = exceptionType.getName();
		this.exceptionType = (Class<? extends Throwable>) exceptionType;
	}

	/**
	 * 为给定 {@code exceptionPattern} 创建新的 {@code RollbackRuleAttribute} 实例。
	 * <p>有关回滚规则语义、模式及可能意外匹配的警告，请参阅
	 * {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 JavaDoc。
	 * <p>为提升类型安全并避免意外匹配，请改用 {@link #RollbackRuleAttribute(Class)}。
	 * @param exceptionPattern 异常名称模式；也可为全限定类名
	 * @throws IllegalArgumentException 若 {@code exceptionPattern} 为 {@code null} 或空
	 */
	public RollbackRuleAttribute(String exceptionPattern) {
		Assert.hasText(exceptionPattern, "'exceptionPattern' cannot be null or empty");
		this.exceptionPattern = exceptionPattern;
		this.exceptionType = null;
	}


	/**
	 * 获取本规则用于匹配的已配置异常名称模式。
	 * @see #getDepth(Throwable)
	 */
	public String getExceptionName() {
		return this.exceptionPattern;
	}

	/**
	 * 返回超类匹配深度，语义如下。
	 * <ul>
	 * <li>{@code -1} 表示本规则不匹配所供 {@code exception}。</li>
	 * <li>{@code 0} 表示本规则直接匹配所供 {@code exception}。</li>
	 * <li>其他正数表示本规则在超类层次中匹配所供 {@code exception}，
	 * 该值为所供 {@code exception} 与本规则直接匹配的异常之间类层次的层数。</li>
	 * </ul>
	 * <p>比较针对同一异常匹配的回滚规则时，匹配深度较小的规则胜出。
	 * 例如直接匹配（{@code depth == 0}）优于超类层次中的匹配（{@code depth > 0}）。
	 * <p>通过 {@link #RollbackRuleAttribute(String)} 以异常模式构造时，
	 * 对嵌套异常类型或名称相似的异常类型的匹配将返回
	 * 类层次中对应层级的深度，如同直接匹配一样。
	 */
	public int getDepth(Throwable exception) {
		return getDepth(exception.getClass(), 0);
	}


	private int getDepth(Class<?> exceptionType, int depth) {
		if (this.exceptionType != null) {
			if (this.exceptionType.equals(exceptionType)) {
				// 找到了！
				return depth;
			}
		}
		else if (exceptionType.getName().contains(this.exceptionPattern)) {
			// 找到了！
			return depth;
		}
		// 若已到达可搜索的尽头仍未找到...
		if (exceptionType == Throwable.class) {
			return -1;
		}
		return getDepth(exceptionType.getSuperclass(), depth + 1);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RollbackRuleAttribute that &&
				this.exceptionPattern.equals(that.exceptionPattern)));
	}

	@Override
	public int hashCode() {
		return this.exceptionPattern.hashCode();
	}

	@Override
	public String toString() {
		return "RollbackRuleAttribute with pattern [" + this.exceptionPattern + "]";
	}

}
