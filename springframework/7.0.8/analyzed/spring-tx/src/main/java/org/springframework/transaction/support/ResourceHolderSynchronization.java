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

/**
 * 通过 {@link TransactionSynchronizationManager} 管理已绑定
 * {@link ResourceHolder} 的 {@link TransactionSynchronization} 实现。
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 * @param <H> 资源持有者类型
 * @param <K> 资源键类型
 */
public abstract class ResourceHolderSynchronization<H extends ResourceHolder, K>
		implements TransactionSynchronization {

	private final H resourceHolder;

	private final K resourceKey;

	private volatile boolean holderActive = true;


	/**
	 * 为给定持有者创建新的 ResourceHolderSynchronization。
	 * @param resourceHolder 要管理的 ResourceHolder
	 * @param resourceKey 绑定 ResourceHolder 所用的键
	 * @see TransactionSynchronizationManager#bindResource
	 */
	public ResourceHolderSynchronization(H resourceHolder, K resourceKey) {
		this.resourceHolder = resourceHolder;
		this.resourceKey = resourceKey;
	}


	@Override
	public void suspend() {
		if (this.holderActive) {
			TransactionSynchronizationManager.unbindResource(this.resourceKey);
		}
	}

	@Override
	public void resume() {
		if (this.holderActive) {
			TransactionSynchronizationManager.bindResource(this.resourceKey, this.resourceHolder);
		}
	}

	@Override
	public void flush() {
		flushResource(this.resourceHolder);
	}

	@Override
	public void beforeCommit(boolean readOnly) {
	}

	@Override
	public void beforeCompletion() {
		if (shouldUnbindAtCompletion()) {
			TransactionSynchronizationManager.unbindResource(this.resourceKey);
			this.holderActive = false;
			if (shouldReleaseBeforeCompletion()) {
				releaseResource(this.resourceHolder, this.resourceKey);
			}
		}
	}

	@Override
	public void afterCommit() {
		if (!shouldReleaseBeforeCompletion()) {
			processResourceAfterCommit(this.resourceHolder);
		}
	}

	@Override
	public void afterCompletion(int status) {
		if (shouldUnbindAtCompletion()) {
			boolean releaseNecessary = false;
			if (this.holderActive) {
				// 线程绑定的资源持有者可能已不可用，
				// 因为 afterCompletion 可能由不同线程调用。
				this.holderActive = false;
				TransactionSynchronizationManager.unbindResourceIfPossible(this.resourceKey);
				this.resourceHolder.unbound();
				releaseNecessary = true;
			}
			else {
				releaseNecessary = shouldReleaseAfterCompletion(this.resourceHolder);
			}
			if (releaseNecessary) {
				releaseResource(this.resourceHolder, this.resourceKey);
			}
		}
		else {
			// 可能是预先绑定的资源……
			cleanupResource(this.resourceHolder, this.resourceKey, (status == STATUS_COMMITTED));
		}
		this.resourceHolder.reset();
	}


	/**
	 * 返回此持有者是否应在完成时解绑
	 * （或在事务结束后仍保留在线程上）。
	 * <p>默认实现返回 {@code true}。
	 */
	protected boolean shouldUnbindAtCompletion() {
		return true;
	}

	/**
	 * 返回此持有者的资源是否应在事务完成前释放（{@code true}）
	 * 或在事务完成后释放（{@code false}）。
	 * <p>注意：仅当资源从线程解绑时才会释放
	 * （{@link #shouldUnbindAtCompletion()}）。
	 * <p>默认实现返回 {@code true}。
	 * @see #releaseResource
	 */
	protected boolean shouldReleaseBeforeCompletion() {
		return true;
	}

	/**
	 * 返回此持有者的资源是否应在事务完成后释放（{@code true}）。
	 * <p>默认实现返回 {@code !shouldReleaseBeforeCompletion()}，
	 * 若完成前未尝试释放则在完成后释放。
	 * @see #releaseResource
	 */
	protected boolean shouldReleaseAfterCompletion(H resourceHolder) {
		return !shouldReleaseBeforeCompletion();
	}

	/**
	 * 给定资源持有者的 flush 回调。
	 * @param resourceHolder 要 flush 的资源持有者
	 */
	protected void flushResource(H resourceHolder) {
	}

	/**
	 * 给定资源持有者的 after-commit 回调。
	 * 仅在资源尚未释放时调用
	 * （{@link #shouldReleaseBeforeCompletion()}）。
	 * @param resourceHolder 要处理的资源持有者
	 */
	protected void processResourceAfterCommit(H resourceHolder) {
	}

	/**
	 * 释放给定资源（在从线程解绑之后）。
	 * @param resourceHolder 要处理的资源持有者
	 * @param resourceKey ResourceHolder 绑定所用的键
	 */
	protected void releaseResource(H resourceHolder, K resourceKey) {
	}

	/**
	 * 对给定资源执行清理（资源仍保留在线程绑定中）。
	 * @param resourceHolder 要处理的资源持有者
	 * @param resourceKey ResourceHolder 绑定所用的键
	 * @param committed 事务是否已提交（{@code true}）或已回滚（{@code false}）
	 */
	protected void cleanupResource(H resourceHolder, K resourceKey, boolean committed) {
	}

}
