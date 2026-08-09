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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * 简单的基于事务的 {@link Scope} 实现，
 * 委托给 {@link TransactionSynchronizationManager} 的资源绑定机制。
 *
 * <p><b>注意：</b>与 {@link org.springframework.context.support.SimpleThreadScope} 类似，
 * 此事务作用域在常见上下文中默认未注册。
 * 需在配置中显式将其分配给某个作用域键，
 * 可通过 {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope}
 * 或 {@link org.springframework.beans.factory.config.CustomScopeConfigurer} Bean 完成。
 *
 * @author Juergen Hoeller
 * @since 4.2
 * @see org.springframework.context.support.SimpleThreadScope
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope
 * @see org.springframework.beans.factory.config.CustomScopeConfigurer
 */
public class SimpleTransactionScope implements Scope {

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
		if (scopedObjects == null) {
			scopedObjects = new ScopedObjectsHolder();
			TransactionSynchronizationManager.registerSynchronization(new CleanupSynchronization(scopedObjects));
			TransactionSynchronizationManager.bindResource(this, scopedObjects);
		}
		// 注意：请勿将以下代码改为使用 Map::computeIfAbsent。详情见
		// https://github.com/spring-projects/spring-framework/issues/25801。
		Object scopedObject = scopedObjects.scopedInstances.get(name);
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			scopedObjects.scopedInstances.put(name, scopedObject);
		}
		return scopedObject;
	}

	@Override
	public @Nullable Object remove(String name) {
		ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
		if (scopedObjects != null) {
			scopedObjects.destructionCallbacks.remove(name);
			return scopedObjects.scopedInstances.remove(name);
		}
		else {
			return null;
		}
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
		if (scopedObjects != null) {
			scopedObjects.destructionCallbacks.put(name, callback);
		}
	}

	@Override
	public @Nullable String getConversationId() {
		return TransactionSynchronizationManager.getCurrentTransactionName();
	}


	/**
	 * 作用域对象的持有者。
	 */
	static class ScopedObjectsHolder {

		final Map<String, Object> scopedInstances = new HashMap<>();

		final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();
	}


	private class CleanupSynchronization implements TransactionSynchronization {

		private final ScopedObjectsHolder scopedObjects;

		public CleanupSynchronization(ScopedObjectsHolder scopedObjects) {
			this.scopedObjects = scopedObjects;
		}

		@Override
		public void suspend() {
			TransactionSynchronizationManager.unbindResource(SimpleTransactionScope.this);
		}

		@Override
		public void resume() {
			TransactionSynchronizationManager.bindResource(SimpleTransactionScope.this, this.scopedObjects);
		}

		@Override
		public void afterCompletion(int status) {
			TransactionSynchronizationManager.unbindResourceIfPossible(SimpleTransactionScope.this);
			for (Runnable callback : this.scopedObjects.destructionCallbacks.values()) {
				callback.run();
			}
			this.scopedObjects.destructionCallbacks.clear();
			this.scopedObjects.scopedInstances.clear();
		}
	}

}
