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
 * 包装可刷新目标对象的 {@link org.springframework.aop.TargetSource} 抽象实现。
 * 子类可判定是否需要刷新，并须提供新目标对象。
 *
 * <p>实现 {@link Refreshable} 接口以允许显式控制刷新状态。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #requiresRefresh()
 * @see #freshTarget()
 */
public abstract class AbstractRefreshableTargetSource implements TargetSource, Refreshable {

	/** 供子类使用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	@SuppressWarnings("NullAway.Init")
	protected Object targetObject;

	private long refreshCheckDelay = -1;

	private long lastRefreshCheck = -1;

	private long lastRefreshTime = -1;

	private long refreshCount = 0;


	/**
	 * 设置刷新检查间隔（毫秒）。
	 * 默认为 -1，表示不进行任何刷新检查。
	 * <p>注意：仅当 {@link #requiresRefresh()} 返回 {@code true} 时才会实际刷新。
	 */
	public void setRefreshCheckDelay(long refreshCheckDelay) {
		this.refreshCheckDelay = refreshCheckDelay;
	}


	@Override
	public synchronized Class<?> getTargetClass() {
		if (this.targetObject == null) {
			refresh();
		}
		return this.targetObject.getClass();
	}

	@Override
	public final synchronized @Nullable Object getTarget() {
		if ((refreshCheckDelayElapsed() && requiresRefresh()) || this.targetObject == null) {
			refresh();
		}
		return this.targetObject;
	}


	@Override
	public final synchronized void refresh() {
		logger.debug("正在尝试刷新目标");

		this.targetObject = freshTarget();
		this.refreshCount++;
		this.lastRefreshTime = System.currentTimeMillis();

		logger.debug("目标刷新成功");
	}

	@Override
	public synchronized long getRefreshCount() {
		return this.refreshCount;
	}

	@Override
	public synchronized long getLastRefreshTime() {
		return this.lastRefreshTime;
	}


	private boolean refreshCheckDelayElapsed() {
		if (this.refreshCheckDelay < 0) {
			return false;
		}

		long currentTimeMillis = System.currentTimeMillis();

		if (this.lastRefreshCheck < 0 || currentTimeMillis - this.lastRefreshCheck > this.refreshCheckDelay) {
			// 即将执行刷新检查——更新时间戳。
			this.lastRefreshCheck = currentTimeMillis;
			logger.debug("刷新检查延迟已过——检查是否需要刷新");
			return true;
		}

		return false;
	}


	/**
	 * 判定是否需要刷新。
	 * 每次刷新检查（延迟已过）时调用。
	 * <p>默认实现始终返回 {@code true}，延迟一过即触发刷新。
	 * 子类应覆盖以对底层目标资源做适当检查。
	 * @return 是否需要刷新
	 */
	protected boolean requiresRefresh() {
		return true;
	}

	/**
	 * 获取新目标对象。
	 * <p>仅当刷新检查发现需要刷新时调用
	 * （即 {@link #requiresRefresh()} 返回 {@code true}）。
	 * @return 新目标对象
	 */
	protected abstract Object freshTarget();

}
