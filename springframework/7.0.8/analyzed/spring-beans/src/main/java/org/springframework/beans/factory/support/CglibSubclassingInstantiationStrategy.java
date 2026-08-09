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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.AotDetector;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用于 BeanFactory 的默认对象实例化策略。
 *
 * <p>若容器需要覆盖方法以实现<em>方法注入</em>，则使用 CGLIB 动态生成子类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

	/**
	 * CGLIB 回调数组中直通行为的索引，
	 * 此时子类不会覆盖原始类的方法。
	 */
	private static final int PASSTHROUGH = 0;

	/**
	 * CGLIB 回调数组中应被覆盖以提供<em>方法查找</em>的索引。
	 */
	private static final int LOOKUP_OVERRIDE = 1;

	/**
	 * CGLIB 回调数组中应通过通用<em>方法替换器</em>覆盖方法的索引。
	 */
	private static final int METHOD_REPLACER = 2;


	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		return instantiateWithMethodInjection(bd, beanName, owner, null);
	}

	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Constructor<?> ctor, Object... args) {

		return new CglibSubclassCreator(bd, owner).instantiate(ctor, args);
	}

	@Override
	public Class<?> getActualBeanClass(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		if (!bd.hasMethodOverrides()) {
			return super.getActualBeanClass(bd, beanName, owner);
		}
		return new CglibSubclassCreator(bd, owner).createEnhancedSubclass(bd);
	}


	/**
	 * 为历史原因创建的内部类，用于在 Spring 3.2 之前避免外部 CGLIB 依赖。
	 */
	private static class CglibSubclassCreator {

		private static final Class<?>[] CALLBACK_TYPES = new Class<?>[]
				{NoOp.class, LookupOverrideMethodInterceptor.class, ReplaceOverrideMethodInterceptor.class};

		private final RootBeanDefinition beanDefinition;

		private final BeanFactory owner;

		CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		/**
		 * 创建动态生成的子类新实例，实现所需的查找方法。
		 * @param ctor 要使用的构造器。若为 {@code null}，使用无参构造器（无参数化或 Setter 注入）
		 * @param args 构造器参数。若 {@code ctor} 为 {@code null} 则忽略
		 * @return 动态生成子类的新实例
		 */
		public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
			// 创建 CGLIB 增强子类
			Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
			Object instance;
			if (ctor == null) {
				instance = BeanUtils.instantiateClass(subclass);
			}
			else {
				try {
					Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
					instance = enhancedSubclassConstructor.newInstance(args);
				}
				catch (Exception ex) {
					throw new BeanInstantiationException(this.beanDefinition.getBeanClass(),
							"Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
				}
			}
			// SPR-10785：将回调直接设置在实例上，而非增强类（通过 Enhancer），以避免内存泄漏
			Factory factory = (Factory) instance;
			factory.setCallbacks(new Callback[] {NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner),
					new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner)});
			return instance;
		}

		/**
		 * 使用 CGLIB 为给定 Bean 定义创建 Bean 类的增强子类。
		 */
		public Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(beanDefinition.getBeanClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setAttemptLoad(AotDetector.useGeneratedArtifacts());
			if (this.owner instanceof ConfigurableBeanFactory cbf) {
				ClassLoader cl = cbf.getBeanClassLoader();
				enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
			}
			enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
			enhancer.setCallbackTypes(CALLBACK_TYPES);
			return enhancer.createClass();
		}
	}


	/**
	 * 提供 CGLIB 所需的 hashCode 和 equals 方法，
	 * 确保 CGLIB 不会为每个 Bean 生成不同的类。
	 * 身份基于类和 Bean 定义。
	 */
	private static class CglibIdentitySupport {

		private final RootBeanDefinition beanDefinition;

		public CglibIdentitySupport(RootBeanDefinition beanDefinition) {
			this.beanDefinition = beanDefinition;
		}

		public RootBeanDefinition getBeanDefinition() {
			return this.beanDefinition;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (other != null && getClass() == other.getClass() &&
					this.beanDefinition.equals(((CglibIdentitySupport) other).beanDefinition));
		}

		@Override
		public int hashCode() {
			return this.beanDefinition.hashCode();
		}
	}


	/**
	 * 用于过滤方法拦截行为的 CGLIB 回调。
	 */
	private static class MethodOverrideCallbackFilter extends CglibIdentitySupport implements CallbackFilter {

		private static final Log logger = LogFactory.getLog(MethodOverrideCallbackFilter.class);

		public MethodOverrideCallbackFilter(RootBeanDefinition beanDefinition) {
			super(beanDefinition);
		}

		@Override
		public int accept(Method method) {
			MethodOverride methodOverride = getBeanDefinition().getMethodOverrides().getOverride(method);
			if (logger.isTraceEnabled()) {
				logger.trace("MethodOverride for " + method + ": " + methodOverride);
			}
			if (methodOverride == null) {
				return PASSTHROUGH;
			}
			else if (methodOverride instanceof LookupOverride) {
				return LOOKUP_OVERRIDE;
			}
			else if (methodOverride instanceof ReplaceOverride) {
				return METHOD_REPLACER;
			}
			throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " +
					methodOverride.getClass().getName());
		}
	}


	/**
	 * CGLIB MethodInterceptor，覆盖方法并在容器中查找 Bean 作为返回值。
	 */
	private static class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public LookupOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		@Override
		public @Nullable Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			// 类型转换安全，因为 CallbackFilter 已选择性过滤
			LookupOverride lo = (LookupOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(lo != null, "LookupOverride not found");
			Object[] argsToUse = (args.length > 0 ? args : null);  // if no-arg, don't insist on args at all
			if (StringUtils.hasText(lo.getBeanName())) {
				Object bean = (argsToUse != null ? this.owner.getBean(lo.getBeanName(), argsToUse) :
						this.owner.getBean(lo.getBeanName()));
				// 通过 equals(null) 检测包级保护的 NullBean 实例
				return (bean.equals(null) ? null : bean);
			}
			else {
				// 按（可能为泛型的）方法返回类型查找目标 Bean
				ResolvableType genericReturnType = ResolvableType.forMethodReturnType(method);
				return (argsToUse != null ? this.owner.getBeanProvider(genericReturnType).getObject(argsToUse) :
						this.owner.getBeanProvider(genericReturnType).getObject());
			}
		}
	}


	/**
	 * CGLIB MethodInterceptor，覆盖方法并调用通用 {@link MethodReplacer}。
	 */
	private static class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public ReplaceOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		@Override
		public @Nullable Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			ReplaceOverride ro = (ReplaceOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(ro != null, "ReplaceOverride not found");
			MethodReplacer mr = this.owner.getBean(ro.getMethodReplacerBeanName(), MethodReplacer.class);
			return processReturnType(method, mr.reimplement(obj, method, args));
		}

		private <T> @Nullable T processReturnType(Method method, @Nullable T returnValue) {
			Class<?> returnType = method.getReturnType();
			if (returnValue == null && returnType != void.class && returnType.isPrimitive()) {
				throw new IllegalStateException(
						"Null return value from MethodReplacer does not match primitive return type for: " + method);
			}
			return returnValue;
		}
	}

}
