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

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.util.ObjectUtils;

/**
 * 持有 Advice 的正则表达式方法切入点的便捷类，
 * 使其成为 {@link org.springframework.aop.Advisor}。
 *
 * <p>使用 "pattern" 和 "patterns" 透传属性配置本类。
 * 这些属性与 {@link AbstractRegexpMethodPointcut} 的 pattern
 * 和 patterns 属性类似。
 *
 * <p>可委托给任意 {@link AbstractRegexpMethodPointcut} 子类。
 * 默认使用 {@link JdkRegexpMethodPointcut}。
 * 要选择特定实现，覆盖 {@link #createPointcut} 方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPattern
 * @see #setPatterns
 * @see JdkRegexpMethodPointcut
 */
@SuppressWarnings("serial")
public class RegexpMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

	private String @Nullable [] patterns;

	private @Nullable AbstractRegexpMethodPointcut pointcut;

	private final Object pointcutMonitor = new SerializableMonitor();


	/**
	 * 创建空的 RegexpMethodPointcutAdvisor。
	 * @see #setPattern
	 * @see #setPatterns
	 * @see #setAdvice
	 */
	public RegexpMethodPointcutAdvisor() {
	}

	/**
	 * 为给定 advice 创建 RegexpMethodPointcutAdvisor。
	 * 之后仍需指定 pattern。
	 * @param advice 要使用的 advice
	 * @see #setPattern
	 * @see #setPatterns
	 */
	public RegexpMethodPointcutAdvisor(Advice advice) {
		setAdvice(advice);
	}

	/**
	 * 为给定 advice 创建 RegexpMethodPointcutAdvisor。
	 * @param pattern 要使用的 pattern
	 * @param advice 要使用的 advice
	 */
	public RegexpMethodPointcutAdvisor(String pattern, Advice advice) {
		setPattern(pattern);
		setAdvice(advice);
	}

	/**
	 * 为给定 advice 创建 RegexpMethodPointcutAdvisor。
	 * @param patterns 要使用的 patterns
	 * @param advice 要使用的 advice
	 */
	public RegexpMethodPointcutAdvisor(String[] patterns, Advice advice) {
		setPatterns(patterns);
		setAdvice(advice);
	}


	/**
	 * 设置定义要匹配方法的正则表达式。
	 * <p>使用本方法或 {@link #setPatterns} 之一，不可同时使用。
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(pattern);
	}

	/**
	 * 设置定义要匹配方法的正则表达式。
	 * 将透传给切入点实现。
	 * <p>匹配为所有模式的并集；任一模式匹配则切入点匹配。
	 * @see AbstractRegexpMethodPointcut#setPatterns
	 */
	public void setPatterns(String... patterns) {
		this.patterns = patterns;
	}


	/**
	 * 初始化本 Advisor 内持有的单例 Pointcut。
	 */
	@Override
	public Pointcut getPointcut() {
		synchronized (this.pointcutMonitor) {
			if (this.pointcut == null) {
				this.pointcut = createPointcut();
				if (this.patterns != null) {
					this.pointcut.setPatterns(this.patterns);
				}
			}
			return this.pointcut;
		}
	}

	/**
	 * 创建实际切入点：默认使用 {@link JdkRegexpMethodPointcut}。
	 * @return Pointcut 实例（永不为 {@code null}）
	 */
	protected AbstractRegexpMethodPointcut createPointcut() {
		return new JdkRegexpMethodPointcut();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": advice [" + getAdvice() +
				"], pointcut patterns " + ObjectUtils.nullSafeToString(this.patterns);
	}


	/**
	 * 用于可序列化监视器对象的空类。
	 */
	private static class SerializableMonitor implements Serializable {
	}

}
