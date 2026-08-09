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
 * Spring AOP 框架基于 JDK {@link java.lang.reflect.Proxy 动态代理}
 * 的 {@link AopProxy} 实现。
 *
 * <p>创建动态代理，实现 AopProxy 暴露的接口。
 * 动态代理<i>不能</i>用于代理类中定义的方法，仅适用于接口。
 *
 * <p>此类对象应通过由 {@link AdvisedSupport} 配置的代理工厂获取。
 * 本类为 Spring AOP 框架内部类，客户端代码无需直接使用。
 *
 * <p>若底层（目标）类线程安全，则本类创建的代理也线程安全。
 *
 * <p>当所有 Advisor（含 Advice 与 Pointcut）及 TargetSource 可序列化时，
 * 代理可序列化。
 *
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

	/** 使用 Spring 1.2 的 serialVersionUID 以保持互操作性。 */
	private static final long serialVersionUID = 5531744639992436476L;


	private static final String COROUTINES_FLOW_CLASS_NAME = "kotlinx.coroutines.flow.Flow";

	private static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", JdkDynamicAopProxy.class.getClassLoader());

	/** 使用 static Log 以避免序列化问题。 */
	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/** 用于配置本代理的配置对象。 */
	private final AdvisedSupport advised;

	/** 缓存在 {@link AdvisedSupport#proxyMetadataCache} 中。 */
	private transient ProxiedInterfacesCache cache;


	/**
	 * 为给定 AOP 配置构造新的 JdkDynamicAopProxy。
	 * @param config 作为 AdvisedSupport 对象的 AOP 配置
	 * @throws AopConfigException 若配置无效；
	 * 此时抛出明确异常，而非稍后出现难以排查的失败。
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		this.advised = config;

		// 若尚未缓存，则初始化 ProxiedInterfacesCache
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


	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
		}
		return Proxy.newProxyInstance(determineClassLoader(classLoader), this.cache.proxiedInterfaces, this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
		return Proxy.getProxyClass(determineClassLoader(classLoader), this.cache.proxiedInterfaces);
	}

	/**
	 * 判断是否建议使用 JDK 引导或平台类加载器 ->
	 * 改用能加载 Spring 基础设施类的更高级类加载器。
	 */
	private ClassLoader determineClassLoader(@Nullable ClassLoader classLoader) {
		if (classLoader == null) {
			// JDK 引导类加载器 -> 改用 spring-aop ClassLoader。
			return getClass().getClassLoader();
		}
		if (classLoader.getParent() == null) {
			// 可能是 JDK 9+ 上的 JDK 平台类加载器
			ClassLoader aopClassLoader = getClass().getClassLoader();
			ClassLoader aopParent = aopClassLoader.getParent();
			while (aopParent != null) {
				if (classLoader == aopParent) {
					// 建议的 ClassLoader 是 spring-aop ClassLoader 的祖先
					// -> 改用 spring-aop ClassLoader 本身。
					return aopClassLoader;
				}
				aopParent = aopParent.getParent();
			}
		}
		// 常规情况：直接使用建议的 ClassLoader。
		return classLoader;
	}


	/**
	 * {@code InvocationHandler.invoke} 的实现。
	 * <p>调用方将看到目标抛出的确切异常，
	 * 除非钩子方法抛出异常。
	 */
	@Override
	public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Object target = null;

		try {
			if (!this.cache.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// 目标本身未实现 equals(Object) 方法。
				return equals(args[0]);
			}
			else if (!this.cache.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// 目标本身未实现 hashCode() 方法。
				return hashCode();
			}
			else if (method.getDeclaringClass() == DecoratingProxy.class) {
				// 仅声明 getDecoratedClass() -> 分派到代理配置。
				return AopProxyUtils.ultimateTargetClass(this.advised);
			}
			else if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// 在 ProxyConfig 上以服务方式调用代理配置...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			if (this.advised.isExposeProxy()) {
				// 必要时使调用可用。
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// 尽可能晚获取目标，以缩短「持有」目标的时间（可能来自对象池）。
			target = targetSource.getTarget();
			Class<?> targetClass = (target != null ? target.getClass() : null);

			// 获取本方法的拦截链。
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// 检查是否有 Advice。若无，可直接反射调用目标，避免创建 MethodInvocation。
			if (chain.isEmpty()) {
				// 可跳过创建 MethodInvocation：直接调用目标
				// 注意最终调用者必须是 InvokerInterceptor，
				// 仅对目标做反射操作，无热替换或复杂代理。
				@Nullable Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				// 需要创建方法调用...
				MethodInvocation invocation =
						new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// 通过拦截器链继续执行连接点。
				retVal = invocation.proceed();
			}

			// 必要时调整返回值。
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target &&
					returnType != Object.class && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// 特殊情况：返回 "this" 且方法返回类型兼容。
				// 注意：若目标在另一返回对象中设置自引用，则无法处理。
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
				// 必定来自 TargetSource。
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// 恢复旧代理。
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * 相等性指接口、Advisor 与 TargetSource 均相等。
	 * <p>比较对象可能是 JdkDynamicAopProxy 实例本身，
	 * 或包装 JdkDynamicAopProxy 的动态代理。
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
			// 无效比较...
			return false;
		}

		// 执行至此，otherProxy 为另一 AopProxy。
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * 代理使用 TargetSource 的哈希码。
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；反序列化后仅初始化状态。
		ois.defaultReadObject();

		// 初始化 transient 字段。
		this.cache = new ProxiedInterfacesCache(this.advised);
	}


	/**
	 * 完整被代理接口及派生元数据的持有者，
	 * 缓存在 {@link AdvisedSupport#proxyMetadataCache} 中。
	 * @since 6.1.3
	 */
	private static final class ProxiedInterfacesCache {

		final Class<?>[] proxiedInterfaces;

		final boolean equalsDefined;

		final boolean hashCodeDefined;

		ProxiedInterfacesCache(AdvisedSupport config) {
			this.proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(config, true);

			// 在提供的接口集合中查找可能定义的 {@link #equals} 或 {@link #hashCode} 方法。
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
