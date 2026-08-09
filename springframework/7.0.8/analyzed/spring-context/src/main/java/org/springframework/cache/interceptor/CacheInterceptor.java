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

package org.springframework.cache.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 基于 Spring 通用缓存基础设施（{@link org.springframework.cache.Cache}）
 * 实现声明式缓存管理的 AOP Alliance {@link MethodInterceptor}。
 *
 * <p>继承自 {@link CacheAspectSupport}，该类封装了与 Spring 底层缓存 API 的集成逻辑。
 * {@code CacheInterceptor} 仅按正确顺序调用父类的相关方法。
 *
 * <p>{@code CacheInterceptor} 是线程安全的。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.1
 */
@SuppressWarnings("serial")
public class CacheInterceptor extends CacheAspectSupport implements MethodInterceptor, Serializable {

	@Override
	public @Nullable Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();

		// 将 AOP Alliance 调用适配为 CacheOperationInvoker
		CacheOperationInvoker aopAllianceInvoker = () -> {
			try {
				return invocation.proceed();
			}
			catch (Throwable ex) {
				throw new CacheOperationInvoker.ThrowableWrapper(ex);
			}
		};

		Object target = invocation.getThis();
		Assert.state(target != null, "Target must not be null");
		try {
			return execute(aopAllianceInvoker, target, method, invocation.getArguments());
		}
		catch (CacheOperationInvoker.ThrowableWrapper th) {
			// 解包底层调用抛出的受检/非受检异常
			throw th.getOriginal();
		}
	}

}
