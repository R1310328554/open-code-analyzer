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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.aop.scope.ScopedObject;
import org.springframework.core.InfrastructureProxy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 在所有当前已注册同步上触发特定 {@link TransactionSynchronization} 回调方法的工具类。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @see TransactionSynchronization
 * @see TransactionSynchronizationManager#getSynchronizations()
 */
abstract class TransactionSynchronizationUtils {

	private static final Log logger = LogFactory.getLog(TransactionSynchronizationUtils.class);

	private static final boolean SPRING_AOP_PRESENT = ClassUtils.isPresent(
			"org.springframework.aop.scope.ScopedObject", TransactionSynchronizationUtils.class.getClassLoader());


	/**
	 * 必要时解包给定资源句柄；否则原样返回。
	 * @see InfrastructureProxy#getWrappedObject()
	 */
	static Object unwrapResourceIfNecessary(Object resource) {
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
	 * 实际调用给定 Spring TransactionSynchronization 对象的 {@code triggerBeforeCommit} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public static Mono<Void> triggerBeforeCommit(Collection<TransactionSynchronization> synchronizations, boolean readOnly) {
		return Flux.fromIterable(synchronizations).concatMap(it -> it.beforeCommit(readOnly)).then();
	}

	/**
	 * 实际调用给定 Spring TransactionSynchronization 对象的 {@code beforeCompletion} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @see TransactionSynchronization#beforeCompletion()
	 */
	public static Mono<Void> triggerBeforeCompletion(Collection<TransactionSynchronization> synchronizations) {
		return Flux.fromIterable(synchronizations)
				.concatMap(TransactionSynchronization::beforeCompletion).onErrorContinue((t, o) ->
						logger.error("TransactionSynchronization.beforeCompletion threw exception", t)).then();
	}

	/**
	 * 实际调用给定 Spring TransactionSynchronization 对象的 {@code afterCommit} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @see TransactionSynchronization#afterCommit()
	 */
	public static Mono<Void> invokeAfterCommit(Collection<TransactionSynchronization> synchronizations) {
		return Flux.fromIterable(synchronizations)
				.concatMap(TransactionSynchronization::afterCommit)
				.then();
	}

	/**
	 * 实际调用给定 Spring TransactionSynchronization 对象的 {@code afterCompletion} 方法。
	 * @param synchronizations TransactionSynchronization 对象列表
	 * @param completionStatus 按 TransactionSynchronization 接口常量的完成状态
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	public static Mono<Void> invokeAfterCompletion(
			Collection<TransactionSynchronization> synchronizations, int completionStatus) {

		return Flux.fromIterable(synchronizations).concatMap(it -> it.afterCompletion(completionStatus))
				.onErrorContinue((t, o) -> logger.error("TransactionSynchronization.afterCompletion threw exception", t)).then();
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
