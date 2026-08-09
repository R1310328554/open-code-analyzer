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

package org.springframework.transaction.support;

import java.io.Serializable;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;

/**
 * {@link TransactionDefinition} 接口的默认实现，
 * 提供 bean 风格配置与合理默认值
 * （PROPAGATION_REQUIRED、ISOLATION_DEFAULT、TIMEOUT_DEFAULT、readOnly=false）。
 *
 * <p>是 {@link TransactionTemplate} 和
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute} 的基类。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 08.05.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

	/** TransactionDefinition 中传播常量的前缀。 */
	public static final String PREFIX_PROPAGATION = "PROPAGATION_";

	/** TransactionDefinition 中隔离常量的前缀。 */
	public static final String PREFIX_ISOLATION = "ISOLATION_";

	/** 描述字符串中事务超时值的前缀。 */
	public static final String PREFIX_TIMEOUT = "timeout_";

	/** 描述字符串中只读事务的标记。 */
	public static final String READ_ONLY_MARKER = "readOnly";


	/**
	 * {@link TransactionDefinition} 中传播常量的名称到值映射。
	 */
	static final Map<String, Integer> propagationConstants = Map.of(
			"PROPAGATION_REQUIRED", TransactionDefinition.PROPAGATION_REQUIRED,
			"PROPAGATION_SUPPORTS", TransactionDefinition.PROPAGATION_SUPPORTS,
			"PROPAGATION_MANDATORY", TransactionDefinition.PROPAGATION_MANDATORY,
			"PROPAGATION_REQUIRES_NEW", TransactionDefinition.PROPAGATION_REQUIRES_NEW,
			"PROPAGATION_NOT_SUPPORTED", TransactionDefinition.PROPAGATION_NOT_SUPPORTED,
			"PROPAGATION_NEVER", TransactionDefinition.PROPAGATION_NEVER,
			"PROPAGATION_NESTED", TransactionDefinition.PROPAGATION_NESTED
		);

	/**
	 * {@link TransactionDefinition} 中隔离常量的名称到值映射。
	 */
	static final Map<String, Integer> isolationConstants = Map.of(
			"ISOLATION_DEFAULT", TransactionDefinition.ISOLATION_DEFAULT,
			"ISOLATION_READ_UNCOMMITTED", TransactionDefinition.ISOLATION_READ_UNCOMMITTED,
			"ISOLATION_READ_COMMITTED", TransactionDefinition.ISOLATION_READ_COMMITTED,
			"ISOLATION_REPEATABLE_READ", TransactionDefinition.ISOLATION_REPEATABLE_READ,
			"ISOLATION_SERIALIZABLE", TransactionDefinition.ISOLATION_SERIALIZABLE
		);

	private int propagationBehavior = PROPAGATION_REQUIRED;

	private int isolationLevel = ISOLATION_DEFAULT;

	private int timeout = TIMEOUT_DEFAULT;

	private boolean readOnly = false;

	private @Nullable String name;


	/**
	 * 使用默认设置创建新的 {@code DefaultTransactionDefinition}。
	 * 可通过 bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionDefinition() {
	}

	/**
	 * 拷贝构造函数。定义可通过 bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionDefinition(TransactionDefinition other) {
		this.propagationBehavior = other.getPropagationBehavior();
		this.isolationLevel = other.getIsolationLevel();
		this.timeout = other.getTimeout();
		this.readOnly = other.isReadOnly();
		this.name = other.getName();
	}

	/**
	 * 使用给定传播行为创建新的 {@code DefaultTransactionDefinition}。
	 * 可通过 bean 属性 setter 修改。
	 * @param propagationBehavior TransactionDefinition 接口中的传播常量之一
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public DefaultTransactionDefinition(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}


	/**
	 * 通过 {@link TransactionDefinition} 中对应常量名称设置传播行为
	 * ——例如 {@code "PROPAGATION_REQUIRED"}。
	 * @param constantName 常量名称
	 * @throws IllegalArgumentException 若提供的值无法解析为 {@code PROPAGATION_} 常量之一或为 {@code null}
	 * @see #setPropagationBehavior
	 * @see #PROPAGATION_REQUIRED
	 */
	public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer propagationBehavior = propagationConstants.get(constantName);
		Assert.notNull(propagationBehavior, "Only propagation behavior constants allowed");
		this.propagationBehavior = propagationBehavior;
	}

	/**
	 * 设置传播行为。必须是 TransactionDefinition 接口中的传播常量之一。默认为 PROPAGATION_REQUIRED。
	 * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时
	 * 拒绝隔离级别声明，可将事务管理器的 "validateExistingTransaction" 标志设为 "true"。
	 * <p>不支持自定义隔离级别的事务管理器在收到非 {@link #ISOLATION_DEFAULT} 级别时将抛出异常。
	 * @throws IllegalArgumentException 若提供的值不是 {@code PROPAGATION_} 常量之一
	 * @see #PROPAGATION_REQUIRED
	 */
	public final void setPropagationBehavior(int propagationBehavior) {
		Assert.isTrue(propagationConstants.containsValue(propagationBehavior),
				"Only values of propagation constants allowed");
		this.propagationBehavior = propagationBehavior;
	}

	@Override
	public final int getPropagationBehavior() {
		return this.propagationBehavior;
	}

	/**
	 * 通过 {@link TransactionDefinition} 中对应常量名称设置隔离级别
	 * ——例如 {@code "ISOLATION_DEFAULT"}。
	 * @param constantName 常量名称
	 * @throws IllegalArgumentException 若提供的值无法解析为 {@code ISOLATION_} 常量之一或为 {@code null}
	 * @see #setIsolationLevel
	 * @see #ISOLATION_DEFAULT
	 */
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer isolationLevel = isolationConstants.get(constantName);
		Assert.notNull(isolationLevel, "Only isolation constants allowed");
		this.isolationLevel = isolationLevel;
	}

	/**
	 * 设置隔离级别。必须是 TransactionDefinition 接口中的隔离常量之一。默认为 ISOLATION_DEFAULT。
	 * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时
	 * 拒绝隔离级别声明，可将事务管理器的 "validateExistingTransaction" 标志设为 "true"。
	 * <p>不支持自定义隔离级别的事务管理器在收到非 {@link #ISOLATION_DEFAULT} 级别时将抛出异常。
	 * @throws IllegalArgumentException 若提供的值不是 {@code ISOLATION_} 常量之一
	 * @see #ISOLATION_DEFAULT
	 */
	public final void setIsolationLevel(int isolationLevel) {
		Assert.isTrue(isolationConstants.containsValue(isolationLevel),
				"Only values of isolation constants allowed");
		this.isolationLevel = isolationLevel;
	}

	@Override
	public final int getIsolationLevel() {
		return this.isolationLevel;
	}

	/**
	 * 设置要应用的超时（秒数）。
	 * 默认为 TIMEOUT_DEFAULT（-1）。
	 * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。
	 * <p>不支持超时的事务管理器在收到非 {@link #TIMEOUT_DEFAULT} 超时时将抛出异常。
	 * @see #TIMEOUT_DEFAULT
	 */
	public final void setTimeout(int timeout) {
		if (timeout < TIMEOUT_DEFAULT) {
			throw new IllegalArgumentException("Timeout must be a non-negative integer or TIMEOUT_DEFAULT");
		}
		this.timeout = timeout;
	}

	@Override
	public final int getTimeout() {
		return this.timeout;
	}

	/**
	 * 设置是否作为只读事务优化。默认为 "false"。
	 * <p>只读标志适用于任意事务上下文，无论由实际资源事务
	 * （{@link #PROPAGATION_REQUIRED}/{@link #PROPAGATION_REQUIRES_NEW}）支持，
	 * 或在资源层非事务运行（{@link #PROPAGATION_SUPPORTS}）。
	 * 后者情况下，该标志仅适用于应用内受管资源，如 Hibernate {@code Session}。
	 * <p>这仅作为实际事务子系统的提示；<i>不必然</i>导致写访问失败。
	 * 无法解释只读提示的事务管理器在请求只读事务时<i>不会</i>抛出异常。
	 */
	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public final boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * 设置本事务名称。默认无。
	 * <p>若适用（例如 WebLogic），将作为事务监视器中显示的事务名称。
	 */
	public final void setName(String name) {
		this.name = name;
	}

	@Override
	public final @Nullable String getName() {
		return this.name;
	}


	/**
	 * 本实现比较 {@code toString()} 结果。
	 * @see #toString()
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TransactionDefinition && toString().equals(other.toString())));
	}

	/**
	 * 本实现返回 {@code toString()} 的哈希码。
	 * @see #toString()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 返回本事务定义的标识描述。
	 * <p>格式与 {@link org.springframework.transaction.interceptor.TransactionAttributeEditor} 使用的格式一致，
	 * 以便将 {@code toString} 结果填入类型为
	 * {@link org.springframework.transaction.interceptor.TransactionAttribute} 的 bean 属性。
	 * <p>子类须覆盖以实现正确的 {@code equals} 和 {@code hashCode} 行为。
	 * 也可直接覆盖 {@link #equals} 和 {@link #hashCode}。
	 * @see #getDefinitionDescription()
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	@Override
	public String toString() {
		return getDefinitionDescription().toString();
	}

	/**
	 * 返回本事务定义的标识描述。
	 * <p>供子类在其 {@code toString()} 结果中使用。
	 */
	protected final StringBuilder getDefinitionDescription() {
		StringBuilder result = new StringBuilder();
		result.append(getPropagationBehaviorName(this.propagationBehavior));
		result.append(',');
		result.append(getIsolationLevelName(this.isolationLevel));
		if (this.timeout != TIMEOUT_DEFAULT) {
			result.append(',');
			result.append(PREFIX_TIMEOUT).append(this.timeout);
		}
		if (this.readOnly) {
			result.append(',');
			result.append(READ_ONLY_MARKER);
		}
		return result;
	}

	private static String getPropagationBehaviorName(int propagationBehavior) {
		return switch(propagationBehavior) {
			case TransactionDefinition.PROPAGATION_REQUIRED -> "PROPAGATION_REQUIRED";
			case TransactionDefinition.PROPAGATION_SUPPORTS -> "PROPAGATION_SUPPORTS";
			case TransactionDefinition.PROPAGATION_MANDATORY -> "PROPAGATION_MANDATORY";
			case TransactionDefinition.PROPAGATION_REQUIRES_NEW -> "PROPAGATION_REQUIRES_NEW";
			case TransactionDefinition.PROPAGATION_NOT_SUPPORTED -> "PROPAGATION_NOT_SUPPORTED";
			case TransactionDefinition.PROPAGATION_NEVER -> "PROPAGATION_NEVER";
			case TransactionDefinition.PROPAGATION_NESTED -> "PROPAGATION_NESTED";
			default -> throw new IllegalArgumentException("Unsupported propagation behavior: " + propagationBehavior);
		};
	}

	static String getIsolationLevelName(int isolationLevel) {
		return switch(isolationLevel) {
			case TransactionDefinition.ISOLATION_DEFAULT -> "ISOLATION_DEFAULT";
			case TransactionDefinition.ISOLATION_READ_UNCOMMITTED -> "ISOLATION_READ_UNCOMMITTED";
			case TransactionDefinition.ISOLATION_READ_COMMITTED -> "ISOLATION_READ_COMMITTED";
			case TransactionDefinition.ISOLATION_REPEATABLE_READ -> "ISOLATION_REPEATABLE_READ";
			case TransactionDefinition.ISOLATION_SERIALIZABLE -> "ISOLATION_SERIALIZABLE";
			default -> throw new IllegalArgumentException("Unsupported isolation level: " + isolationLevel);
		};
	}

}
