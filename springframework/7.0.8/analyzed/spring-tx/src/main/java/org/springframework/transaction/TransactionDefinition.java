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

package org.springframework.transaction;

import org.jspecify.annotations.Nullable;

/**
 * 定义符合 Spring 规范的事务属性的接口。
 * 基于与 EJB CMT 属性类似的事务传播行为定义。
 *
 * <p>注意，隔离级别与超时设置仅在真正启动新事务时才会生效。
 * 只有 {@link #PROPAGATION_REQUIRED}、{@link #PROPAGATION_REQUIRES_NEW}
 * 和 {@link #PROPAGATION_NESTED} 能触发新事务，
 * 因此在其他情况下通常无需指定这些设置。
 * 此外，并非所有事务管理器都支持这些高级特性，
 * 给定非默认值时可能抛出相应异常。
 *
 * <p>{@linkplain #isReadOnly() 只读标志}适用于任意事务上下文，
 * 无论是否由实际资源事务支持，或在资源层非事务运行。
 * 后者情况下，该标志仅适用于应用内受管资源，如 Hibernate {@code Session}。
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see PlatformTransactionManager#getTransaction(TransactionDefinition)
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 */
public interface TransactionDefinition {

	/**
	 * 支持当前事务；若不存在则创建新事务。
	 * 与同名 EJB 事务属性类似。
	 * <p>这通常是事务定义的默认设置，
	 * 并通常定义事务同步范围。
	 */
	int PROPAGATION_REQUIRED = 0;

	/**
	 * 支持当前事务；若不存在则以非事务方式执行。
	 * 与同名 EJB 事务属性类似。
	 * <p><b>注意：</b>对于支持事务同步的事务管理器，
	 * {@code PROPAGATION_SUPPORTS} 与完全无事务略有不同，
	 * 因为它定义了同步可能适用的事务范围。
	 * 因此相同资源（JDBC {@code Connection}、Hibernate {@code Session} 等）
	 * 将在整个指定范围内共享。具体行为取决于事务管理器的同步配置。
	 * <p>一般而言，请谨慎使用 {@code PROPAGATION_SUPPORTS}。
	 * 尤其不要在 {@code PROPAGATION_SUPPORTS} 范围内
	 * <i>依赖</i> {@code PROPAGATION_REQUIRED} 或 {@code PROPAGATION_REQUIRES_NEW}
	 * （可能导致运行时同步冲突）。若无法避免此类嵌套，
	 * 请适当配置事务管理器（通常切换为 "synchronization on actual transaction"）。
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * 支持当前事务；若不存在当前事务则抛出异常。
	 * 与同名 EJB 事务属性类似。
	 * <p>注意，{@code PROPAGATION_MANDATORY} 范围内的事务同步
	 * 始终由外围事务驱动。
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * 创建新事务，若存在当前事务则将其挂起。
	 * 与同名 EJB 事务属性类似。
	 * <p><b>注意：</b>并非所有事务管理器都能开箱即用地实现实际事务挂起。
	 * 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager}，
	 * 它需要可用的 {@code jakarta.transaction.TransactionManager}
	 * （在标准 Jakarta EE 中因服务器而异）。
	 * <p>{@code PROPAGATION_REQUIRES_NEW} 范围始终定义自己的事务同步。
	 * 现有同步将被适当挂起并恢复。
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * 不支持当前事务；始终以非事务方式执行。
	 * 与同名 EJB 事务属性类似。
	 * <p><b>注意：</b>并非所有事务管理器都能开箱即用地实现实际事务挂起。
	 * 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager}，
	 * 它需要可用的 {@code jakarta.transaction.TransactionManager}
	 * （在标准 Jakarta EE 中因服务器而异）。
	 * <p>注意，{@code PROPAGATION_NOT_SUPPORTED} 范围内<i>不</i>可用事务同步。
	 * 现有同步将被适当挂起并恢复。
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_NOT_SUPPORTED = 4;

	/**
	 * 不支持当前事务；若存在当前事务则抛出异常。
	 * 与同名 EJB 事务属性类似。
	 * <p>注意，{@code PROPAGATION_NEVER} 范围内<i>不</i>可用事务同步。
	 */
	int PROPAGATION_NEVER = 5;

	/**
	 * 若存在当前事务则在嵌套事务内执行，
	 * 否则行为类似 {@link #PROPAGATION_REQUIRED}。EJB 中无对应特性。
	 * <p><b>注意：</b>嵌套事务的实际创建仅在特定事务管理器上有效。
	 * 开箱即用情况下，仅适用于使用 JDBC 3.0+ 驱动时的
	 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}。
	 * 部分 JTA 提供者也可能支持嵌套事务。
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	int PROPAGATION_NESTED = 6;


	/**
	 * 使用底层数据存储的默认隔离级别。
	 * <p>其他级别均对应 JDBC 隔离级别。
	 * @see java.sql.Connection
	 */
	int ISOLATION_DEFAULT = -1;

