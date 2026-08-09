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
 * {@link org.springframework.aop.TargetSource} 实现将延迟创建用户管理对象。
 * <p>C 惰性目标对象的创建由用户通过实现 {@link #createObject()} 方法来控制。此 {@code TargetSource} 将在第一次访问代理时调用此
 * 方法。
 * <p> 当您需要将对某些依赖项的引用传递给对象但实际上并不希望在首次使用该依赖项之前创建该依赖项时，此功能非常有用。一个典型的场景是连接到远程资源。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2.4
 * @see #isInitialized()
 * @see #createObject()
 */
public abstract class AbstractLazyCreationTargetSource implements TargetSource {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private @Nullable Object lazyTarget;


	/**
	 * 返回此 TargetSource 的惰性目标对象是否已被获取。
	 */
	public synchronized boolean isInitialized() {
		return (this.lazyTarget != null);
	}

	/**
	 * 如果目标是 {@code null}（尚未初始化），则此默认实现返回 {@code null}；如果目标已初始化，则返回目标类。 <p>子类可能希望重写此方法，以便在目标仍然是
	 *  {@code null} 时提供有意义的值。
	 * @see #isInitialized()
	 */
	@Override
	public synchronized @Nullable Class<?> getTargetClass() {
		return (this.lazyTarget != null ? this.lazyTarget.getClass() : null);
	}

	/**
	 * 返回延迟初始化的目标对象，如果尚不存在则即时创建它。
	 * @see #createObject()
	 */
	@Override
	public synchronized Object getTarget() throws Exception {
		if (this.lazyTarget == null) {
			logger.debug("Initializing lazy target object");
			this.lazyTarget = createObject();
		}
		return this.lazyTarget;
	}


	/**
	 * 子类应该实现此方法以返回延迟初始化的对象。第一次调用代理时调用。
	 * @return 创建的对象
	 * @throws Exception 如果创建失败
	 */
	protected abstract Object createObject() throws Exception;

}
