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

package org.springframework.aop.framework;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.core.DecoratingProxy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * AOP 代理工厂的实用方法。
 * <p>主要供AOP框架内部使用。
 * <p>请参阅 {@link org.springframework.aop.support.AopUtils}，了解不依赖于 AOP 框架内部的通用 AOP 实用方法的集合。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see org.springframework.aop.support.AopUtils
 */
public abstract class AopProxyUtils {

	/**
	 * 获取给定代理后面的单例目标对象（如果有）。
	 * @param candidate 要检查的（潜在）代理
	 * @return 在 {@link SingletonTargetSource} 中管理的单例目标对象，或在任何其他情况下在 {@code null} 中管理的单例目标对象（不是代理，不是现有的单例目标）
	 * @since 4.3.8
	 * @see Advised#getTargetSource()
	 * @see SingletonTargetSource#getTarget()
	 */
	public static @Nullable Object getSingletonTarget(Object candidate) {
		if (candidate instanceof Advised advised) {
			TargetSource targetSource = advised.getTargetSource();
			if (targetSource instanceof SingletonTargetSource singleTargetSource) {
				return singleTargetSource.getTarget();
			}
		}
		return null;
	}

	/**
	 * 确定给定 bean 实例的最终目标类，不仅遍历顶级代理，还遍历任意数量的嵌套代理 –尽可能长且没有副作用，即仅针对单例目标。
	 * @param candidate 要检查的实例（可能是 AOP 代理）
	 * @return 最终目标类（或给定对象的普通类作为后备；绝不是 {@code null}）
	 * @see org.springframework.aop.TargetClassAware#getTargetClass()
	 * @see Advised#getTargetSource()
	 */
	public static Class<?> ultimateTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Object current = candidate;
		Class<?> result = null;
		while (current instanceof TargetClassAware targetClassAware) {
			result = targetClassAware.getTargetClass();
			current = getSingletonTarget(current);
		}
		if (result == null) {
			result = (AopUtils.isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	/**
	 * 完成 Spring AOP 生成的 JDK 动态代理中通常需要的接口集。 <p>具体来说，{@link SpringProxy}、{@link Advised}和{@link
	 * DecoratingProxy}将被追加到用户指定的接口集中。 <p> 在注册 {@linkplain
	 * org.springframework.aot.hint.ProxyHints proxy hints} 以获取 Spring 的 AOT 支持时，此方法非常有用，如以下通过
	 * {@code static} 导入使用此方法的示例所示。 <pre class="code"> RuntimeHints 提示 =
	 * ...hints.proxies().registerJdkProxy(completeJdkProxyInterfaces(MyInterface.class));
	 * </pre>
	 * @param userInterfaces 由要代理的组件实现的用户指定的接口集
	 * @return 代理应该实现的完整接口集
	 * @throws IllegalArgumentException 如果提供的 {@code Class} 是 {@code null}，不是 {@linkplain Class#isInterface() interface}，或者是 {@linkplain Class#isSealed() sealed} 接口
	 * @since 6.0
	 * @see SpringProxy
	 * @see Advised
	 * @see DecoratingProxy
	 * @see org.springframework.aot.hint.RuntimeHints#proxies()
	 * @see org.springframework.aot.hint.ProxyHints#registerJdkProxy(Class...)
	 */
	public static Class<?>[] completeJdkProxyInterfaces(Class<?>... userInterfaces) {
		List<Class<?>> completedInterfaces = new ArrayList<>(userInterfaces.length + 3);
		for (Class<?> ifc : userInterfaces) {
			Assert.notNull(ifc, "'userInterfaces' must not contain null values");
			Assert.isTrue(ifc.isInterface() && !ifc.isSealed(),
					() -> ifc.getName() + " must be a non-sealed interface");
			completedInterfaces.add(ifc);
		}
		completedInterfaces.add(SpringProxy.class);
		completedInterfaces.add(Advised.class);
		completedInterfaces.add(DecoratingProxy.class);
		return completedInterfaces.toArray(Class<?>[]::new);
	}

	/**
	 * 确定给定 AOP 配置的完整接口集。 <p> 这将始终添加 {@link Advised} 接口，除非 AdvisedSupport 的 {@link
	 * AdvisedSupport#setOpaque "opaque"} 标志打开。始终添加 {@link org.springframework.aop.SpringProxy}
	 * 标记接口。
	 * @param advised 代理配置
	 * @return 完整的代理接口集
	 * @see SpringProxy
	 * @see Advised
	 */
	public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
		return completeProxiedInterfaces(advised, false);
	}

	/**
	 * 确定给定 AOP 配置的完整接口集。 <p> 这将始终添加 {@link Advised} 接口，除非 AdvisedSupport 的 {@link
	 * AdvisedSupport#setOpaque "opaque"} 标志打开。始终添加 {@link org.springframework.aop.SpringProxy}
	 * 标记接口。
	 * @param advised 代理配置
	 * @param decoratingProxy 是否公开{@link DecoratingProxy}接口
	 * @return 完整的代理接口集
	 * @since 4.3
	 * @see SpringProxy
	 * @see Advised
	 * @see DecoratingProxy
	 */
	static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
		Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
		if (specifiedInterfaces.length == 0) {
			// 没有用户指定的接口：检查目标类是否是接口。
			Class<?> targetClass = advised.getTargetClass();
			if (targetClass != null) {
				if (targetClass.isInterface()) {
					advised.setInterfaces(targetClass);
				}
				else if (Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
					advised.setInterfaces(targetClass.getInterfaces());
				}
				specifiedInterfaces = advised.getProxiedInterfaces();
			}
		}
		List<Class<?>> proxiedInterfaces = new ArrayList<>(specifiedInterfaces.length + 3);
		for (Class<?> ifc : specifiedInterfaces) {
			// 只有非密封接口实际上才有资格进行 JDK 代理（在 JDK 17 上）
			if (!ifc.isSealed()) {
				proxiedInterfaces.add(ifc);
			}
		}
		if (!advised.isInterfaceProxied(SpringProxy.class)) {
			proxiedInterfaces.add(SpringProxy.class);
		}
		if (!advised.isOpaque() && !advised.isInterfaceProxied(Advised.class)) {
			proxiedInterfaces.add(Advised.class);
		}
		if (decoratingProxy && !advised.isInterfaceProxied(DecoratingProxy.class)) {
			proxiedInterfaces.add(DecoratingProxy.class);
		}
		return ClassUtils.toClassArray(proxiedInterfaces);
	}

