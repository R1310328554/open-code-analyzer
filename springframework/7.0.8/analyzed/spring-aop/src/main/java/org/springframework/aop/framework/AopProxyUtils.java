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
 * AOP 代理工厂的工具方法。
 *
 * <p>主要供 AOP 框架内部使用。
 *
 * <p>不依赖 AOP 框架内部的通用 AOP 工具方法见
 * {@link org.springframework.aop.support.AopUtils}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see org.springframework.aop.support.AopUtils
 */
public abstract class AopProxyUtils {

	/**
	 * 获取给定代理背后的单例目标对象（若有）。
	 * @param candidate 待检查的（潜在）代理
	 * @return {@link SingletonTargetSource} 中管理的单例目标对象，
	 * 其他情况（非代理、非现有单例目标）返回 {@code null}
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
	 * 确定给定 Bean 实例的终极目标类，不仅遍历顶层代理，
	 * 还遍历任意层嵌套代理——在无副作用前提下，即仅针对单例目标。
	 * @param candidate 待检查的实例（可能是 AOP 代理）
	 * @return 终极目标类（或回退为给定对象的普通类；永不 {@code null}）
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
	 * 补全 Spring AOP 生成的 JDK 动态代理通常所需的接口集合。
	 * <p>具体而言，{@link SpringProxy}、{@link Advised} 与 {@link DecoratingProxy}
	 * 会追加到用户指定接口集合中。
	 * <p>为 Spring AOT 支持注册
	 * {@linkplain org.springframework.aot.hint.ProxyHints 代理提示} 时本方法很有用，
	 * 如下例通过 {@code static} 导入使用本方法：
	 * <pre class="code">
	 * RuntimeHints hints = ...
	 * hints.proxies().registerJdkProxy(completeJdkProxyInterfaces(MyInterface.class));
	 * </pre>
	 * @param userInterfaces 待代理组件实现的用户指定接口集合
	 * @return 代理应实现的完整接口集合
	 * @throws IllegalArgumentException 若提供的 {@code Class} 为 {@code null}、
	 * 非 {@linkplain Class#isInterface() 接口}，或为
	 * {@linkplain Class#isSealed() 密封} 接口
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
	 * 确定给定 AOP 配置应代理的完整接口集合。
	 * <p>除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque "opaque"} 标志开启，
	 * 否则始终添加 {@link Advised} 接口。始终添加
	 * {@link org.springframework.aop.SpringProxy} 标记接口。
	 * @param advised 代理配置
	 * @return 应代理的完整接口集合
	 * @see SpringProxy
	 * @see Advised
	 */
	public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
		return completeProxiedInterfaces(advised, false);
	}

	/**
	 * 确定给定 AOP 配置应代理的完整接口集合。
	 * <p>除非 AdvisedSupport 的 {@link AdvisedSupport#setOpaque "opaque"} 标志开启，
	 * 否则始终添加 {@link Advised} 接口。始终添加
	 * {@link org.springframework.aop.SpringProxy} 标记接口。
	 * @param advised 代理配置
	 * @param decoratingProxy 是否暴露 {@link DecoratingProxy} 接口
	 * @return 应代理的完整接口集合
	 * @since 4.3
	 * @see SpringProxy
	 * @see Advised
	 * @see DecoratingProxy
	 */
	static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
		Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
		if (specifiedInterfaces.length == 0) {
			// 无用户指定接口：检查目标类是否为接口。
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
			// 仅非密封接口才真正适合 JDK 代理（JDK 17 上）
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
	 * 提取给定代理实现的用户指定接口，
	 * 即代理实现的除 Advised 外的所有接口。
	 * @param proxy 待分析的代理（通常为 JDK 动态代理）
	 * @return 代理实现的用户指定接口，保持原始顺序
	 * （永不 {@code null} 或空）
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
	 * 检查给定 AdvisedSupport 对象背后代理的相等性。
	 * 不同于 AdvisedSupport 对象本身的相等性：
	 * 而是比较接口、通知器与目标源。
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		return (a == b ||
				(equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource())));
	}

	/**
	 * 检查给定 AdvisedSupport 对象背后被代理接口的相等性。
	 */
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}

	/**
	 * 检查给定 AdvisedSupport 对象背后通知器的相等性。
	 */
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return a.getAdvisorCount() == b.getAdvisorCount() && Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}


	/**
	 * 必要时将给定参数适配为目标方法签名，
	 * 尤其当可变参数数组与方法的声明可变参数数组类型不匹配时。
	 * @param method 目标方法
	 * @param arguments 给定参数
	 * @return 克隆后的参数数组，无需适配则返回原数组
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
