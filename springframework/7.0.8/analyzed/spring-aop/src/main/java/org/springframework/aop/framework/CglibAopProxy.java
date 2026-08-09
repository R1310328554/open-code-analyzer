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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.aot.AotDetector;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.GeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Dispatcher;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.cglib.transform.impl.UndeclaredThrowableStrategy;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.SmartClassLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Spring AOP 框架的基于 CGLIB 的 {@link AopProxy} 实现。
 * 这种类型的 <p> 对象应通过代理工厂获取，并由 {@link AdvisedSupport} 对象配置。该类是 Spring AOP 框架的内部类，不需要由客户端代码直接使用
 * 。
 * 如果需要，<p>{@link DefaultAopProxyFactory} 将自动创建基于 CGLIB 的代理，例如在代理目标类的情况下（有关详细信息，请参阅 {@link
 * DefaultAopProxyFactory attendant javadoc}）。
 * 如果底层（目标）类是线程安全的，则使用此类创建的 <p>Proxies 也是线程安全的。
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Chris Beams
 * @author Dave Syer
 * @author Sebastien Deleuze
 * @see org.springframework.cglib.proxy.Enhancer
 * @see AdvisedSupport#setProxyTargetClass
 * @see DefaultAopProxyFactory
 */
@SuppressWarnings("serial")
class CglibAopProxy implements AopProxy, Serializable {

	// CGLIB 回调数组索引常量
	private static final int AOP_PROXY = 0;
	private static final int INVOKE_TARGET = 1;
	private static final int NO_OVERRIDE = 2;
	private static final int DISPATCH_TARGET = 3;
	private static final int DISPATCH_ADVISED = 4;
	private static final int INVOKE_EQUALS = 5;
	private static final int INVOKE_HASHCODE = 6;


	private static final String COROUTINES_FLOW_CLASS_NAME = "kotlinx.coroutines.flow.Flow";

	/**
	 * 判断是否 Present。
	 */
	private static final boolean COROUTINES_REACTOR_PRESENT = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", CglibAopProxy.class.getClassLoader());

	/** `undeclaredThrowableStrategy`：该类的成员状态。 */
	private static final GeneratorStrategy undeclaredThrowableStrategy =
			new UndeclaredThrowableStrategy(UndeclaredThrowableException.class);

	/**
	 */
	protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

	/**
	 */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<>();


	/**
	 */
	protected final AdvisedSupport advised;

	/** 构造器相关状态（`constructorArgs`）。 */
	protected Object @Nullable [] constructorArgs;

	/** 构造器相关状态（`constructorArgTypes`）。 */
	protected Class<?> @Nullable [] constructorArgTypes;

	/**
	 */
	private final transient AdvisedDispatcher advisedDispatcher;

	/**
	 * 方法 `emptyMap`：完成本类中与「empty Map」相关的职责。
	 */
	private transient Map<Method, Integer> fixedInterceptorMap = Collections.emptyMap();

	/** 拦截器相关状态（`fixedInterceptorOffset`）。 */
	private transient int fixedInterceptorOffset;


