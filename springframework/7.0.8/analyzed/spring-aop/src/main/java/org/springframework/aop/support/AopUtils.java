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
 * AOP 支持代码的实用方法。
 * <p>主要供Spring的AOP支持内部使用。
 * <p>请参阅 {@link org.springframework.aop.framework.AopProxyUtils}，了解特定于框架的 AOP
 * 实用方法的集合，这些方法依赖于 Spring 的 AOP 框架实现的内部结构。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

	/**
	 * 判断是否 Present。
	 */
	private static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", AopUtils.class.getClassLoader());


	/**
	 * 检查给定对象是 JDK 动态代理还是 CGLIB 代理。 <p> 此方法还检查给定对象是否是 {@link SpringProxy} 的实例。
	 * @param object 检查对象
	 * @see #isJdkDynamicProxy
	 * @see #isCglibProxy
	 */
	@Contract("null -> false")
	public static boolean isAopProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) ||
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)));
	}

	/**
	 * 检查给定对象是否是 JDK 动态代理。 <p> 此方法超越了 {@link Proxy#isProxyClass(Class)} 的实现，还额外检查给定对象是否是
	 * {@link SpringProxy} 的实例。
	 * @param object 检查对象
	 * @see java.lang.reflect.Proxy#isProxyClass
	 */
	@Contract("null -> false")
	public static boolean isJdkDynamicProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
	}

	/**
	 * 检查给定对象是否是 CGLIB 代理。 <p> 此方法超越了 {@link ClassUtils#isCglibProxy(Object)} 的实现，还额外检查给定对象是否是
	 * {@link SpringProxy} 的实例。
	 * @param object 检查对象
	 * @see ClassUtils#isCglibProxy(Object)
	 */
	@Contract("null -> false")
	public static boolean isCglibProxy(@Nullable Object object) {
		return (object instanceof SpringProxy &&
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
	}

	/**
	 * 确定给定 bean 实例的目标类（可能是 AOP 代理）。 <p> 返回 AOP 代理的目标类或普通类。
	 * @param candidate 要检查的实例（可能是 AOP 代理）
	 * @return 目标类（或给定对象的普通类作为后备；绝不是 {@code null}）
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
	 * 选择目标类型上的可调用方法：给定方法本身（如果实际在目标类型上公开），或者目标类型的接口之一或目标类型本身上的相应方法。
	 * @param method 检查方法
	 * @param targetType 搜索方法的目标类型（通常是 AOP 代理）
	 * @return 目标类型上相应的可调用方法
	 * @throws IllegalStateException 如果给定的方法在给定的目标类型上不可调用（通常是由于代理不匹配）
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
	 * 确定给定方法是否是“等于”方法。
	 * @see java.lang.Object#equals
	 */
	public static boolean isEqualsMethod(@Nullable Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}

	/**
	 * 确定给定方法是否是“hashCode”方法。
	 * @see java.lang.Object#hashCode
	 */
	public static boolean isHashCodeMethod(@Nullable Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}

	/**
	 * 确定给定方法是否是“toString”方法。
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(@Nullable Method method) {
		return ReflectionUtils.isToStringMethod(method);
	}

	/**
	 * 确定给定方法是否是“finalize”方法。
	 * @see java.lang.Object#finalize()
	 */
	public static boolean isFinalizeMethod(@Nullable Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterCount() == 0);
	}

	/**
	 * 给定一个方法，该方法可能来自接口，以及当前AOP调用中使用的目标类，如果有，则查找对应的目标方法。例如，方法可以是 {@code IFoo.bar()}，目标类可以是 {@co
	 * de DefaultFoo}。在这种情况下，该方法可以是{@code DefaultFoo.bar()}。这使得可以找到该方法的属性。 <p><b>NOTE:</b> 与 {@
	 * link org.springframework.util.ClassUtils#getMostSpecificMethod} 相比，此方法解析桥接方法，以便从 <i>orig
	 * inal</i> 方法定义中检索属性。
	 * @param method 要调用的方法，可能来自接口
	 * @param targetClass 当前调用的目标类（可以是 {@code null} 或者甚至可能不实现该方法）
	 * @return 特定目标方法，如果 {@code targetClass} 未实现，则使用原始方法
	 * @see org.springframework.util.ClassUtils#getMostSpecificMethod
	 * @see org.springframework.core.BridgeMethodResolver#getMostSpecificMethod
	 */
	public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
		Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
		return BridgeMethodResolver.getMostSpecificMethod(method, specificTargetClass);
	}

	/**
	 * 给定的切入点是否可以应用于给定的类？ <p>这是一个重要的测试，因为它可用于优化类的切入点。
	 * @param pc 要检查的静态或动态切入点
	 * @param targetClass 要测试的类
	 * @return 切入点可以应用于任何方法
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass) {
		return canApply(pc, targetClass, false);
	}

	/**
	 * 给定的切入点是否可以应用于给定的类？ <p>这是一个重要的测试，因为它可用于优化类的切入点。
	 * @param pc 要检查的静态或动态切入点
	 * @param targetClass 要测试的类
	 * @param hasIntroductions 该 bean 的顾问链是否包含任何介绍
	 * @return 切入点可以应用于任何方法
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		if (methodMatcher == MethodMatcher.TRUE) {
			// 如果我们匹配任何方法，则无需迭代方法......
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
	 * 给定的顾问可以申请给定的课程吗？这是一个重要的测试，因为它可以用来优化班级的顾问。
	 * @param advisor 顾问检查
	 * @param targetClass 我们正在测试的类
	 * @return 切入点可以应用于任何方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}

	/**
	 * 给定的顾问可以申请给定的课程吗？ <p>这是一个重要的测试，因为它可用于优化课程的顾问。此版本还考虑了介绍（对于IntroductionAwareMethodMatchers）
	 * 。
	 * @param advisor 顾问检查
	 * @param targetClass 我们正在测试的类
	 * @param hasIntroductions 该 bean 的顾问链是否包含任何介绍
	 * @return 切入点可以应用于任何方法
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		if (advisor instanceof IntroductionAdvisor ia) {
			return ia.getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor pca) {
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// 它没有切入点，因此我们假设它适用。
			return true;
		}
	}

	/**
	 * 确定适用于给定类的 {@code candidateAdvisors} 列表的子列表。
	 * @param candidateAdvisors 顾问进行评估
	 * @param clazz 目标类别
	 * @return 可应用于给定类的对象的顾问程序（可能是按原样传入的列表）
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
				// 已经处理了
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
	 * @param method 调用的方法
	 * @param args 该方法的参数
	 * @return 调用结果（如果有）
	 * @throws Throwable 如果由目标方法抛出
	 * @throws org.springframework.aop.AopInvocationException 如果出现反射错误
	 */
	public static @Nullable Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, @Nullable Object[] args)
			throws Throwable {

		// 使用反射来调用该方法。
		try {
			Method originalMethod = BridgeMethodResolver.findBridgedMethod(method);
			ReflectionUtils.makeAccessible(originalMethod);
			return (COROUTINES_REACTOR_PRESENT && KotlinDetector.isSuspendingFunction(originalMethod) ?
					KotlinDelegate.invokeSuspendingFunction(originalMethod, target, args) : originalMethod.invoke(target, args));
		}
		catch (InvocationTargetException ex) {
			// 调用的方法引发了已检查的异常。
			// 我们必须重新扔掉它。客户端不会看到拦截器。
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
	 * 内部类以避免运行时对 Kotlin 的硬依赖。
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
