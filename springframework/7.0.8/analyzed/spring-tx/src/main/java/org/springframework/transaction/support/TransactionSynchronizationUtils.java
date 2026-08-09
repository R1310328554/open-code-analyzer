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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.scope.ScopedObject;
import org.springframework.core.InfrastructureProxy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 用于在所有当前已注册同步上触发特定 {@link TransactionSynchronization}
 * 回调方法的工具方法。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see TransactionSynchronization
 * @see TransactionSynchronizationManager#getSynchronizations()
 */
public abstract class TransactionSynchronizationUtils {

	private static final Log logger = LogFactory.getLog(TransactionSynchronizationUtils.class);

	private static final boolean SPRING_AOP_PRESENT = ClassUtils.isPresent(
			"org.springframework.aop.scope.ScopedObject", TransactionSynchronizationUtils.class.getClassLoader());


	/**
	 * 检查给定资源事务管理器是否指向给定（底层）资源工厂。
	 * @see ResourceTransactionManager#getResourceFactory()
	 * @see InfrastructureProxy#getWrappedObject()
	 */
	public static boolean sameResourceFactory(ResourceTransactionManager tm, Object resourceFactory) {
		return unwrapResourceIfNecessary(tm.getResourceFactory()).equals(unwrapResourceIfNecessary(resourceFactory));
	}

	/**
	 * 必要时解包给定资源句柄；否则原样返回。
	 * @since 5.3.4
	 * @see InfrastructureProxy#getWrappedObject()
	 */
	public static Object unwrapResourceIfNecessary(Object resource) {
		Assert.notNull(resource, "Resource must not be null");
		Object resourceRef = resource;
		// 解包基础设施代理
		if (resourceRef instanceof InfrastructureProxy infrastructureProxy) {
			resourceRef = infrastructureProxy.getWrappedObject();
		}
		if (SPRING_AOP_PRESENT) {
			// 再解包作用域代理
			resourceRef = ScopedProxyUnwrapper.unwrapIfNecessary(resourceRef);
		}
		return resourceRef;
	}


	/**
	 * 在所有当前已注册同步上触发 {@code flush} 回调。
	 * @throws RuntimeException 若 {@code flush} 回调抛出
	 * @see TransactionSynchronization#flush()
	 */
	public static void triggerFlush() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.flush();
			}
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code savepoint} 回调。
	 * @throws RuntimeException 若 {@code savepoint} 回调抛出
	 * @since 6.2
	 * @see TransactionSynchronization#savepoint
	 */
	static void triggerSavepoint(Object savepoint) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.savepoint(savepoint);
			}
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code savepointRollback} 回调。
	 * @throws RuntimeException 若 {@code savepointRollback} 回调抛出
	 * @since 6.2
	 * @see TransactionSynchronization#savepointRollback
	 */
	static void triggerSavepointRollback(Object savepoint) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.savepointRollback(savepoint);
			}
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code beforeCommit} 回调。
	 * @param readOnly 事务是否定义为只读事务
	 * @throws RuntimeException 若 {@code beforeCommit} 回调抛出
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public static void triggerBeforeCommit(boolean readOnly) {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.beforeCommit(readOnly);
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code beforeCompletion} 回调。
	 * @see TransactionSynchronization#beforeCompletion()
	 */
	public static void triggerBeforeCompletion() {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			try {
				synchronization.beforeCompletion();
			}
			catch (Throwable ex) {
				logger.error("TransactionSynchronization.beforeCompletion threw exception", ex);
			}
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code afterCommit} 回调。
	 * @throws RuntimeException 若 {@code afterCommit} 回调抛出
	 * @see TransactionSynchronizationManager#getSynchronizations()
	 * @see TransactionSynchronization#afterCommit()
	 */
	public static void triggerAfterCommit() {
		invokeAfterCommit(TransactionSynchronizationManager.getSynchronizations());
	}

	/**
	 * 实际调用给定 Spring TransactionSynchronization 对象的
	 * {@code afterCommit} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @see TransactionSynchronization#afterCommit()
	 */
	public static void invokeAfterCommit(@Nullable List<TransactionSynchronization> synchronizations) {
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				synchronization.afterCommit();
			}
		}
	}

	/**
	 * 在所有当前已注册同步上触发 {@code afterCompletion} 回调。
	 * @param completionStatus 根据 TransactionSynchronization 接口中
	 * 常量表示的完成状态
	 * @see TransactionSynchronizationManager#getSynchronizations()
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	public static void triggerAfterCompletion(int completionStatus) {
		List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
		invokeAfterCompletion(synchronizations, completionStatus);
	}

	/**
	 * 实际调用给定 Spring TransactionSynchronization 对象的
	 * {@code afterCompletion} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @param completionStatus 根据 TransactionSynchronization 接口中
	 * 常量表示的完成状态
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	public static void invokeAfterCompletion(@Nullable List<TransactionSynchronization> synchronizations,
			int completionStatus) {

		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(completionStatus);
				}
				catch (Throwable ex) {
					logger.error("TransactionSynchronization.afterCompletion threw exception", ex);
				}
			}
		}
	}


	/**
	 * 内部类，避免对 AOP 模块的硬编码依赖。
	 */
	private static class ScopedProxyUnwrapper {

		public static Object unwrapIfNecessary(Object resource) {
			if (resource instanceof ScopedObject scopedObject) {
				return scopedObject.getTargetObject();
			}
			else {
				return resource;
			}
		}
	}

}
