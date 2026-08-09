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

/* ===== [OCA 中文解析] =====
class TransactionSynchronizationManager — 意图说明

线程级事务同步与资源绑定中心：通过 ThreadLocal 维护当前线程的事务资源、同步回调及事务特性（名称、只读、隔离级别、实际事务标志），供 JDBC/Hibernate 等资源管理与标准事务管理器协作。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
package org.springframework.transaction.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

/**
 * 按线程管理资源与事务同步的中心委托类。
 * 供资源管理代码使用，典型应用代码不应直接使用。
 *
 * <p>每个键仅支持绑定一个资源且不可覆盖，即同一键需先移除旧资源才能设置新资源。
 * 若同步已激活，则支持维护事务同步回调列表。
 *
 * <p>资源管理代码应通过 {@code getResource} 检查线程绑定资源（如 JDBC Connection 或 Hibernate Session）。
 * 此类代码通常不应自行将资源绑定到线程，这是事务管理器的职责。
 * 另一种做法是在事务同步激活后首次使用时惰性绑定，以支持跨任意数量资源的事务。
 *
 * <p>事务同步须由事务管理器通过 {@link #initSynchronization()} 与 {@link #clearSynchronization()} 激活与清理。
 * {@link AbstractPlatformTransactionManager} 及所有标准 Spring 事务管理器（如
 * {@link org.springframework.transaction.jta.JtaTransactionManager}、
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}）均自动支持。
 *
 * <p>资源管理代码仅在本管理器激活时注册同步，可通过 {@link #isSynchronizationActive} 检查；
 * 否则应立即清理资源。若事务同步未激活，要么无当前事务，要么事务管理器不支持同步。
 *
 * <p>同步机制例如在 JTA 事务内始终返回相同资源：对给定 DataSource 或 SessionFactory
 * 分别返回同一 JDBC Connection 或 Hibernate Session。
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see #isSynchronizationActive
 * @see #registerSynchronization
 * @see TransactionSynchronization
 * @see AbstractPlatformTransactionManager#setTransactionSynchronization
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 */
public abstract class TransactionSynchronizationManager {

	private static final ThreadLocal<Map<Object, Object>> resources =
			new NamedThreadLocal<>("Transactional resources");

	private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations =
			new NamedThreadLocal<>("Transaction synchronizations");

	private static final ThreadLocal<String> currentTransactionName =
			new NamedThreadLocal<>("Current transaction name");

	private static final ThreadLocal<Boolean> currentTransactionReadOnly =
			new NamedThreadLocal<>("Current transaction read-only status");

	private static final ThreadLocal<Integer> currentTransactionIsolationLevel =
			new NamedThreadLocal<>("Current transaction isolation level");

	private static final ThreadLocal<Boolean> actualTransactionActive =
			new NamedThreadLocal<>("Actual transaction active");


	//-------------------------------------------------------------------------
	// 事务关联资源句柄管理
	//-------------------------------------------------------------------------

	/**
	 * 返回绑定到当前线程的所有资源。
	 * <p>主要用于调试。资源管理器应始终对感兴趣的具体资源键调用 {@code hasResource}。
	 * @return 资源键（通常为资源工厂）到资源值（通常为活动资源对象）的 Map；
	 * 若当前无绑定资源则返回空 Map
	 * @see #hasResource
	 */
	public static Map<Object, Object> getResourceMap() {
		Map<Object, Object> map = resources.get();
		return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
	}