	/**
	 * 表示可能发生脏读、不可重复读和幻读。
	 * <p>该级别允许一个事务修改的行在提交前
	 * 被另一事务读取（"脏读"）。
	 * 若任一修改被回滚，第二事务将读到无效行。
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	int ISOLATION_READ_UNCOMMITTED = 1;  // same as java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;

	/**
	 * 表示防止脏读；不可重复读和幻读仍可能发生。
	 * <p>该级别仅禁止事务读取含未提交修改的行。
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	int ISOLATION_READ_COMMITTED = 2;  // same as java.sql.Connection.TRANSACTION_READ_COMMITTED;

	/**
	 * 表示防止脏读和不可重复读；幻读仍可能发生。
	 * <p>该级别禁止读取含未提交修改的行，
	 * 也禁止一事务读行、第二事务修改行、第一事务再读得到不同值
	 * （"不可重复读"）的情况。
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	int ISOLATION_REPEATABLE_READ = 4;  // same as java.sql.Connection.TRANSACTION_REPEATABLE_READ;

	/**
	 * 表示防止脏读、不可重复读和幻读。
	 * <p>该级别包含 {@link #ISOLATION_REPEATABLE_READ} 的限制，
	 * 并进一步禁止：一事务读取满足 {@code WHERE} 条件的所有行，
	 * 第二事务插入满足该 {@code WHERE} 条件的行，
	 * 第一事务再次按相同条件读取时得到额外 "幻" 行的情况。
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	int ISOLATION_SERIALIZABLE = 8;  // same as java.sql.Connection.TRANSACTION_SERIALIZABLE;


	/**
	 * 使用底层事务系统的默认超时，
	 * 若不支持超时则为无超时。
	 */
	int TIMEOUT_DEFAULT = -1;


	/**
	 * 返回传播行为。
	 * <p>必须返回 {@link TransactionDefinition 本接口} 定义的
	 * {@code PROPAGATION_XXX} 常量之一。
	 * <p>默认为 {@link #PROPAGATION_REQUIRED}。
	 * @return 传播行为
	 * @see #PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	default int getPropagationBehavior() {
		return PROPAGATION_REQUIRED;
	}

	/**
	 * 返回隔离级别。
	 * <p>必须返回 {@link TransactionDefinition 本接口} 定义的
	 * {@code ISOLATION_XXX} 常量之一，其值与 {@link java.sql.Connection} 上同名常量一致。
	 * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时
	 * 拒绝隔离级别声明，可将事务管理器的 "validateExistingTransaction" 标志设为 "true"。
	 * <p>默认为 {@link #ISOLATION_DEFAULT}。不支持自定义隔离级别的事务管理器
	 * 在收到非 {@link #ISOLATION_DEFAULT} 级别时将抛出异常。
	 * @return 隔离级别
	 * @see #ISOLATION_DEFAULT
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
	 */
	default int getIsolationLevel() {
		return ISOLATION_DEFAULT;
	}

	/**
	 * 返回事务超时。
	 * <p>必须返回秒数，或 {@link #TIMEOUT_DEFAULT}。
	 * <p>专用于 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。
	 * <p>不支持超时的事务管理器在收到非 {@link #TIMEOUT_DEFAULT} 超时时将抛出异常。
	 * <p>默认为 {@link #TIMEOUT_DEFAULT}。
	 * @return 事务超时
	 */
	default int getTimeout() {
		return TIMEOUT_DEFAULT;
	}

	/**
	 * 返回是否作为只读事务优化。
	 * <p>只读标志适用于任意事务上下文，无论由实际资源事务
	 * （{@link #PROPAGATION_REQUIRED}/{@link #PROPAGATION_REQUIRES_NEW}）支持，
	 * 或在资源层非事务运行（{@link #PROPAGATION_SUPPORTS}）。
	 * 后者情况下，该标志仅适用于应用内受管资源，如 Hibernate {@code Session}。
	 * <p>这仅作为实际事务子系统的提示；<i>不必然</i>导致写访问失败。
	 * 无法解释只读提示的事务管理器在请求只读事务时<i>不会</i>抛出异常。
	 * @return 若事务应优化为只读则为 {@code true}（默认 {@code false}）
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	default boolean isReadOnly() {
		return false;
	}

	/**
	 * 返回本事务名称。可为 {@code null}。
	 * <p>若适用，将作为事务监视器中显示的事务名称。
	 * <p>对于 Spring 声明式事务，暴露的名称默认为
	 * {@code 全限定类名 + "." + 方法名}。
	 * @return 本事务名称（默认 {@code null}）
	 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionName()
	 */
	default @Nullable String getName() {
		return null;
	}


	// Static builder methods

	/**
	 * 返回带默认值的不可修改 {@code TransactionDefinition}。
	 * <p>如需定制，请改用可修改的
	 * {@link org.springframework.transaction.support.DefaultTransactionDefinition}。
	 * @since 5.2
	 */
	static TransactionDefinition withDefaults() {
		return StaticTransactionDefinition.INSTANCE;
	}

}
