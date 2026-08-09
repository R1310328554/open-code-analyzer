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

/**
 * {@link RollbackRuleAttribute} 的标记子类，行为与
 * {@code RollbackRuleAttribute} 超类相反。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 09.04.2003
 */
@SuppressWarnings("serial")
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {

	/**
	 * 为给定 {@code exceptionType} 创建新的 {@code NoRollbackRuleAttribute} 实例。
	 * @param exceptionType 异常类型；必须是 {@link Throwable} 或其子类
	 * @throws IllegalArgumentException 若 {@code exceptionType} 不是 {@code Throwable} 类型或为 {@code null}
	 * @see RollbackRuleAttribute#RollbackRuleAttribute(Class)
	 */
	public NoRollbackRuleAttribute(Class<?> exceptionType) {
		super(exceptionType);
	}

	/**
	 * 为给定 {@code exceptionPattern} 创建新的 {@code NoRollbackRuleAttribute} 实例。
	 * @param exceptionPattern 异常名称模式；也可为全限定类名
	 * @throws IllegalArgumentException 若 {@code exceptionPattern} 为 {@code null} 或空
	 * @see RollbackRuleAttribute#RollbackRuleAttribute(String)
	 */
	public NoRollbackRuleAttribute(String exceptionPattern) {
		super(exceptionPattern);
	}

	@Override
	public String toString() {
		return "No" + super.toString();
	}

}
