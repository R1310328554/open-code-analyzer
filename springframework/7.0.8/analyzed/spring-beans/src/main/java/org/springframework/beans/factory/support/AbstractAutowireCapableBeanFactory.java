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

package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 实现默认 Bean 创建流程的抽象工厂超类，具备 {@link RootBeanDefinition} 所描述的全部能力。
 * 在 {@link AbstractBeanFactory#createBean} 之上，进一步实现
 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}。
 *
 * <p>负责：实例化（含构造器解析）、属性填充、装配（含自动装配）、初始化；
 * 处理运行时 Bean 引用、托管集合解析、调用初始化方法等。
 * 支持构造器自动装配、按名称装配属性、按类型装配属性。
 *
 * <p>与上下层的衔接：
 * <ul>
 * <li>{@link AbstractBeanFactory#doGetBean} 在需要真正创建时回调本类 {@link #createBean} → {@link #doCreateBean}；</li>
 * <li>{@link #doCreateBean} 在单例且允许循环依赖时，通过
 * {@link DefaultSingletonBeanRegistry#addSingletonFactory} 把早期引用工厂放进三级缓存；</li>
 * <li>属性注入阶段若触发 {@code getBean}，可从二级/三级缓存拿到尚未完全初始化的引用，从而打破循环。</li>
 * </ul>
 *
 * <p>子类必须实现的主要模板方法是
 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)}，用于自动装配时解析依赖。
 * 若工厂是可列举的 {@link org.springframework.beans.factory.ListableBeanFactory}，通常靠搜索 BeanDefinition 匹配；
 * 否则可做简化匹配。
 *
 * <p>注意：本类<i>不</i>假定、也不实现 BeanDefinition 注册表能力。
 * 同时实现 {@link org.springframework.beans.factory.ListableBeanFactory} 与
 * {@link BeanDefinitionRegistry}（分别对应 API / SPI）的完整工厂见
 * {@link DefaultListableBeanFactory}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 13.02.2004
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	/** 创建 Bean 实例的策略（默认 CGLIB 子类化策略）。 */
	private InstantiationStrategy instantiationStrategy;

	/** 解析方法参数名的策略（构造器/工厂方法参数名匹配时使用）。 */
	private @Nullable ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	/** 是否自动尝试解决 Bean 之间的循环依赖（单例早期暴露）。 */
	private boolean allowCircularReferences = true;

	/**
	 * 循环依赖时，即使最终 Bean 会被包装（例如 AOP），是否仍允许注入「原始」实例。
	 * 默认 false：若已注入原始对象却最终被包装，且存在实际依赖方，则抛异常。
	 */
	private boolean allowRawInjectionDespiteWrapping = false;

	/**
	 * 依赖检查与自动装配时忽略的依赖类型集合（如 {@code String}）。默认无。
	 */
	private final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

	/**
	 * 依赖检查与自动装配时忽略的依赖接口集合。
	 * <p>默认忽略 {@code BeanNameAware}、{@code BeanFactoryAware}、
	 * {@code BeanClassLoaderAware}（这些由 {@code Aware} 回调注入，而非属性装配）。
	 */
	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

	/**
	 * 当前正在创建的 Bean 名称（ThreadLocal）。
	 * 用于在用户自定义 {@link Supplier} 回调里触发 {@code getBean} 时，隐式登记依赖关系。
	 */
	private final NamedThreadLocal<String> currentlyCreatedBean = new NamedThreadLocal<>("Currently created bean");

	/** 未完成初始化的 FactoryBean 实例缓存：FactoryBean 名 → BeanWrapper（供类型检查捷径复用）。 */
	private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

	/** 每个工厂类上候选工厂方法的缓存。 */
	private final ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

	/** 过滤后的 PropertyDescriptor 缓存：Bean Class → 描述符数组（依赖检查用）。 */
	private final ConcurrentMap<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache =
			new ConcurrentHashMap<>();


	/**
	 * 创建一个新的 AbstractAutowireCapableBeanFactory。
	 * 默认忽略若干 Aware 接口，并采用 {@link CglibSubclassingInstantiationStrategy}。
	 */
	public AbstractAutowireCapableBeanFactory() {
		super();
		ignoreDependencyInterface(BeanNameAware.class);
		ignoreDependencyInterface(BeanFactoryAware.class);
		ignoreDependencyInterface(BeanClassLoaderAware.class);
		this.instantiationStrategy = new CglibSubclassingInstantiationStrategy();
	}

	/**
	 * 使用给定父工厂创建 AbstractAutowireCapableBeanFactory。
	 * @param parentBeanFactory 父 BeanFactory；没有则为 {@code null}
	 */
	public AbstractAutowireCapableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		this();
		setParentBeanFactory(parentBeanFactory);
	}


	/**
	 * 设置创建 Bean 实例时使用的实例化策略。
	 * 默认为 {@link CglibSubclassingInstantiationStrategy}。
	 * @see CglibSubclassingInstantiationStrategy
	 */
	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * 返回创建 Bean 实例时使用的实例化策略。
	 */
	public InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}

	/**
	 * 设置在需要时用于解析方法参数名的 {@link ParameterNameDiscoverer}
	 * （例如构造器参数名匹配）。
	 * <p>默认为 {@link DefaultParameterNameDiscoverer}。
	 */
	public void setParameterNameDiscoverer(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * 返回用于解析方法参数名的 {@link ParameterNameDiscoverer}（可能为 {@code null}）。
	 */
	public @Nullable ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.parameterNameDiscoverer;
	}

	/**
	 * 设置是否允许 Bean 之间存在循环依赖，并在可能时自动解决。
	 * <p>注意：解决循环依赖意味着参与其中的某个 Bean 会拿到另一个尚未完全初始化的引用，
	 * 可能在初始化阶段带来微妙副作用；不过对许多场景仍然可用。
	 * <p>默认为 {@code true}。关闭后遇到循环依赖直接抛异常，彻底禁止。
	 * <p><b>说明：</b>一般不建议依赖 Bean 循环引用；更稳妥的做法是抽出第三方 Bean 封装二者共用逻辑。
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	/**
	 * 返回是否允许 Bean 之间的循环依赖。
	 * @since 5.3.10
	 * @see #setAllowCircularReferences
	 */
	public boolean isAllowCircularReferences() {
		return this.allowCircularReferences;
	}

	/**
	 * 设置：即便最终会被包装（例如 AOP 自动代理），是否仍允许把「原始」Bean 实例
	 * 注入到其他 Bean 的属性中。
	 * <p>仅作为循环依赖无法以别的方式解决时的最后手段：宁可注入原始实例，
	 * 也不让整个装配失败。
	 * <p>默认为 {@code false}。打开后允许部分引用拿到未包装的原始 Bean。
	 * <p><b>说明：</b>尤其在涉及自动代理时，仍不建议依赖循环引用。
	 * @see #setAllowCircularReferences
	 */
	public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}

	/**
	 * 返回是否允许注入原始（未包装）Bean 实例。
	 * @since 5.3.10
	 * @see #setAllowRawInjectionDespiteWrapping
	 */
	public boolean isAllowRawInjectionDespiteWrapping() {
		return this.allowRawInjectionDespiteWrapping;
	}

	/**
	 * 自动装配时忽略给定依赖类型（例如 {@code String}）。默认无忽略类型。
	 */
	public void ignoreDependencyType(Class<?> type) {
		this.ignoredDependencyTypes.add(type);
	}

	/**
	 * 自动装配时忽略给定依赖接口。
	 * <p>应用上下文通常用它登记「以其他方式注入」的依赖，例如通过
	 * {@code BeanFactoryAware} 注入 {@code BeanFactory}，或通过
	 * {@code ApplicationContextAware} 注入 {@code ApplicationContext}。
	 * <p>默认已忽略 {@code BeanNameAware}、{@code BeanFactoryAware}、
	 * {@code BeanClassLoaderAware}。若要忽略更多类型，对每种类型调用本方法。
	 * @see org.springframework.beans.factory.BeanNameAware
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.beans.factory.BeanClassLoaderAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	/**
	 * 从另一工厂复制配置，并额外同步实例化策略、循环依赖开关及忽略依赖集合。
	 */
	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof AbstractAutowireCapableBeanFactory otherAutowireFactory) {
			this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
			this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
			this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
			this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
		}
	}


	//-------------------------------------------------------------------------
	// 创建并填充「外部已有 / 按类型临时创建」Bean 实例的常用入口
	//-------------------------------------------------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		// 使用非单例 BD，避免把临时创建的 Bean 登记为依赖方
		RootBeanDefinition bd = new CreateFromClassBeanDefinition(beanClass);
		bd.setScope(SCOPE_PROTOTYPE);
		bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
		return (T) createBean(beanClass.getName(), bd, null);
	}

	@Override
	public void autowireBean(Object existingBean) {
		// 使用非单例 BD，避免把已有实例登记为依赖方
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
		bd.setScope(SCOPE_PROTOTYPE);
		bd.allowCaching = ClassUtils.isCacheSafe(bd.getBeanClass(), getBeanClassLoader());
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public Object configureBean(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition mbd = getMergedBeanDefinition(beanName);
		RootBeanDefinition bd = null;
		if (mbd instanceof RootBeanDefinition rbd) {
			bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
		}
		if (bd == null) {
			bd = new RootBeanDefinition(mbd);
		}
		if (!bd.isPrototype()) {
			bd.setScope(SCOPE_PROTOTYPE);
			bd.allowCaching = ClassUtils.isCacheSafe(ClassUtils.getUserClass(existingBean), getBeanClassLoader());
		}
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(beanName, bd, bw);
		return initializeBean(beanName, existingBean, bd);
	}


	//-------------------------------------------------------------------------
	// 细粒度控制 Bean 生命周期的专用方法（AutowireCapableBeanFactory SPI）
	//-------------------------------------------------------------------------

	@Deprecated(since = "6.1")
	@Override
	public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// 使用非单例 BD，避免登记依赖
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		return createBean(beanClass.getName(), bd, null);
	}

	@Override
	public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// 使用非单例 BD，避免登记依赖
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
		}
		else {
			Object bean = getInstantiationStrategy().instantiate(bd, null, this);
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}

	@Override
	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {

		if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
			throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
		}
		// 使用非单例 BD，避免登记依赖
		RootBeanDefinition bd =
				new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition bd = getMergedBeanDefinition(beanName);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}

	@Override
	public Object initializeBean(Object existingBean, String beanName) {
		return initializeBean(beanName, existingBean, null);
	}

	@Deprecated(since = "6.1")
	@Override
	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessBeforeInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Deprecated(since = "6.1")
	@Override
	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessAfterInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Override
	public void destroyBean(Object existingBean) {
		new DisposableBeanAdapter(existingBean, getBeanPostProcessorCache().destructionAware).destroy();
	}


	//-------------------------------------------------------------------------
	// 解析注入点的委托方法
	//-------------------------------------------------------------------------

	/**
	 * 按名称解析 Bean，并暂时把当前注入点设到 ThreadLocal，供构造器解析等路径读取。
	 */
	@Override
	public Object resolveBeanByName(String name, DependencyDescriptor descriptor) {
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			return getBean(name, descriptor.getDependencyType());
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	/**
	 * 解析依赖注入点的便捷重载，最终委托给子类实现的四参数
	 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)}。
	 * <p>本类自身不实现候选 Bean 的按类型搜索；完整实现见
	 * {@link DefaultListableBeanFactory#resolveDependency}。
	 */
	@Override
	public @Nullable Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException {
		return resolveDependency(descriptor, requestingBeanName, null, null);
	}


	//---------------------------------------------------------------------
	// 实现 AbstractBeanFactory 相关模板方法（createBean / 类型预测等）
	//---------------------------------------------------------------------

	/**
	 * 本类的中枢方法：创建实例、填充属性、应用后处理器等。
	 * 由 {@link AbstractBeanFactory#doGetBean} 在缓存未命中时调用。
	 * @see #doCreateBean
	 */
	@Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object @Nullable [] args)
			throws BeanCreationException {

		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		RootBeanDefinition mbdToUse = mbd;

		// 确保此时已解析出 Bean Class；若是动态解析出的 Class，不能写回共享的合并 BD，需克隆一份
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
			try {
				mbdToUse.prepareMethodOverrides();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
						beanName, "Validation of method overrides failed", ex);
			}
		}

		try {
			// 实例化前捷径：InstantiationAwareBeanPostProcessor 可直接返回代理，跳过正常创建
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
			// 进入完整创建：实例化 → 早期暴露 → 属性填充 → 初始化
			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// 已带创建上下文的异常，或需上抛给 DefaultSingletonBeanRegistry 的非法单例状态
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}

	/**
	 * 真正创建指定 Bean。此前的预处理（如 {@code postProcessBeforeInstantiation}）已完成。
	 * <p>区分默认无参实例化、工厂方法实例化、构造器自动装配等路径。
	 * <p>主链路：{@code createBeanInstance} →（可选）{@code addSingletonFactory} 早期暴露 →
	 * {@code populateBean} → {@code initializeBean}；最后登记销毁回调并返回对外暴露对象。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的合并 BeanDefinition
	 * @param args 构造器或工厂方法的显式参数
	 * @return 新建的 Bean 实例
	 * @throws BeanCreationException 若无法创建
	 * @see #instantiateBean
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 */
	protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object @Nullable [] args)
			throws BeanCreationException {

		// 1) 实例化：优先复用类型检查阶段缓存的 FactoryBean 包装；否则走 createBeanInstance
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		// 2) 合并 BD 后处理（如 AutowiredAnnotationBeanPostProcessor 缓存注入元数据），只做一次
		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex);
				}
				mbd.markAsPostProcessed();
			}
		}

		// 3) 单例 + 允许循环依赖 + 当前正在创建 → 向三级缓存登记 ObjectFactory（早期暴露）
		//    即便后续因 BeanFactoryAware 等再次触发 getBean，也能解析循环引用
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			if (logger.isTraceEnabled()) {
				logger.trace("Eagerly caching bean '" + beanName +
						"' to allow for resolving potential circular references");
			}
			// 工厂被调用时会走 getEarlyBeanReference，Smart 后处理器可在此生成早期代理
			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		// 4) 属性填充 + 初始化（Aware / 初始化前后置 / init-method；AOP 代理常在后置生成）
		Object exposedObject = bean;
		try {
			populateBean(beanName, mbd, instanceWrapper);
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException bce && beanName.equals(bce.getBeanName())) {
				throw bce;
			}
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
		}

		// 5) 若发生过早期引用：核对「最终暴露对象」与「已被注入出去的早期引用」是否一致
		if (earlySingletonExposure) {
			// allowEarlyReference=false：只查一级/二级缓存，不再触发三级工厂
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					// 未被后置包装：对外仍用早期引用（可能已是早期代理）
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					// 最终被包装（如变成 AOP 代理），但循环依赖方已注入了原始对象 → 除非允许 raw 注入，否则报错
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = CollectionUtils.newLinkedHashSet(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
								"] in its raw version as part of a circular reference, but has eventually been " +
								"wrapped. This means that said other beans do not use the final version of the " +
								"bean. This is often the result of over-eager type matching - consider using " +
								"'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
					}
				}
			}
		}

		// 6) 按需登记 DisposableBean / destroy-method，供容器关闭时销毁
		try {
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
		}

		return exposedObject;
	}

	@Override
	protected @Nullable Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
		// 让 SmartInstantiationAwareBeanPostProcessor 预测「实例化前捷径」之后的最终类型
		if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			boolean matchingOnlyFactoryBean = (typesToMatch.length == 1 && typesToMatch[0] == FactoryBean.class);
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				Class<?> predicted = bp.predictBeanType(targetType, beanName);
				if (predicted != null &&
						(!matchingOnlyFactoryBean || FactoryBean.class.isAssignableFrom(predicted))) {
					return predicted;
				}
			}
		}
		return targetType;
	}

	/**
	 * 根据给定 BeanDefinition 判定目标类型。
	 * @param beanName Bean 名称（用于错误处理）
	 * @param mbd 该 Bean 的合并 BeanDefinition
	 * @param typesToMatch 内部类型匹配时要对照的类型
	 * （同时表明返回的 {@code Class} 不会暴露给应用代码）
	 * @return 可确定则返回 Bean 类型，否则 {@code null}
	 */
	protected @Nullable Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = mbd.getTargetType();
		if (targetType == null) {
			if (mbd.getFactoryMethodName() != null) {
				targetType = getTypeForFactoryMethod(beanName, mbd, typesToMatch);
			}
			else {
				targetType = resolveBeanClass(mbd, beanName, typesToMatch);
				if (mbd.hasBeanClass()) {
					targetType = getInstantiationStrategy().getActualBeanClass(mbd, beanName, this);
				}
			}
			if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
				mbd.resolvedTargetType = targetType;
			}
		}
		return targetType;
	}

	/**
	 * 针对「基于工厂方法」的 BeanDefinition 判定目标类型。
	 * 仅在目标 Bean 尚未有已注册单例时调用。
	 * <p>实现会对照 {@link #createBean} 的多种创建策略做类型推断；
	 * 尽可能做静态类型检查，避免为了判型而真正创建目标 Bean。
	 * @param beanName Bean 名称（用于错误处理）
	 * @param mbd 该 Bean 的合并 BeanDefinition
	 * @param typesToMatch 内部类型匹配时要对照的类型
	 * （同时表明返回的 {@code Class} 不会暴露给应用代码）
	 * @return 可确定则返回 Bean 类型，否则 {@code null}
	 * @see #createBean
	 */
	protected @Nullable Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
		if (cachedReturnType != null) {
			return cachedReturnType.resolve();
		}

		Class<?> commonType = null;
		Method uniqueCandidate = mbd.factoryMethodToIntrospect;

		if (uniqueCandidate == null) {
			Class<?> factoryClass;
			boolean isStatic = true;

			String factoryBeanName = mbd.getFactoryBeanName();
			if (factoryBeanName != null) {
				if (factoryBeanName.equals(beanName)) {
					throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
							"factory-bean reference points back to the same bean definition");
				}
				// 实例工厂：在工厂 Bean 的类上查看工厂方法返回类型
				factoryClass = getType(factoryBeanName);
				isStatic = false;
			}
			else {
				// 静态工厂：在 Bean 自身 class 上查看工厂方法返回类型
				factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
			}

			if (factoryClass == null) {
				return null;
			}
			factoryClass = ClassUtils.getUserClass(factoryClass);

			// 若所有候选工厂方法返回类型一致则可确定；因类型转换/自动装配往往无法锁定唯一方法
			int minNrOfArgs =
					(mbd.hasConstructorArgumentValues() ? mbd.getConstructorArgumentValues().getArgumentCount() : 0);
			Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(factoryClass,
					clazz -> ReflectionUtils.getUniqueDeclaredMethods(clazz, ReflectionUtils.USER_DECLARED_METHODS));

			for (Method candidate : candidates) {
				if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate) &&
						candidate.getParameterCount() >= minNrOfArgs) {
					// 带泛型参数的方法：尽量结合实参把返回类型解析清楚
					if (candidate.getTypeParameters().length > 0) {
						try {
							// 完整解析参数名与构造参数值，供返回类型推断
							ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
							Class<?>[] paramTypes = candidate.getParameterTypes();
							@Nullable String[] paramNames = null;
							if (cav.containsNamedArgument()) {
								ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
								if (pnd != null) {
									paramNames = pnd.getParameterNames(candidate);
								}
							}
							Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = CollectionUtils.newHashSet(paramTypes.length);
							@Nullable Object[] args = new Object[paramTypes.length];
							for (int i = 0; i < args.length; i++) {
								ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
										i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
								if (valueHolder == null) {
									valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
								}
								if (valueHolder != null) {
									args[i] = valueHolder.getValue();
									usedValueHolders.add(valueHolder);
								}
							}
							Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(
									candidate, args, getBeanClassLoader());
							uniqueCandidate = (commonType == null && returnType == candidate.getReturnType() ?
									candidate : null);
							commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
							if (commonType == null) {
								// 返回类型歧义：无法确定
								return null;
							}
						}
						catch (Throwable ex) {
							if (logger.isDebugEnabled()) {
								logger.debug("Failed to resolve generic return type for factory method: " + ex);
							}
						}
					}
					else {
						uniqueCandidate = (commonType == null ? candidate : null);
						commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
						if (commonType == null) {
							// 返回类型歧义：无法确定
							return null;
						}
					}
				}
			}

			mbd.factoryMethodToIntrospect = uniqueCandidate;
			if (commonType == null) {
				return null;
			}
		}

		// 已找到公共返回类型；对非参数化的唯一候选，缓存完整返回类型声明上下文
		try {
			cachedReturnType = (uniqueCandidate != null ?
					ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
			mbd.factoryMethodReturnType = cachedReturnType;
			return cachedReturnType.resolve();
		}
		catch (LinkageError err) {
			// 例如泛型返回类型触发 NoClassDefFoundError
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to resolve type for factory method of bean '" + beanName + "': " +
						(uniqueCandidate != null ? uniqueCandidate : commonType), err);
			}
			return null;
		}
	}

	/**
	 * 本实现优先用 FactoryBean 的泛型参数元数据判断产物类型；
	 * 若声明为原始类型，则在尚未填充属性的「半成品」实例上调用 {@code getObjectType}。
	 * 仍得不到类型且 {@code allowInit} 为 {@code true} 时，回退为完整创建 FactoryBean
	 * （委托超类实现）。
	 * <p>捷径检查仅用于单例 FactoryBean；若 FactoryBean 本身不是单例，
	 * 则会完整创建以检查其暴露对象的类型。
	 */
	@Override
	protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
		ResolvableType result;

		// BD 属性上是否已声明 FactoryBean 产物类型
		try {
			result = getTypeForFactoryBeanFromAttributes(mbd);
			if (result != ResolvableType.NONE) {
				return result;
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
					String.valueOf(ex.getMessage()));
		}

		// Supplier 提供的实例：立刻尝试 targetType / beanClass 上的泛型
		if (mbd.getInstanceSupplier() != null) {
			result = getFactoryBeanGeneric(mbd.targetType);
			if (result.resolve() != null) {
				return result;
			}
			result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
			if (result.resolve() != null) {
				return result;
			}
		}

		// 考虑工厂方法路径
		String factoryBeanName = mbd.getFactoryBeanName();
		String factoryMethodName = mbd.getFactoryMethodName();

		// 扫描工厂 Bean 上的工厂方法签名
		if (factoryBeanName != null) {
			if (factoryMethodName != null) {
				// 尽量只靠工厂方法声明推断产物类型，避免实例化承载工厂的 Bean
				BeanDefinition factoryBeanDefinition = getBeanDefinition(factoryBeanName);
				Class<?> factoryBeanClass;
				if (factoryBeanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition &&
						abstractBeanDefinition.hasBeanClass()) {
					factoryBeanClass = abstractBeanDefinition.getBeanClass();
				}
				else {
					RootBeanDefinition fbmbd = getMergedBeanDefinition(factoryBeanName, factoryBeanDefinition);
					factoryBeanClass = determineTargetType(factoryBeanName, fbmbd);
				}
				if (factoryBeanClass != null) {
					result = getTypeForFactoryBeanFromMethod(factoryBeanClass, factoryMethodName);
					if (result.resolve() != null) {
						return result;
					}
				}
			}
			// 上面解析不出，且被引用的工厂 Bean 尚不宜做元数据缓存时直接退出，
			// 避免仅为了拿 FactoryBean 产物类型而强迫创建另一个 Bean
			if (!isBeanEligibleForMetadataCaching(factoryBeanName)) {
				return ResolvableType.NONE;
			}
		}

		// 允许初始化时：可早创建 FactoryBean 并调用 getObjectType()
		if (allowInit) {
			FactoryBean<?> factoryBean = (mbd.isSingleton() ?
					getSingletonFactoryBeanForTypeCheck(beanName, mbd) :
					getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));
			if (factoryBean != null) {
				// 用早期阶段实例尝试取产物类型
				Class<?> type = getTypeForFactoryBean(factoryBean);
				if (type != null) {
					return ResolvableType.forClass(type);
				}
				// 捷径实例仍无类型：回退完整创建 FactoryBean
				return super.getTypeForFactoryBean(beanName, mbd, true);
			}
		}

		if (factoryBeanName == null && mbd.hasBeanClass() && factoryMethodName != null) {
			// 无法早期实例化：从静态工厂方法签名或类继承层次推断
			return getTypeForFactoryBeanFromMethod(mbd.getBeanClass(), factoryMethodName);
		}

		// 普通路径回退：再试 targetType / beanClass 泛型
		if (mbd.getInstanceSupplier() == null) {
			result = getFactoryBeanGeneric(mbd.targetType);
			if (result.resolve() != null) {
				return result;
			}
			result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
			if (result.resolve() != null) {
				return result;
			}
		}

		// 仍无法解析 FactoryBean 产物类型
		return ResolvableType.NONE;
	}

	/**
	 * 内省给定 Bean 类上的工厂方法签名，尝试找出其中声明的公共 {@code FactoryBean} 产物类型。
	 * @param beanClass 查找工厂方法所在的 Bean 类
	 * @param factoryMethodName 工厂方法名
	 * @return 公共的 {@code FactoryBean} 产物类型；没有则 {@code null}
	 */
	private ResolvableType getTypeForFactoryBeanFromMethod(Class<?> beanClass, String factoryMethodName) {
		// CGLIB 子类方法会隐藏泛型参数，应看原始用户类
		Class<?> factoryBeanClass = ClassUtils.getUserClass(beanClass);
		FactoryBeanMethodTypeFinder finder = new FactoryBeanMethodTypeFinder(factoryMethodName);
		ReflectionUtils.doWithMethods(factoryBeanClass, finder, ReflectionUtils.USER_DECLARED_METHODS);
		return finder.getResult();
	}

	/**
	 * 获取指定 Bean 的早期引用，通常用于解决循环依赖。
	 * 会依次调用 {@link SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference}，
	 * 因此 AOP 可在此返回早期代理，保证注入出去的与最终暴露的是同一代理。
	 * @param beanName Bean 名称（用于错误处理）
	 * @param mbd 该 Bean 的合并 BeanDefinition
	 * @param bean 原始 Bean 实例
	 * @return 作为 Bean 引用对外暴露的对象
	 */
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				exposedObject = bp.getEarlyBeanReference(exposedObject, beanName);
			}
		}
		return exposedObject;
	}


	//---------------------------------------------------------------------
	// 内部实现方法
	//---------------------------------------------------------------------

	/**
	 * 获取用于 {@code getObjectType()} 的「捷径」单例 FactoryBean 实例，不做完整初始化。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @return FactoryBean 实例；无法取得捷径实例时返回 {@code null}
	 */
	private @Nullable FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		Boolean lockFlag = isCurrentThreadAllowedToHoldSingletonLock();
		if (lockFlag == null) {
			this.singletonLock.lock();
		}
		else {
			boolean locked = (lockFlag && this.singletonLock.tryLock());
			if (!locked) {
				// 拿不到锁则放弃捷径实例，但仍解析 class 以便后续按类型解析
				resolveBeanClass(mbd, beanName);
				return null;
			}
		}

		try {
			BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);
			if (bw != null) {
				return (FactoryBean<?>) bw.getWrappedInstance();
			}
			Object beanInstance = getSingleton(beanName, false);
			if (beanInstance instanceof FactoryBean<?> factoryBean) {
				return factoryBean;
			}
			if (isSingletonCurrentlyInCreation(beanName) ||
					(mbd.getFactoryBeanName() != null && isSingletonCurrentlyInCreation(mbd.getFactoryBeanName()))) {
				return null;
			}

			Object instance;
			try {
				// 即便只是部分创建，也要标记为「创建中」，防止并发重入
				beforeSingletonCreation(beanName);
				// 后处理器可直接返回代理，代替目标实例
				instance = resolveBeforeInstantiation(beanName, mbd);
				if (instance == null) {
					bw = createBeanInstance(beanName, mbd, null);
					instance = bw.getWrappedInstance();
					// 缓存半成品，供真正 doCreateBean 时复用，避免二次实例化
					this.factoryBeanInstanceCache.put(beanName, bw);
				}
			}
			catch (UnsatisfiedDependencyException ex) {
				// 多半是配置错误，不要吞掉
				throw ex;
			}
			catch (BeanCreationException ex) {
				// LinkageError 首次带完整栈，后续往往只剩 NoClassDefFoundError，不宜吞掉
				if (ex.contains(LinkageError.class)) {
					throw ex;
				}
				// 实例化失败：可能时机过早
				if (logger.isDebugEnabled()) {
					logger.debug("Bean creation exception on singleton FactoryBean type check: " + ex);
				}
				onSuppressedException(ex);
				return null;
			}
			finally {
				// 结束本次部分创建
				afterSingletonCreation(beanName);
			}

			return getFactoryBean(beanName, instance);
		}
		finally {
			this.singletonLock.unlock();
		}
	}

	/**
	 * 获取用于 {@code getObjectType()} 的「捷径」非单例 FactoryBean 实例，不做完整初始化。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @return FactoryBean 实例；无法取得捷径实例时返回 {@code null}
	 */
	private @Nullable FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		if (isPrototypeCurrentlyInCreation(beanName)) {
			return null;
		}

		Object instance;
		try {
			// 即便只是部分创建，也要标记原型「创建中」
			beforePrototypeCreation(beanName);
			// 后处理器可直接返回代理
			instance = resolveBeforeInstantiation(beanName, mbd);
			if (instance == null) {
				BeanWrapper bw = createBeanInstance(beanName, mbd, null);
				instance = bw.getWrappedInstance();
			}
		}
		catch (UnsatisfiedDependencyException ex) {
			// 多半是配置错误，不要吞掉
			throw ex;
		}
		catch (BeanCreationException ex) {
			// 实例化失败：可能时机过早
			if (logger.isDebugEnabled()) {
				logger.debug("Bean creation exception on non-singleton FactoryBean type check: " + ex);
			}
			onSuppressedException(ex);
			return null;
		}
		finally {
			// 结束本次部分创建
			afterPrototypeCreation(beanName);
		}

		return getFactoryBean(beanName, instance);
	}

	/**
	 * 对指定合并 BD 应用 {@link MergedBeanDefinitionPostProcessor}，
	 * 调用其 {@code postProcessMergedBeanDefinition}（常用于缓存注入点元数据）。
	 * @param mbd 该 Bean 的合并 BeanDefinition
	 * @param beanType 托管 Bean 实例的实际类型
	 * @param beanName Bean 名称
	 * @see MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
	 */
	protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
			processor.postProcessMergedBeanDefinition(mbd, beanType, beanName);
		}
	}

	/**
	 * 应用「实例化前」后处理器，判断是否存在跳过正常实例化的捷径。
	 * 若 {@code postProcessBeforeInstantiation} 返回非 null，还会立刻走一遍
	 * {@code postProcessAfterInitialization}（因为跳过了完整生命周期）。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @return 捷径得到的实例；没有捷径则为 {@code null}
	 */
	@SuppressWarnings("deprecation")
	protected @Nullable Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			// 确保此时已解析出 Bean Class
			if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
				Class<?> targetType = determineTargetType(beanName, mbd);
				if (targetType != null) {
					bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
					if (bean != null) {
						// 捷径实例也要经过初始化后置，否则可能缺代理等增强
						bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
					}
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}

	/**
	 * 对指定 Bean（按 class 与名称）应用 {@link InstantiationAwareBeanPostProcessor}，
	 * 调用其 {@code postProcessBeforeInstantiation}。
	 * <p>任一后处理器返回非 null 对象时，将直接作为 Bean 使用，不再实例化目标类；
	 * 返回 {@code null} 则继续正常实例化。
	 * @param beanClass 待实例化 Bean 的类型
	 * @param beanName Bean 名称
	 * @return 用来替代目标默认实例的对象；继续正常实例化则返回 {@code null}
	 * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
	 */
	protected @Nullable Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
			Object result = bp.postProcessBeforeInstantiation(beanClass, beanName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * 为指定 Bean 创建新实例，按合适策略分支：
	 * Supplier / 工厂方法 / 构造器自动装配 / 简单无参实例化。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @param args 构造器或工厂方法的显式参数
	 * @return 新实例的 BeanWrapper
	 * @see #obtainFromSupplier
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 * @see #instantiateBean
	 */
	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object @Nullable [] args) {
		// 确保此时已解析出 Bean Class
		Class<?> beanClass = resolveBeanClass(mbd, beanName);

		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
		}

		// 路径 A：BD 配置了 InstanceSupplier（如函数式注册 / BeanRegistrar）
		if (args == null) {
			Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
			if (instanceSupplier != null) {
				return obtainFromSupplier(instanceSupplier, beanName, mbd);
			}
		}

		// 路径 B：工厂方法（@Bean / factory-method）
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		// 路径 C：同一 Bean 再次创建时，复用已解析的构造器/工厂方法，避免重复推断
		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
		if (resolved) {
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				return instantiateBean(beanName, mbd);
			}
		}

		// 路径 D：后处理器给出的候选构造器，或显式 AUTOWIRE_CONSTRUCTOR / 构造参数 / getBean 实参
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// 路径 E：首选构造器（如 Kotlin primary / 单一公共构造器）
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// 路径 F：默认无参构造
		return instantiateBean(beanName, mbd);
	}

	/**
	 * 从给定 {@link Supplier} 获取 Bean 实例，并用 ThreadLocal 标记当前创建中的 Bean，
	 * 以便 Supplier 内部 {@code getBean} 时隐式登记依赖。
	 * @param supplier 已配置的 Supplier
	 * @param beanName 对应的 Bean 名称
	 * @return 新实例的 BeanWrapper
	 */
	private BeanWrapper obtainFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd) {
		String outerBean = this.currentlyCreatedBean.get();
		this.currentlyCreatedBean.set(beanName);
		Object instance;

		try {
			instance = obtainInstanceFromSupplier(supplier, beanName, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException bce && beanName.equals(bce.getBeanName())) {
				throw bce;
			}
			throw new BeanCreationException(beanName, "Instantiation of supplied bean failed", ex);
		}
		finally {
			if (outerBean != null) {
				this.currentlyCreatedBean.set(outerBean);
			}
			else {
				this.currentlyCreatedBean.remove();
			}
		}

		if (instance == null) {
			instance = new NullBean();
		}
		BeanWrapper bw = new BeanWrapperImpl(instance);
		initBeanWrapper(bw);
		return bw;
	}

	/**
	 * 从给定 {@link Supplier} 实际取出实例（支持 {@link ThrowingSupplier}）。
	 * @param supplier 已配置的 Supplier
	 * @param beanName 对应的 Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @return Bean 实例（可能为 {@code null}）
	 * @since 6.0.7
	 */
	protected @Nullable Object obtainInstanceFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd)
			throws Exception {

		if (supplier instanceof ThrowingSupplier<?> throwingSupplier) {
			return throwingSupplier.getWithException();
		}
		return supplier.get();
	}

	/**
	 * 重写以便在 {@link Supplier} 回调期间程序化 {@code getBean} 时，
	 * 把「当前正在创建的 Bean」隐式登记为对所取 Bean 的依赖方。
	 * @since 5.0
	 * @see #obtainFromSupplier
	 */
	@Override
	protected Object getObjectForBeanInstance(Object beanInstance, @Nullable Class<?> requiredType,
			String name, String beanName, @Nullable RootBeanDefinition mbd) {

		String currentlyCreatedBean = this.currentlyCreatedBean.get();
		if (currentlyCreatedBean != null) {
			registerDependentBean(beanName, currentlyCreatedBean);
		}

		return super.getObjectForBeanInstance(beanInstance, requiredType, name, beanName, mbd);
	}

	/**
	 * 通过已注册的 {@link SmartInstantiationAwareBeanPostProcessor} 确定候选构造器
	 * （例如 {@code @Autowired} 构造器推断）。
	 * @param beanClass Bean 的原始 Class
	 * @param beanName Bean 名称
	 * @return 候选构造器；未指定则为 {@code null}
	 * @throws org.springframework.beans.BeansException 出错时
	 * @see org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors
	 */
	protected Constructor<?> @Nullable [] determineConstructorsFromBeanPostProcessors(@Nullable Class<?> beanClass, String beanName)
			throws BeansException {

		if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				Constructor<?>[] ctors = bp.determineCandidateConstructors(beanClass, beanName);
				if (ctors != null) {
					return ctors;
				}
			}
		}
		return null;
	}

	/**
	 * 使用默认（无参）构造器实例化给定 Bean。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @return 新实例的 BeanWrapper
	 */
	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
		try {
			Object beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, this);
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
		}
	}

	/**
	 * 通过具名工厂方法实例化 Bean。
	 * 方法可以是静态的（BD 指定 class 而非 factoryBean），也可以是已通过依赖注入配置好的工厂对象上的实例方法。
	 * 实际解析委托给 {@link ConstructorResolver#instantiateUsingFactoryMethod}。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @param explicitArgs 通过 {@code getBean} 程序化传入的参数；{@code null} 表示使用 BD 中的构造参数
	 * @return 新实例的 BeanWrapper
	 * @see #getBean(String, Object[])
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(
			String beanName, RootBeanDefinition mbd, @Nullable Object @Nullable [] explicitArgs) {

		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
	}

	/**
	 * 「构造器自动装配」（按类型匹配构造参数）行为。
	 * 即便显式指定了部分构造参数，也会用工厂中的 Bean 去匹配剩余参数。
	 * <p>对应构造器注入：此模式下工厂可承载期望构造器解析依赖的组件。
	 * 实际解析委托给 {@link ConstructorResolver#autowireConstructor}。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @param ctors 选定的候选构造器
	 * @param explicitArgs 通过 {@code getBean} 程序化传入的参数；{@code null} 表示使用 BD 中的构造参数
	 * @return 新实例的 BeanWrapper
	 */
	protected BeanWrapper autowireConstructor(
			String beanName, RootBeanDefinition mbd, Constructor<?> @Nullable [] ctors, @Nullable Object @Nullable [] explicitArgs) {

		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
	}

	/**
	 * 用 BeanDefinition 中的属性值填充给定 {@link BeanWrapper} 中的 Bean 实例。
	 * <p>顺序概览：{@code postProcessAfterInstantiation}（可短路）→
	 * byName/byType 自动装配收集属性 → {@code postProcessProperties}（如 {@code @Autowired} 字段注入）→
	 * 依赖检查 → {@link #applyPropertyValues} 真正设值。
	 * @param beanName Bean 名称
	 * @param mbd 该 Bean 的 BeanDefinition
	 * @param bw 持有 Bean 实例的 BeanWrapper
	 */
	protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
		if (bw == null) {
			if (mbd.hasPropertyValues()) {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
			}
			else {
				// null 实例无需属性填充
				return;
			}
		}

		if (bw.getWrappedClass().isRecord()) {
			if (mbd.hasPropertyValues()) {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to a record");
			}
			else {
				// record 不可变，跳过属性填充
				return;
			}
		}

		// 1) 实例化后、设属性前：InstantiationAwareBPP 可改状态或完全跳过属性填充（常用于字段注入风格）
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
				if (!bp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
					return;
				}
			}
		}

		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

		// 2) XML 风格的 byName / byType：把自动装配结果并入 PropertyValues
		int resolvedAutowireMode = mbd.getResolvedAutowireMode();
		if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
			if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}
			if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}
			pvs = newPvs;
		}
		// 3) postProcessProperties：注解驱动注入（@Autowired/@Resource/@Value 等）主要发生在这里
		if (hasInstantiationAwareBeanPostProcessors()) {
			if (pvs == null) {
				pvs = mbd.getPropertyValues();
			}
			for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
				PropertyValues pvsToUse = bp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
				if (pvsToUse == null) {
					return;
				}
				pvs = pvsToUse;
			}
		}

		// 4) 可选依赖检查：确认必须设置的属性都已提供
		boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
		if (needsDepCheck) {
			PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			checkDependencies(beanName, mbd, filteredPds, pvs);
		}

		// 5) 解析引用并写入 Bean（深度拷贝 + 类型转换）
		if (pvs != null) {
			applyPropertyValues(beanName, mbd, bw, pvs);
		}
	}

	/**
	 * 在 autowire="byName" 时，用工厂中同名 Bean 填补尚未满足的属性引用。
	 * @param beanName 正在装配的 Bean 名（主要用于调试信息）
	 * @param mbd 通过自动装配更新的 BeanDefinition
	 * @param bw 可从中获取 Bean 信息的 BeanWrapper
	 * @param pvs 用于登记已装配对象的 PropertyValues
	 */
	protected void autowireByName(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				registerDependentBean(propertyName, beanName);
				if (logger.isTraceEnabled()) {
					logger.trace("Added autowiring by name from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
							"' by name: no matching bean found");
				}
			}
		}
	}

	/**
	 * 「按类型自动装配」属性行为的定义（autowire by type）。
	 * <p>类似 PicoContainer 默认策略：工厂中该属性类型应恰好有一个 Bean。
	 * 小命名空间下配置简单，但大型应用通常更依赖注解注入等标准 Spring 行为。
	 * <p>依赖解析最终调用抽象方法 {@link #resolveDependency}（由
	 * {@link DefaultListableBeanFactory} 等子类实现）。
	 * @param beanName 要按类型自动装配的 Bean 名
	 * @param mbd 通过自动装配更新的合并 BeanDefinition
	 * @param bw 可从中获取 Bean 信息的 BeanWrapper
	 * @param pvs 用于登记已装配对象的 PropertyValues
	 */
	protected void autowireByType(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}

		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		Set<String> autowiredBeanNames = new LinkedHashSet<>(propertyNames.length * 2);
		for (String propertyName : propertyNames) {
			try {
				PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
				// Object 类型按类型装配没有意义，即使它技术上是「未满足的非简单属性」
				if (Object.class != pd.getPropertyType()) {
					MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
					// 对 PriorityOrdered 后处理器自身，禁止为类型匹配而急切初始化，避免引导期循环
					boolean eager = !(bw.getWrappedInstance() instanceof PriorityOrdered);
					DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
					// resolveDependency 为子类模板方法：真正按类型/限定符在工厂中找候选
					Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
					if (autowiredArgument != null) {
						pvs.add(propertyName, autowiredArgument);
					}
					for (String autowiredBeanName : autowiredBeanNames) {
						registerDependentBean(autowiredBeanName, beanName);
						if (logger.isTraceEnabled()) {
							logger.trace("Autowiring by type from bean name '" + beanName + "' via property '" +
									propertyName + "' to bean named '" + autowiredBeanName + "'");
						}
					}
					autowiredBeanNames.clear();
				}
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
			}
		}
	}


	/**
	 * 返回尚未满足的非简单 Bean 属性名数组。
	 * 它们多半是对工厂中其他 Bean 的未满足引用；不含基本类型、String 等简单属性。
	 * @param mbd 创建该 Bean 时使用的合并 BeanDefinition
	 * @param bw 创建该 Bean 时使用的 BeanWrapper
	 * @return Bean 属性名数组
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<>();
		PropertyValues pvs = mbd.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
					!BeanUtils.isSimpleProperty(pd.getPropertyType())) {
				result.add(pd.getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * 从给定 BeanWrapper 提取过滤后的 PropertyDescriptor 集合，
	 * 排除被忽略的依赖类型，以及定义在被忽略依赖接口上的属性。
	 * @param bw 创建该 Bean 时使用的 BeanWrapper
	 * @param cache 是否按 Bean Class 缓存过滤后的 PropertyDescriptor
	 * @return 过滤后的 PropertyDescriptor
	 * @see #isExcludedFromDependencyCheck
	 * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
		PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
		if (filtered == null) {
			filtered = filterPropertyDescriptorsForDependencyCheck(bw);
			if (cache) {
				PropertyDescriptor[] existing =
						this.filteredPropertyDescriptorsCache.putIfAbsent(bw.getWrappedClass(), filtered);
				if (existing != null) {
					filtered = existing;
				}
			}
		}
		return filtered;
	}

	/**
	 * 从给定 BeanWrapper 提取过滤后的 PropertyDescriptor 集合，
	 * 排除被忽略的依赖类型，以及定义在被忽略依赖接口上的属性。
	 * @param bw 创建该 Bean 时使用的 BeanWrapper
	 * @return 过滤后的 PropertyDescriptor
	 * @see #isExcludedFromDependencyCheck
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
		List<PropertyDescriptor> pds = new ArrayList<>(Arrays.asList(bw.getPropertyDescriptors()));
		pds.removeIf(this::isExcludedFromDependencyCheck);
		return pds.toArray(new PropertyDescriptor[0]);
	}

	/**
	 * 判断给定 Bean 属性是否排除在依赖检查之外。
	 * <p>本实现排除 CGLIB 生成的属性、类型属于忽略依赖类型的属性，
	 * 以及定义在忽略依赖接口上的属性。
	 * @param pd Bean 属性的 PropertyDescriptor
	 * @return 是否排除该属性
	 * @see #ignoreDependencyType(Class)
	 * @see #ignoreDependencyInterface(Class)
	 */
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				this.ignoredDependencyTypes.contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
	}

	/**
	 * 按需执行依赖检查：确认应设置的属性都已提供。
	 * 检查范围可以是 objects（协作 Bean）、simple（基本类型与 String）或 all（二者皆查）。
	 * @param beanName Bean 名称
	 * @param mbd 创建该 Bean 时使用的合并 BeanDefinition
	 * @param pds 目标 Bean 相关的属性描述符
	 * @param pvs 将应用到 Bean 的属性值
	 * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 */
	protected void checkDependencies(
			String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, @Nullable PropertyValues pvs)
			throws UnsatisfiedDependencyException {

		int dependencyCheck = mbd.getDependencyCheck();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && (pvs == null || !pvs.contains(pd.getName()))) {
				boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
				boolean unsatisfied = (dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_ALL) ||
						(isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
						(!isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(),
							"Set this property value or disable dependency checking for this bean.");
				}
			}
		}
	}

	/**
	 * 应用给定属性值，解析其中对工厂内其他 Bean 的运行时引用。
	 * 必须做深拷贝，以免永久修改原始 PropertyValues。
	 * @param beanName Bean 名称（便于异常信息）
	 * @param mbd 合并后的 BeanDefinition
	 * @param bw 包装目标对象的 BeanWrapper
	 * @param pvs 新的属性值
	 */
	protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
		if (pvs.isEmpty()) {
			return;
		}

		MutablePropertyValues mpvs = null;
		List<PropertyValue> original;

		if (pvs instanceof MutablePropertyValues _mpvs) {
			mpvs = _mpvs;
			if (mpvs.isConverted()) {
				// 捷径：已转换过则直接设值
				try {
					bw.setPropertyValues(mpvs);
					return;
				}
				catch (BeansException ex) {
					throw new BeanCreationException(
							mbd.getResourceDescription(), beanName, "Error setting property values", ex);
				}
			}
			original = mpvs.getPropertyValueList();
		}
		else {
			original = Arrays.asList(pvs.getPropertyValues());
		}

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

		// 深拷贝并解析引用（RuntimeBeanReference、内部 Bean、集合等）
		List<PropertyValue> deepCopy = new ArrayList<>(original.size());
		boolean resolveNecessary = false;
		for (PropertyValue pv : original) {
			if (pv.isConverted()) {
				deepCopy.add(pv);
			}
			else {
				String propertyName = pv.getName();
				Object originalValue = pv.getValue();
				// 自动装配标记 → 转为 DependencyDescriptor，后续走 resolveDependency
				if (originalValue == AutowiredPropertyMarker.INSTANCE) {
					Method writeMethod = bw.getPropertyDescriptor(propertyName).getWriteMethod();
					if (writeMethod == null) {
						throw new IllegalArgumentException("Autowire marker for property without write method: " + pv);
					}
					originalValue = new DependencyDescriptor(new MethodParameter(writeMethod, 0), true);
				}
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;
				boolean convertible = isConvertibleProperty(propertyName, bw);
				if (convertible) {
					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
				}
				// 尽可能把转换结果缓存进合并 BD，避免每个实例都重新转换
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				}
				else if (convertible && originalValue instanceof TypedStringValue typedStringValue &&
						!typedStringValue.isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				}
				else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		if (mpvs != null && !resolveNecessary) {
			mpvs.setConverted();
		}

		// 把（可能已整形的）深拷贝写回目标 Bean
		try {
			bw.setPropertyValues(new MutablePropertyValues(deepCopy));
		}
		catch (BeansException ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
		}
	}

	/**
	 * 判断工厂是否应为给定属性缓存已转换的值（嵌套/索引属性或非唯一写方法则不缓存）。
	 */
	private boolean isConvertibleProperty(String propertyName, BeanWrapper bw) {
		try {
			return !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName) &&
					BeanUtils.hasUniqueWriteMethod(bw.getPropertyDescriptor(propertyName));
		}
		catch (InvalidPropertyException ex) {
			return false;
		}
	}

	/**
	 * 将给定值转换为指定目标属性所需的类型。
	 */
	private @Nullable Object convertForProperty(
			@Nullable Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {

		if (converter instanceof BeanWrapperImpl beanWrapper) {
			return beanWrapper.convertForProperty(value, propertyName);
		}
		else {
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
			return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
		}
	}


	/**
	 * 初始化给定 Bean 实例：应用工厂回调、初始化方法以及 BeanPostProcessor。
	 * <p>传统定义的 Bean 由 {@link #createBean}/{@link #doCreateBean} 调用；
	 * 对已有实例则由公共 {@link #initializeBean(Object, String)} 进入。
	 * <p>顺序：Aware 回调 → {@code postProcessBeforeInitialization} →
	 * init-method（含 {@link InitializingBean}）→ {@code postProcessAfterInitialization}
	 * （AOP 自动代理常在此步生成）。
	 * @param beanName 工厂中的 Bean 名（便于调试）
	 * @param bean 待初始化的新实例
	 * @param mbd 创建该 Bean 时使用的定义；对已有实例可为 {@code null}
	 * @return 初始化后的实例（可能已被包装）
	 * @see BeanNameAware
	 * @see BeanClassLoaderAware
	 * @see BeanFactoryAware
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #invokeInitMethods
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	@SuppressWarnings("deprecation")
	protected Object initializeBean(String beanName, Object bean, @Nullable RootBeanDefinition mbd) {
		// NullBean 无需初始化
		if (bean.getClass() == NullBean.class) {
			return bean;
		}

		// 1) Aware 回调：BeanName / BeanClassLoader / BeanFactory
		invokeAwareMethods(beanName, bean);

		Object wrappedBean = bean;
		// 2) 初始化前的 BeanPostProcessor（如 @PostConstruct 的部分处理在此链路相关 BPP 中）
		if (mbd == null || !mbd.isSynthetic()) {
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		try {
			// 3) InitializingBean.afterPropertiesSet + 自定义 init-method
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null), beanName, ex.getMessage(), ex);
		}
		// 4) 初始化后的 BeanPostProcessor：AutoProxyCreator 常在此包装为代理
		if (mbd == null || !mbd.isSynthetic()) {
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}

	/** 若 Bean 实现了相关 {@link Aware} 接口，则注入名称 / ClassLoader / 本工厂。 */
	private void invokeAwareMethods(String beanName, Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof BeanNameAware beanNameAware) {
				beanNameAware.setBeanName(beanName);
			}
			if (bean instanceof BeanClassLoaderAware beanClassLoaderAware) {
				ClassLoader bcl = getBeanClassLoader();
				if (bcl != null) {
					beanClassLoaderAware.setBeanClassLoader(bcl);
				}
			}
			if (bean instanceof BeanFactoryAware beanFactoryAware) {
				beanFactoryAware.setBeanFactory(AbstractAutowireCapableBeanFactory.this);
			}
		}
	}

	/**
	 * 在属性设置完毕后，给 Bean 一次自初始化机会（并借机知晓所属工厂）。
	 * <p>即检查是否实现 {@link InitializingBean} 或定义了自定义 init 方法，并在需要时调用。
	 * @param beanName 工厂中的 Bean 名（便于调试）
	 * @param bean 待初始化的新实例
	 * @param mbd 创建该 Bean 时使用的合并定义；对已有实例可为 {@code null}
	 * @throws Throwable init 方法或调用过程抛出时
	 * @see #invokeCustomInitMethod
	 */
	protected void invokeInitMethods(String beanName, Object bean, @Nullable RootBeanDefinition mbd)
			throws Throwable {

		boolean isInitializingBean = (bean instanceof InitializingBean);
		if (isInitializingBean && (mbd == null || !mbd.hasAnyExternallyManagedInitMethod("afterPropertiesSet"))) {
			if (logger.isTraceEnabled()) {
				logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			((InitializingBean) bean).afterPropertiesSet();
		}

		if (mbd != null && bean.getClass() != NullBean.class) {
			String[] initMethodNames = mbd.getInitMethodNames();
			if (initMethodNames != null) {
				for (String initMethodName : initMethodNames) {
					if (StringUtils.hasLength(initMethodName) &&
							!(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
							!mbd.hasAnyExternallyManagedInitMethod(initMethodName)) {
						invokeCustomInitMethod(beanName, bean, mbd, initMethodName);
					}
				}
			}
		}
	}

	/**
	 * 在给定 Bean 上调用指定的自定义 init 方法。
	 * <p>由 {@link #invokeInitMethods(String, Object, RootBeanDefinition)} 调用。
	 * <p>子类可重写以支持带参数的 init 方法解析。
	 * @see #invokeInitMethods
	 */
	protected void invokeCustomInitMethod(String beanName, Object bean, RootBeanDefinition mbd, String initMethodName)
			throws Throwable {

		Class<?> beanClass = bean.getClass();
		MethodDescriptor descriptor = MethodDescriptor.create(beanName, beanClass, initMethodName);
		String methodName = descriptor.methodName();

		Method initMethod = (mbd.isNonPublicAccessAllowed() ?
				BeanUtils.findMethod(descriptor.declaringClass(), methodName) :
				ClassUtils.getMethodIfAvailable(beanClass, methodName));

		if (initMethod == null) {
			if (mbd.isEnforceInitMethod()) {
				throw new BeanDefinitionValidationException("Could not find an init method named '" +
						methodName + "' on bean with name '" + beanName + "'");
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("No default init method named '" + methodName +
							"' found on bean with name '" + beanName + "'");
				}
				// 忽略不存在的默认生命周期方法
				return;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Invoking init method '" + methodName + "' on bean with name '" + beanName + "'");
		}
		Method methodToInvoke = ClassUtils.getPubliclyAccessibleMethodIfPossible(initMethod, beanClass);

		try {
			ReflectionUtils.makeAccessible(methodToInvoke);
			methodToInvoke.invoke(bean);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	/**
	 * 对从 FactoryBean 取得的对象应用所有已注册 BPP 的
	 * {@code postProcessAfterInitialization}（例如自动代理）。
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return applyBeanPostProcessorsAfterInitialization(object, beanName);
	}

	/**
	 * 重写以便同时清理 FactoryBean 半成品实例缓存。
	 */
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanInstanceCache.remove(beanName);
	}

	/**
	 * 重写以便同时清空 FactoryBean 半成品实例缓存。
	 */
	@Override
	protected void clearSingletonCache() {
		super.clearSingletonCache();
		this.factoryBeanInstanceCache.clear();
	}

	/**
	 * 向协作委托对象暴露 logger。
	 * @since 5.0.7
	 */
	Log getLogger() {
		return logger;
	}


	/**
	 * 供 {@code #createBean(Class)} 使用的 {@link RootBeanDefinition} 子类：
	 * 除默认构造器外，还可灵活选择 Kotlin primary / 唯一公共 / 唯一非公共构造器候选。
	 * @see BeanUtils#getResolvableConstructor(Class)
	 */
	@SuppressWarnings("serial")
	private static class CreateFromClassBeanDefinition extends RootBeanDefinition {

		public CreateFromClassBeanDefinition(Class<?> beanClass) {
			super(beanClass);
		}

		public CreateFromClassBeanDefinition(CreateFromClassBeanDefinition original) {
			super(original);
		}

		@Override
		public Constructor<?> @Nullable [] getPreferredConstructors() {
			Constructor<?>[] fromAttribute = super.getPreferredConstructors();
			if (fromAttribute != null) {
				return fromAttribute;
			}
			return ConstructorResolver.determinePreferredConstructors(getBeanClass());
		}

		@Override
		public RootBeanDefinition cloneBeanDefinition() {
			return new CreateFromClassBeanDefinition(this);
		}
	}


	/**
	 * 专用于古老的 autowire="byType" 模式的 {@link DependencyDescriptor} 变体。
	 * 始终可选；选择主候选时不考虑参数名。
	 */
	@SuppressWarnings("serial")
	private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

		public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
			super(methodParameter, false, eager);
		}

		@Override
		public @Nullable String getDependencyName() {
			return null;
		}
	}


	/**
	 * 用于查找 {@link FactoryBean} 类型信息的 {@link MethodCallback}。
	 */
	private static class FactoryBeanMethodTypeFinder implements MethodCallback {

		private final String factoryMethodName;

		private ResolvableType result = ResolvableType.NONE;

		FactoryBeanMethodTypeFinder(String factoryMethodName) {
			this.factoryMethodName = factoryMethodName;
		}

		@Override
		public void doWith(Method method) throws IllegalArgumentException {
			if (isFactoryBeanMethod(method)) {
				ResolvableType returnType = ResolvableType.forMethodReturnType(method);
				ResolvableType candidate = returnType.as(FactoryBean.class).getGeneric();
				if (this.result == ResolvableType.NONE) {
					this.result = candidate;
				}
				else {
					Class<?> resolvedResult = this.result.resolve();
					Class<?> commonAncestor = ClassUtils.determineCommonAncestor(candidate.resolve(), resolvedResult);
					if (!ObjectUtils.nullSafeEquals(resolvedResult, commonAncestor)) {
						this.result = ResolvableType.forClass(commonAncestor);
					}
				}
			}
		}

		private boolean isFactoryBeanMethod(Method method) {
			return (method.getName().equals(this.factoryMethodName) &&
					FactoryBean.class.isAssignableFrom(method.getReturnType()));
		}

		ResolvableType getResult() {
			Class<?> resolved = this.result.resolve();
			boolean foundResult = resolved != null && resolved != Object.class;
			return (foundResult ? this.result : ResolvableType.NONE);
		}
	}

}
