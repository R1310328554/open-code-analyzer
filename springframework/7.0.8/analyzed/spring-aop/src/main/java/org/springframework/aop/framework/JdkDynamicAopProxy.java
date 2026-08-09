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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Spring AOP框架的基于JDK的{@link AopProxy}实现，基于JDK {@link java.lang.reflect.Proxy dynamic
 * proxies}。
 * <p>创建动态代理，实现AopProxy公开的接口。动态代理 <i> 不能使用 </i> 来代理类中定义的方法，而不是接口中定义的方法。
 * 这种类型的 <p> 对象应通过由 {@link AdvisedSupport} 类配置的代理工厂获取。该类是 Spring AOP 框架的内部类，不需要由客户端代码直接使用。
 * 如果底层（目标）类是线程安全的，则使用此类创建的 <p>Proxies 将是线程安全的。
 * 只要所有 Advisor（包括 Advice 和 Pointcut）和 TargetSource 都是可序列化的，<p>Proxies 都是可序列化的。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @author Sergey Tsypanov
 * @author Sebastien Deleuze
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	/**
	 */
	private static final long serialVersionUID = 5531744639992436476L;


	private static final String COROUTINES_FLOW_CLASS_NAME = "kotlinx.coroutines.flow.Flow";

	/**
	 * 判断是否 Present。
	 */
	private static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", JdkDynamicAopProxy.class.getClassLoader());

	/**
	 */
	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/**
	 */
	private final AdvisedSupport advised;

	/**
	 */
	private transient ProxiedInterfacesCache cache;


	/**
	 * 为给定的 AOP 配置构造一个新的 JdkDynamicAopProxy。
	 * @param config AOP 配置为 AdvisedSupport 对象
	 * @throws AopConfigException 如果配置无效。在这种情况下，我们尝试抛出一个信息性异常，而不是让以后发生神秘的故障。
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		this.advised = config;

		// 如果尚未缓存，则初始化 ProxiedInterfacesCache
		ProxiedInterfacesCache cache;
		if (config.proxyMetadataCache instanceof ProxiedInterfacesCache proxiedInterfacesCache) {
			cache = proxiedInterfacesCache;
		}
		else {
			cache = new ProxiedInterfacesCache(config);
			config.proxyMetadataCache = cache;
		}
		this.cache = cache;
	}


	/**
	 * 获取 Proxy（`Proxy`）。
	 */
	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 获取 Proxy（`Proxy`）。
	 */
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
		}
		return Proxy.newProxyInstance(determineClassLoader(classLoader), this.cache.proxiedInterfaces, this);
	}

	/**
	 * 获取 Proxy Class（`ProxyClass`）。
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
		return Proxy.getProxyClass(determineClassLoader(classLoader), this.cache.proxiedInterfaces);
	}

	/**
	 * 确定是否建议使用 JDK 引导程序或平台加载器 -> 使用可以看到 Spring 基础结构类的更高级别加载器。
	 */
	private ClassLoader determineClassLoader(@Nullable ClassLoader classLoader) {
		if (classLoader == null) {
			// JDK 引导加载程序 -> 使用 spring-aop ClassLoader 代替。
			return getClass().getClassLoader();
		}
		if (classLoader.getParent() == null) {
			// 可能是 JDK 9+ 上的 JDK 平台加载器
			ClassLoader aopClassLoader = getClass().getClassLoader();
			ClassLoader aopParent = aopClassLoader.getParent();
			while (aopParent != null) {
				if (classLoader == aopParent) {
					// 建议的 ClassLoader 是 spring-aop ClassLoader 的祖先
					// -> 使用 spring-aop ClassLoader 本身。
					return aopClassLoader;
				}
				aopParent = aopParent.getParent();
			}
		}
		// 常规情况：按原样使用建议的类加载器。
		return classLoader;
	}


	/**
	 * {@code InvocationHandler.invoke} 的实现。 <p>Callers 将准确地看到目标抛出的异常，除非钩子方法抛出异常。
	 */
	@Override
	public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Object target = null;

		try {
			if (!this.cache.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// 目标本身不实现 equals(Object) 方法。
				return equals(args[0]);
			}
			else if (!this.cache.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// 目标本身不实现 hashCode() 方法。
				return hashCode();
			}
			else if (method.getDeclaringClass() == DecoratingProxy.class) {
				// 仅声明了 getDecolatedClass() -> 分派到代理配置。
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}
			else if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// 使用代理配置对 ProxyConfig 进行服务调用...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			if (this.advised.isExposeProxy()) {
				// 如有必要，使调用可用。
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// 尽可能晚地到达，以尽量减少我们“拥有”目标的时间，
			// 万一它来自池子。
			target = targetSource.getTarget();
			Class<?> targetClass = (target != null ? target.getClass() : null);

			// 获取该方法的拦截链。
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// 看看我们是否有任何建议。如果我们不这样做，我们可以直接依靠
			// 目标的反射调用，并避免创建 MethodInitation。
			if (chain.isEmpty()) {
				// 我们可以跳过创建方法调用：直接调用目标
				// 请注意，最终的调用者必须是 InvokerInterceptor，因此我们知道它确实如此
				// 只是对目标进行反射操作，没有热交换或花哨的代理。
				@Nullable Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				// 我们需要创建一个方法调用...
				MethodInvocation invocation =
						new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// 通过拦截器链前往连接点。
				retVal = invocation.proceed();
			}

			// 必要时按摩返回值。
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target &&
					returnType != Object.class && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// 特殊情况：它返回“this”和方法的返回类型
				// 是类型兼容的。请注意，如果目标设定，我们无能为力
				// 在另一个返回的对象中对其自身的引用。
				retVal = proxy;
			}
			else if (retVal == null && returnType != void.class && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			if (COROUTINES_REACTOR_PRESENT && KotlinDetector.isSuspendingFunction(method)) {
				return COROUTINES_FLOW_CLASS_NAME.equals(new MethodParameter(method, -1).getParameterType().getName()) ?
						CoroutinesUtils.asFlow(retVal) : CoroutinesUtils.awaitSingleOrNull(retVal, args[args.length - 1]);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// 必须来自 TargetSource。
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// 恢复旧代理。
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * 平等意味着接口、顾问程序和 TargetSource 是平等的。 <p> 比较的对象可能是 JdkDynamicAopProxy 实例本身，也可能是包装
	 * JdkDynamicAopProxy 实例的动态代理。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy jdkDynamicAopProxy) {
			otherProxy = jdkDynamicAopProxy;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy jdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = jdkDynamicAopProxy;
		}
		else {
			// 没有有效的比较...
			return false;
		}

		// 如果我们到达这里，otherProxy 就是另一个 AopProxy。
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * Proxy 使用 TargetSource 的哈希码。
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；只需在反序列化后初始化状态即可。
		ois.defaultReadObject();

		// 初始化瞬态字段。
		this.cache = new ProxiedInterfacesCache(this.advised);
	}


	/**
	 * 完整代理接口和派生元数据的持有者，将缓存在 {@link AdvisedSupport#proxyMetadataCache} 中。
	 * @since 6.1.3
	 */
	private static final class ProxiedInterfacesCache {

		final Class<?>[] proxiedInterfaces;

		final boolean equalsDefined;

		final boolean hashCodeDefined;

		ProxiedInterfacesCache(AdvisedSupport config) {
			this.proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(config, true);

			// 查找任何可能定义的 {@link #equals} 或 {@link #hashCode} 方法
			// 在提供的一组接口上。
			boolean equalsDefined = false;
			boolean hashCodeDefined = false;
			for (Class<?> proxiedInterface : this.proxiedInterfaces) {
				Method[] methods = proxiedInterface.getDeclaredMethods();
				for (Method method : methods) {
					if (AopUtils.isEqualsMethod(method)) {
						equalsDefined = true;
						if (hashCodeDefined) {
							break;
						}
					}
					if (AopUtils.isHashCodeMethod(method)) {
						hashCodeDefined = true;
						if (equalsDefined) {
							break;
						}
					}
				}
				if (equalsDefined && hashCodeDefined) {
					break;
				}
			}
			this.equalsDefined = equalsDefined;
			this.hashCodeDefined = hashCodeDefined;
		}
	}

}
