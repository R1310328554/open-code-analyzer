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

package org.springframework.context.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;

/**
 * 简单的基于线程的 {@link Scope} 实现。
 *
 * <p><b>注意：</b>此线程作用域在常见上下文中默认未注册。
 * 需在装配中显式将其分配给作用域键，可通过
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope}
 * 或 {@link org.springframework.beans.factory.config.CustomScopeConfigurer} Bean 完成。
 *
 * <p>{@code SimpleThreadScope} <em>不会清理</em>与其关联的任何对象。
 * 因此在 Web 环境中通常更宜使用请求绑定的作用域实现（如
 * {@code org.springframework.web.context.request.RequestScope}），
 * 以完整支持作用域属性生命周期（含可靠销毁）。
 *
 * <p>若需支持销毁回调的基于线程的 {@code Scope} 实现，可参考
 * <a href="https://www.springbyexample.org/examples/custom-thread-scope-module.html">Spring by Example</a>。
 *
 * <p>感谢 Eugene Kuleshov 提交线程作用域的原始原型！
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.web.context.request.RequestScope
 */
public class SimpleThreadScope implements Scope {

	private static final Log logger = LogFactory.getLog(SimpleThreadScope.class);

	/** 当前线程的作用域 Bean 映射（线程名 → Bean 名 → 实例）。 */
	private final ThreadLocal<Map<String, Object>> threadScope = NamedThreadLocal.withInitial(
			"SimpleThreadScope", HashMap::new);

	/**
	 * 从当前线程作用域获取 Bean；不存在则通过 objectFactory 创建并缓存。
	 */
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> scope = this.threadScope.get();
		// 注意：请勿改为使用 Map::computeIfAbsent，详见
		// https://github.com/spring-projects/spring-framework/issues/25801
		Object scopedObject = scope.get(name);
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			scope.put(name, scopedObject);
		}
		return scopedObject;
	}

	/**
	 * 从当前线程作用域移除并返回指定名称的 Bean。
	 */
	@Override
	public @Nullable Object remove(String name) {
		Map<String, Object> scope = this.threadScope.get();
		return scope.remove(name);
	}

	/**
	 * 注册销毁回调；本实现仅记录警告，不实际支持销毁。
	 */
	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		logger.warn("SimpleThreadScope does not support destruction callbacks. " +
				"Consider using RequestScope in a web environment.");
	}

	/**
	 * 返回当前线程名作为会话标识。
	 */
	@Override
	public String getConversationId() {
		return Thread.currentThread().getName();
	}

}
