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

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Job;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.CoroutinesUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * AOP 支持代码的工具方法。
 *
 * <p>主要供 Spring AOP 支持内部使用。
 *
 * <p>依赖 Spring AOP 框架内部实现的框架专用 AOP 工具方法见
 * {@link org.springframework.aop.framework.AopProxyUtils}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

	private static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", AopUtils.class.getClassLoader());


	/**
	 * 检查给定对象是否为 JDK 动态代理或 CGLIB 代理。
	 * <p>本方法还会检查给定对象是否为 {@link SpringProxy} 的实例。
	 * @param object 待检查的对象
	 * @see #isJdkDynamicProxy
	 * @see #isCglibProxy
	 */
	@Contract("null -> false")
	public static boolean isAopProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) ||
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)));
	}

	/**
	 * 检查给定对象是否为 JDK 动态代理。
	 * <p>本方法在 {@link Proxy#isProxyClass(Class)} 基础上，
	 * 额外检查给定对象是否为 {@link SpringProxy} 的实例。
	 * @param object 待检查的对象
	 * @see java.lang.reflect.Proxy#isProxyClass
	 */
	@Contract("null -> false")
	public static boolean isJdkDynamicProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
	}

	/**
	 * 检查给定对象是否为 CGLIB 代理。
	 * <p>本方法在 {@link ClassUtils#isCglibProxy(Object)} 基础上，
	 * 额外检查给定对象是否为 {@link SpringProxy} 的实例。
	 * @param object 待检查的对象
	 * @see ClassUtils#isCglibProxy(Object)
	 */
	@Contract("null -> false")
	public static boolean isCglibProxy(@Nullable Object object) {
		return (object instanceof SpringProxy &&
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
	}

	/**
	 * 确定给定 Bean 实例（可能是 AOP 代理）的目标类。
	 * <p>对 AOP 代理返回目标类，否则返回普通类。
	 * @param candidate 待检查的实例（可能是 AOP 代理）
	 * @return 目标类（或作为回退的给定对象普通类；
	 * 永不为 {@code null}）
	 * @see org.springframework.aop.TargetClassAware#getTargetClass()
	 * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
	 */
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if (candidate instanceof TargetClassAware targetClassAware) {
			result = targetClassAware.getTargetClass();
		}
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	/**
	 * 在目标类型上选择可调用方法：若给定方法在目标类型上实际暴露则直接使用，
	 * 否则在目标类型的接口或目标类型本身上查找对应方法。
	 * @param method 待检查的方法
	 * @param targetType 搜索方法的目标类型（通常为 AOP 代理）
	 * @return 目标类型上对应的可调用方法
	 * @throws IllegalStateException 若给定方法在目标类型上不可调用
	 * （通常因代理不匹配）
	 * @since 4.3
	 * @see MethodIntrospector#selectInvocableMethod(Method, Class)
	 */
	public static Method selectInvocableMethod(Method method, @Nullable Class<?> targetType) {
		if (targetType == null) {
			return method;
		}
		Method methodToUse = MethodIntrospector.selectInvocableMethod(method, targetType);
		if (Modifier.isPrivate(methodToUse.getModifiers()) && !Modifier.isStatic(methodToUse.getModifiers()) &&
				SpringProxy.class.isAssignableFrom(targetType)) {
			throw new IllegalStateException(String.format(
					"Need to invoke method '%s' found on proxy for target class '%s' but cannot " +
					"be delegated to target bean. Switch its visibility to package or protected.",
					method.getName(), method.getDeclaringClass().getSimpleName()));
		}
		return methodToUse;
	}

	/**
	 * 判断给定方法是否为 "equals" 方法。
	 * @see java.lang.Object#equals
	 */
	public static boolean isEqualsMethod(@Nullable Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}

	/**
	 * 判断给定方法是否为 "hashCode" 方法。
	 * @see java.lang.Object#hashCode
	 */
	public static boolean isHashCodeMethod(@Nullable Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}

	/**
	 * 判断给定方法是否为 "toString" 方法。
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(@Nullable Method method) {
		return ReflectionUtils.isToStringMethod(method);
	}

	/**
	 * 判断给定方法是否为 "finalize" 方法。
	 * @see java.lang.Object#finalize()
	 */
	public static boolean isFinalizeMethod(@Nullable Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterCount() == 0);
	}

	/**
	 * 给定可能来自接口的方法及当前 AOP 调用使用的目标类，
	 * 查找对应的目标方法（若存在）。
	 * 例如方法可能是 {@code IFoo.bar()}，目标类可能是 {@code DefaultFoo}，
	 * 此时方法可能是 {@code DefaultFoo.bar()}。
	 * 从而可找到该方法上的属性。
	 * <p><b>注意：</b>与 {@link org.springframework.util.ClassUtils#getMostSpecificMethod} 不同，
	 * 本方法解析桥接方法，以从<i>原始</i>方法定义获取属性。
	 * @param method 待调用的方法，可能来自接口
	 * @param targetClass 当前调用的目标类
	 * （可为 {@code null} 或可能未实现该方法）
	 * @return 具体目标方法；若 {@code targetClass} 未实现则返回原方法
	 * @see org.springframework.util.ClassUtils#getMostSpecificMethod
	 * @see org.springframework.core.BridgeMethodResolver#getMostSpecificMethod
	 */
	public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
		Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
		return BridgeMethodResolver.getMostSpecificMethod(method, specificTargetClass);
	}

	/**
	 * 给定切入点是否能在给定类上应用？
	 * <p>这是重要测试，可用于对类优化掉切入点。
	 * @param pc 待检查的静态或动态切入点
	 * @param targetClass 待测试的类
	 * @return 切入点是否可应用于任意方法
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass) {
		return canApply(pc, targetClass, false);
	}

	/**
	 * 给定切入点是否能在给定类上应用？
	 * <p>这是重要测试，可用于对类优化掉切入点。
	 * @param pc 待检查的静态或动态切入点
	 * @param targetClass 待测试的类
	 * @param hasIntroductions 本 Bean 的 advisor 链是否包含引入
	 * @return 切入点是否可应用于任意方法
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		if (methodMatcher == MethodMatcher.TRUE) {
			// 若本就会匹配任意方法，则无需遍历方法...
			return true;
		}

		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher iamm) {
			introductionAwareMethodMatcher = iamm;
		}

		Set<Class<?>> classes = new LinkedHashSet<>();
		if (!Proxy.isProxyClass(targetClass)) {
			classes.add(ClassUtils.getUserClass(targetClass));
		}
		classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));

		for (Class<?> clazz : classes) {
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
			for (Method method : methods) {
				if (introductionAwareMethodMatcher != null ?
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions) :
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 给定 advisor 是否能在给定类上应用？
	 * 这是重要测试，可用于对类优化掉 advisor。
	 * @param advisor 待检查的 advisor
	 * @param targetClass 待测试的类
	 * @return 切入点是否可应用于任意方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}

	/**
	 * 给定 advisor 是否能在给定类上应用？
	 * <p>这是重要测试，可用于对类优化掉 advisor。
	 * 本版本还考虑引入（用于 IntroductionAwareMethodMatcher）。
	 * @param advisor 待检查的 advisor
	 * @param targetClass 待测试的类
	 * @param hasIntroductions 本 Bean 的 advisor 链是否包含引入
	 * @return 切入点是否可应用于任意方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		if (advisor instanceof IntroductionAdvisor ia) {
			return ia.getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor pca) {
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// 无切入点，假定可应用。
			return true;
		}
	}

	/**
	 * 确定 {@code candidateAdvisors} 列表中适用于给定类的子列表。
	 * @param candidateAdvisors 待评估的 Advisor
	 * @param clazz 目标类
	 * @return 可应用于给定类对象的 Advisor 子列表
	 * （可能是原 List 本身）
	 */
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		List<Advisor> eligibleAdvisors = new ArrayList<>();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
				eligibleAdvisors.add(candidate);
			}
		}
		boolean hasIntroductions = !eligibleAdvisors.isEmpty();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor) {
				// 已处理
				continue;
			}
			if (canApply(candidate, clazz, hasIntroductions)) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * 作为 AOP 方法调用的一部分，通过反射调用给定目标。
	 * @param target 目标对象
	 * @param method 要调用的方法
	 * @param args 方法参数
	 * @return 调用结果（若有）
	 * @throws Throwable 若目标方法抛出
	 * @throws org.springframework.aop.AopInvocationException 若反射出错
	 */
	public static @Nullable Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, @Nullable Object[] args)
			throws Throwable {

		// 使用反射调用方法。
		try {
			Method originalMethod = BridgeMethodResolver.findBridgedMethod(method);
			ReflectionUtils.makeAccessible(originalMethod);
			return (COROUTINES_REACTOR_PRESENT && KotlinDetector.isSuspendingFunction(originalMethod) ?
					KotlinDelegate.invokeSuspendingFunction(originalMethod, target, args) : originalMethod.invoke(target, args));
		}
		catch (InvocationTargetException ex) {
			// 被调用方法抛出受检异常。
			// 必须重新抛出；客户端不会看到拦截器。
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" +
					method + "] on target [" + target + "]", ex);
		}
		catch (IllegalAccessException | InaccessibleObjectException ex) {
			throw new AopInvocationException("Could not access method [" + method + "]", ex);
		}
	}


	/**
	 * 内部类，避免运行时对 Kotlin 的硬依赖。
	 */
	private static class KotlinDelegate {

		public static Object invokeSuspendingFunction(Method method, @Nullable Object target, @Nullable Object... args) {
			Continuation<?> continuation = (Continuation<?>) args[args.length -1];
			Assert.state(continuation != null, "No Continuation available");
			CoroutineContext context = continuation.getContext().minusKey(Job.Key);
			return CoroutinesUtils.invokeSuspendingFunction(context, method, target, args);
		}
	}

}