	/**
	 * 为给定的 AOP 配置创建一个新的 CglibAopProxy。
	 * @param config AOP 配置为 AdvisedSupport 对象
	 * @throws AopConfigException 如果配置无效。在这种情况下，我们尝试抛出一个信息性异常，而不是让以后发生神秘的故障。
	 */
	public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		this.advised = config;
		this.advisedDispatcher = new AdvisedDispatcher(this.advised);
	}

	/**
	 * 设置用于创建代理的构造函数参数。
	 * @param constructorArgs 构造函数参数值
	 * @param constructorArgTypes 构造函数参数类型
	 */
	public void setConstructorArguments(Object @Nullable [] constructorArgs, Class<?> @Nullable [] constructorArgTypes) {
		if (constructorArgs == null || constructorArgTypes == null) {
			throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
		}
		if (constructorArgs.length != constructorArgTypes.length) {
			throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
					") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
		}
		this.constructorArgs = constructorArgs;
		this.constructorArgTypes = constructorArgTypes;
	}


	/**
	 * 获取 Proxy（`Proxy`）。
	 */
	@Override
	public Object getProxy() {
		return buildProxy(null, false);
	}

	/**
	 * 获取 Proxy（`Proxy`）。
	 */
	@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		return buildProxy(classLoader, false);
	}

	/**
	 * 获取 Proxy Class（`ProxyClass`）。
	 */
	@Override
	public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
		return (Class<?>) buildProxy(classLoader, true);
	}

	/**
	 * 构建：Proxy（方法 `buildProxy`）。
	 */
	private Object buildProxy(@Nullable ClassLoader classLoader, boolean classOnly) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating CGLIB proxy: " + this.advised.getTargetSource());
		}

		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");

			Class<?> proxySuperClass = rootClass;
			if (rootClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}

			// 验证类，根据需要写入日志消息。
			validateClassIfNecessary(proxySuperClass, classLoader);

			// 配置 CGLIB 增强器...
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				if (classLoader instanceof SmartClassLoader smartClassLoader &&
						smartClassLoader.isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
			enhancer.setSuperclass(proxySuperClass);
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setAttemptLoad(enhancer.getUseCache() && AotDetector.useGeneratedArtifacts());
			enhancer.setStrategy(KotlinDetector.isKotlinType(proxySuperClass) ?
					new ClassLoaderAwareGeneratorStrategy(classLoader) :
					new ClassLoaderAwareGeneratorStrategy(classLoader, undeclaredThrowableStrategy)
			);

			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class<?>[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			// 在上面的 getCallbacks 调用之后，fixedInterceptorMap 仅在此时填充
			ProxyCallbackFilter filter = new ProxyCallbackFilter(
					this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset);
			enhancer.setCallbackFilter(filter);
			enhancer.setCallbackTypes(types);

			// 生成代理类并创建代理实例。
			// ProxyCallbackFilter 具有具有 Advisor 访问权限的方法自省功能。
			try {
				return (classOnly ? createProxyClass(enhancer) : createProxyClassAndInstance(enhancer, callbacks));
			}
			finally {
				// 将 ProxyCallbackFilter 减少为仅键状态以发挥其类缓存作用
				// 在 CGLIB$CALLBACK_FILTER 字段中，不会泄漏任何 Advisor 状态...
				filter.advised.reduceToAdvisorKey();
			}
		}
		catch (CodeGenerationException | IllegalArgumentException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of " + this.advised.getTargetClass() +
					": Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (Throwable ex) {
			// TargetSource.getTarget() 失败
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}

	/**
	 * 创建：Proxy Class（方法 `createProxyClass`）。
	 */
	protected Class<?> createProxyClass(Enhancer enhancer) {
		enhancer.setInterceptDuringConstruction(false);
		return enhancer.createClass();
	}

	/**
	 * 创建：Proxy Class And Instance（方法 `createProxyClassAndInstance`）。
	 */
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		enhancer.setInterceptDuringConstruction(false);
		enhancer.setCallbacks(callbacks);
		return (this.constructorArgs != null && this.constructorArgTypes != null ?
				enhancer.create(this.constructorArgTypes, this.constructorArgs) :
				enhancer.create());
	}

	/**
	 * 创建 CGLIB {@link Enhancer}。子类可能希望重写它以返回自定义 {@link Enhancer} 实现。
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}

	/**
	 * 检查提供的 {@code Class} 是否已经过验证，如果没有则进行验证。
	 */
	private void validateClassIfNecessary(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader) {
		if (!this.advised.isOptimize() && logger.isInfoEnabled()) {
			synchronized (validatedClasses) {
				validatedClasses.computeIfAbsent(proxySuperClass, clazz -> {
					doValidateClass(clazz, proxyClassLoader, ClassUtils.getAllInterfacesForClassAsSet(clazz));
					return Boolean.TRUE;
				});
			}
		}
	}

	/**
	 * 检查给定 {@code Class} 上的最终方法以及跨 ClassLoader 的包可见方法，并将警告写入日志中找到的每个方法。
	 */
	private void doValidateClass(Class<?> proxySuperClass, @Nullable ClassLoader proxyClassLoader, Set<Class<?>> ifcs) {
		if (proxySuperClass != Object.class) {
			Method[] methods = proxySuperClass.getDeclaredMethods();
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isStatic(mod) && !Modifier.isPrivate(mod)) {
					if (Modifier.isFinal(mod)) {
						if (logger.isWarnEnabled() && Modifier.isPublic(mod)) {
							if (implementsInterface(method, ifcs)) {
								logger.warn("Unable to proxy interface-implementing method [" + method + "] because " +
										"it is marked as final, consider using interface-based JDK proxies instead.");
							}
							else {
								logger.warn("Public final method [" + method + "] cannot get proxied via CGLIB, " +
										"consider removing the final marker or using interface-based JDK proxies.");
							}
						}
						if (logger.isDebugEnabled()) {
							logger.debug("Final method [" + method + "] cannot get proxied via CGLIB: " +
									"Calls to this method will NOT be routed to the target instance and " +
									"might lead to NPEs against uninitialized fields in the proxy instance.");
						}
					}
					else if (logger.isDebugEnabled() && !Modifier.isPublic(mod) && !Modifier.isProtected(mod) &&
							proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
						logger.debug("Method [" + method + "] is package-visible across different ClassLoaders " +
								"and cannot get proxied via CGLIB: Declare this method as public or protected " +
								"if you need to support invocations through the proxy.");
					}
				}
			}
			doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader, ifcs);
		}
	}

	/**
	 * 获取 Callbacks（`Callbacks`）。
	 */
	private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
		// 用于优化选择的参数...
		boolean isStatic = this.advised.getTargetSource().isStatic();
		boolean isFrozen = this.advised.isFrozen();
		boolean exposeProxy = this.advised.isExposeProxy();

		// 选择一个“aop”拦截器（用于 AOP 调用）。
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

		// 选择“直接瞄准目标”拦截器。 （用于调用
		// 不建议但可以退货）。可能需要公开代理。
		Callback targetInterceptor;
		if (exposeProxy) {
			targetInterceptor = (isStatic ?
					new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource()));
		}
		else {
			targetInterceptor = (isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource()));
		}

		// 选择“直接到目标”调度程序（用于
		// 对无法返回此静态目标的不建议调用）。
		Callback targetDispatcher = (isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp());

		Callback[] mainCallbacks = new Callback[] {
				aopInterceptor,  // for normal advice
				targetInterceptor,  // invoke target without considering advice, if optimized
				new SerializableNoOp(),  // no override for methods mapped to this
				targetDispatcher, this.advisedDispatcher,
				new EqualsInterceptor(this.advised),
				new HashCodeInterceptor(this.advised)
		};

		// 如果目标是静态目标并且建议链被冻结，
		// 那么我们可以通过发送AOP调用来进行一些优化
		// 使用该方法的固定链直接到达目标。
		if (isStatic && isFrozen) {
			Method[] methods = rootClass.getMethods();
			int methodsCount = methods.length;
			List<Callback> fixedCallbacks = new ArrayList<>(methodsCount);
			this.fixedInterceptorMap = CollectionUtils.newHashMap(methodsCount);

			int advicedMethodCount = methodsCount;
			for (int x = 0; x < methodsCount; x++) {
				Method method = methods[x];
				// 不要为 java.lang.Object 的非重写方法创建建议
				if (method.getDeclaringClass() == Object.class) {
					advicedMethodCount--;
					continue;
				}
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, rootClass);
				fixedCallbacks.add(new FixedChainStaticTargetInterceptor(
						chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass()));
				this.fixedInterceptorMap.put(method, x - (methodsCount - advicedMethodCount) );
			}

			// 现在复制 mainCallbacks 中的两个回调
			// 和fixedCallbacks 到回调数组中。
			Callback[] callbacks = new Callback[mainCallbacks.length + advicedMethodCount];
			System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
			System.arraycopy(fixedCallbacks.toArray(Callback[]::new), 0, callbacks,
					mainCallbacks.length, advicedMethodCount);
			this.fixedInterceptorOffset = mainCallbacks.length;
			return callbacks;
		}
		return mainCallbacks;
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof CglibAopProxy that &&
				AopProxyUtils.equalsInProxy(this.advised, that.advised)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}


	/**
	 * 检查给定方法是否在任何给定接口上声明。
	 */
	private static boolean implementsInterface(Method method, Set<Class<?>> ifcs) {
		for (Class<?> ifc : ifcs) {
			if (ClassUtils.hasMethod(ifc, method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 处理一个返回值。如果需要将 {@code this} 的返回包装为 {@code proxy}，并验证 {@code null} 是否未作为原语返回。如果需要，还负责从 {@c
	 * ode Mono} 到 Kotlin 协程的转换。
	 */
	private static @Nullable Object processReturnType(
			Object proxy, @Nullable Object target, Method method, Object[] arguments, @Nullable Object returnValue) {

		// 必要时按摩返回值
		if (returnValue != null && returnValue == target &&
				!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
			// 特殊情况：它返回“this”。请注意，我们无法提供帮助
			// 如果目标在另一个返回的对象中设置对其自身的引用。
			returnValue = proxy;
		}
		Class<?> returnType = method.getReturnType();
		if (returnValue == null && returnType != void.class && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"Null return value from advice does not match primitive return type for: " + method);
		}
		if (COROUTINES_REACTOR_PRESENT && KotlinDetector.isSuspendingFunction(method)) {
			return COROUTINES_FLOW_CLASS_NAME.equals(new MethodParameter(method, -1).getParameterType().getName()) ?
					CoroutinesUtils.asFlow(returnValue) :
					CoroutinesUtils.awaitSingleOrNull(returnValue, arguments[arguments.length - 1]);
		}
		return returnValue;
	}


	/**
	 * CGLIB 的 NoOp 接口的可串行替代。公开以允许在框架的其他地方使用。
	 */
	public static class SerializableNoOp implements NoOp, Serializable {
	}


	/**
	 * 用于没有建议链的静态目标的方法拦截器。该调用将直接传递回目标。当需要暴露代理并且无法确定该方法不会返回 {@code this} 时使用。
	 */
	private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final @Nullable Object target;

		public StaticUnadvisedInterceptor(@Nullable Object target) {
			this.target = target;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object retVal = AopUtils.invokeJoinpointUsingReflection(this.target, method, args);
			return processReturnType(proxy, this.target, method, args, retVal);
		}
	}


	/**
	 * 当要公开代理时，方法拦截器用于没有通知链的静态目标。
	 */
	private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		private final @Nullable Object target;

		public StaticUnadvisedExposedInterceptor(@Nullable Object target) {
			this.target = target;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = AopUtils.invokeJoinpointUsingReflection(this.target, method, args);
				return processReturnType(proxy, this.target, method, args, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * 拦截器用于调用动态目标，而无需创建方法调用或评估建议链。 （我们知道没有关于此方法的建议。）
	 */
	private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final TargetSource targetSource;

		public DynamicUnadvisedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = this.targetSource.getTarget();
			try {
				Object retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
				return processReturnType(proxy, target, method, args, retVal);
			}
			finally {
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}


	/**
	 * 当代理需要公开时，针对不建议的动态目标的拦截器。
	 */
	private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		private final TargetSource targetSource;

		public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			Object target = this.targetSource.getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
				return processReturnType(proxy, target, method, args, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
				if (target != null) {
					this.targetSource.releaseTarget(target);
				}
			}
		}
	}


	/**
	 * 静态目标的调度程序。调度程序比拦截器快得多。只要可以确定某个方法绝对不会返回“this”，就会使用此方法
	 */
	private static class StaticDispatcher implements Dispatcher, Serializable {

		private final @Nullable Object target;

		public StaticDispatcher(@Nullable Object target) {
			this.target = target;
		}

		@Override
		public @Nullable Object loadObject() {
			return this.target;
		}
	}


	/**
	 * Advised 类上声明的任何方法的调度程序。
	 */
	private static class AdvisedDispatcher implements Dispatcher, Serializable {

		private final AdvisedSupport advised;

		public AdvisedDispatcher(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object loadObject() {
			return this.advised;
		}
	}


	/**
	 * {@code equals} 方法的调度程序。确保方法调用始终由此类处理。
	 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			Object other = args[0];
			if (proxy == other) {
				return true;
			}
			if (other instanceof Factory factory) {
				Callback callback = factory.getCallback(INVOKE_EQUALS);
				return (callback instanceof EqualsInterceptor that &&
						AopProxyUtils.equalsInProxy(this.advised, that.advised));
			}
			return false;
		}
	}


	/**
	 * {@code hashCode} 方法的调度程序。确保方法调用始终由此类处理。
	 */
	private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public HashCodeInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
		}
	}


	/**
	 * 拦截器专门用于冻结静态代理上的建议方法。
	 */
	private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

		private final List<Object> adviceChain;

		private final @Nullable Object target;

		private final @Nullable Class<?> targetClass;

		public FixedChainStaticTargetInterceptor(
				List<Object> adviceChain, @Nullable Object target, @Nullable Class<?> targetClass) {

			this.adviceChain = adviceChain;
			this.target = target;
			this.targetClass = targetClass;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			MethodInvocation invocation = new ReflectiveMethodInvocation(
					proxy, this.target, method, args, this.targetClass, this.adviceChain);
			// 如果我们到达这里，我们需要创建一个 MethodInitation。
			Object retVal = invocation.proceed();
			retVal = processReturnType(proxy, this.target, method, args, retVal);
			return retVal;
		}
	}


	/**
	 * 通用 AOP 回调。当目标是动态的或代理未冻结时使用。
	 */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public DynamicAdvisedInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public @Nullable Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			boolean setProxyContext = false;
			Object target = null;
			TargetSource targetSource = this.advised.getTargetSource();
			try {
				if (this.advised.isExposeProxy()) {
					// 如有必要，使调用可用。
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// 尽可能晚地到达，以尽量减少我们“拥有”目标的时间，以防它来自池子......
				target = targetSource.getTarget();
				Class<?> targetClass = (target != null ? target.getClass() : null);
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object retVal;
				// 检查我们是否只有一个 InvokerInterceptor：即
				// 没有真正的建议，只是对目标的反思性调用。
				if (chain.isEmpty()) {
					// 我们可以跳过创建方法调用：直接调用目标。
					// 请注意，最终的调用者必须是 InvokerInterceptor，所以我们知道
					// 它只是对目标进行反射操作，并且不执行任何热操作
					// 交换或花哨的代理。
					@Nullable Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
				}
				else {
					// 我们需要创建一个方法调用...
					retVal = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain).proceed();
				}
				return processReturnType(proxy, target, method, args, retVal);
			}
			finally {
				if (target != null && !targetSource.isStatic()) {
					targetSource.releaseTarget(target);
				}
				if (setProxyContext) {
					// 恢复旧代理。
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other ||
					(other instanceof DynamicAdvisedInterceptor dynamicAdvisedInterceptor &&
							this.advised.equals(dynamicAdvisedInterceptor.advised)));
		}

		/**
		 * CGLIB 使用它来驱动代理创建。
		 */
		@Override
		public int hashCode() {
			return this.advised.hashCode();
		}
	}


	/**
	 * CallbackFilter 将回调分配给方法。
	 */
	private static class ProxyCallbackFilter implements CallbackFilter {

		final AdvisedSupport advised;

		private final Map<Method, Integer> fixedInterceptorMap;

		private final int fixedInterceptorOffset;

		public ProxyCallbackFilter(
				AdvisedSupport advised, Map<Method, Integer> fixedInterceptorMap, int fixedInterceptorOffset) {

			this.advised = advised;
			this.fixedInterceptorMap = fixedInterceptorMap;
			this.fixedInterceptorOffset = fixedInterceptorOffset;
		}

		/**
		 * 实现 CallbackFilter.accept() 以返回我们需要的回调的索引。 <p>
		 * 每个代理的回调由一组通用的固定回调和一组特定于用于具有固定建议链的静态目标的方法的回调组成。 <p>使用的回调是这样确定的： <dl> <dt>对于暴露的代理</dt>
		 * <dd>暴露代理需要在方法/链调用之前和之后执行代码。这意味着我们必须使用 DynamicAdvisedInterceptor，因为所有其他拦截器都可以避免对
		 * try/catch 块的需要</dd> <dt>对于 Object.finalize()：</dt> <dd>不使用此方法的重写。</dd> <dt>对于
		 * equals()：</dt> <dd>EqualsInterceptor 用于将 equals() 调用重定向到此代理的特殊处理程序。</dd> <dt>对于 Advised
		 * 上的方法类：</dt> <dd>AdvisedDispatcher 用于将调用直接分派到目标</dd> <dt>对于建议方法：</dt>
		 * <dd>如果目标是静态的并且建议链被冻结，则特定于该方法的 FixChainStaticTargetInterceptor 用于调用建议链。否则，将使用
		 * DynamicAdvisedInterceptor。</dd> <dt>对于非建议方法：</dt> <dd>如果可以确定该方法不会返回 {@code this}，或者当
		 * {@code ProxyFactory.getExposeProxy()} 返回 {@code false} 时，则使用 Dispatcher。对于静态目标，使用
		 * StaticDispatcher；对于动态目标，使用 DynamicUnadvisedInterceptor。如果该方法可能返回 {@code this}，则
		 * StaticUnadvisedInterceptor 用于静态目标 - DynamicUnadvisedInterceptor 已经考虑到了这一点。</dd> </dl>
		 */
		@Override
		public int accept(Method method) {
			if (AopUtils.isFinalizeMethod(method)) {
				logger.trace("Found finalize() method - using NO_OVERRIDE");
				return NO_OVERRIDE;
			}
			if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Method is declared on Advised interface: " + method);
				}
				return DISPATCH_ADVISED;
			}
			// 我们必须始终代理 equals，以直接调用它。
			if (AopUtils.isEqualsMethod(method)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Found 'equals' method: " + method);
				}
				return INVOKE_EQUALS;
			}
			// 我们必须始终根据代理计算 hashCode。
			if (AopUtils.isHashCodeMethod(method)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Found 'hashCode' method: " + method);
				}
				return INVOKE_HASHCODE;
			}
			Class<?> targetClass = this.advised.getTargetClass();
			// 代理尚不可用，但这应该不重要。
			List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			boolean haveAdvice = !chain.isEmpty();
			boolean isStatic = this.advised.getTargetSource().isStatic();
			boolean isFrozen = this.advised.isFrozen();
			boolean exposeProxy = this.advised.isExposeProxy();
			if (haveAdvice || !isFrozen) {
				// 如果暴露代理，则必须使用 AOP_PROXY。
				if (exposeProxy) {
					if (logger.isTraceEnabled()) {
						logger.trace("Must expose proxy on advised method: " + method);
					}
					return AOP_PROXY;
				}
				// 检查我们是否有固定的拦截器来服务这个方法。
				// 否则使用 AOP_PROXY。
				if (isStatic && isFrozen && this.fixedInterceptorMap.containsKey(method)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Method has advice and optimizations are enabled: " + method);
					}
					// 我们知道我们正在优化，因此我们可以使用FixedStaticChainInterceptors。
					int index = this.fixedInterceptorMap.get(method);
					return (index + this.fixedInterceptorOffset);
				}
				else {
					if (logger.isTraceEnabled()) {
						logger.trace("Unable to apply any optimizations to advised method: " + method);
					}
					return AOP_PROXY;
				}
			}
			else {
				// 查看方法的返回类型是否在目标类型的类层次结构之外。
				// 如果是这样，我们知道它永远不需要返回类型消息并且可以使用调度程序。
				// 如果代理被暴露，那么必须使用已经正确的拦截器
				// 配置。如果目标不是静态的，那么我们就不能使用调度程序，因为
				// 调用后需要显式释放目标。
				if (exposeProxy || !isStatic) {
					return INVOKE_TARGET;
				}
				Class<?> returnType = method.getReturnType();
				if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Method return type is assignable from target type and " +
								"may therefore return 'this' - using INVOKE_TARGET: " + method);
					}
					return INVOKE_TARGET;
				}
				else {
					if (logger.isTraceEnabled()) {
						logger.trace("Method return type ensures 'this' cannot be returned - " +
								"using DISPATCH_TARGET: " + method);
					}
					return DISPATCH_TARGET;
				}
			}
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof ProxyCallbackFilter that &&
					this.advised.getAdvisorKey().equals(that.advised.getAdvisorKey()) &&
					AopProxyUtils.equalsProxiedInterfaces(this.advised, that.advised) &&
					ObjectUtils.nullSafeEquals(this.advised.getTargetClass(), that.advised.getTargetClass()) &&
					this.advised.getTargetSource().isStatic() == that.advised.getTargetSource().isStatic() &&
					this.advised.isFrozen() == that.advised.isFrozen() &&
					this.advised.isExposeProxy() == that.advised.isExposeProxy() &&
					this.advised.isOpaque() == that.advised.isOpaque()));
		}

		@Override
		public int hashCode() {
			return this.advised.getAdvisorKey().hashCode();
		}
	}

}
