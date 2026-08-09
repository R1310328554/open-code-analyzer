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
 * 方便实现{@link org.springframework.aop.IntroductionInterceptor}接口。
 * <p>子类只需扩展该类并实现自己要引入的接口即可。在这种情况下，委托是子类实例本身。或者，单独的委托可以实现该接口，并通过委托 bean 属性进行设置。
 * <p>Delegates 或子类可以实现任意数量的接口。默认情况下，除IntroductionInterceptor 之外的所有接口均从子类或委托中获取。
 * <p>{@code suppressInterface} 方法可用于抑制委托实现的接口，但不应将其引入到所属的 AOP 代理中。
 * <p>如果委托是可序列化的，则该类的实例是可序列化的。
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
	 * 实际实现接口的对象。如果子类实现了引入的接口，则可能是“this”。
	 */
	private @Nullable Object delegate;


	/**
	 * 构造一个新的 DelegatingIntroductionInterceptor，提供一个实现要引入的接口的委托。
	 * @param delegate 实现引入的接口的委托
	 */
	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}

	/**
	 * 构造一个新的 DelegatingIntroductionInterceptor。委托将是子类，它必须实现附加接口。
	 */
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}


	/**
	 * 两个构造函数都使用此 init 方法，因为不可能将“this”引用从一个构造函数传递到另一个构造函数。
	 * @param delegate 委托对象
	 */
	private void init(Object delegate) {
		Assert.notNull(delegate, "Delegate must not be null");
		this.delegate = delegate;
		implementInterfacesOnObject(delegate);

		// 我们不想暴露控制接口
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}


	/**
	 * 如果子类想要在 around 建议中执行自定义行为，则可能需要覆盖它。但是，子类应该调用此方法，该方法处理引入的接口并转发到目标。
	 */
	@Override
	public @Nullable Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			// 使用以下方法而不是直接反射，我们
			// 正确处理 InvocableTargetException
			// 如果引入的方法抛出异常。
			Object retVal = AopUtils.invokeJoinpointUsingReflection(this.delegate, mi.getMethod(), mi.getArguments());

			// 如果可能的话，按摩返回值：如果委托返回自身，
			// 我们真的想返回代理。
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
	 * 继续使用提供的 {@link
	 * org.aopalliance.intercept.MethodInterceptor}。子类可以重写此方法以拦截目标对象上的方法调用，这在引入需要监视引入它的对象时非常有用。该方法是
	 * <strong>never</strong> 在引入的接口上为 {@link MethodInvocation MethodInvocations} 调用的。
	 */
	protected @Nullable Object doProceed(MethodInvocation mi) throws Throwable {
		// 如果我们到达这里，只需传递调用即可。
		return mi.proceed();
	}

}
