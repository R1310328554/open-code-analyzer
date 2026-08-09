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

package org.springframework.transaction.reactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.transaction.NoTransactionException;
import org.springframework.util.Assert;

/**
 * 按订阅者上下文管理资源与事务同步的中心委托。
 * 供资源管理代码使用，非典型应用代码。
 *
 * <p>每个键支持一个资源且不可覆盖，即同一键需先移除资源才能设置新资源。
 * 若同步已激活，支持事务同步列表。
 *
 * <p>资源管理代码应通过 {@code getResource} 检查上下文绑定资源（如数据库连接）。
 * 此类代码通常不应将资源绑定到工作单元，这是事务管理器的职责。
 * 另一选择是在事务同步激活时首次使用时惰性绑定，以执行跨任意数量资源的事务。
 *
 * <p>事务同步须由事务管理器通过 {@link #initSynchronization()} 和
 * {@link #clearSynchronization()} 激活与停用。
 * {@link AbstractReactiveTransactionManager} 自动支持，因此所有标准 Spring 事务管理器均支持。
 *
 * <p>资源管理代码仅在本管理器激活时注册同步，可通过 {@link #isSynchronizationActive} 检查；
 * 否则应立即清理资源。若事务同步未激活，要么无当前事务，要么事务管理器不支持事务同步。
 *
 * <p>同步例如用于在事务内始终返回相同资源，
 * 如对给定连接工厂始终返回同一数据库连接。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see #isSynchronizationActive
 * @see #registerSynchronization
 * @see TransactionSynchronization
 */
public class TransactionSynchronizationManager {

	private final TransactionContext transactionContext;


	public TransactionSynchronizationManager(TransactionContext transactionContext) {
		Assert.notNull(transactionContext, "TransactionContext must not be null");
		this.transactionContext = transactionContext;
	}


	/**
	 * 获取与当前事务上下文关联的 {@link TransactionSynchronizationManager}。
	 * <p>主要供需要绑定资源或同步的代码使用。
	 * @throws NoTransactionException 若找不到事务信息——
	 * 例如因在受管事务外调用方法
	 */
	public static Mono<TransactionSynchronizationManager> forCurrentTransaction() {
		return TransactionContextManager.currentContext().map(TransactionSynchronizationManager::new);
	}


