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

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.aop.IntroductionInterceptor} 接口的便捷实现。
 *
 * <p>子类只需继承本类并实现要引入的接口即可，
 * 此时委托即为子类实例本身。也可由独立委托实现接口，
 * 并通过 delegate bean 属性设置。
 *
 * <p>委托或子类可实现任意数量接口。
 * 默认从子类或委托收集除 IntroductionInterceptor 外的所有接口。
 *
 * <p>可使用 {@code suppressInterface} 方法抑制委托已实现、
 * 但不应引入到所属 AOP 代理的接口。
 *
 * <p>若委托可序列化，则本类实例也可序列化。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.11.2003
 * @see #suppressInterface
 * @see DelegatePerTargetObjectIntroductionInterceptor
 */
@SuppressWarnings("serial")
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport
		implements IntroductionInterceptor {

	/**
	 * 实际实现接口的对象。
	 * 若子类实现引入接口，则可为 "this"。
	 */
	private @Nullable Object delegate;


	/**
	 * 构造新的 DelegatingIntroductionInterceptor，
	 * 提供实现要引入接口的委托。
	 * @param delegate 实现引入接口的委托
	 */
	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}

	/**
	 * 构造新的 DelegatingIntroductionInterceptor。
	 * 委托为子类本身，子类须实现额外接口。
	 */
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}


	/**
	 * 两个构造函数均使用此 init 方法，
	 * 因无法将 "this" 引用从一个构造函数传给另一个。
	 * @param delegate 委托对象
	 */
	private void init(Object delegate) {
		Assert.notNull(delegate, "Delegate must not be null");
		this.delegate = delegate;
		implementInterfacesOnObject(delegate);

		// 不暴露控制接口
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
			// 使用以下方法而非直接反射，
			// 可在引入方法抛出异常时正确处理 InvocationTargetException。
			Object retVal = AopUtils.invokeJoinpointUsingReflection(this.delegate, mi.getMethod(), mi.getArguments());

			// 尽可能调整返回值：若委托返回自身，
			// 实际应返回代理。
			if (retVal == this.delegate && mi instanceof ProxyMethodInvocation pmi) {
				Object proxy = pmi.getProxy();
				if (mi.getMethod().getReturnType().isInstance(proxy)) {
					retVal = proxy;
				}
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

}
