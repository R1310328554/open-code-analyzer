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

package org.springframework.context.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aot.AotDetector;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.cglib.core.ClassGenerator;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.cglib.transform.ClassEmitterTransformer;
import org.springframework.cglib.transform.TransformingClassGenerator;
import org.springframework.core.SmartClassLoader;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/* ===== [OCA 中文解析] =====
class ConfigurationClassEnhancer — 意图说明

class `ConfigurationClassEnhancer`：用 CGLIB 增强 @Configuration 类，使 @Bean 方法调用走容器单例/作用域语义而非直接 new。；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 通过生成 CGLIB 子类增强 {@link Configuration} 类，
 * 使其与 Spring 容器协作以遵守 {@code @Bean} 方法的作用域语义。
 * 生成的子类会覆盖每个 {@code @Bean} 方法：仅当容器确实需要新实例时才
 * 委托给真实实现；否则调用返回容器中同名 Bean 的引用。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see #enhance
 * @see ConfigurationClassPostProcessor
 */
class ConfigurationClassEnhancer {

	// The callbacks to use. Note that these callbacks must be stateless.
	static final Callback[] CALLBACKS = new Callback[] {
			new BeanMethodInterceptor(),
			new BeanFactoryAwareMethodInterceptor(),
			NoOp.INSTANCE
	};

	// [OCA] 字段 `CALLBACK_FILTER`：类成员状态。
	private static final ConditionalCallbackFilter CALLBACK_FILTER = new ConditionalCallbackFilter(CALLBACKS);

	// [OCA] 字段 `BEAN_FACTORY_FIELD`：类成员状态。
	private static final String BEAN_FACTORY_FIELD = "$$beanFactory";


	// [OCA] 字段 `logger`：类成员状态。
	private static final Log logger = LogFactory.getLog(ConfigurationClassEnhancer.class);

	// [OCA] 字段 `objenesis`：类成员状态。
	private static final SpringObjenesis objenesis = new SpringObjenesis();


