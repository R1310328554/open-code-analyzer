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

package org.springframework.aop.support;

import java.util.Map;
import java.util.WeakHashMap;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.aop.IntroductionInterceptor} 接口的便捷实现。
 *
 * <p>与 {@link DelegatingIntroductionInterceptor} 的区别在于：
 * 本类的一个实例可用于通知多个目标对象，每个目标对象拥有<i>各自</i>的委托
 * （而 DelegatingIntroductionInterceptor 共享同一委托，因此所有目标共享同一状态）。
 *
 * <p>可使用 {@code suppressInterface} 方法抑制委托类已实现、
 * 但不应引入到所属 AOP 代理的接口。
 *
 * <p>若委托可序列化，则本类实例也可序列化。
 *
 * <p><i>注意：本类与 {@link DelegatingIntroductionInterceptor} 在实现上存在相似性，
 * 未来可能重构提取公共祖先类。</i>
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 * @see #suppressInterface
 * @see DelegatingIntroductionInterceptor
 */
@SuppressWarnings("serial")
public class DelegatePerTargetObjectIntroductionInterceptor extends IntroductionInfoSupport
		implements IntroductionInterceptor {

	/**
	 * 对键持有弱引用，避免干扰垃圾回收。
	 */
	private final Map<Object, Object> delegateMap = new WeakHashMap<>();

	private final Class<?> defaultImplType;

	private final Class<?> interfaceType;


	public DelegatePerTargetObjectIntroductionInterceptor(Class<?> defaultImplType, Class<?> interfaceType) {
		this.defaultImplType = defaultImplType;
		this.interfaceType = interfaceType;
		// 立即创建新委托（但不存入 map）。
		// 原因有二：
		// 1) 若实例化委托有问题则尽早失败
		// 2) 仅一次性填充接口 map
		Object delegate = createNewDelegate();
		implementInterfacesOnObject(delegate);
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}


	/**
	 * 若子类需在环绕通知中执行自定义行为，可覆盖本方法。
	 * 但子类应调用本方法，以处理引入接口及向目标转发。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			Object delegate = getIntroductionDelegateFor(mi.getThis());

			// 使用以下方法而非直接反射，
			// 可在引入方法抛出异常时正确处理 InvocationTargetException。
			Object retVal = AopUtils.invokeJoinpointUsingReflection(delegate, mi.getMethod(), mi.getArguments());

			// 尽可能调整返回值：若委托返回自身，
			// 实际应返回代理。
			if (retVal == delegate && mi instanceof ProxyMethodInvocation pmi) {
				retVal = pmi.getProxy();
			}
			return retVal;
		}

		return doProceed(mi);
	}

	/**
	 * 继续执行提供的 {@link org.aopalliance.intercept.MethodInterceptor}。
	 * 子类可覆盖本方法以拦截目标对象上的方法调用，
	 * 适用于引入需监控被引入对象的情况。
	 * 对引入接口上的 {@link MethodInvocation MethodInvocation} <strong>永不</strong>调用本方法。
	 */
	protected @Nullable Object doProceed(MethodInvocation mi) throws Throwable {
		// 执行到此则直接传递调用。
		return mi.proceed();
	}

	private Object getIntroductionDelegateFor(@Nullable Object targetObject) {
		synchronized (this.delegateMap) {
			if (this.delegateMap.containsKey(targetObject)) {
				return this.delegateMap.get(targetObject);
			}
			else {
				Object delegate = createNewDelegate();
				this.delegateMap.put(targetObject, delegate);
				return delegate;
			}
		}
	}

	private Object createNewDelegate() {
		try {
			return ReflectionUtils.accessibleConstructor(this.defaultImplType).newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("Cannot create default implementation for '" +
					this.interfaceType.getName() + "' mixin (" + this.defaultImplType.getName() + "): " + ex);
		}
	}

}
