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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;

/**
 * Spring 对 AOP Alliance
 * {@link org.aopalliance.intercept.MethodInvocation} 接口的实现，
 * 同时实现扩展的 {@link org.springframework.aop.ProxyMethodInvocation} 接口。
 *
 * <p>通过反射调用目标对象。子类可覆盖 {@link #invokeJoinpoint()} 方法
 * 改变此行为，因此也是更专用 MethodInvocation 实现的有用基类。
 *
 * <p>可使用 {@link #invocableClone()} 克隆调用，
 * 对每个克隆重复调用 {@link #proceed()}。
 * 也可通过 {@link #setUserAttribute} / {@link #getUserAttribute}
 * 为调用附加自定义属性。
 *
 * <p><b>注意：</b> 本类视为内部类，不应直接访问。
 * 公开的唯一原因是与现有框架集成（如 Pitchfork）兼容。
 * 其他用途请使用 {@link ProxyMethodInvocation} 接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @see #invokeJoinpoint
 * @see #proceed
 * @see #invocableClone
 * @see #setUserAttribute
 * @see #getUserAttribute
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

	protected final Object proxy;

	protected final @Nullable Object target;

	protected final Method method;

	protected @Nullable Object[] arguments;

	private final @Nullable Class<?> targetClass;

	/**
	 * 本调用用户特定属性的延迟初始化映射。
	 */
	private @Nullable Map<String, Object> userAttributes;

	/**
	 * 需要动态检查的 MethodInterceptor 与 InterceptorAndDynamicMethodMatcher 列表。
	 */
	protected final List<?> interceptorsAndDynamicMethodMatchers;

	/**
	 * 当前正在调用的拦截器索引（从 0 起）。
	 * 调用前为 -1；调用后为当前拦截器索引。
	 */
	private int currentInterceptorIndex = -1;


	/**
	 * 以给定参数构造新的 ReflectiveMethodInvocation。
	 * @param proxy 发起调用的代理对象
	 * @param target 要调用的目标对象
	 * @param method 要调用的方法
	 * @param arguments 方法调用参数
	 * @param targetClass 目标类，用于 MethodMatcher 调用
	 * @param interceptorsAndDynamicMethodMatchers 应应用的拦截器，
	 * 以及运行时需评估的 InterceptorAndDynamicMethodMatcher。
	 * 此结构中包含的 MethodMatcher 必须已在静态范围内尽可能完成匹配。
	 * 传递数组可能快约 10%，但会增加代码复杂度，且仅适用于静态切入点。
	 */
	protected ReflectiveMethodInvocation(
			Object proxy, @Nullable Object target, Method method, @Nullable Object[] arguments,
			@Nullable Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		this.method = BridgeMethodResolver.findBridgedMethod(method);
		this.arguments = AopProxyUtils.adaptArgumentsIfNecessary(method, arguments);
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
	}


	@Override
	public final Object getProxy() {
		return this.proxy;
	}

	@Override
	public final @Nullable Object getThis() {
		return this.target;
	}

	@Override
	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * 返回在代理接口上调用的方法。
	 * 可能与底层实现类上调用的方法不一致。
	 */
	@Override
	public final Method getMethod() {
		return this.method;
	}

	@Override
	public final @Nullable Object[] getArguments() {
		return this.arguments;
	}

	@Override
	public void setArguments(@Nullable Object... arguments) {
		this.arguments = arguments;
	}


	@Override
	public @Nullable Object proceed() throws Throwable {
		// 从索引 -1 开始，并提前递增。
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}

		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher dm) {
			// 在此评估动态方法匹配器：静态部分已评估并确认匹配。
			Class<?> targetClass = (this.targetClass != null ? this.targetClass : this.method.getDeclaringClass());
			if (dm.matcher().matches(this.method, targetClass, this.arguments)) {
				return dm.interceptor().invoke(this);
			}
			else {
				// 动态匹配失败。
				// 跳过本拦截器，调用链中下一个。
				return proceed();
			}
		}
		else {
			// 为拦截器，直接调用：切入点在本对象构造前已静态评估。
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

	/**
	 * 通过反射调用连接点。
	 * 子类可覆盖以使用自定义调用方式。
	 * @return 连接点返回值
	 * @throws Throwable 若调用连接点导致异常
	 */
	protected @Nullable Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
	}


	/**
	 * 本实现返回本调用对象的浅拷贝，
	 * 包含原始参数数组的独立副本。
	 * <p>此处需要浅拷贝：使用相同拦截器链及其他对象引用，
	 * 但当前拦截器索引须为独立值。
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MethodInvocation invocableClone() {
		@Nullable Object[] cloneArguments = this.arguments;
		if (this.arguments.length > 0) {
			// 构建参数数组的独立副本。
			cloneArguments = this.arguments.clone();
		}
		return invocableClone(cloneArguments);
	}

	/**
	 * 本实现返回本调用对象的浅拷贝，
	 * 克隆使用给定参数数组。
	 * <p>此处需要浅拷贝：使用相同拦截器链及其他对象引用，
	 * 但当前拦截器索引须为独立值。
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MethodInvocation invocableClone(@Nullable Object... arguments) {
		// 强制初始化用户属性 Map，
		// 以便克隆共享 Map 引用。
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}

		// 创建 MethodInvocation 克隆。
		try {
			ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
			clone.arguments = arguments;
			return clone;
		}
		catch (CloneNotSupportedException ex) {
			throw new IllegalStateException(
					"Should be able to clone object of type [" + getClass() + "]: " + ex);
		}
	}


	@Override
	public void setUserAttribute(String key, @Nullable Object value) {
		if (value != null) {
			if (this.userAttributes == null) {
				this.userAttributes = new HashMap<>();
			}
			this.userAttributes.put(key, value);
		}
		else {
			if (this.userAttributes != null) {
				this.userAttributes.remove(key);
			}
		}
	}

	@Override
	public @Nullable Object getUserAttribute(String key) {
		return (this.userAttributes != null ? this.userAttributes.get(key) : null);
	}

	/**
	 * 返回与本调用关联的用户属性。
	 * 本方法提供调用绑定的 ThreadLocal 替代方案。
	 * <p>此 Map 延迟初始化，AOP 框架本身不使用。
	 * @return 与本调用关联的用户属性（永不为 {@code null}）
	 */
	public Map<String, Object> getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}
		return this.userAttributes;
	}


	@Override
	public String toString() {
		// 不对 target 做 toString，它可能被代理。
		StringBuilder sb = new StringBuilder("ReflectiveMethodInvocation: ");
		sb.append(this.method).append("; ");
		if (this.target == null) {
			sb.append("target is null");
		}
		else {
			sb.append("target is of class [").append(this.target.getClass().getName()).append(']');
		}
		return sb.toString();
	}

}