	/* ===== [OCA 中文解析] =====
方法 enhance — 意图与阅读要点

方法 `enhance` 复杂度较高（CCN≈10, NLOC≈36）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
 * 加载指定类并生成带容器感知回调的 CGLIB 子类，以尊重作用域等 Bean 语义。
 * @return 增强后的子类
 */
	public Class<?> enhance(Class<?> configClass, @Nullable ClassLoader classLoader) {
		if (EnhancedConfiguration.class.isAssignableFrom(configClass)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Ignoring request to enhance %s as it has " +
						"already been enhanced. This usually indicates that more than one " +
						"ConfigurationClassPostProcessor has been registered (for example, via " +
						"<context:annotation-config>). This is harmless, but you may " +
						"want check your configuration and remove one CCPP if possible",
						configClass.getName()));
			}
			return configClass;
		}

		try {
			// Use original ClassLoader if config class not locally loaded in overriding class loader
			boolean classLoaderMismatch = (classLoader != null && classLoader != configClass.getClassLoader());
			if (classLoaderMismatch && classLoader instanceof SmartClassLoader smartClassLoader) {
				classLoader = smartClassLoader.getOriginalClassLoader();
				classLoaderMismatch = (classLoader != configClass.getClassLoader());
			}
			// Use original ClassLoader if config class relies on package visibility
			if (classLoaderMismatch && reliesOnPackageVisibility(configClass)) {
				classLoader = configClass.getClassLoader();
				classLoaderMismatch = false;
			}
			Enhancer enhancer = newEnhancer(configClass, classLoader);
			Class<?> enhancedClass = createClass(enhancer, classLoaderMismatch);
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Successfully enhanced %s; enhanced class name is: %s",
						configClass.getName(), enhancedClass.getName()));
			}
			return enhancedClass;
		}
		catch (CodeGenerationException ex) {
			throw new BeanDefinitionStoreException("Could not enhance configuration class [" + configClass.getName() +
					"]. Consider declaring @Configuration(proxyBeanMethods=false) without inter-bean references " +
					"between @Bean methods on the configuration class, avoiding the need for CGLIB enhancement.", ex);
		}
	}

	/* ===== [OCA 中文解析] =====
方法 reliesOnPackageVisibility — 意图与阅读要点

方法 `reliesOnPackageVisibility` 复杂度较高（CCN≈10, NLOC≈21）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
 * 检查给定配置类是否依赖包可见性（类/构造器或 {@code @Bean} 方法）。
 */
	private boolean reliesOnPackageVisibility(Class<?> configSuperClass) {
		int mod = configSuperClass.getModifiers();
		if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) {
			return true;
		}
		for (Constructor<?> ctor : configSuperClass.getDeclaredConstructors()) {
			mod = ctor.getModifiers();
			if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) {
				return true;
			}
		}
		for (Method method : ReflectionUtils.getDeclaredMethods(configSuperClass)) {
			if (BeanAnnotationHelper.isBeanAnnotated(method)) {
				mod = method.getModifiers();
				if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new CGLIB {@link Enhancer} instance.
	 */
	private Enhancer newEnhancer(Class<?> configSuperClass, @Nullable ClassLoader classLoader) {
		Enhancer enhancer = new Enhancer();
		if (classLoader != null) {
			enhancer.setClassLoader(classLoader);
			if (classLoader instanceof SmartClassLoader smartClassLoader &&
					smartClassLoader.isClassReloadable(configSuperClass)) {
				enhancer.setUseCache(false);
			}
		}
		enhancer.setSuperclass(configSuperClass);
		enhancer.setInterfaces(new Class<?>[] {EnhancedConfiguration.class});
		enhancer.setUseFactory(false);
		enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
		enhancer.setAttemptLoad(enhancer.getUseCache() && AotDetector.useGeneratedArtifacts());
		enhancer.setStrategy(new BeanFactoryAwareGeneratorStrategy(classLoader));
		enhancer.setCallbackFilter(CALLBACK_FILTER);
		enhancer.setCallbackTypes(CALLBACK_FILTER.getCallbackTypes());
		return enhancer;
	}

	/**
	 * Uses enhancer to generate a subclass of superclass,
	 * ensuring that callbacks are registered for the new subclass.
	 */
	private Class<?> createClass(Enhancer enhancer, boolean fallback) {
		Class<?> subclass;
		try {
			subclass = enhancer.createClass();
		}
		catch (Throwable ex) {
			if (!fallback) {
				throw (ex instanceof CodeGenerationException cgex ? cgex : new CodeGenerationException(ex));
			}
			// Possibly a package-visible @Bean method declaration not accessible
			// in the given ClassLoader -> retry with original ClassLoader
			enhancer.setClassLoader(null);
			subclass = enhancer.createClass();
		}

		// Registering callbacks statically (as opposed to thread-local)
		// is critical for usage in an OSGi environment (SPR-5932)...
		Enhancer.registerStaticCallbacks(subclass, CALLBACKS);
		return subclass;
	}


	/* ===== [OCA 中文解析] =====
interface EnhancedConfiguration — 意图说明

interface `EnhancedConfiguration`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 所有 @Configuration CGLIB 子类应实现的标记接口，
 * 便于 {@link ConfigurationClassEnhancer#enhance} 幂等检测。
 * <p>同时扩展 {@link BeanFactoryAware}，增强的配置类需要访问创建它的 {@link BeanFactory}。
 */
	public interface EnhancedConfiguration extends BeanFactoryAware {
	}


	/* ===== [OCA 中文解析] =====
interface ConditionalCallback — 意图说明

interface `ConditionalCallback`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 条件 {@link Callback}。
 * @see ConditionalCallbackFilter
 */
	private interface ConditionalCallback extends Callback {

		boolean isMatch(Method candidateMethod);
	}


	/* ===== [OCA 中文解析] =====
class ConditionalCallbackFilter — 意图说明

class `ConditionalCallbackFilter`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 按 {@link ConditionalCallback} 定义顺序询问各 {@link Callback} 的 {@link CallbackFilter}。
 */
	private static class ConditionalCallbackFilter implements CallbackFilter {

		private final Callback[] callbacks;

		private final Class<?>[] callbackTypes;

		public ConditionalCallbackFilter(Callback[] callbacks) {
			this.callbacks = callbacks;
			this.callbackTypes = new Class<?>[callbacks.length];
			for (int i = 0; i < callbacks.length; i++) {
				this.callbackTypes[i] = callbacks[i].getClass();
			}
		}

		@Override
		public int accept(Method method) {
			for (int i = 0; i < this.callbacks.length; i++) {
				Callback callback = this.callbacks[i];
				if (!(callback instanceof ConditionalCallback conditional) || conditional.isMatch(method)) {
					return i;
				}
			}
			throw new IllegalStateException("No callback available for method " + method.getName());
		}

		public Class<?>[] getCallbackTypes() {
			return this.callbackTypes;
		}
	}


	/* ===== [OCA 中文解析] =====
class BeanFactoryAwareGeneratorStrategy — 意图说明

Bean 工厂：存在与获取 Bean 实例的核心入口；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * CGLIB DefaultGeneratorStrategy 的扩展，引入 {@link BeanFactory} 字段；
 * 生成类期间将应用 ClassLoader 设为线程上下文 ClassLoader（供 ASM 解析公共超类）。
 */
	private static class BeanFactoryAwareGeneratorStrategy extends ClassLoaderAwareGeneratorStrategy {

		public BeanFactoryAwareGeneratorStrategy(@Nullable ClassLoader classLoader) {
			super(classLoader);
		}

		@Override
		protected ClassGenerator transform(ClassGenerator cg) throws Exception {
			ClassEmitterTransformer transformer = new ClassEmitterTransformer() {
				@Override
				public void end_class() {
					declare_field(Opcodes.ACC_PUBLIC, BEAN_FACTORY_FIELD, Type.getType(BeanFactory.class), null);
					super.end_class();
				}
			};
			return new TransformingClassGenerator(cg, transformer);
		}
	}


	/* ===== [OCA 中文解析] =====
class BeanFactoryAwareMethodInterceptor — 意图说明

Bean 工厂：存在与获取 Bean 实例的核心入口；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 拦截 {@code @Configuration} 实例上对 {@link BeanFactoryAware#setBeanFactory(BeanFactory)} 的调用以记录 {@link BeanFactory}。
 * @see EnhancedConfiguration
 */
	private static class BeanFactoryAwareMethodInterceptor implements MethodInterceptor, ConditionalCallback {

		@Override
		public @Nullable Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			Field field = ReflectionUtils.findField(obj.getClass(), BEAN_FACTORY_FIELD);
			Assert.state(field != null, "Unable to find generated BeanFactory field");
			field.set(obj, args[0]);

			// Does the actual (non-CGLIB) superclass implement BeanFactoryAware?
			// If so, call its setBeanFactory() method. If not, just exit.
			if (BeanFactoryAware.class.isAssignableFrom(ClassUtils.getUserClass(obj.getClass().getSuperclass()))) {
				return proxy.invokeSuper(obj, args);
			}
			return null;
		}

		@Override
		public boolean isMatch(Method candidateMethod) {
			return isSetBeanFactory(candidateMethod);
		}

		public static boolean isSetBeanFactory(Method candidateMethod) {
			return (candidateMethod.getName().equals("setBeanFactory") &&
					candidateMethod.getParameterCount() == 1 &&
					BeanFactory.class == candidateMethod.getParameterTypes()[0] &&
					BeanFactoryAware.class.isAssignableFrom(candidateMethod.getDeclaringClass()));
		}
	}


	/* ===== [OCA 中文解析] =====
class BeanMethodInterceptor — 意图说明

拦截器：调用链中的前置/后置逻辑；源文件: `spring-context/src/main/java/org/springframework/context/annotation/ConfigurationClassEnhancer.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
	===== [OCA 中文解析结束] ===== */
	/**
 * 拦截所有 {@link Bean} 标注方法的调用，确保正确处理作用域与 AOP 代理等 Bean 语义。
 * @see Bean
 * @see ConfigurationClassEnhancer
 */
	private static class BeanMethodInterceptor implements MethodInterceptor, ConditionalCallback {

		/**
		 * Enhance a {@link Bean @Bean} method to check the supplied BeanFactory for the
		 * existence of this bean object.
		 * @throws Throwable as a catch-all for any exception that may be thrown when invoking the
		 * super implementation of the proxied method i.e., the actual {@code @Bean} method
		 */
		@Override
		public @Nullable Object intercept(Object enhancedConfigInstance, Method beanMethod, Object[] beanMethodArgs,
					MethodProxy cglibMethodProxy) throws Throwable {

			ConfigurableBeanFactory beanFactory = getBeanFactory(enhancedConfigInstance);
			String beanName = BeanAnnotationHelper.determineBeanNameFor(beanMethod, beanFactory);

			// Determine whether this bean is a scoped-proxy
			if (BeanAnnotationHelper.isScopedProxy(beanMethod)) {
				String scopedBeanName = ScopedProxyCreator.getTargetBeanName(beanName);
				if (beanFactory.isCurrentlyInCreation(scopedBeanName)) {
					beanName = scopedBeanName;
				}
			}

			// To handle the case of an inter-bean method reference, we must explicitly check the
			// container for already cached instances.

			// First, check to see if the requested bean is a FactoryBean. If so, create a subclass
			// proxy that intercepts calls to getObject() and returns any cached bean instance.
			// This ensures that the semantics of calling a FactoryBean from within @Bean methods
			// is the same as that of referring to a FactoryBean within XML. See SPR-6602.
			String factoryBeanName = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
			if (factoryContainsBean(beanFactory, factoryBeanName) && factoryContainsBean(beanFactory, beanName)) {
				Object factoryBean = beanFactory.getBean(factoryBeanName);
				if (factoryBean instanceof ScopedProxyFactoryBean) {
					// Scoped proxy factory beans are a special case and should not be further proxied
				}
				else {
					// It is a candidate FactoryBean - go ahead with enhancement
					return enhanceFactoryBean(factoryBean, beanMethod.getReturnType(), beanFactory, beanName);
				}
			}

			if (isCurrentlyInvokedFactoryMethod(beanMethod)) {
				// The factory is calling the bean method in order to instantiate and register the bean
				// (i.e. via a getBean() call) -> invoke the super implementation of the method to actually
				// create the bean instance.
				if (logger.isInfoEnabled() &&
						BeanFactoryPostProcessor.class.isAssignableFrom(beanMethod.getReturnType())) {
					logger.info("""
							@Bean method %s.%s is non-static and returns an object assignable to Spring's \
							BeanFactoryPostProcessor interface. This will result in a failure to process \
							annotations such as @Autowired, @Resource, and @PostConstruct within the method's \
							declaring @Configuration class. Add the 'static' modifier to this method to avoid \
							these container lifecycle issues; see @Bean javadoc for complete details."""
								.formatted(beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName()));
				}
				return cglibMethodProxy.invokeSuper(enhancedConfigInstance, beanMethodArgs);
			}

			return resolveBeanReference(beanMethod, beanMethodArgs, beanFactory, beanName);
		}

		private @Nullable Object resolveBeanReference(Method beanMethod, Object[] beanMethodArgs,
				ConfigurableBeanFactory beanFactory, String beanName) {

			// The user (i.e. not the factory) is requesting this bean through a call to
			// the bean method, direct or indirect. The bean may have already been marked
			// as 'in creation' in certain autowiring scenarios; if so, temporarily set
			// the in-creation status to false in order to avoid an exception.
			boolean alreadyInCreation = beanFactory.isCurrentlyInCreation(beanName);
			try {
				if (alreadyInCreation) {
					beanFactory.setCurrentlyInCreation(beanName, false);
				}
				boolean useArgs = !ObjectUtils.isEmpty(beanMethodArgs);
				if (useArgs && beanFactory.isSingleton(beanName)) {
					// Stubbed null arguments just for reference purposes,
					// expecting them to be autowired for regular singleton references?
					// A safe assumption since @Bean singleton arguments cannot be optional...
					for (Object arg : beanMethodArgs) {
						if (arg == null) {
							useArgs = false;
							break;
						}
					}
				}
				Object beanInstance = (useArgs ? beanFactory.getBean(beanName, beanMethodArgs) :
						beanFactory.getBean(beanName));
				if (!ClassUtils.isAssignableValue(beanMethod.getReturnType(), beanInstance)) {
					// Detect package-protected NullBean instance through equals(null) check
					if (beanInstance.equals(null)) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("@Bean method %s.%s called as bean reference " +
									"for type [%s] returned null bean; resolving to null value.",
									beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName(),
									beanMethod.getReturnType().getName()));
						}
						beanInstance = null;
					}
					else {
						String msg = String.format("@Bean method %s.%s called as bean reference " +
								"for type [%s] but overridden by non-compatible bean instance of type [%s].",
								beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName(),
								beanMethod.getReturnType().getName(), beanInstance.getClass().getName());
						try {
							BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
							msg += " Overriding bean of same name declared in: " + beanDefinition.getResourceDescription();
						}
						catch (NoSuchBeanDefinitionException ex) {
							// Ignore - simply no detailed message then.
						}
						throw new IllegalStateException(msg);
					}
				}
				Method currentlyInvoked = SimpleInstantiationStrategy.getCurrentlyInvokedFactoryMethod();
				if (currentlyInvoked != null) {
					String outerBeanName = BeanAnnotationHelper.determineBeanNameFor(currentlyInvoked, beanFactory);
					beanFactory.registerDependentBean(beanName, outerBeanName);
				}
				return beanInstance;
			}
			finally {
				if (alreadyInCreation) {
					beanFactory.setCurrentlyInCreation(beanName, true);
				}
			}
		}

		@Override
		public boolean isMatch(Method candidateMethod) {
			return (candidateMethod.getDeclaringClass() != Object.class &&
					!BeanFactoryAwareMethodInterceptor.isSetBeanFactory(candidateMethod) &&
					BeanAnnotationHelper.isBeanAnnotated(candidateMethod));
		}

		private ConfigurableBeanFactory getBeanFactory(Object enhancedConfigInstance) {
			Field field = ReflectionUtils.findField(enhancedConfigInstance.getClass(), BEAN_FACTORY_FIELD);
			Assert.state(field != null, "Unable to find generated bean factory field");
			Object beanFactory = ReflectionUtils.getField(field, enhancedConfigInstance);
			Assert.state(beanFactory != null, "BeanFactory has not been injected into @Configuration class");
			Assert.state(beanFactory instanceof ConfigurableBeanFactory,
					"Injected BeanFactory is not a ConfigurableBeanFactory");
			return (ConfigurableBeanFactory) beanFactory;
		}

		/**
		 * Check the BeanFactory to see whether the bean named <var>beanName</var> already
		 * exists. Accounts for the fact that the requested bean may be "in creation", i.e.:
		 * we're in the middle of servicing the initial request for this bean. From an enhanced
		 * factory method's perspective, this means that the bean does not actually yet exist,
		 * and that it is now our job to create it for the first time by executing the logic
		 * in the corresponding factory method.
		 * <p>Said another way, this check repurposes
		 * {@link ConfigurableBeanFactory#isCurrentlyInCreation(String)} to determine whether
		 * the container is calling this method or the user is calling this method.
		 * @param beanName name of bean to check for
		 * @return whether <var>beanName</var> already exists in the factory
		 */
		private boolean factoryContainsBean(ConfigurableBeanFactory beanFactory, String beanName) {
			return (beanFactory.containsBean(beanName) && !beanFactory.isCurrentlyInCreation(beanName));
		}

		/**
		 * Check whether the given method corresponds to the container's currently invoked
		 * factory method. Compares method name and parameter types only in order to work
		 * around a potential problem with covariant return types (currently only known
		 * to happen on Groovy classes).
		 */
		private boolean isCurrentlyInvokedFactoryMethod(Method method) {
			Method currentlyInvoked = SimpleInstantiationStrategy.getCurrentlyInvokedFactoryMethod();
			return (currentlyInvoked != null && method.getName().equals(currentlyInvoked.getName()) &&
					Arrays.equals(method.getParameterTypes(), currentlyInvoked.getParameterTypes()));
		}

		/**
		 * Create a subclass proxy that intercepts calls to getObject(), delegating to the current BeanFactory
		 * instead of creating a new instance. These proxies are created only when calling a FactoryBean from
		 * within a Bean method, allowing for proper scoping semantics even when working against the FactoryBean
		 * instance directly. If a FactoryBean instance is fetched through the container via &-dereferencing,
		 * it will not be proxied. This too is aligned with the way XML configuration works.
		 */
		private Object enhanceFactoryBean(Object factoryBean, Class<?> exposedType,
				ConfigurableBeanFactory beanFactory, String beanName) {

			try {
				Class<?> clazz = factoryBean.getClass();
				boolean finalClass = Modifier.isFinal(clazz.getModifiers());
				boolean finalMethod = Modifier.isFinal(clazz.getMethod("getObject").getModifiers());
				if (finalClass || finalMethod) {
					if (exposedType.isInterface()) {
						if (logger.isTraceEnabled()) {
							logger.trace("Creating interface proxy for FactoryBean '" + beanName + "' of type [" +
									clazz.getName() + "] for use within another @Bean method because its " +
									(finalClass ? "implementation class" : "getObject() method") +
									" is final: Otherwise a getObject() call would not be routed to the factory.");
						}
						return createInterfaceProxyForFactoryBean(factoryBean, exposedType, beanFactory, beanName);
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Unable to proxy FactoryBean '" + beanName + "' of type [" +
									clazz.getName() + "] for use within another @Bean method because its " +
									(finalClass ? "implementation class" : "getObject() method") +
									" is final: A getObject() call will NOT be routed to the factory. " +
									"Consider declaring the return type as a FactoryBean interface.");
						}
						return factoryBean;
					}
				}
			}
			catch (NoSuchMethodException ex) {
				// No getObject() method -> shouldn't happen, but as long as nobody is trying to call it...
			}

			return createCglibProxyForFactoryBean(factoryBean, beanFactory, beanName);
		}

		private Object createInterfaceProxyForFactoryBean(Object factoryBean, Class<?> interfaceType,
				ConfigurableBeanFactory beanFactory, String beanName) {

			return Proxy.newProxyInstance(
					factoryBean.getClass().getClassLoader(), new Class<?>[] {interfaceType},
					(proxy, method, args) -> {
						if (method.getName().equals("getObject") && args == null) {
							return beanFactory.getBean(beanName);
						}
						return ReflectionUtils.invokeMethod(method, factoryBean, args);
					});
		}

		private Object createCglibProxyForFactoryBean(Object factoryBean,
				ConfigurableBeanFactory beanFactory, String beanName) {

			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(factoryBean.getClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setAttemptLoad(AotDetector.useGeneratedArtifacts());
			enhancer.setCallbackType(MethodInterceptor.class);

			// Ideally create enhanced FactoryBean proxy without constructor side effects,
			// analogous to AOP proxy creation in ObjenesisCglibAopProxy...
			Class<?> fbClass = enhancer.createClass();
			Object fbProxy = null;

			if (objenesis.isWorthTrying()) {
				try {
					fbProxy = objenesis.newInstance(fbClass, enhancer.getUseCache());
				}
				catch (ObjenesisException ex) {
					logger.debug("Unable to instantiate enhanced FactoryBean using Objenesis, " +
							"falling back to regular construction", ex);
				}
			}

			if (fbProxy == null) {
				try {
					fbProxy = ReflectionUtils.accessibleConstructor(fbClass).newInstance();
				}
				catch (Throwable ex) {
					throw new IllegalStateException("Unable to instantiate enhanced FactoryBean using Objenesis, " +
							"and regular FactoryBean instantiation via default constructor fails as well", ex);
				}
			}

			((Factory) fbProxy).setCallback(0, (MethodInterceptor) (obj, method, args, proxy) -> {
				if (method.getName().equals("getObject") && args.length == 0) {
					return beanFactory.getBean(beanName);
				}
				return method.invoke(factoryBean, args);
			});

			return fbProxy;
		}
	}

}
