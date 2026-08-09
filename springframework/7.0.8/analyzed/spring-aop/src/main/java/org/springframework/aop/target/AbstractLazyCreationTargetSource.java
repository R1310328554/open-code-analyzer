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

package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;

/**
 * 延迟创建用户管理对象的 {@link org.springframework.aop.TargetSource} 实现。
 *
 * <p>通过实现 {@link #createObject()} 方法由用户控制延迟目标对象的创建。
 * 本 {@code TargetSource} 在首次访问代理时调用该方法。
 *
 * <p>适用于需要向对象传递某依赖引用、但希望直到首次使用时才创建该依赖的场景。
 * 典型用例是连接远程资源。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.4
 * @see #isInitialized()
 * @see #createObject()
 */
public abstract class AbstractLazyCreationTargetSource implements TargetSource {

	/** 供子类使用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 延迟初始化的目标对象。 */
	private @Nullable Object lazyTarget;


	/**
	 * 返回本 TargetSource 的延迟目标对象是否已被获取。
	 */
	public synchronized boolean isInitialized() {
		return (this.lazyTarget != null);
	}

	/**
	 * 默认实现：若目标为 {@code null}（尚未初始化）则返回 {@code null}，
	 * 若已初始化则返回目标类。
	 * <p>子类可覆盖本方法，在目标仍为 {@code null} 时提供有意义的值。
	 * @see #isInitialized()
	 */
	@Override
	public synchronized @Nullable Class<?> getTargetClass() {
		return (this.lazyTarget != null ? this.lazyTarget.getClass() : null);
	}

	/**
	 * 返回延迟初始化的目标对象；若尚不存在则即时创建。
	 * @see #createObject()
	 */
	@Override
	public synchronized Object getTarget() throws Exception {
		if (this.lazyTarget == null) {
			logger.debug("正在初始化延迟目标对象");
			this.lazyTarget = createObject();
		}
		return this.lazyTarget;
	}


	/**
	 * 子类应实现本方法以返回延迟初始化的对象。
	 * 在首次调用代理时触发。
	 * @return 创建的对象
	 * @throws Exception 若创建失败
	 */
	protected abstract Object createObject() throws Exception;

}
