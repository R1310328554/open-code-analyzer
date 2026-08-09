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
 * 方便实现{@link org.springframework.aop.IntroductionInterceptor}接口。
 * <p>This 与 {@link DelegatingIntroductionInterceptor}
 * 的不同之处在于，此类的单个实例可用于建议多个目标对象，并且每个目标对象将具有其 <i>own</i> 委托（而
 * DelegatingIntroductionInterceptor 共享相同的委托，因此所有目标之间具有相同的状态）。
 * <p>{@code suppressInterface} 方法可用于抑制委托类实现的接口，但不应将其引入到所属的 AOP 代理中。
 * <p>如果委托是可序列化的，则该类的实例是可序列化的。
 * <p><i>注意：此类与 {@link DelegatingIntroductionInterceptor}
 * 之间存在一些实现相似之处，建议将来可能进行重构以提取共同的祖先类。</i>
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
	 * 保留对键的弱引用，因为我们不想干扰垃圾收集。
	 */
	private final Map<Object, Object> delegateMap = new WeakHashMap<>();

	/** 类型相关状态（`defaultImplType`）。 */
	private final Class<?> defaultImplType;

	/** 类型相关状态（`interfaceType`）。 */
	private final Class<?> interfaceType;


	/**
	 * 创建 `DelegatePerTargetObjectIntroductionInterceptor` 的新实例。
	 */
	public DelegatePerTargetObjectIntroductionInterceptor(Class<?> defaultImplType, Class<?> interfaceType) {
		this.defaultImplType = defaultImplType;
		this.interfaceType = interfaceType;
		// 现在创建一个新的委托（但不要将其存储在地图中）。
		// 我们这样做有两个原因：
		// 1) 如果实例化委托出现问题，则尽早失败
		// 2) 一次且仅一次地填充接口映射
		Object delegate = createNewDelegate();
		implementInterfacesOnObject(delegate);
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}


	/**
	 * 如果子类想要在 around 建议中执行自定义行为，则可能需要覆盖它。但是，子类应该调用此方法，该方法处理引入的接口并转发到目标。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			Object delegate = getIntroductionDelegateFor(mi.getThis());

			// 使用以下方法而不是直接反射，
			// 我们得到了 InvocableTargetException 的正确处理
			// 如果引入的方法抛出异常。
			Object retVal = AopUtils.invokeJoinpointUsingReflection(delegate, mi.getMethod(), mi.getArguments());

			// 如果可能的话，按摩返回值：如果委托返回自身，
			// 我们真的想返回代理。
			if (retVal == delegate && mi instanceof ProxyMethodInvocation pmi) {
				retVal = pmi.getProxy();
			}
			return retVal;
		}

		return doProceed(mi);
	}

	/**
	 * 继续使用提供的 {@link
	 * org.aopalliance.intercept.MethodInterceptor}。子类可以重写此方法以拦截目标对象上的方法调用，这在引入需要监视引入它的对象时非常有用。该方法是
	 * <strong>never</strong> 在引入的接口上为 {@link MethodInvocation MethodInvocations} 调用的。
	 */
	protected @Nullable Object doProceed(MethodInvocation mi) throws Throwable {
		// 如果我们到达这里，只需传递调用即可。
		return mi.proceed();
	}

	/**
	 * 获取 Introduction Delegate For（`IntroductionDelegateFor`）。
	 */
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

	/**
	 * 创建：New Delegate（方法 `createNewDelegate`）。
	 */
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
