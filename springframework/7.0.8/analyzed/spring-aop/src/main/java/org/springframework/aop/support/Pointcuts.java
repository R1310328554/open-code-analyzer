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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;

/**
 * 用于匹配 getter 和 setter 的切入点常量，以及用于操作和评估切入点的静态方法。
 * <p> 这些方法对于使用并集和交集方法组成切入点特别有用。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class Pointcuts {

	/**
	 */
	public static final Pointcut SETTERS = SetterPointcut.INSTANCE;

	/**
	 */
	public static final Pointcut GETTERS = GetterPointcut.INSTANCE;


	/**
	 * 匹配给定切入点的 <b> 或 </b>（或两者）匹配的所有方法。
	 * @param pc1 第一个切入点
	 * @param pc2 第二个切入点
	 * @return 与给定切入点匹配的所有方法相匹配的不同切入点
	 */
	public static Pointcut union(Pointcut pc1, Pointcut pc2) {
		return new ComposablePointcut(pc1).union(pc2);
	}

	/**
	 * 匹配 <b> 和 </b> 给定切入点匹配的所有方法。
	 * @param pc1 第一个切入点
	 * @param pc2 第二个切入点
	 * @return 与两个给定切入点都匹配的所有方法相匹配的不同切入点
	 */
	public static Pointcut intersection(Pointcut pc1, Pointcut pc2) {
		return new ComposablePointcut(pc1).intersection(pc2);
	}

	/**
	 * 对切​​入点匹配执行最便宜的检查。
	 * @param pointcut 要匹配的切入点
	 * @param method 候选方法
	 * @param targetClass 目标类别
	 * @param args 方法的参数
	 * @return 有一个运行时匹配
	 */
	public static boolean matches(Pointcut pointcut, Method method, Class<?> targetClass, Object... args) {
		Assert.notNull(pointcut, "Pointcut must not be null");
		if (pointcut == Pointcut.TRUE) {
			return true;
		}
		if (pointcut.getClassFilter().matches(targetClass)) {
			// 只检查是否通过了第一个障碍。
			MethodMatcher mm = pointcut.getMethodMatcher();
			if (mm.matches(method, targetClass)) {
				// 我们可能需要额外的运行时（参数）检查。
				return (!mm.isRuntime() || mm.matches(method, targetClass, args));
			}
		}
		return false;
	}


	/**
	 * 与 bean 属性设置器匹配的切入点实现。
	 */
	@SuppressWarnings("serial")
	private static class SetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

		public static final SetterPointcut INSTANCE = new SetterPointcut();

		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return (method.getName().startsWith("set") &&
					method.getParameterCount() == 1 &&
					method.getReturnType() == void.class);
		}

		private Object readResolve() {
			return INSTANCE;
		}

		@Override
		public String toString() {
			return "Pointcuts.SETTERS";
		}
	}


	/**
	 * 与 bean 属性 getter 匹配的切入点实现。
	 */
	@SuppressWarnings("serial")
	private static class GetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

		public static final GetterPointcut INSTANCE = new GetterPointcut();

		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return (method.getName().startsWith("get") &&
					method.getParameterCount() == 0 &&
					method.getReturnType() != void.class);
		}

		private Object readResolve() {
			return INSTANCE;
		}

		@Override
		public String toString() {
			return "Pointcuts.GETTERS";
		}
	}

}
