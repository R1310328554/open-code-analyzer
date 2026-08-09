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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * Spring 通用事务属性实现。
 * 默认在运行时异常时回滚，受检异常时不回滚。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 16.03.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	private @Nullable String descriptor;

	private @Nullable String timeoutString;

	private @Nullable String qualifier;

	private Collection<String> labels = Collections.emptyList();


	/**
	 * 以默认设置创建新的 {@code DefaultTransactionAttribute}。
	 * 可通过 Bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute() {
	}

	/**
	 * 拷贝构造函数。定义可通过 Bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute(TransactionAttribute other) {
		super(other);
	}

	/**
	 * 以给定传播行为创建新的 {@code DefaultTransactionAttribute}。
	 * 可通过 Bean 属性 setter 修改。
	 * @param propagationBehavior TransactionDefinition 接口中的传播常量之一
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}


	/**
	 * 设置本事务属性的描述符，
	 * 例如指示属性适用的位置。
	 * @since 4.3.4
	 */
	public void setDescriptor(@Nullable String descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * 返回本事务属性的描述符，
	 * 若无则为 {@code null}。
	 * @since 4.3.4
	 */
	public @Nullable String getDescriptor() {
		return this.descriptor;
	}

	/**
	 * 设置要应用的超时（若有），
	 * 为解析为秒数的字符串值。
	 * @since 5.3
	 * @see #setTimeout
	 * @see #resolveAttributeStrings
	 */
	public void setTimeoutString(@Nullable String timeoutString) {
		this.timeoutString = timeoutString;
	}

	/**
	 * 返回要应用的超时（若有），
	 * 为解析为秒数的字符串值。
	 * @since 5.3
	 * @see #getTimeout
	 * @see #resolveAttributeStrings
	 */
	public @Nullable String getTimeoutString() {
		return this.timeoutString;
	}

	/**
	 * 将限定符值与本事务属性关联。
	 * <p>可用于选择相应的事务管理器处理此特定事务。
	 * @since 3.0
	 * @see #resolveAttributeStrings
	 */
	public void setQualifier(@Nullable String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * 返回与本事务属性关联的限定符值。
	 * @since 3.0
	 */
	@Override
	public @Nullable String getQualifier() {
		return this.qualifier;
	}

	/**
	 * 将一个或多个标签与本事务属性关联。
	 * <p>可用于应用特定事务行为或纯描述用途。
	 * @since 5.3
	 * @see #resolveAttributeStrings
	 */
	public void setLabels(Collection<String> labels) {
		this.labels = labels;
	}

	@Override
	public Collection<String> getLabels() {
		return this.labels;
	}

	/**
	 * 默认行为与 EJB 相同：在未检查异常（{@link RuntimeException}）时回滚，
	 * 假定超出任何业务规则的意外结果。此外，我们也尝试在 {@link Error} 时回滚，
	 * 这同样是明确的意外结果。相比之下，受检异常被视为业务异常，
	 * 因此是事务性业务方法的常规预期结果，
	 * 即一种仍允许资源操作正常完成的替代返回值。
	 * <p>这与 TransactionTemplate 的默认行为大体一致，
	 * 但 TransactionTemplate 也会在未声明的受检异常时回滚（边界情况）。
	 * 对于声明式事务，我们预期受检异常被有意声明为业务异常，默认导致提交。
	 * @see org.springframework.transaction.support.TransactionTemplate#execute
	 */
	@Override
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}


	/**
	 * 解析定义为可解析字符串的属性值：
	 * {@link #setTimeoutString}、{@link #setQualifier}、{@link #setLabels}。
	 * 通常用于解析 "${...}" 占位符。
	 * @param resolver 要应用的内嵌值解析器（若有）
	 * @since 5.3
	 */
	public void resolveAttributeStrings(@Nullable StringValueResolver resolver) {
		String timeoutString = this.timeoutString;
		if (StringUtils.hasText(timeoutString)) {
			if (resolver != null) {
				timeoutString = resolver.resolveStringValue(timeoutString);
			}
			if (StringUtils.hasLength(timeoutString)) {
				try {
					setTimeout(Integer.parseInt(timeoutString));
				}
				catch (RuntimeException ex) {
					throw new IllegalArgumentException(
							"Invalid timeoutString value \"" + timeoutString + "\"; " + ex);
				}
			}
		}

		if (resolver != null) {
			if (this.qualifier != null) {
				this.qualifier = resolver.resolveStringValue(this.qualifier);
			}
			Set<String> resolvedLabels = CollectionUtils.newLinkedHashSet(this.labels.size());
			for (String label : this.labels) {
				resolvedLabels.add(resolver.resolveStringValue(label));
			}
			this.labels = resolvedLabels;
		}
	}

	/**
	 * 返回本事务属性的标识描述。
	 * <p>供子类使用，以包含在其 {@code toString()} 结果中。
	 */
	protected final StringBuilder getAttributeDescription() {
		StringBuilder result = getDefinitionDescription();
		if (StringUtils.hasText(this.qualifier)) {
			result.append("; '").append(this.qualifier).append('\'');
		}
		if (!this.labels.isEmpty()) {
			result.append("; ").append(this.labels);
		}
		return result;
	}

}
