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

import reactor.core.publisher.Mono;

/**
 * 管理通过 {@link TransactionSynchronizationManager} 绑定的资源对象的
 * {@link TransactionSynchronization} 实现。
 *
 * @author Mark Paluch
 * @author Juergen Hoeller
 * @since 5.2
 * @param <O> 资源持有者类型
 * @param <K> 资源键类型
 */
public abstract class ReactiveResourceSynchronization<O, K> implements TransactionSynchronization {

	private final O resourceObject;

	private final K resourceKey;

	private final TransactionSynchronizationManager synchronizationManager;

	private volatile boolean holderActive = true;


	/**
	 * 为给定持有者创建新的 ReactiveResourceSynchronization。
	 * @param resourceObject 要管理的资源对象
	 * @param resourceKey 绑定资源对象的键
	 * @param synchronizationManager 绑定到当前事务的同步管理器
	 * @see TransactionSynchronizationManager#bindResource
	 */
	public ReactiveResourceSynchronization(
			O resourceObject, K resourceKey, TransactionSynchronizationManager synchronizationManager) {

		this.resourceObject = resourceObject;
		this.resourceKey = resourceKey;
		this.synchronizationManager = synchronizationManager;
	}


	@Override
	public Mono<Void> suspend() {
		if (this.holderActive) {
			this.synchronizationManager.unbindResource(this.resourceKey);
		}
		return Mono.empty();
	}

	@Override
	public Mono<Void> resume() {
		if (this.holderActive) {
			this.synchronizationManager.bindResource(this.resourceKey, this.resourceObject);
		}
		return Mono.empty();
	}

	@Override
	public Mono<Void> beforeCommit(boolean readOnly) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> beforeCompletion() {
		if (shouldUnbindAtCompletion()) {
			this.synchronizationManager.unbindResource(this.resourceKey);
			this.holderActive = false;
			if (shouldReleaseBeforeCompletion()) {
				return releaseResource(this.resourceObject, this.resourceKey);
			}
		}
		return Mono.empty();
	}

	@Override
	public Mono<Void> afterCommit() {
		if (!shouldReleaseBeforeCompletion()) {
			return processResourceAfterCommit(this.resourceObject);
		}
		return Mono.empty();
	}

	@Override
	public Mono<Void> afterCompletion(int status) {
		return Mono.defer(() -> {
			Mono<Void> sync = Mono.empty();
			if (shouldUnbindAtCompletion()) {
				boolean releaseNecessary = false;
				if (this.holderActive) {
					// 线程绑定的资源持有者可能已不可用，
					// 因为 afterCompletion 可能从不同线程调用。
					this.holderActive = false;
					this.synchronizationManager.unbindResourceIfPossible(this.resourceKey);
					releaseNecessary = true;
				}
				else {
					releaseNecessary = shouldReleaseAfterCompletion(this.resourceObject);
				}
				if (releaseNecessary) {
					sync = releaseResource(this.resourceObject, this.resourceKey);
				}
			}
			else {
				// 可能是预绑定的资源...
				sync = cleanupResource(this.resourceObject, this.resourceKey, (status == STATUS_COMMITTED));
			}
			return sync;
		});
	}


	/**
	 * 返回本持有者是否应在完成时解绑
	 * （或事务后仍保留绑定到线程）。
	 * <p>默认实现返回 {@code true}。
	 */
	protected boolean shouldUnbindAtCompletion() {
		return true;
	}

	/**
	 * 返回本持有者的资源是否应在事务完成前释放（{@code true}）
	 * 或在事务完成后释放（{@code false}）。
	 * <p>注意，资源仅在与线程解绑时（{@link #shouldUnbindAtCompletion()}）才会释放。
	 * <p>默认实现返回 {@code true}。
	 * @see #releaseResource
	 */
	protected boolean shouldReleaseBeforeCompletion() {
		return true;
	}

	/**
	 * 返回本持有者的资源是否应在事务完成后释放（{@code true}）。
	 * <p>默认实现返回 {@code !shouldReleaseBeforeCompletion()}，
	 * 若完成前未尝试释放则在完成后释放。
	 * @see #releaseResource
	 */
	protected boolean shouldReleaseAfterCompletion(O resourceHolder) {
		return !shouldReleaseBeforeCompletion();
	}

	/**
	 * 给定资源持有者的提交后回调。
	 * 仅在资源尚未释放时调用（{@link #shouldReleaseBeforeCompletion()}）。
	 * @param resourceHolder 要处理的资源持有者
	 */
	protected Mono<Void> processResourceAfterCommit(O resourceHolder) {
		return Mono.empty();
	}

	/**
	 * 释放给定资源（在与线程解绑之后）。
	 * @param resourceHolder 要处理的资源持有者
	 * @param resourceKey 资源对象绑定的键
	 */
	protected Mono<Void> releaseResource(O resourceHolder, K resourceKey) {
		return Mono.empty();
	}

	/**
	 * 对给定资源执行清理（资源仍绑定到线程）。
	 * @param resourceHolder 要处理的资源持有者
	 * @param resourceKey 资源对象绑定的键
	 * @param committed 事务是否已提交（{@code true}）或已回滚（{@code false}）
	 */
	protected Mono<Void> cleanupResource(O resourceHolder, K resourceKey, boolean committed) {
		return Mono.empty();
	}

}