	/**
	 * 检查当前线程是否绑定了给定键的资源。
	 * @param key 要检查的键（通常为资源工厂）
	 * @return 当前线程是否有绑定值
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static boolean hasResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		return (value != null);
	}

	/**
	 * 获取绑定到当前线程的给定键资源。
	 * @param key 要检查的键（通常为资源工厂）
	 * @return 绑定到当前线程的值（通常为活动资源对象），无则为 {@code null}
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static @Nullable Object getResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doGetResource(actualKey);
	}

	/**
	 * 实际检查给定键所绑定资源的值。
	 */
	private static @Nullable Object doGetResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.get(actualKey);
		// Transparently remove ResourceHolder that was marked as void...
		if (value instanceof ResourceHolder resourceHolder && resourceHolder.isVoid()) {
			map.remove(actualKey);
			// Remove entire ThreadLocal if empty...
			if (map.isEmpty()) {
				resources.remove();
			}
			value = null;
		}
		return value;
	}

	/**
	 * 将给定资源以给定键绑定到当前线程。
	 * <p><b>注意：任何绑定的资源都须通过 {@link #unbindResource} 显式解绑。
	 * 若需在事务完成后自动解绑，请改用 {@link #bindSynchronizedResource}。</b>
	 * @param key 绑定值的键（通常为资源工厂）
	 * @param value 要绑定的值（通常为活动资源对象）
	 * @throws IllegalStateException 若线程上已有绑定值
	 * @see ResourceTransactionManager#getResourceFactory()
	 * @see #bindSynchronizedResource
	 */
	public static void bindResource(Object key, Object value) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object oldValue = doBindResource(actualKey, value);
		if (oldValue != null) {
			throw new IllegalStateException(
					"Already value [" + oldValue + "] for key [" + actualKey + "] bound to thread");
		}
	}

	/**
	 * 将给定资源以给定键绑定到当前线程，并与当前事务同步，以便事务完成后自动解绑。
	 * <p>这相当于以编程方式注册事务作用域资源，类似 BeanFactory 驱动的 {@link SimpleTransactionScope}。
	 * <p>若给定键已有绑定值，将在事务完成后保留并在解绑后重新绑定，恢复本次 bind 前的状态。
	 * @param key 绑定值的键（通常为资源工厂）
	 * @param value 要绑定的值（通常为活动资源对象）
	 * @throws IllegalStateException 若事务同步未激活
	 * @since 7.0
	 * @see #bindResource
	 * @see #registerSynchronization
	 */
	public static void bindSynchronizedResource(Object key, Object value) throws IllegalStateException {
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object oldValue = doBindResource(actualKey, value);
		synchs.add(new TransactionSynchronization() {
			@Override
			public void suspend() {
				doUnbindResource(actualKey);
			}
			@Override
			public void resume() {
				Object existingValue = doBindResource(actualKey, value);
				if (existingValue != null) {
					throw new IllegalStateException(
							"Unexpected value [" + existingValue + "] for key [" + actualKey + "] bound on resume");
				}
			}
			@Override
			public void afterCompletion(int status) {
				doUnbindResource(actualKey);
				if (oldValue != null) {
					doBindResource(actualKey, oldValue);
				}
			}
		});
	}

	/**
	 * 实际将给定资源以给定键绑定到当前线程。
	 */
	private static @Nullable Object doBindResource(Object actualKey, Object value) {
		Assert.notNull(value, "Value must not be null");
		Map<Object, Object> map = resources.get();
		// set ThreadLocal Map if none found
		if (map == null) {
			map = new HashMap<>();
			resources.set(map);
		}
		Object oldValue = map.put(actualKey, value);
		// Transparently suppress a ResourceHolder that was marked as void...
		if (oldValue instanceof ResourceHolder resourceHolder && resourceHolder.isVoid()) {
			oldValue = null;
		}
		return oldValue;
	}

	/**
	 * 从当前线程解绑给定键的资源。
	 * <p>此显式步骤仅在使用 {@link #bindResource} 时需要。
	 * 若需自动解绑，请考虑 {@link #bindSynchronizedResource}。
	 * @param key 要解绑的键（通常为资源工厂）
	 * @return 先前绑定的值（通常为活动资源对象）
	 * @throws IllegalStateException 若线程上无绑定值
	 * @see ResourceTransactionManager#getResourceFactory()
	 * @see #bindResource
	 * @see #unbindResourceIfPossible
	 */
	public static Object unbindResource(Object key) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doUnbindResource(actualKey);
		if (value == null) {
			throw new IllegalStateException("No value for key [" + actualKey + "] bound to thread");
		}
		return value;
	}

	/**
	 * 从当前线程解绑给定键的资源。
	 * <p>此显式步骤仅在使用 {@link #bindResource} 时需要。
	 * 若需自动解绑，请考虑 {@link #bindSynchronizedResource}。
	 * @param key 要解绑的键（通常为资源工厂）
	 * @return 先前绑定的值，无绑定则为 {@code null}
	 * @see #bindResource
	 * @see #unbindResource
	 */
	public static @Nullable Object unbindResourceIfPossible(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doUnbindResource(actualKey);
	}

	/**
	 * 实际移除给定键所绑定资源的值。
	 */
	private static @Nullable Object doUnbindResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.remove(actualKey);
		// Remove entire ThreadLocal if empty...
		if (map.isEmpty()) {
			resources.remove();
		}
		// Transparently suppress a ResourceHolder that was marked as void...
		if (value instanceof ResourceHolder resourceHolder && resourceHolder.isVoid()) {
			value = null;
		}
		return value;
	}


	//-------------------------------------------------------------------------
	// 事务同步管理
	//-------------------------------------------------------------------------

	/**
	 * 返回当前线程的事务同步是否已激活。
	 * 可在注册前调用以避免不必要的实例创建。
	 * @see #registerSynchronization
	 */
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}

	/**
	 * 为当前线程激活事务同步。
	 * 由事务管理器在事务开始时调用。
	 * @throws IllegalStateException 若同步已激活
	 */
	public static void initSynchronization() throws IllegalStateException {
		if (isSynchronizationActive()) {
			throw new IllegalStateException("Cannot activate transaction synchronization - already active");
		}
		synchronizations.set(new LinkedHashSet<>());
	}

	/**
	 * 为当前线程注册新的事务同步。
	 * 通常由资源管理代码调用。
	 * <p>注意，同步对象可实现 {@link org.springframework.core.Ordered} 接口，
	 * 将按 order 值（若有）顺序执行。
	 * @param synchronization 要注册的同步对象
	 * @throws IllegalStateException 若事务同步未激活
	 * @see org.springframework.core.Ordered
	 */
	public static void registerSynchronization(TransactionSynchronization synchronization)
			throws IllegalStateException {

		Assert.notNull(synchronization, "TransactionSynchronization must not be null");
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		synchs.add(synchronization);
	}

	/**
	 * 返回当前线程所有已注册同步的不可修改快照列表。
	 * @return TransactionSynchronization 实例的不可修改 List
	 * @throws IllegalStateException 若同步未激活
	 * @see TransactionSynchronization
	 */
	public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		// 返回不可修改快照，避免在迭代并调用同步回调时
		// 发生 ConcurrentModificationException（回调可能注册更多同步）。
		if (synchs.isEmpty()) {
			return Collections.emptyList();
		}
		else if (synchs.size() == 1) {
			return Collections.singletonList(synchs.iterator().next());
		}
		else {
			// 在此惰性排序，而非在 registerSynchronization 中。
			List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
			OrderComparator.sort(sortedSynchs);
			return Collections.unmodifiableList(sortedSynchs);
		}
	}

	/**
	 * 为当前线程停用事务同步。
	 * 由事务管理器在事务清理时调用。
	 * @throws IllegalStateException 若同步未激活
	 */
	public static void clearSynchronization() throws IllegalStateException {
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
		}
		synchronizations.remove();
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
	public static void setCurrentTransactionName(@Nullable String name) {
		currentTransactionName.set(name);
	}

	/**
	 * 返回当前事务名称，未设置则为 {@code null}。
	 * 供资源管理代码按用例优化，例如为特定命名事务优化 fetch 策略。
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public static @Nullable String getCurrentTransactionName() {
		return currentTransactionName.get();
	}

	/**
	 * 暴露当前事务的只读标志。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param readOnly {@code true} 将当前事务标记为只读；{@code false} 重置只读标记
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 */
	public static void setCurrentTransactionReadOnly(boolean readOnly) {
		currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
	}

	/**
	 * 返回当前事务是否标记为只读。
	 * 供资源管理代码在准备新创建资源（如 Hibernate Session）时调用。
	 * <p>注意，事务同步在 {@code beforeCommit} 回调中接收只读标志，
	 * 以便在提交时抑制变更检测。本方法用于更早的只读检查，
	 * 例如预先设置 Hibernate Session 的 flush 模式为 "FlushMode.MANUAL"。
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public static boolean isCurrentTransactionReadOnly() {
		return (currentTransactionReadOnly.get() != null);
	}

	/**
	 * 暴露当前事务的隔离级别。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param isolationLevel 要暴露的隔离级别，按 JDBC Connection 常量
	 * （等同于相应 Spring TransactionDefinition 常量），或 {@code null} 重置
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static void setCurrentTransactionIsolationLevel(@Nullable Integer isolationLevel) {
		currentTransactionIsolationLevel.set(isolationLevel);
	}

	/**
	 * 返回当前事务的隔离级别（若有）。
	 * 供资源管理代码在准备新创建资源（如 JDBC Connection）时调用。
	 * @return 当前暴露的隔离级别，按 JDBC Connection 常量
	 * （等同于相应 Spring TransactionDefinition 常量），无则为 {@code null}
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static @Nullable Integer getCurrentTransactionIsolationLevel() {
		return currentTransactionIsolationLevel.get();
	}

	/**
	 * 暴露当前是否存在实际活动事务。
	 * 由事务管理器在事务开始和清理时调用。
	 * @param active {@code true} 将当前线程标记为关联实际事务；{@code false} 重置该标记
	 */
	public static void setActualTransactionActive(boolean active) {
		actualTransactionActive.set(active ? Boolean.TRUE : null);
	}

	/**
	 * 返回当前是否存在实际活动事务。
	 * 表示当前线程是否关联实际事务，而非仅关联活动的事务同步。
	 * <p>供资源管理代码区分活动事务同步（有无底层资源事务；
	 * 在 PROPAGATION_SUPPORTS 下也有）与实际活动事务（有底层资源事务；
	 * 在 PROPAGATION_REQUIRED、PROPAGATION_REQUIRES_NEW 等下）。
	 * @see #isSynchronizationActive()
	 */
	public static boolean isActualTransactionActive() {
		return (actualTransactionActive.get() != null);
	}


	/**
	 * 清除当前线程的整个事务同步状态：
	 * 已注册同步以及各种事务特性。
	 * @see #clearSynchronization()
	 * @see #setCurrentTransactionName
	 * @see #setCurrentTransactionReadOnly
	 * @see #setCurrentTransactionIsolationLevel
	 * @see #setActualTransactionActive
	 */
	public static void clear() {
		synchronizations.remove();
		currentTransactionName.remove();
		currentTransactionReadOnly.remove();
		currentTransactionIsolationLevel.remove();
		actualTransactionActive.remove();
	}

}