	/**
	 * 提取给定代理实现的用户指定的接口，即代理实现的所有非建议接口。
	 * @param proxy 要分析的代理（通常是 JDK 动态代理）
	 * @return 代理实现的用户指定的接口，按原始顺序（绝不是 {@code null} 或空）
	 * @see Advised
	 */
	public static Class<?>[] proxiedUserInterfaces(Object proxy) {
		Class<?>[] proxyInterfaces = proxy.getClass().getInterfaces();
		int nonUserIfcCount = 0;
		if (proxy instanceof SpringProxy) {
			nonUserIfcCount++;
		}
		if (proxy instanceof Advised) {
			nonUserIfcCount++;
		}
		if (proxy instanceof DecoratingProxy) {
			nonUserIfcCount++;
		}
		Class<?>[] userInterfaces = Arrays.copyOf(proxyInterfaces, proxyInterfaces.length - nonUserIfcCount);
		Assert.notEmpty(userInterfaces, "JDK proxy must implement one or more interfaces");
		return userInterfaces;
	}

	/**
	 * 检查给定 AdvisedSupport 对象背后的代理是否相等。与 AdvisedSupport 对象的平等不同：相反，接口、顾问和目标源的平等。
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		return (a == b ||
				(equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource())));
	}

	/**
	 * 检查给定 AdvisedSupport 对象后面的代理接口是否相等。
	 */
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}

	/**
	 * 检查给定 AdvisedSupport 对象背后的顾问是否相等。
	 */
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return a.getAdvisorCount() == b.getAdvisorCount() && Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}


	/**
	 * 如有必要，请使给定参数适应给定方法中的目标签名：特别是，如果给定的 vararg 参数数组与方法中声明的 vararg 参数的数组类型不匹配。
	 * @param method 目标方法
	 * @param arguments 给定的参数
	 * @return 克隆的参数数组，如果不需要调整则为原始参数数组
	 * @since 4.2.3
	 */
	static @Nullable Object[] adaptArgumentsIfNecessary(Method method, @Nullable Object[] arguments) {
		if (ObjectUtils.isEmpty(arguments)) {
			return new Object[0];
		}
		if (method.isVarArgs() && (method.getParameterCount() == arguments.length)) {
			Class<?>[] paramTypes = method.getParameterTypes();
			int varargIndex = paramTypes.length - 1;
			Class<?> varargType = paramTypes[varargIndex];
			if (varargType.isArray()) {
				Object varargArray = arguments[varargIndex];
				if (varargArray instanceof Object[] && !varargType.isInstance(varargArray)) {
					Object[] newArguments = new Object[arguments.length];
					System.arraycopy(arguments, 0, newArguments, 0, varargIndex);
					Class<?> targetElementType = varargType.componentType();
					int varargLength = Array.getLength(varargArray);
					Object newVarargArray = Array.newInstance(targetElementType, varargLength);
					System.arraycopy(varargArray, 0, newVarargArray, 0, varargLength);
					newArguments[varargIndex] = newVarargArray;
					return newArguments;
				}
			}
		}
		return arguments;
	}

}
