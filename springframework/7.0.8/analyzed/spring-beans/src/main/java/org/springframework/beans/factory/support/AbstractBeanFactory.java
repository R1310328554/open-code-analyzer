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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * {@link org.springframework.beans.factory.BeanFactory} 实现的抽象基类，
 * 提供 {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} SPI 的完整能力。
 * <i>不</i>假定工厂一定可列举（listable）：因此也可作为从后端资源按需读取
 * BeanDefinition 的工厂实现的基类（这类访问往往代价较高）。
 *
 * <p>本类借助基类 {@link org.springframework.beans.factory.support.DefaultSingletonBeanRegistry}
 * 提供单例缓存，并处理单例/原型判定、{@link org.springframework.beans.factory.FactoryBean}、
 * 别名、父子 BeanDefinition 合并，以及销毁（{@link org.springframework.beans.factory.DisposableBean}
 * 与自定义 destroy 方法）。通过实现 {@link org.springframework.beans.factory.HierarchicalBeanFactory}，
 * 还可组成工厂层级（本工厂找不到时委托父工厂）。
 *
 * <p>与兄弟类的大致分工：
 * <ul>
 * <li>{@link DefaultSingletonBeanRegistry}：三级缓存、循环依赖早期引用、单例销毁与依赖图；</li>
 * <li>本类 {@code AbstractBeanFactory}：{@code getBean}/{@code doGetBean} 主流程、合并 BD、
 * scope、FactoryBean 解包、类型匹配、后处理器登记等；</li>
 * <li>{@link AbstractAutowireCapableBeanFactory}：真正的 {@link #createBean}（实例化、注入、初始化）；</li>
 * <li>{@link DefaultListableBeanFactory}：可列举注册表与按类型解析。</li>
 * </ul>
 *
 * <p>子类必须实现的主要模板方法是 {@link #getBeanDefinition} 与 {@link #createBean}，
 * 分别用于按名取定义、按定义创建实例。默认实现分别见
 * {@link DefaultListableBeanFactory} 与 {@link AbstractAutowireCapableBeanFactory}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 15 April 2001
 * @see #getBeanDefinition
 * @see #createBean
 * @see AbstractAutowireCapableBeanFactory#createBean
 * @see DefaultListableBeanFactory#getBeanDefinition
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

	/** 父 BeanFactory，用于层级查找与 Bean 定义继承。 */
	private @Nullable BeanFactory parentBeanFactory;

	/** 解析 Bean 类名时使用的 ClassLoader（必要时）。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** 临时 ClassLoader，用于类型检查等场景的短暂解析（例如织入）。 */
	private @Nullable ClassLoader tempClassLoader;

	/** 是否缓存 Bean 元数据；为 false 时每次访问重新获取/合并。 */
	private boolean cacheBeanMetadata = true;

	/** BeanDefinition 属性值中表达式的解析策略。 */
	private @Nullable BeanExpressionResolver beanExpressionResolver;

	/** Spring ConversionService，可替代传统 PropertyEditor 做类型转换。 */
	private @Nullable ConversionService conversionService;

	/** 会覆盖默认编辑器的 PropertyEditorRegistrar 集合。 */
	private final Set<PropertyEditorRegistrar> defaultEditorRegistrars = new LinkedHashSet<>(4);

	/** 自定义 PropertyEditorRegistrar 集合（不覆盖默认编辑器）。 */
	private final Set<PropertyEditorRegistrar> propertyEditorRegistrars = new LinkedHashSet<>(4);

	/** 自定义 PropertyEditor：目标类型 → 编辑器 Class。 */
	private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<>(4);

	/** 自定义 TypeConverter；若设置则覆盖默认 PropertyEditor 机制。 */
	private @Nullable TypeConverter typeConverter;

	/** 嵌入式字符串解析器列表（例如注解属性中的占位符）。 */
	private final List<StringValueResolver> embeddedValueResolvers = new CopyOnWriteArrayList<>();

	/** 将应用于本工厂所创建 Bean 的 BeanPostProcessor 列表。 */
	private final List<BeanPostProcessor> beanPostProcessors = new BeanPostProcessorCacheAwareList();

	/** 按类型预过滤后的后处理器缓存（避免每次遍历全表）。 */
	private @Nullable BeanPostProcessorCache beanPostProcessorCache;

	/** 作用域标识 → 对应 {@link Scope} 实现。 */
	private final Map<String, Scope> scopes = new LinkedHashMap<>(8);

	/** 应用启动度量（记录 Bean 实例化等步骤）。 */
	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

	/** beanName → 已合并的 {@link RootBeanDefinition} 缓存。 */
	private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<>(256);

	/** 至少创建过一次（或已标记为即将创建）的 Bean 名称集合。 */
	private final Set<String> alreadyCreated = ConcurrentHashMap.newKeySet(256);

	/** 当前线程正在创建中的原型 Bean 名称（String 或 Set&lt;String&gt;）。 */
	private final ThreadLocal<Object> prototypesCurrentlyInCreation =
			new NamedThreadLocal<>("Prototype beans currently in creation");


	/**
	 * 创建一个新的 AbstractBeanFactory。
	 */
	public AbstractBeanFactory() {
	}

	/**
	 * 使用给定父工厂创建一个新的 AbstractBeanFactory。
	 * @param parentBeanFactory 父 BeanFactory，没有则为 {@code null}
	 * @see #getBean
	 */
	public AbstractBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return doGetBean(name, requiredType, null, false);
	}

	@Override
	public Object getBean(String name, @Nullable Object @Nullable ... args) throws BeansException {
		return doGetBean(name, null, args, false);
	}

	/**
	 * 返回指定 Bean 的一个实例（可能是共享单例，也可能是独立实例）。
	 * @param name the name of the bean to retrieve
	 * @param requiredType the required type of the bean to retrieve
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @return an instance of the bean
	 * @throws BeansException if the bean could not be created
	 */
	public <T> T getBean(String name, @Nullable Class<T> requiredType, @Nullable Object @Nullable ... args)
			throws BeansException {

		return doGetBean(name, requiredType, args, false);
	}

	/**
	 * 返回指定 Bean 的一个实例（可能是共享单例，也可能是独立实例）。
	 * <p>这是 {@code getBean} 族方法的真正入口：先查单例缓存，再必要时创建并按 scope 分支，
	 * 最后经 FactoryBean 解包与类型适配后返回。
	 * @param name the name of the bean to retrieve
	 * @param requiredType the required type of the bean to retrieve
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @param typeCheckOnly whether the instance is obtained for a type check,
	 * not for actual use
	 * @return an instance of the bean
	 * @throws BeansException if the bean could not be created
	 */
	@SuppressWarnings("unchecked")
	protected <T> T doGetBean(
			String name, @Nullable Class<T> requiredType, @Nullable Object @Nullable [] args, boolean typeCheckOnly)
			throws BeansException {

		// 去掉 & 前缀并解析别名，得到规范 beanName
		String beanName = transformedBeanName(name);
		Object beanInstance;

		// 1) 尽快查单例缓存（含循环依赖时的早期引用）；有显式 args 时不走缓存，必须新建
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			if (logger.isTraceEnabled()) {
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			// 可能是 FactoryBean：按 name 是否带 & 决定返回工厂本身还是 getObject() 产物
			beanInstance = getObjectForBeanInstance(sharedInstance, requiredType, name, beanName, null);
		}

		else {
			// 2) 原型正在本线程创建中 → 原型循环依赖，直接失败（单例才靠三级缓存解环）
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// 3) 本工厂没有该 BD，但有父工厂 → 整段查找委托给父工厂
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (parentBeanFactory instanceof AbstractBeanFactory abf) {
					return abf.doGetBean(nameToLookup, requiredType, args, typeCheckOnly);
				}
				else if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else if (requiredType != null) {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					return (T) parentBeanFactory.getBean(nameToLookup);
				}
			}

			// 真正使用（非仅类型检查）时标记为已创建，并可能让合并 BD 重新合并以吃到最新元数据
			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			StartupStep beanCreation = this.applicationStartup.start("spring.beans.instantiate")
					.tag("beanName", name);
			try {
				if (requiredType != null) {
					beanCreation.tag("beanType", requiredType::toString);
				}
				// 4) 取合并后的 RootBeanDefinition，并校验（例如 abstract 不可实例化）
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// 5) 先初始化 depends-on；并检测 depends-on 环
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						if (isDependent(beanName, dep)) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
						}
						registerDependentBean(dep, beanName);
						try {
							getBean(dep);
						}
						catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
						}
						catch (BeanCreationException ex) {
							if (requiredType != null) {
								// Wrap exception with current bean metadata but only if specifically
								// requested (indicated by required type), not for depends-on cascades.
								throw new BeanCreationException(mbd.getResourceDescription(), beanName,
										"Failed to initialize dependency '" + ex.getBeanName() + "' of " +
												requiredType.getSimpleName() + " bean '" + beanName + "': " +
												ex.getMessage(), ex);
							}
							throw ex;
						}
					}
				}

				// 6) 按 scope 创建实例（真正创建交给子类 createBean）
				if (mbd.isSingleton()) {
					// 单例：交给 DefaultSingletonBeanRegistry.getSingleton 保证只创建一次，并支持循环依赖
					sharedInstance = getSingleton(beanName, () -> {
						try {
							return createBean(beanName, mbd, args);
						}
						catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					});
					beanInstance = getObjectForBeanInstance(sharedInstance, requiredType, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// 原型：每次新建；用 ThreadLocal 标记创建中以检测原型环
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					beanInstance = getObjectForBeanInstance(prototypeInstance, requiredType, name, beanName, mbd);
				}

				else {
					// 自定义 scope（request/session 等）：委托 Scope.get，创建前后同样标记「创建中」
					String scopeName = mbd.getScope();
					if (!StringUtils.hasLength(scopeName)) {
						throw new IllegalStateException("No scope name defined for bean '" + beanName + "'");
					}
					Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, () -> {
							beforePrototypeCreation(beanName);
							try {
								return createBean(beanName, mbd, args);
							}
							finally {
								afterPrototypeCreation(beanName);
							}
						});
						beanInstance = getObjectForBeanInstance(scopedInstance, requiredType, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new ScopeNotActiveException(beanName, scopeName, ex);
					}
				}
			}
			catch (BeansException ex) {
				beanCreation.tag("exception", ex.getClass().toString());
				beanCreation.tag("message", String.valueOf(ex.getMessage()));
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
			finally {
				beanCreation.end();
				// 未开启元数据缓存时，用完即标记合并 BD 为 stale
				if (!isCacheBeanMetadata()) {
					clearMergedBeanDefinition(beanName);
				}
			}
		}

		// 7) 若调用方指定了 requiredType，必要时做类型转换
		return adaptBeanInstance(name, beanInstance, requiredType);
	}

	@SuppressWarnings("unchecked")
	<T> T adaptBeanInstance(String name, Object bean, @Nullable Class<?> requiredType) {
		// 调用方要求的类型与实际实例不匹配时，尝试 TypeConverter 转换
		if (requiredType != null && !requiredType.isInstance(bean)) {
			try {
				Object convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
				if (convertedBean == null) {
					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
				}
				return (T) convertedBean;
			}
			catch (TypeMismatchException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to convert bean '" + name + "' to required type '" +
							ClassUtils.getQualifiedName(requiredType) + "'", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}

	@Override
	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
			// 带 & 时必须确实是 FactoryBean 才算「包含」
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
		}
		// Not found -> check parent.
		BeanFactory parentBeanFactory = getParentBeanFactory();
		return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean<?> factoryBean) {
				// &bean → 工厂本身是单例；否则看 FactoryBean 产物是否单例
				return (BeanFactoryUtils.isFactoryDereference(name) || factoryBean.isSingleton());
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name);
			}
		}

		// No singleton instance found -> check bean definition.
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.isSingleton(originalBeanName(name));
		}

		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

		// In case of FactoryBean, return singleton status of created object if not a dereference.
		if (mbd.isSingleton()) {
			if (isFactoryBean(beanName, mbd)) {
				if (BeanFactoryUtils.isFactoryDereference(name)) {
					return true;
				}
				FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.isSingleton();
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name);
			}
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.isPrototype(originalBeanName(name));
		}

		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isPrototype()) {
			// In case of FactoryBean, return singleton status of created object if not a dereference.
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName, mbd));
		}

		// Singleton or scoped - not a prototype.
		// However, FactoryBean may still produce a prototype object...
		if (BeanFactoryUtils.isFactoryDereference(name)) {
			return false;
		}
		if (isFactoryBean(beanName, mbd)) {
			FactoryBean<?> fb = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
			return ((fb instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isPrototype()) ||
					!fb.isSingleton());
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		return isTypeMatch(name, typeToMatch, true);
	}

	/**
	 * {@link #isTypeMatch(String, ResolvableType)} 的内部扩展变体，
	 * 判断给定名称的 Bean 是否匹配指定类型。可附加约束，避免过早创建 Bean。
	 * @param name the name of the bean to query
	 * @param typeToMatch the type to match against (as a {@code ResolvableType})
	 * @return {@code true} if the bean type matches, {@code false} if it
	 * doesn't match or cannot be determined yet
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 5.2
	 * @see #getBean
	 * @see #getType
	 */
	protected boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowFactoryBeanInit)
			throws NoSuchBeanDefinitionException {

		String beanName = transformedBeanName(name);
		boolean isFactoryDereference = BeanFactoryUtils.isFactoryDereference(name);

		// ----- 路径 A：已有手动注册/已创建的单例实例 -----
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null && beanInstance.getClass() != NullBean.class) {

			// FactoryBean：非 & 引用时匹配的是 getObject 产物类型
			if (beanInstance instanceof FactoryBean<?> factoryBean) {
				if (!isFactoryDereference) {
					Class<?> classToMatch = typeToMatch.resolve();
					if (factoryBean instanceof SmartFactoryBean<?> smartFactoryBean &&
							classToMatch != null && smartFactoryBean.supportsType(classToMatch)) {
						return true;
					}
					Class<?> type = getTypeForFactoryBean(factoryBean);
					if (type == null) {
						return false;
					}
					if (typeToMatch.isAssignableFrom(type)) {
						return true;
					}
					else if (typeToMatch.hasGenerics() && containsBeanDefinition(beanName)) {
						// 原始类型不够时，用 BD 上的泛型目标类型再比一次
						RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
						ResolvableType targetType = mbd.targetType;
						if (targetType == null) {
							targetType = mbd.factoryMethodReturnType;
						}
						if (targetType == null) {
							return false;
						}
						Class<?> targetClass = targetType.resolve();
						if (targetClass != null && FactoryBean.class.isAssignableFrom(targetClass)) {
							if (classToMatch != null && !FactoryBean.class.isAssignableFrom(classToMatch) &&
									!classToMatch.isAssignableFrom(targetType.toClass())) {
								return typeToMatch.isAssignableFrom(targetType.getGeneric());
							}
						}
						else {
							return typeToMatch.isAssignableFrom(targetType);
						}
					}
					return false;
				}
			}
			else if (isFactoryDereference) {
				// 普通 Bean 却用 & 解引用 → 不匹配
				return false;
			}

			// 直接对暴露出的实例做匹配
			if (typeToMatch.isInstance(beanInstance)) {
				// Direct match for exposed instance?
				return true;
			}
			else if (typeToMatch.hasGenerics() && containsBeanDefinition(beanName)) {
				// 泛型可能只在目标类上匹配，代理上未必可见
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				Class<?> targetType = mbd.getTargetType();
				if (targetType != null && targetType != ClassUtils.getUserClass(beanInstance)) {
					// Check raw class match as well, making sure it's exposed on the proxy.
					Class<?> classToMatch = typeToMatch.resolve();
					if (classToMatch != null && !classToMatch.isInstance(beanInstance)) {
						return false;
					}
					if (typeToMatch.isAssignableFrom(targetType)) {
						return true;
					}
				}
				ResolvableType resolvableType = mbd.targetType;
				if (resolvableType == null) {
					resolvableType = mbd.factoryMethodReturnType;
				}
				return (resolvableType != null && typeToMatch.isAssignableFrom(resolvableType));
			}
			else {
				return false;
			}
		}
		else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			// 注册了 null 单例（NullBean）且无 BD
			return false;
		}

		// ----- 路径 B：尚无实例 → 查 BD / 父工厂 -----
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.isTypeMatch(originalBeanName(name), typeToMatch);
		}

		// Retrieve corresponding bean definition.
		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();

		// 准备预测类型时要匹配的类型数组（常同时带上 FactoryBean.class）
		Class<?> classToMatch = typeToMatch.resolve();
		if (classToMatch == null) {
			classToMatch = FactoryBean.class;
		}
		Class<?>[] typesToMatch = (FactoryBean.class == classToMatch ?
				new Class<?>[] {classToMatch} : new Class<?>[] {FactoryBean.class, classToMatch});

		// Attempt to predict the bean type
		Class<?> predictedType = null;

		// 普通引用 + 装饰后的 FactoryBean：优先预测被装饰目标的类型（即最终 getObject 类型）
		if (!isFactoryDereference && dbd != null && isFactoryBean(beanName, mbd)) {
			// We should only attempt if the user explicitly set lazy-init to true
			// and we know the merged bean definition is for a factory bean.
			if (!mbd.isLazyInit() || allowFactoryBeanInit) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetType = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
				if (targetType != null && !FactoryBean.class.isAssignableFrom(targetType)) {
					predictedType = targetType;
				}
			}
		}

		// If we couldn't use the target type, try regular prediction.
		if (predictedType == null) {
			predictedType = predictBeanType(beanName, mbd, typesToMatch);
			if (predictedType == null) {
				return false;
			}
		}

		// Attempt to get the actual ResolvableType for the bean.
		ResolvableType beanType = null;

		// 预测结果是 FactoryBean：非 & 时改为看它创建的对象类型
		if (FactoryBean.class.isAssignableFrom(predictedType)) {
			if (beanInstance == null && !isFactoryDereference) {
				beanType = getTypeForFactoryBean(beanName, mbd, allowFactoryBeanInit);
				predictedType = beanType.resolve();
				if (predictedType == null) {
					return false;
				}
			}
		}
		else if (isFactoryDereference) {
			// 特殊情况：SmartInstantiationAwareBPP 预测出了非 FactoryBean，
			// 但调用方却在 & 解引用 → 再按 FactoryBean 预测一次原始类型
			predictedType = predictBeanType(beanName, mbd, FactoryBean.class);
			if (predictedType == null || !FactoryBean.class.isAssignableFrom(predictedType)) {
				return false;
			}
		}

		// 没有精确 ResolvableType 时，若 BD 的 targetType / 工厂方法返回类型与预测一致则采用之（保留泛型）
		if (beanType == null) {
			ResolvableType definedType = mbd.targetType;
			if (definedType == null) {
				definedType = mbd.factoryMethodReturnType;
			}
			if (definedType != null && definedType.resolve() == predictedType) {
				beanType = definedType;
			}
		}

		// If we have a bean type use it so that generics are considered
		if (beanType != null) {
			return typeToMatch.isAssignableFrom(beanType);
		}

		// If we don't have a bean type, fallback to the predicted type
		return typeToMatch.isAssignableFrom(predictedType);
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		return isTypeMatch(name, ResolvableType.forRawClass(typeToMatch));
	}

	@Override
	public @Nullable Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return getType(name, true);
	}

	@Override
	public @Nullable Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		// 1) 已有单例：FactoryBean 且非 & → 返回 getObjectType；否则返回实例 Class
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null && beanInstance.getClass() != NullBean.class) {
			if (beanInstance instanceof FactoryBean<?> factoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return getTypeForFactoryBean(factoryBean);
			}
			else {
				return beanInstance.getClass();
			}
		}

		// 2) 本工厂无 BD → 问父工厂
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.getType(originalBeanName(name));
		}

		// 3) 根据合并 BD 预测类型
		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		Class<?> beanClass = predictBeanType(beanName, mbd);

		if (beanClass != null) {
			// Check bean class whether we're dealing with a FactoryBean.
			if (FactoryBean.class.isAssignableFrom(beanClass)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					// If it's a FactoryBean, we want to look at what it creates, not at the factory class.
					beanClass = getTypeForFactoryBean(beanName, mbd, allowFactoryBeanInit).resolve();
				}
			}
			else if (BeanFactoryUtils.isFactoryDereference(name)) {
				return null;
			}
		}

		if (beanClass == null) {
			// 预测失败时，尝试装饰定义上的目标类型（代理类型往往更难判断）
			BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
			if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
				if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
					return targetClass;
				}
			}
		}

		return beanClass;
	}

	@Override
	public String[] getAliases(String name) {
		String beanName = transformedBeanName(name);
		List<String> aliases = new ArrayList<>();
		boolean hasFactoryPrefix = (!name.isEmpty() && name.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX_CHAR);
		String fullBeanName = beanName;
		if (hasFactoryPrefix) {
			fullBeanName = FACTORY_BEAN_PREFIX + beanName;
		}
		if (!fullBeanName.equals(name)) {
			aliases.add(fullBeanName);
		}
		String[] retrievedAliases = super.getAliases(beanName);
		String prefix = (hasFactoryPrefix ? FACTORY_BEAN_PREFIX : "");
		for (String retrievedAlias : retrievedAliases) {
			String alias = prefix + retrievedAlias;
			if (!alias.equals(name)) {
				aliases.add(alias);
			}
		}
		if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null) {
				aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
			}
		}
		return StringUtils.toStringArray(aliases);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public @Nullable BeanFactory getParentBeanFactory() {
		return this.parentBeanFactory;
	}

	@Override
	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
				(!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName)));
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public void setParentBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
			throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
		}
		if (this == parentBeanFactory) {
			throw new IllegalStateException("Cannot set parent bean factory to self");
		}
		this.parentBeanFactory = parentBeanFactory;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
	}

	@Override
	public @Nullable ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public void setTempClassLoader(@Nullable ClassLoader tempClassLoader) {
		this.tempClassLoader = tempClassLoader;
	}

	@Override
	public @Nullable ClassLoader getTempClassLoader() {
		return this.tempClassLoader;
	}

	@Override
	public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
		this.cacheBeanMetadata = cacheBeanMetadata;
	}

	@Override
	public boolean isCacheBeanMetadata() {
		return this.cacheBeanMetadata;
	}

	@Override
	public void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver) {
		this.beanExpressionResolver = resolver;
	}

	@Override
	public @Nullable BeanExpressionResolver getBeanExpressionResolver() {
		return this.beanExpressionResolver;
	}

	@Override
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public @Nullable ConversionService getConversionService() {
		return this.conversionService;
	}

	@Override
	public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
		Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
		if (registrar.overridesDefaultEditors()) {
			this.defaultEditorRegistrars.add(registrar);
		}
		else {
			this.propertyEditorRegistrars.add(registrar);
		}
	}

	/**
	 * 返回 PropertyEditorRegistrar 集合。
	 */
	public Set<PropertyEditorRegistrar> getPropertyEditorRegistrars() {
		return this.propertyEditorRegistrars;
	}

	@Override
	public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
		Assert.notNull(requiredType, "Required type must not be null");
		Assert.notNull(propertyEditorClass, "PropertyEditor class must not be null");
		this.customEditors.put(requiredType, propertyEditorClass);
	}

	@Override
	public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
		registerCustomEditors(registry);
	}

	/**
	 * 返回自定义编辑器映射：目标 Class → PropertyEditor Class。
	 */
	public Map<Class<?>, Class<? extends PropertyEditor>> getCustomEditors() {
		return this.customEditors;
	}

	@Override
	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}

	/**
	 * 返回自定义 TypeConverter（若有）。
	 * @return the custom TypeConverter, or {@code null} if none specified
	 */
	protected @Nullable TypeConverter getCustomTypeConverter() {
		return this.typeConverter;
	}

	@Override
	public TypeConverter getTypeConverter() {
		TypeConverter customConverter = getCustomTypeConverter();
		if (customConverter != null) {
			return customConverter;
		}
		else {
			// 无自定义时：新建 SimpleTypeConverter 并登记本工厂的编辑器
			SimpleTypeConverter typeConverter = new SimpleTypeConverter();
			typeConverter.setConversionService(getConversionService());
			registerCustomEditors(typeConverter);
			return typeConverter;
		}
	}

	@Override
	public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.embeddedValueResolvers.add(valueResolver);
	}

	@Override
	public boolean hasEmbeddedValueResolver() {
		return !this.embeddedValueResolvers.isEmpty();
	}

	@Override
	public @Nullable String resolveEmbeddedValue(@Nullable String value) {
		if (value == null) {
			return null;
		}
		String result = value;
		for (StringValueResolver resolver : this.embeddedValueResolvers) {
			result = resolver.resolveStringValue(result);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	@Override
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
		synchronized (this.beanPostProcessors) {
			// Remove from old position, if any
			this.beanPostProcessors.remove(beanPostProcessor);
			// Add to end of list
			this.beanPostProcessors.add(beanPostProcessor);
		}
	}

	/**
	 * 批量添加将应用于本工厂所创建 Bean 的 BeanPostProcessor。
	 * 应在工厂配置阶段调用。
	 * @since 5.3
	 * @see #addBeanPostProcessor
	 */
	public void addBeanPostProcessors(Collection<? extends BeanPostProcessor> beanPostProcessors) {
		synchronized (this.beanPostProcessors) {
			// Remove from old position, if any
			this.beanPostProcessors.removeAll(beanPostProcessors);
			// Add to end of list
			this.beanPostProcessors.addAll(beanPostProcessors);
		}
	}

	@Override
	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	/**
	 * 返回将应用于本工厂所创建 Bean 的 BeanPostProcessor 列表。
	 */
	public List<BeanPostProcessor> getBeanPostProcessors() {
		return this.beanPostProcessors;
	}

	/**
	 * 返回按类型预过滤的后处理器内部缓存；若尚无则现场（重新）构建。
	 * @since 5.3
	 */
	BeanPostProcessorCache getBeanPostProcessorCache() {
		synchronized (this.beanPostProcessors) {
			BeanPostProcessorCache bppCache = this.beanPostProcessorCache;
			if (bppCache == null) {
				bppCache = new BeanPostProcessorCache();
				for (BeanPostProcessor bpp : this.beanPostProcessors) {
					if (bpp instanceof InstantiationAwareBeanPostProcessor instantiationAwareBpp) {
						bppCache.instantiationAware.add(instantiationAwareBpp);
						if (bpp instanceof SmartInstantiationAwareBeanPostProcessor smartInstantiationAwareBpp) {
							bppCache.smartInstantiationAware.add(smartInstantiationAwareBpp);
						}
					}
					if (bpp instanceof DestructionAwareBeanPostProcessor destructionAwareBpp) {
						bppCache.destructionAware.add(destructionAwareBpp);
					}
					if (bpp instanceof MergedBeanDefinitionPostProcessor mergedBeanDefBpp) {
						bppCache.mergedDefinition.add(mergedBeanDefBpp);
					}
				}
				this.beanPostProcessorCache = bppCache;
			}
			return bppCache;
		}
	}

	private void resetBeanPostProcessorCache() {
		synchronized (this.beanPostProcessors) {
			this.beanPostProcessorCache = null;
		}
	}

	/**
	 * 本工厂是否持有会在单例创建时应用的 InstantiationAwareBeanPostProcessor。
	 * @see #addBeanPostProcessor
	 * @see org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
	 */
	protected boolean hasInstantiationAwareBeanPostProcessors() {
		return !getBeanPostProcessorCache().instantiationAware.isEmpty();
	}

	/**
	 * 本工厂是否持有会在关闭时应用的 DestructionAwareBeanPostProcessor。
	 * @see #addBeanPostProcessor
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
	 */
	protected boolean hasDestructionAwareBeanPostProcessors() {
		return !getBeanPostProcessorCache().destructionAware.isEmpty();
	}

	@Override
	public void registerScope(String scopeName, Scope scope) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		Assert.notNull(scope, "Scope must not be null");
		if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
		}
		Scope previous = this.scopes.put(scopeName, scope);
		if (previous != null && previous != scope) {
			if (logger.isDebugEnabled()) {
				logger.debug("Replacing scope '" + scopeName + "' from [" + previous + "] to [" + scope + "]");
			}
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("Registering scope '" + scopeName + "' with implementation [" + scope + "]");
			}
		}
	}

	@Override
	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	@Override
	public @Nullable Scope getRegisteredScope(String scopeName) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		return this.scopes.get(scopeName);
	}

	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		Assert.notNull(applicationStartup, "ApplicationStartup must not be null");
		this.applicationStartup = applicationStartup;
	}

	@Override
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		Assert.notNull(otherFactory, "BeanFactory must not be null");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
		setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
		setConversionService(otherFactory.getConversionService());
		if (otherFactory instanceof AbstractBeanFactory otherAbstractFactory) {
			this.defaultEditorRegistrars.addAll(otherAbstractFactory.defaultEditorRegistrars);
			this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
			this.customEditors.putAll(otherAbstractFactory.customEditors);
			this.typeConverter = otherAbstractFactory.typeConverter;
			this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
			this.scopes.putAll(otherAbstractFactory.scopes);
		}
		else {
			setTypeConverter(otherFactory.getTypeConverter());
			String[] otherScopeNames = otherFactory.getRegisteredScopeNames();
			for (String scopeName : otherScopeNames) {
				this.scopes.put(scopeName, otherFactory.getRegisteredScope(scopeName));
			}
		}
	}

	/**
	 * 返回给定 Bean 名称的「合并后」BeanDefinition；若是子定义则与父定义合并。
	 * <p>此 {@code getMergedBeanDefinition} 也会考虑祖先工厂中的定义。
	 * @param name the name of the bean to retrieve the merged definition for
	 * (may be an alias)
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	@Override
	public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
		String beanName = transformedBeanName(name);
		// 本工厂没有该 BD 且父工厂可配置 → 直接问父工厂
		if (getParentBeanFactory() instanceof ConfigurableBeanFactory parent && !containsBeanDefinition(beanName)) {
			return parent.getMergedBeanDefinition(beanName);
		}
		// 在本地解析合并定义
		return getMergedLocalBeanDefinition(beanName);
	}

	@Override
	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			return (beanInstance instanceof FactoryBean);
		}
		// No singleton instance found -> check bean definition.
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory cbf) {
			// No bean definition found in this factory -> delegate to parent.
			return cbf.isFactoryBean(name);
		}
		return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
	}

	@Override
	public boolean isActuallyInCreation(String beanName) {
		return (isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName));
	}

	/**
	 * 指定原型 Bean 是否正在当前线程中创建。
	 * @param beanName the name of the bean
	 */
	protected boolean isPrototypeCurrentlyInCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		return (curVal != null &&
				(curVal.equals(beanName) || (curVal instanceof Set<?> set && set.contains(beanName))));
	}

	/**
	 * 原型创建前的回调。
	 * <p>默认实现把该原型登记为「当前正在创建」。
	 * @param beanName the name of the prototype about to be created
	 * @see #isPrototypeCurrentlyInCreation
	 */
	@SuppressWarnings("unchecked")
	protected void beforePrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal == null) {
			this.prototypesCurrentlyInCreation.set(beanName);
		}
		else if (curVal instanceof String strValue) {
			Set<String> beanNameSet = CollectionUtils.newHashSet(2);
			beanNameSet.add(strValue);
			beanNameSet.add(beanName);
			this.prototypesCurrentlyInCreation.set(beanNameSet);
		}
		else {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.add(beanName);
		}
	}

	/**
	 * 原型创建后的回调。
	 * <p>默认实现取消「正在创建」标记。
	 * @param beanName the name of the prototype that has been created
	 * @see #isPrototypeCurrentlyInCreation
	 */
	@SuppressWarnings("unchecked")
	protected void afterPrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal instanceof String) {
			this.prototypesCurrentlyInCreation.remove();
		}
		else if (curVal instanceof Set<?> beanNameSet) {
			beanNameSet.remove(beanName);
			if (beanNameSet.isEmpty()) {
				this.prototypesCurrentlyInCreation.remove();
			}
		}
	}

	@Override
	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
	}

	/**
	 * 按给定 BeanDefinition 销毁 Bean 实例（通常是从本工厂取得的原型实例）。
	 * @param beanName the name of the bean definition
	 * @param bean the bean instance to destroy
	 * @param mbd the merged bean definition
	 */
	protected void destroyBean(String beanName, Object bean, RootBeanDefinition mbd) {
		new DisposableBeanAdapter(
				bean, beanName, mbd, getBeanPostProcessorCache().destructionAware).destroy();
	}

	@Override
	public void destroyScopedBean(String beanName) {
		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isSingleton() || mbd.isPrototype()) {
			throw new IllegalArgumentException(
					"Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
		}
		String scopeName = mbd.getScope();
		Scope scope = this.scopes.get(scopeName);
		if (scope == null) {
			throw new IllegalStateException("No Scope SPI registered for scope name '" + scopeName + "'");
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mbd);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * 返回规范 Bean 名称：必要时去掉工厂解引用前缀 {@code &}，并把别名解析为规范名。
	 * @param name the user-specified name
	 * @return the transformed bean name
	 */
	protected String transformedBeanName(String name) {
		return canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}

	/**
	 * 确定原始 Bean 名称：把本地别名解析为规范名，并在原 name 带 {@code &} 时补回前缀。
	 * @param name the user-specified name
	 * @return the original bean name
	 */
	protected String originalBeanName(String name) {
		String beanName = transformedBeanName(name);
		if (!name.isEmpty() && name.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX_CHAR) {
			beanName = FACTORY_BEAN_PREFIX + beanName;
		}
		return beanName;
	}

	/**
	 * 用本工厂已登记的自定义编辑器初始化给定 BeanWrapper。
	 * 供将要创建并填充 Bean 实例的 BeanWrapper 调用。
	 * <p>默认实现委托给 {@link #registerCustomEditors}，子类可覆盖。
	 * @param bw the BeanWrapper to initialize
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		bw.setConversionService(getConversionService());
		registerCustomEditors(bw);
	}

	/**
	 * 用本 BeanFactory 已登记的自定义编辑器初始化给定 PropertyEditorRegistry。
	 * <p>供创建/填充 Bean 的 BeanWrapper，以及构造参数、工厂方法类型转换用的
	 * SimpleTypeConverter 调用。
	 * @param registry the PropertyEditorRegistry to initialize
	 */
	protected void registerCustomEditors(PropertyEditorRegistry registry) {
		if (registry instanceof PropertyEditorRegistrySupport registrySupport) {
			registrySupport.useConfigValueEditors();
			if (!this.defaultEditorRegistrars.isEmpty()) {
				// 优化：仅在真正需要时惰性覆盖默认编辑器
				registrySupport.setDefaultEditorRegistrar(new BeanFactoryDefaultEditorRegistrar());
			}
		}
		else if (!this.defaultEditorRegistrars.isEmpty()) {
			// 回退：主动覆盖默认编辑器
			applyEditorRegistrars(registry, this.defaultEditorRegistrars);
		}

		if (!this.propertyEditorRegistrars.isEmpty()) {
			applyEditorRegistrars(registry, this.propertyEditorRegistrars);
		}
		if (!this.customEditors.isEmpty()) {
			this.customEditors.forEach((requiredType, editorClass) ->
					registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass)));
		}
	}

	private void applyEditorRegistrars(PropertyEditorRegistry registry, Set<PropertyEditorRegistrar> registrars) {
		for (PropertyEditorRegistrar registrar : registrars) {
			try {
				registrar.registerCustomEditors(registry);
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException bce) {
					String bceBeanName = bce.getBeanName();
					if (bceBeanName != null && isCurrentlyInCreation(bceBeanName)) {
						if (logger.isDebugEnabled()) {
							logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName() +
									"] failed because it tried to obtain currently created bean '" +
									ex.getBeanName() + "': " + ex.getMessage());
						}
						onSuppressedException(ex);
						return;
					}
				}
				throw ex;
			}
		}
	}

	/**
	 * 返回合并后的 RootBeanDefinition；若指定 Bean 对应子定义则沿父定义链合并。
	 * @param beanName the name of the bean to retrieve the merged definition for
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
		// 先无锁快查并发 Map；命中且未标记 stale 则直接用
		RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
		if (mbd != null && !mbd.stale) {
			return mbd;
		}
		// 未命中或已过期 → 取原始 BD 再走完整合并
		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}

	/**
	 * 为给定顶层 Bean 返回 RootBeanDefinition；若是子定义则与父合并。
	 * @param beanName the name of the bean definition
	 * @param bd the original bean definition (Root/ChildBeanDefinition)
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeanDefinitionStoreException {

		return getMergedBeanDefinition(beanName, bd, null);
	}

	/**
	 * 为给定 Bean 返回 RootBeanDefinition；若是子定义则与父合并。
	 * @param beanName the name of the bean definition
	 * @param bd the original bean definition (Root/ChildBeanDefinition)
	 * @param containingBd the containing bean definition in case of inner bean,
	 * or {@code null} in case of a top-level bean
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;
			RootBeanDefinition previous = null;

			// 顶层 Bean：持锁后再查一次，保证拿到同一份合并实例
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			if (mbd == null || mbd.stale) {
				previous = mbd;
				if (bd.getParentName() == null) {
					// 根定义：克隆或包装成 RootBeanDefinition
					if (bd instanceof RootBeanDefinition rootBeanDef) {
						mbd = rootBeanDef.cloneBeanDefinition();
					}
					else {
						mbd = new RootBeanDefinition(bd);
					}
				}
				else {
					// 子定义：先递归拿到父的合并结果，再 overrideFrom 覆盖
					BeanDefinition pbd;
					try {
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
							// 与父同名：只能到可配置的父工厂里解析
							if (getParentBeanFactory() instanceof ConfigurableBeanFactory parent) {
								pbd = parent.getMergedBeanDefinition(parentBeanName);
							}
							else {
								throw new NoSuchBeanDefinitionException(parentBeanName,
										"Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +
												"': cannot be resolved without a ConfigurableBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
								"Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
					}
					// Deep copy with overridden values.
					mbd = new RootBeanDefinition(pbd);
					mbd.overrideFrom(bd);
				}

				// 未配置 scope 时默认单例
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(SCOPE_SINGLETON);
				}

				// 内嵌在非单例外层 Bean 中的内嵌 Bean，自身也不能是单例——按外层 scope 纠正
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}

				// 缓存合并结果（之后若元数据变更仍可能重新合并）
				if (containingBd == null && (isCacheBeanMetadata() || isBeanEligibleForMetadataCaching(beanName))) {
					cacheMergedBeanDefinition(mbd, beanName);
				}
			}
			if (previous != null) {
				// stale 重建时，把仍适用的类型/工厂方法等缓存字段抄到新 mbd
				copyRelevantMergedBeanDefinitionCaches(previous, mbd);
			}
			return mbd;
		}
	}

	private void copyRelevantMergedBeanDefinitionCaches(RootBeanDefinition previous, RootBeanDefinition mbd) {
		if (ObjectUtils.nullSafeEquals(mbd.getBeanClassName(), previous.getBeanClassName()) &&
				ObjectUtils.nullSafeEquals(mbd.getFactoryBeanName(), previous.getFactoryBeanName()) &&
				ObjectUtils.nullSafeEquals(mbd.getFactoryMethodName(), previous.getFactoryMethodName())) {
			ResolvableType targetType = mbd.targetType;
			ResolvableType previousTargetType = previous.targetType;
			if (targetType == null || targetType.equals(previousTargetType)) {
				mbd.targetType = previousTargetType;
				mbd.isFactoryBean = previous.isFactoryBean;
				mbd.resolvedTargetType = previous.resolvedTargetType;
				mbd.factoryMethodReturnType = previous.factoryMethodReturnType;
				mbd.factoryMethodToIntrospect = previous.factoryMethodToIntrospect;
			}
			if (previous.hasMethodOverrides()) {
				mbd.setMethodOverrides(new MethodOverrides(previous.getMethodOverrides()));
			}
		}
	}

	/**
	 * 缓存给定的合并 BeanDefinition。
	 * <p>子类可覆盖，以便从最终后处理后的定义再派生额外缓存状态。
	 * @param mbd the merged bean definition to cache
	 * @param beanName the name of the bean
	 * @since 6.2.6
	 */
	protected void cacheMergedBeanDefinition(RootBeanDefinition mbd, String beanName) {
		this.mergedBeanDefinitions.put(beanName, mbd);
	}

	/**
	 * 检查给定合并 BeanDefinition，必要时抛出校验异常。
	 * @param mbd the merged bean definition to check
	 * @param beanName the name of the bean
	 * @param args the arguments for bean creation, if any
	 */
	protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, @Nullable Object @Nullable [] args) {
		if (mbd.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
		}
	}

	/**
	 * 清除指定 Bean 的合并定义（标记 stale），下次访问时再重建。
	 * @param beanName the bean name to clear the merged definition for
	 */
	protected void clearMergedBeanDefinition(String beanName) {
		RootBeanDefinition bd = this.mergedBeanDefinitions.get(beanName);
		if (bd != null) {
			bd.stale = true;
		}
	}

	/**
	 * 清理合并 BeanDefinition 缓存：对尚未具备完整元数据缓存资格的条目标记 stale。
	 * <p>通常在原始定义变更后触发，例如应用了 {@code BeanFactoryPostProcessor}。
	 * 注意：此时已创建过的 Bean，其元数据会保留。
	 * @since 4.2
	 */
	public void clearMetadataCache() {
		this.mergedBeanDefinitions.forEach((beanName, bd) -> {
			if (!isBeanEligibleForMetadataCaching(beanName)) {
				bd.stale = true;
			}
		});
	}

	/**
	 * 解析指定 BeanDefinition 的 Bean 类：必要时把类名解析为 Class，
	 * 并把结果存回定义供后续使用。
	 * @param mbd the merged bean definition to determine the class for
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return the resolved bean class (or {@code null} if none)
	 * @throws CannotLoadBeanClassException if we failed to load the class
	 */
	protected @Nullable Class<?> resolveBeanClass(RootBeanDefinition mbd, String beanName, Class<?>... typesToMatch)
			throws CannotLoadBeanClassException {

		try {
			if (mbd.hasBeanClass()) {
				return mbd.getBeanClass();
			}
			Class<?> beanClass = doResolveBeanClass(mbd, typesToMatch);
			if (mbd.hasBeanClass()) {
				mbd.prepareMethodOverrides();
			}
			return beanClass;
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(),
					beanName, "Validation of method overrides failed", ex);
		}
	}

	private @Nullable Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch)
			throws ClassNotFoundException {

		ClassLoader beanClassLoader = getBeanClassLoader();
		ClassLoader dynamicLoader = beanClassLoader;
		boolean freshResolve = false;

		if (!ObjectUtils.isEmpty(typesToMatch)) {
			// 仅做类型检查、尚未真正实例化时，可用临时 ClassLoader（如织入场景）
			ClassLoader tempClassLoader = getTempClassLoader();
			if (tempClassLoader != null) {
				dynamicLoader = tempClassLoader;
				freshResolve = true;
				if (tempClassLoader instanceof DecoratingClassLoader dcl) {
					for (Class<?> typeToMatch : typesToMatch) {
						dcl.excludeClass(typeToMatch.getName());
					}
				}
			}
		}

		String className = mbd.getBeanClassName();
		if (className != null) {
			Object evaluated = evaluateBeanDefinitionString(className, mbd);
			if (!className.equals(evaluated)) {
				// 动态解析出的表达式结果（4.2 起支持）
				if (evaluated instanceof Class<?> clazz) {
					return clazz;
				}
				else if (evaluated instanceof String name) {
					className = name;
					freshResolve = true;
				}
				else {
					throw new IllegalStateException("Invalid class name expression result: " + evaluated);
				}
			}
			if (freshResolve) {
				// 对着临时 ClassLoader 解析时尽早返回，避免把 Class 写进 BD 缓存
				if (dynamicLoader != null) {
					try {
						return dynamicLoader.loadClass(className);
					}
					catch (ClassNotFoundException ex) {
						if (logger.isTraceEnabled()) {
							logger.trace("Could not load class [" + className + "] from " + dynamicLoader + ": " + ex);
						}
					}
				}
				return ClassUtils.forName(className, dynamicLoader);
			}
		}

		// 常规解析，并把结果缓存在 BeanDefinition 中
		return mbd.resolveBeanClass(beanClassLoader);
	}

	/**
	 * 把 BeanDefinition 中的字符串当作表达式求值（若配置了解析器）。
	 * @param value the value to check
	 * @param beanDefinition the bean definition that the value comes from
	 * @return the resolved value
	 * @see #setBeanExpressionResolver
	 */
	protected @Nullable Object evaluateBeanDefinitionString(@Nullable String value, @Nullable BeanDefinition beanDefinition) {
		if (this.beanExpressionResolver == null) {
			return value;
		}

		Scope scope = null;
		if (beanDefinition != null) {
			String scopeName = beanDefinition.getScope();
			if (scopeName != null) {
				scope = getRegisteredScope(scopeName);
			}
		}
		return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
	}


	/**
	 * 预测指定 Bean 最终处理后的实例类型。由 {@link #getType} 与 {@link #isTypeMatch} 调用。
	 * 无需专门处理 FactoryBean，因为这里只关心原始 Bean 类型。
	 * <p>本实现较简单：不能处理工厂方法与 InstantiationAwareBeanPostProcessor，
	 * 仅对标准 Bean 能正确预测。子类应覆盖以做更精细的类型探测。
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition to determine the type for
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return the type of the bean, or {@code null} if not predictable
	 */
	protected @Nullable Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = mbd.getTargetType();
		if (targetType != null) {
			return targetType;
		}
		if (mbd.getFactoryMethodName() != null) {
			return null;
		}
		return resolveBeanClass(mbd, beanName, typesToMatch);
	}

	/**
	 * 判断给定 Bean 是否定义为 {@link FactoryBean}。
	 * @param beanName the name of the bean
	 * @param mbd the corresponding bean definition
	 */
	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		Boolean result = mbd.isFactoryBean;
		if (result == null) {
			Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
			result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
			mbd.isFactoryBean = result;
		}
		return result;
	}

	/**
	 * 在尚无目标 Bean 单例实例时，尽可能确定给定 FactoryBean 定义的 Bean 类型。
	 * 若 {@code allowInit} 为 {@code true} 且无法用其他方式判定，允许实例化目标工厂 Bean；
	 * 否则仅限于签名与相关元数据的内省。
	 * <p>若 BD 上未设置 {@link FactoryBean#OBJECT_TYPE_ATTRIBUTE} 且 {@code allowInit} 为
	 * {@code true}，默认实现会通过 {@code getBean} 创建 FactoryBean 并调用其
	 * {@code getObjectType}。鼓励子类优化此路径，通常检查工厂 Bean 类或创建它的工厂方法的泛型签名。
	 * 若子类确实实例化了 FactoryBean，应考虑在未完全填充 Bean 时就尝试 {@code getObjectType}；
	 * 失败后再回退到本实现的完整创建。
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param allowInit if initialization of the FactoryBean is permitted if the type
	 * cannot be determined another way
	 * @return the type for the bean if determinable, otherwise {@code ResolvableType.NONE}
	 * @since 5.2
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 * @see #getBean(String)
	 */
	protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
		try {
			ResolvableType result = getTypeForFactoryBeanFromAttributes(mbd);
			if (result != ResolvableType.NONE) {
				return result;
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
					String.valueOf(ex.getMessage()));
		}

		if (allowInit && mbd.isSingleton()) {
			try {
				// typeCheckOnly=true：可创建 FactoryBean 以询问 getObjectType，但不把它记为「已正式创建」
				FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
				Class<?> objectType = getTypeForFactoryBean(factoryBean);
				return (objectType != null ? ResolvableType.forClass(objectType) : ResolvableType.NONE);
			}
			catch (BeanCreationException ex) {
				if (ex.contains(BeanCurrentlyInCreationException.class)) {
					logger.trace(LogMessage.format("Bean currently in creation on FactoryBean type check: %s", ex));
				}
				else if (mbd.isLazyInit()) {
					logger.trace(LogMessage.format("Bean creation exception on lazy FactoryBean type check: %s", ex));
				}
				else {
					logger.debug(LogMessage.format("Bean creation exception on eager FactoryBean type check: %s", ex));
				}
				onSuppressedException(ex);
			}
		}

		// FactoryBean type not resolvable
		return ResolvableType.NONE;
	}

	/**
	 * 将指定 Bean 标记为已经创建（或即将创建）。
	 * <p>便于工厂针对该 Bean 的重复创建优化缓存。
	 * @param beanName the name of the bean
	 */
	protected void markBeanAsCreated(String beanName) {
		if (!this.alreadyCreated.contains(beanName)) {
			synchronized (this.mergedBeanDefinitions) {
				if (!isBeanEligibleForMetadataCaching(beanName)) {
					// 真正创建前让定义重新合并，以防期间元数据已变
					clearMergedBeanDefinition(beanName);
				}
				this.alreadyCreated.add(beanName);
			}
		}
	}

	/**
	 * Bean 创建失败后，清理相关缓存元数据。
	 * @param beanName the name of the bean
	 */
	protected void cleanupAfterBeanCreationFailure(String beanName) {
		synchronized (this.mergedBeanDefinitions) {
			this.alreadyCreated.remove(beanName);
		}
	}

	/**
	 * 判断指定 Bean 是否已有资格缓存其 BeanDefinition 元数据。
	 * @param beanName the name of the bean
	 * @return {@code true} if the bean's metadata may be cached
	 * at this point already
	 */
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return this.alreadyCreated.contains(beanName);
	}

	/**
	 * 若该单例仅因类型检查而被创建、尚未正式使用，则移除它。
	 * @param beanName the name of the bean
	 * @return {@code true} if actually removed, {@code false} otherwise
	 */
	protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
		if (!this.alreadyCreated.contains(beanName)) {
			removeSingleton(beanName);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 检查本工厂的 Bean 创建阶段是否已经开始，
	 * 即是否已有 Bean 被标记为已创建。
	 * @since 4.2.2
	 * @see #markBeanAsCreated
	 */
	protected boolean hasBeanCreationStarted() {
		return !this.alreadyCreated.isEmpty();
	}

	/**
	 * 根据给定 Bean 实例得到要对外暴露的对象：
	 * 普通 Bean 直接返回；若是 FactoryBean，则按是否带 {@code &} 前缀决定返回工厂本身还是 {@code getObject()} 产物。
	 * @param beanInstance the shared bean instance
	 * @param name the name that may include factory dereference prefix
	 * @param beanName the canonical bean name
	 * @param mbd the merged bean definition
	 * @return the object to expose for the bean
	 */
	protected Object getObjectForBeanInstance(Object beanInstance, @Nullable Class<?> requiredType,
			String name, String beanName, @Nullable RootBeanDefinition mbd) {

		// 调用方写了 &xxx：必须拿到 FactoryBean 本身
		if (BeanFactoryUtils.isFactoryDereference(name)) {
			if (beanInstance instanceof NullBean) {
				return beanInstance;
			}
			if (!(beanInstance instanceof FactoryBean)) {
				throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
			}
			if (mbd != null) {
				mbd.isFactoryBean = true;
			}
			return beanInstance;
		}

		// 到这里是普通引用：非 FactoryBean 直接返回；是 FactoryBean 则取 getObject() 产物
		if (!(beanInstance instanceof FactoryBean<?> factoryBean)) {
			return beanInstance;
		}

		Object object = null;
		if (mbd != null) {
			mbd.isFactoryBean = true;
		}
		else {
			// 无 mbd 时先看 FactoryBean 产物缓存（单例 FactoryBean 的 getObject 结果）
			object = getCachedObjectForFactoryBean(beanName);
		}
		if (object == null) {
			// Return bean instance from factory.
			// Caches object obtained from FactoryBean if it is a singleton.
			if (mbd == null && containsBeanDefinition(beanName)) {
				mbd = getMergedLocalBeanDefinition(beanName);
			}
			boolean synthetic = (mbd != null && mbd.isSynthetic());
			// synthetic（如某些内部 Bean）不做完整后处理缓存路径
			object = getObjectFromFactoryBean(factoryBean, requiredType, beanName, !synthetic);
		}
		return object;
	}

	/**
	 * 判断给定 Bean 名称是否已在本工厂占用，
	 * 即是否存在同名本地 Bean、别名，或以该名创建的内嵌 Bean。
	 * @param beanName the name to check
	 */
	public boolean isBeanNameInUse(String beanName) {
		return isAlias(beanName) || containsLocalBean(beanName) || hasDependentBean(beanName);
	}

	/**
	 * 判断给定 Bean 是否需要在关闭时销毁。
	 * <p>默认检查 DisposableBean 接口、指定的 destroy 方法，
	 * 以及已登记的 DestructionAwareBeanPostProcessor。
	 * @param bean the bean instance to check
	 * @param mbd the corresponding bean definition
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see AbstractBeanDefinition#getDestroyMethodName()
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
	 */
	protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
		return (bean.getClass() != NullBean.class && (DisposableBeanAdapter.hasDestroyMethod(bean, mbd) ||
				(hasDestructionAwareBeanPostProcessors() && DisposableBeanAdapter.hasApplicableProcessors(
						bean, getBeanPostProcessorCache().destructionAware))));
	}

	/**
	 * 若需要销毁，则把该 Bean 登记到本工厂的可销毁列表（仅单例走注册表；
	 * 自定义 scope 则把回调挂到 Scope 上）。关闭工厂时会调用 DisposableBean
	 * 与/或给定 destroy 方法。
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 * @param mbd the bean definition for the bean
	 * @see RootBeanDefinition#isSingleton
	 * @see RootBeanDefinition#getDependsOn
	 * @see #registerDisposableBean
	 * @see #registerDependentBean
	 */
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
		if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
			if (mbd.isSingleton()) {
				// 登记 DisposableBeanAdapter：统一执行 DestructionAwareBPP、DisposableBean、自定义 destroy
				registerDisposableBean(beanName, new DisposableBeanAdapter(
						bean, beanName, mbd, getBeanPostProcessorCache().destructionAware));
			}
			else {
				// 自定义 scope：把销毁回调交给 Scope
				Scope scope = this.scopes.get(mbd.getScope());
				if (scope == null) {
					throw new IllegalStateException("No Scope registered for scope name '" + mbd.getScope() + "'");
				}
				scope.registerDestructionCallback(beanName, new DisposableBeanAdapter(
						bean, beanName, mbd, getBeanPostProcessorCache().destructionAware));
			}
		}
	}


	//---------------------------------------------------------------------
	// Abstract methods to be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * 检查本工厂是否包含给定名称的 BeanDefinition。
	 * 不考虑本工厂可能参与的层级。当缓存中没有单例实例时由 {@code containsBean} 调用。
	 * <p>具体实现的代价可能较高（例如外部注册表的目录查找）。但对可列举工厂而言，
	 * 通常只是本地哈希查找，因而那里也把它放进了公共接口。同一实现可同时服务本模板方法与公共接口方法。
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 */
	protected abstract boolean containsBeanDefinition(String beanName);

	/**
	 * 返回给定 Bean 名称的 BeanDefinition。
	 * 子类通常应做缓存，因为每当需要定义元数据时本类都会调用此方法。
	 * <p>具体实现的代价可能较高（例如外部注册表的目录查找）。但对可列举工厂而言，
	 * 通常只是本地哈希查找，因而那里也把它放进了公共接口。同一实现可同时服务本模板方法与公共接口方法。
	 * @param beanName the name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name (never {@code null})
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition
	 */
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * 为给定的合并 BeanDefinition（及参数）创建 Bean 实例。
	 * 若是子定义，此时已与父定义合并完毕。
	 * <p>所有取 Bean 的方法最终都委托到此方法做真正创建。
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param args explicit arguments to use for constructor or factory method invocation
	 * @return a new instance of the bean
	 * @throws BeanCreationException if the bean could not be created
	 */
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object @Nullable [] args)
			throws BeanCreationException;


	/**
	 * 会在修改时重置 {@code beanPostProcessorCache} 字段的 CopyOnWriteArrayList。
	 *
	 * @since 5.3
	 */
	@SuppressWarnings("serial")
	private class BeanPostProcessorCacheAwareList extends CopyOnWriteArrayList<BeanPostProcessor> {

		@Override
		public BeanPostProcessor set(int index, BeanPostProcessor element) {
			BeanPostProcessor result = super.set(index, element);
			resetBeanPostProcessorCache();
			return result;
		}

		@Override
		public boolean add(BeanPostProcessor o) {
			boolean success = super.add(o);
			resetBeanPostProcessorCache();
			return success;
		}

		@Override
		public void add(int index, BeanPostProcessor element) {
			super.add(index, element);
			resetBeanPostProcessorCache();
		}

		@Override
		public BeanPostProcessor remove(int index) {
			BeanPostProcessor result = super.remove(index);
			resetBeanPostProcessorCache();
			return result;
		}

		@Override
		public boolean remove(Object o) {
			boolean success = super.remove(o);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean success = super.removeAll(c);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean success = super.retainAll(c);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public boolean addAll(Collection<? extends BeanPostProcessor> c) {
			boolean success = super.addAll(c);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public boolean addAll(int index, Collection<? extends BeanPostProcessor> c) {
			boolean success = super.addAll(index, c);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public boolean removeIf(Predicate<? super BeanPostProcessor> filter) {
			boolean success = super.removeIf(filter);
			if (success) {
				resetBeanPostProcessorCache();
			}
			return success;
		}

		@Override
		public void replaceAll(UnaryOperator<BeanPostProcessor> operator) {
			super.replaceAll(operator);
			resetBeanPostProcessorCache();
		}
	}


	/**
	 * 按类型预过滤后的后处理器内部缓存。
	 *
	 * @since 5.3
	 */
	static class BeanPostProcessorCache {

		/** InstantiationAwareBeanPostProcessor 列表。 */
		final List<InstantiationAwareBeanPostProcessor> instantiationAware = new ArrayList<>();

		/** SmartInstantiationAwareBeanPostProcessor 列表。 */
		final List<SmartInstantiationAwareBeanPostProcessor> smartInstantiationAware = new ArrayList<>();

		/** DestructionAwareBeanPostProcessor 列表。 */
		final List<DestructionAwareBeanPostProcessor> destructionAware = new ArrayList<>();

		/** MergedBeanDefinitionPostProcessor 列表。 */
		final List<MergedBeanDefinitionPostProcessor> mergedDefinition = new ArrayList<>();
	}


	/**
	 * 委托给本工厂 defaultEditorRegistrars 的 {@link PropertyEditorRegistrar}，
	 * 并为「编辑器又回指当前正在创建的 Bean」这类循环引用场景加上异常处理。
	 *
	 * @since 6.2.3
	 */
	class BeanFactoryDefaultEditorRegistrar implements PropertyEditorRegistrar {

		@Override
		public void registerCustomEditors(PropertyEditorRegistry registry) {
			applyEditorRegistrars(registry, defaultEditorRegistrars);
		}
	}

}
