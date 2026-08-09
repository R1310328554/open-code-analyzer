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
 * Spring对AOP联盟{@link org.aopalliance.intercept.MethodInvocation}接口的实现，实现了扩展的{@link
 * org.springframework.aop.ProxyMethodInvocation}接口。
 * <p>使用反射调用目标对象。子类可以重写 {@link #invokeJoinpoint()} 方法来更改此行为，因此这对于更专门的 MethodInitation 实现来说也
 * 是一个有用的基类。
 * <p> 可以使用 {@link #invocableClone()} 方法克隆调用，重复调用 {@link #proceed()}（每个克隆一次）。还可以使用 {@link
 * #setUserAttribute} / {@link #getUserAttribute} 方法将自定义属性附加到调用。
 * <p><b>NOTE:</b> 此类被视为内部类，不应直接访问。它公开的唯一原因是与现有框架集成（例如，Pitchfork）的兼容性。出于任何其他目的，请改用 {@link P
 * roxyMethodInvocation} 接口。
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

	/** 代理相关状态（`proxy`）。 */
	protected final Object proxy;

	/** 目标相关状态（`target`）。 */
	protected final @Nullable Object target;

	/** 方法相关状态（`method`）。 */
	protected final Method method;

	/** `arguments`：该类的成员状态。 */
	protected @Nullable Object[] arguments;

	/** 类相关状态（`targetClass`）。 */
	private final @Nullable Class<?> targetClass;

	/**
	 * 为此调用延迟初始化的用户特定属性的映射。
	 */
	private @Nullable Map<String, Object> userAttributes;

	/**
	 * 需要动态检查的MethodInterceptor和InterceptorAndDynamicMethodMatcher列表。
	 */
	protected final List<?> interceptorsAndDynamicMethodMatchers;

	/**
	 * 我们正在调用的当前拦截器的索引从 0 开始。 -1 直到我们调用: 然后是当前拦截器。
	 */
	private int currentInterceptorIndex = -1;


	/**
	 * 使用给定参数构造一个新的 ReflectiveMethodInitation。
	 * @param proxy 进行调用的代理对象
	 * @param target 要调用的目标对象
	 * @param method 调用的方法
	 * @param arguments 调用该方法的参数
	 * @param targetClass 目标类，用于 MethodMatcher 调用
	 * @param interceptorsAndDynamicMethodMatchers 应应用的拦截器，以及需要在运行时评估的任何 InterceptorAndDynamicMethodMatchers。该结构中包含的 MethodMatchers 必须已经被发现尽可能静态地匹配。传递数组可能会快 10% 左右，但会使代码复杂化。它只适用于静态切入点。
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


	/**
	 * 获取 Proxy（`Proxy`）。
	 */
	@Override
	public final Object getProxy() {
		return this.proxy;
	}

	/**
	 * 获取 This（`This`）。
	 */
	@Override
	public final @Nullable Object getThis() {
		return this.target;
	}

	/**
	 * 获取 Static Part（`StaticPart`）。
	 */
	@Override
	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * 返回在代理接口上调用的方法。可能与该接口的底层实现上调用的方法相对应，也可能不相对应。
	 */
	@Override
	public final Method getMethod() {
		return this.method;
	}

	/**
	 * 获取 Arguments（`Arguments`）。
	 */
	@Override
	public final @Nullable Object[] getArguments() {
		return this.arguments;
	}

	/**
	 * 设置 Arguments（`Arguments`）。
	 */
	@Override
	public void setArguments(@Nullable Object... arguments) {
		this.arguments = arguments;
	}


	/**
	 * 方法 `proceed`：完成本类中与「proceed」相关的职责。
	 */
	@Override
	public @Nullable Object proceed() throws Throwable {
		// 我们从索引 -1 开始并提前递增。
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}

		Object interceptorOrInterceptionAdvice =
				this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher dm) {
			// 在这里评估动态方法匹配器：静态部分已经有
			// 已被评估并发现匹配。
			Class<?> targetClass = (this.targetClass != null ? this.targetClass : this.method.getDeclaringClass());
			if (dm.matcher().matches(this.method, targetClass, this.arguments)) {
				return dm.interceptor().invoke(this);
			}
			else {
				// 动态匹配失败。
				// 跳过此拦截器并调用链中的下一个拦截器。
				return proceed();
			}
		}
		else {
			// 它是一个拦截器，所以我们只需调用它：切入点将具有
			// 在构造该对象之前已静态评估。
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}

	/**
	 * 使用反射调用连接点。子类可以重写它以使用自定义调用。
	 * @return 连接点的返回值
	 * @throws Throwable 如果调用连接点导致异常
	 */
	protected @Nullable Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
	}


	/**
	 * 此实现返回此调用对象的浅表副本，包括原始参数数组的独立副本。 <p>在这种情况下我们想要一个浅拷贝：我们想要使用相同的拦截器链和其他对象引用，但我们想要当前拦截器索引的独立值。
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
	 * 此实现使用克隆的给定参数数组返回此调用对象的浅表副本。 <p>在这种情况下我们想要一个浅拷贝：我们想要使用相同的拦截器链和其他对象引用，但我们想要当前拦截器索引的独立值。
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MethodInvocation invocableClone(@Nullable Object... arguments) {
		// 强制初始化用户属性Map，
		// 用于在克隆中拥有共享的地图引用。
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}

		// 创建方法调用克隆。
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


	/**
	 * 设置 User Attribute（`UserAttribute`）。
	 */
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

	/**
	 * 获取 User Attribute（`UserAttribute`）。
	 */
	@Override
	public @Nullable Object getUserAttribute(String key) {
		return (this.userAttributes != null ? this.userAttributes.get(key) : null);
	}

	/**
	 * 返回与此调用关联的用户属性。此方法提供了 ThreadLocal 的调用绑定替代方案。 <p>该映射是延迟初始化的，并且在AOP框架本身中不使用。
	 * @return 与此调用关联的用户属性（绝不是 {@code null}）
	 */
	public Map<String, Object> getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}
		return this.userAttributes;
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		// 不要在目标上执行 toString，它可能会被代理。
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
