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

package org.springframework.aop.target.dynamic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;

/**
 * 包装可刷新目标对象的抽象 {@link org.springframework.aop.TargetSource} 实现。子类可以判断是否需要刷新，以及是否需要提供新鲜的目标对
 * 象。
 * <p>I 实现 {@link Refreshable} 接口，以便允许对刷新状态进行显式控制。
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #requiresRefresh()
 * @see #freshTarget()
 */
public abstract class AbstractRefreshableTargetSource implements TargetSource, Refreshable {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 目标相关状态（`targetObject`）。 */
	@SuppressWarnings("NullAway.Init")
	protected Object targetObject;

	private long refreshCheckDelay = -1;

	private long lastRefreshCheck = -1;

	private long lastRefreshTime = -1;

	private long refreshCount = 0;


	/**
	 * 设置刷新检查之间的延迟（以毫秒为单位）。默认值为 -1，表示根本不进行刷新检查。 <p>请注意，仅当 {@link #requiresRefresh()} 返回 {@code 
	 * true} 时才会发生实际刷新。
	 */
	public void setRefreshCheckDelay(long refreshCheckDelay) {
		this.refreshCheckDelay = refreshCheckDelay;
	}


	/**
	 * 获取 Target Class（`TargetClass`）。
	 */
	@Override
	public synchronized Class<?> getTargetClass() {
		if (this.targetObject == null) {
			refresh();
		}
		return this.targetObject.getClass();
	}

	/**
	 * 获取 Target（`Target`）。
	 */
	@Override
	public final synchronized @Nullable Object getTarget() {
		if ((refreshCheckDelayElapsed() && requiresRefresh()) || this.targetObject == null) {
			refresh();
		}
		return this.targetObject;
	}


	/**
	 * 刷新（方法 `refresh`）。
	 */
	@Override
	public final synchronized void refresh() {
		logger.debug("Attempting to refresh target");

		this.targetObject = freshTarget();
		this.refreshCount++;
		this.lastRefreshTime = System.currentTimeMillis();

		logger.debug("Target refreshed successfully");
	}

	/**
	 * 获取 Refresh Count（`RefreshCount`）。
	 */
	@Override
	public synchronized long getRefreshCount() {
		return this.refreshCount;
	}

	/**
	 * 获取 Last Refresh Time（`LastRefreshTime`）。
	 */
	@Override
	public synchronized long getLastRefreshTime() {
		return this.lastRefreshTime;
	}


	/**
	 * 刷新：Check Delay Elapsed（方法 `refreshCheckDelayElapsed`）。
	 */
	private boolean refreshCheckDelayElapsed() {
		if (this.refreshCheckDelay < 0) {
			return false;
		}

		long currentTimeMillis = System.currentTimeMillis();

		if (this.lastRefreshCheck < 0 || currentTimeMillis - this.lastRefreshCheck > this.refreshCheckDelay) {
			// 将执行刷新检查 - 更新时间戳。
			this.lastRefreshCheck = currentTimeMillis;
			logger.debug("Refresh check delay elapsed - checking whether refresh is required");
			return true;
		}

		return false;
	}


	/**
	 * 判断是否需要刷新。在刷新检查延迟过后，为每次刷新检查调用。 <p>默认实现总是返回{@code true}，每次延迟过去后都会触发刷新。通过对底层目标资源进行适当检查，由子类覆
	 * 盖。
	 * @return 需要刷新
	 */
	protected boolean requiresRefresh() {
		return true;
	}

	/**
	 * 获取新的目标对象。 <p>仅在刷新检查发现需要刷新时调用（即 {@link #requiresRefresh()} 已返回 {@code true}）。
	 * @return 新鲜的目标对象
	 */
	protected abstract Object freshTarget();

}