	/**
	 * 检查当前上下文是否绑定了给定键的资源。
	 * @param key 要检查的键（通常为资源工厂）
	 * @return 当前上下文是否有绑定值
	 */
	public boolean hasResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		return (value != null);
	}

	/**
	 * 获取绑定到当前上下文的给定键资源。
	 * @param key 要检查的键（通常为资源工厂）
	 * @return 绑定到当前上下文的值（通常为活动资源对象），无则为 {@code null}
	 */
	public @Nullable Object getResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doGetResource(actualKey);
	}

	/**
	 * 实际检查给定键绑定资源的值。
	 */
	private @Nullable Object doGetResource(Object actualKey) {
		return this.transactionContext.getResources().get(actualKey);
	}

	/**
	 * 将给定资源以给定键绑定到当前上下文。
	 * @param key 绑定值的键（通常为资源工厂）
	 * @param value 要绑定的值（通常为活动资源对象）
	 * @throws IllegalStateException 若上下文已有绑定值
	 */
	public void bindResource(Object key, Object value) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Assert.notNull(value, "Value must not be null");
		Map<Object, Object> map = this.transactionContext.getResources();
		Object oldValue = map.put(actualKey, value);
		if (oldValue != null) {
			throw new IllegalStateException(
					"Already value [" + oldValue + "] for key [" + actualKey + "] bound to context");
		}
	}

	/**
	 * 从当前上下文解绑给定键的资源。
	 * @param key 要解绑的键（通常为资源工厂）
	 * @return 先前绑定的值（通常为活动资源对象）
	 * @throws IllegalStateException 若上下文无绑定值
	 */
	public Object unbindResource(Object key) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doUnbindResource(actualKey);
		if (value == null) {
			throw new IllegalStateException("No value for key [" + actualKey + "] bound to context");
		}
		return value;
	}

	/**
	 * 从当前上下文解绑给定键的资源。
	 * @param key 要解绑的键（通常为资源工厂）
	 * @return 先前绑定的值，无绑定则为 {@code null}
	 */
	public @Nullable Object unbindResourceIfPossible(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doUnbindResource(actualKey);
	}

	/**
	 * 实际移除给定键绑定资源的值。
	 */
	private @Nullable Object doUnbindResource(Object actualKey) {
		Map<Object, Object> map = this.transactionContext.getResources();
		return map.remove(actualKey);
	}


	//-------------------------------------------------------------------------
	// 事务同步管理
	//-------------------------------------------------------------------------

	/**
	 * 返回当前上下文的事务同步是否激活。
	 * 可在注册前调用以避免不必要的实例创建。
	 * @see #registerSynchronization
	 */
	public boolean isSynchronizationActive() {
		return (this.transactionContext.getSynchronizations() != null);
	}

	/**
	 * 为当前上下文激活事务同步。
	 * 由事务管理器在事务开始时调用。
	 * @throws IllegalStateException 若同步已激活
	 */
	public void initSynchronization() throws IllegalStateException {
		if (isSynchronizationActive()) {
			throw new IllegalStateException("Cannot activate transaction synchronization - already active");
		}
		this.transactionContext.setSynchronizations(new CopyOnWriteArraySet<>());
	}

	/**
	 * 为当前上下文注册新事务同步。
	 * 通常由资源管理代码调用。
	 * <p>注意，同步可实现 {@link org.springframework.core.Ordered} 接口，
	 * 将按 order 值（若有）顺序执行。
	 * @param synchronization 要注册的同步对象
	 * @throws IllegalStateException 若事务同步未激活
	 * @see org.springframework.core.Ordered
	 */
	public void registerSynchronization(TransactionSynchronization synchronization)
			throws IllegalStateException {

		Assert.notNull(synchronization, "TransactionSynchronization must not be null");
		Set<TransactionSynchronization> synchs = this.transactionContext.getSynchronizations();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		synchs.add(synchronization);
	}

	/**
	 * 返回当前上下文所有已注册同步的不可修改快照列表。
	 * @return TransactionSynchronization 实例的不可修改 List
	 * @throws IllegalStateException 若同步未激活
	 * @see TransactionSynchronization
	 */
	public List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
		Set<TransactionSynchronization> synchs = this.transactionContext.getSynchronizations();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		// 返回不可修改快照，避免在迭代并调用同步回调时
		// 发生 ConcurrentModificationException（回调可能注册更多同步）。
		if (synchs.isEmpty()) {
			return Collections.emptyList();
		}
		else {
			// 在此惰性排序，而非在 registerSynchronization 中。
			List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
			AnnotationAwareOrderComparator.sort(sortedSynchs);
			return Collections.unmodifiableList(sortedSynchs);
		}
	}

	/**
	 * 为当前上下文停用事务同步。
	 * 由事务管理器在事务清理时调用。
	 * @throws IllegalStateException 若同步未激活
	 */
	public void clearSynchronization() throws IllegalStateException {
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
		}
		this.transactionContext.setSynchronizations(null);
	}


	//-------------------------------------------------------------------------
	// 暴露事务特性
	//-------------------------------------------------------------------------

	/**
	 * 暴露当前事务名称（若有）。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param name 事务名称，或 {@code null} 重置
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public void setCurrentTransactionName(@Nullable String name) {
		this.transactionContext.setCurrentTransactionName(name);
	}

	/**
	 * 返回当前事务名称，未设置则为 {@code null}。
	 * 供资源管理代码按用例优化，例如为特定命名事务优化 fetch 策略。
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public @Nullable String getCurrentTransactionName() {
		return this.transactionContext.getCurrentTransactionName();
	}

	/**
	 * 暴露当前事务的只读标志。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param readOnly {@code true} 将当前事务标记为只读；{@code false} 重置只读标记
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 */
	public void setCurrentTransactionReadOnly(boolean readOnly) {
		this.transactionContext.setCurrentTransactionReadOnly(readOnly);
	}

	/**
	 * 返回当前事务是否标记为只读。
	 * 供资源管理代码在准备新创建资源时调用。
	 * <p>注意，事务同步在 {@code beforeCommit} 回调中接收只读标志，
	 * 以便在提交时抑制变更检测。本方法用于更早的只读检查。
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public boolean isCurrentTransactionReadOnly() {
		return this.transactionContext.isCurrentTransactionReadOnly();
	}

	/**
	 * 暴露当前事务的隔离级别。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param isolationLevel 要暴露的隔离级别，按 R2DBC Connection 常量
	 * （等同于相应 Spring TransactionDefinition 常量），或 {@code null} 重置
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public void setCurrentTransactionIsolationLevel(@Nullable Integer isolationLevel) {
		this.transactionContext.setCurrentTransactionIsolationLevel(isolationLevel);
	}

	/**
	 * 返回当前事务的隔离级别（若有）。
	 * 供资源管理代码在准备新创建资源（如 R2DBC Connection）时调用。
	 * @return 当前暴露的隔离级别，按 R2DBC Connection 常量
	 * （等同于相应 Spring TransactionDefinition 常量），无则为 {@code null}
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public @Nullable Integer getCurrentTransactionIsolationLevel() {
		return this.transactionContext.getCurrentTransactionIsolationLevel();
	}

	/**
	 * 暴露当前是否有实际活动事务。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param active {@code true} 将当前上下文标记为关联实际事务；{@code false} 重置该标记
	 */
	public void setActualTransactionActive(boolean active) {
		this.transactionContext.setActualTransactionActive(active);
	}

	/**
	 * 返回当前是否有实际活动事务。
	 * 表示当前上下文是否关联实际事务，而非仅关联活动事务同步。
	 * <p>供资源管理代码区分活动事务同步（有无底层资源事务；
	 * 在 PROPAGATION_SUPPORTS 下也有）与实际活动事务（有底层资源事务；
	 * 在 PROPAGATION_REQUIRED、PROPAGATION_REQUIRES_NEW 等下）。
	 * @see #isSynchronizationActive()
	 */
	public boolean isActualTransactionActive() {
		return this.transactionContext.isActualTransactionActive();
	}

	/**
	 * 清除整个事务同步状态：
	 * 已注册同步以及各种事务特性。
	 * @see #clearSynchronization()
	 * @see #setCurrentTransactionName
	 * @see #setCurrentTransactionReadOnly
	 * @see #setCurrentTransactionIsolationLevel
	 * @see #setActualTransactionActive
	 */
	public void clear() {
		this.transactionContext.clear();
	}

}
