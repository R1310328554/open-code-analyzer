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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.log.LogMessage;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.CompositeIterator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 对 {@link ConfigurableListableBeanFactory} 与 {@link BeanDefinitionRegistry}
 * 的默认实现：以 BeanDefinition 元数据为核心的完整 Bean 工厂，可通过后处理器扩展。
 *
 * <p>典型用法是先注册全部 BeanDefinition（例如从配置文件读入），再按需访问 Bean。
 * 按名称查找因此只需查本地定义表，成本很低，操作的是已解析好的元数据对象。
 *
 * <p>注意：各种格式的 BeanDefinition 读取器通常单独实现，而不是做成工厂子类，
 * 例如 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}。
 *
 * <p>若需要另一种 {@link org.springframework.beans.factory.ListableBeanFactory}
 * 实现，可看 {@link StaticListableBeanFactory}：它管理已有实例，而不是按定义新建。
 *
 * <p>继承关系上，本类通过 {@link AbstractAutowireCapableBeanFactory} /
 * {@link AbstractBeanFactory} 与 {@link DefaultSingletonBeanRegistry} 协作：
 * 本类负责「定义注册、按类型检索、依赖解析、启动预实例化」；
 * 单例缓存、三级缓存与循环依赖处理落在父类注册表一侧。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Costin Leau
 * @author Chris Beams
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 16 April 2001
 * @see #registerBeanDefinition
 * @see #addBeanPostProcessor
 * @see #getBean
 * @see #resolveDependency
 */
@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	/**
	 * 系统属性：强制 Bean 创建全程使用严格锁，而不是 6.2 默认的「严格 + 宽松」混合策略。
	 * 设为 {@code "true"} 可在整个预实例化阶段恢复 6.1.x 风格的严格加锁。
	 * <p>默认情况下，工厂会根据线程名前缀推断是否严格加锁：若额外线程的名字前缀与主引导线程一致，
	 * 则视为外部引导线程并发进入工厂，对其应用严格锁。显式设为 {@code "false"} 可关闭该推断。
	 * @since 6.2.6
	 * @see #preInstantiateSingletons()
	 */
	public static final String STRICT_LOCKING_PROPERTY_NAME = "spring.locking.strict";

	/** JSR-330 {@code jakarta.inject.Provider} 类型；类路径不可用时为 {@code null}。 */
	private static @Nullable Class<?> jakartaInjectProviderClass;

	static {
		try {
			jakartaInjectProviderClass =
					ClassUtils.forName("jakarta.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// 没有 JSR-330 API 时不支持 Provider 注入
			jakartaInjectProviderClass = null;
		}
	}


	/** 序列化 id → 工厂实例的弱引用表，反序列化时据此找回原工厂。 */
	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories =
			new ConcurrentHashMap<>(8);

	/** 本工厂是否强制严格锁；{@code null} 表示按线程名推断。 */
	private final @Nullable Boolean strictLocking = SpringProperties.checkFlag(STRICT_LOCKING_PROPERTY_NAME);

	/** 可选的序列化 id，用于把工厂引用写成可序列化桩对象。 */
	private @Nullable String serializationId;

	/** 是否允许同名 BeanDefinition 被后注册者覆盖；{@code null} 视为允许。 */
	private @Nullable Boolean allowBeanDefinitionOverriding;

	/** 是否允许对 lazy-init Bean 也急切加载 Class（按类型匹配时）。 */
	private boolean allowEagerClassLoading = true;

	/** 后台引导执行器；用于 {@code backgroundInit} 单例的异步预实例化。 */
	private @Nullable Executor bootstrapExecutor;

	/** 依赖集合/数组排序用的比较器（通常是 AnnotationAwareOrderComparator）。 */
	private @Nullable Comparator<Object> dependencyComparator;

	/**
	 * 自动注入候选判定器：是否可作为注入点候选、解析 {@code @Value}/限定符、懒代理等。
	 * 注解驱动场景下通常会被换成 ContextAnnotationAutowireCandidateResolver。
	 */
	private AutowireCandidateResolver autowireCandidateResolver = SimpleAutowireCandidateResolver.INSTANCE;

	/**
	 * 可直接按类型解析的「特殊依赖」：如 BeanFactory、ResourceLoader、ApplicationContext 等，
	 * 不经过普通 BeanDefinition 查找，优先于容器内同类型 Bean。
	 */
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

	/** Bean 名称 → BeanDefinition 的主存储表（注册与覆盖都落在这里）。 */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

	/** Bean 名称 → 合并后的 {@link BeanDefinitionHolder} 缓存，供候选判定复用。 */
	private final Map<String, BeanDefinitionHolder> mergedBeanDefinitionHolders = new ConcurrentHashMap<>(256);

	/**
	 * 带 {@code @Primary}（或 primary 标记）的 Bean 名 → 已解析类型。
	 * 值为 {@link Void} 表示尚未实例化，仅作 primary 索引。
	 */
	private final Map<String, Class<?>> primaryBeanNamesWithType = new ConcurrentHashMap<>(16);

	/** 按依赖类型缓存「含非单例」的 Bean 名数组（配置冻结后启用）。 */
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

	/** 按依赖类型缓存「仅单例」的 Bean 名数组（配置冻结后启用）。 */
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

	/** 按注册顺序保存的 BeanDefinition 名称列表（预实例化也按此顺序）。 */
	private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

	/** 手动 {@link #registerSingleton} 注册、且无对应定义的单例名集合。 */
	private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

	/** 配置冻结后缓存的 BeanDefinition 名称数组快照。 */
	private volatile String @Nullable [] frozenBeanDefinitionNames;

	/** 配置是否已冻结；冻结后允许更积极地缓存按类型查找结果。 */
	private volatile boolean configurationFrozen;

	/** 主引导线程名前缀；仅在预实例化阶段非空，用于锁策略推断。 */
	private volatile @Nullable String mainThreadPrefix;

	/** 标记当前线程处于 MAIN 还是 BACKGROUND 预实例化阶段。 */
	private final NamedThreadLocal<PreInstantiation> preInstantiationThread =
			new NamedThreadLocal<>("Pre-instantiation thread marker");


	/**
	 * 创建一个新的 DefaultListableBeanFactory。
	 */
	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * 创建一个带父工厂的 DefaultListableBeanFactory。
	 * @param parentBeanFactory 父 BeanFactory
	 */
	public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}


	/**
	 * 指定序列化用 id，反序列化时可据此找回本工厂实例。
	 */
	public void setSerializationId(@Nullable String serializationId) {
		if (serializationId != null) {
			serializableFactories.put(serializationId, new WeakReference<>(this));
		}
		else if (this.serializationId != null) {
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}

	/**
	 * 返回序列化 id（若已设置），反序列化时可据此还原本工厂。
	 * @since 4.1.2
	 */
	public @Nullable String getSerializationId() {
		return this.serializationId;
	}

	/**
	 * 是否允许通过再注册同名定义来覆盖已有 BeanDefinition（也会影响别名覆盖）。
	 * 不允许时抛异常。默认视为允许（字段为 {@code null} 时等同 {@code true}）。
	 * @see #registerBeanDefinition
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * 返回是否允许同名 BeanDefinition 覆盖。
	 * @since 4.1.2
	 */
	public boolean isAllowBeanDefinitionOverriding() {
		return !Boolean.FALSE.equals(this.allowBeanDefinitionOverriding);
	}

	/**
	 * 是否允许对标记为 lazy-init 的定义也急切加载 Class。
	 * <p>默认为 {@code true}。关掉后，除非显式按名获取，否则不会为做类型检查而加载懒 Bean 的类；
	 * 按类型查找会跳过尚未解析出类名的定义。
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	/**
	 * 返回是否允许对 lazy-init Bean 急切加载 Class。
	 * @since 4.1.2
	 */
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	@Override
	public void setBootstrapExecutor(@Nullable Executor bootstrapExecutor) {
		this.bootstrapExecutor = bootstrapExecutor;
	}

	@Override
	public @Nullable Executor getBootstrapExecutor() {
		return this.bootstrapExecutor;
	}

	/**
	 * 设置依赖 List/数组的排序比较器。
	 * @since 4.0
	 * @see org.springframework.core.OrderComparator
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	/**
	 * 返回本工厂的依赖比较器（可能为 {@code null}）。
	 * @since 4.0
	 */
	public @Nullable Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	/**
	 * 设置自定义自动注入候选解析器，用于判断某个 BeanDefinition 是否可作为注入候选。
	 */
	public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		if (autowireCandidateResolver instanceof BeanFactoryAware beanFactoryAware) {
			beanFactoryAware.setBeanFactory(this);
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	/**
	 * 返回本工厂的自动注入候选解析器（永不为 {@code null}）。
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}


	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory otherListableFactory) {
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.bootstrapExecutor = otherListableFactory.bootstrapExecutor;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			// 解析器可能 BeanFactoryAware，需要克隆一份再绑到本工厂
			setAutowireCandidateResolver(otherListableFactory.getAutowireCandidateResolver().cloneIfNecessary());
			// 把 ResourceLoader 等可解析依赖一并拷过来
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}


	//---------------------------------------------------------------------
	// 其余 BeanFactory 方法实现（按类型 getBean / ObjectProvider）
	//---------------------------------------------------------------------

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}

	/**
	 * 按类型获取 Bean：先 {@link #resolveNamedBean} 做多候选决策，必要时再问父工厂。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<T> requiredType, @Nullable Object @Nullable ... args) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		// 按类型解析；找不到返回 null，再统一抛 NoSuchBeanDefinitionException
		Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
		if (resolved == null) {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		Assert.notNull(requiredType, "Required type must not be null");
		return getBeanProvider(ResolvableType.forRawClass(requiredType), true);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		return getBeanProvider(requiredType, true);
	}

	public <T> ObjectProvider<T> getBeanProvider(ParameterizedTypeReference<T> requiredType) {
		return getBeanProvider(ResolvableType.forType(requiredType), true);
	}


	//---------------------------------------------------------------------
	// ListableBeanFactory 接口实现（按类型/注解批量查询）
	//---------------------------------------------------------------------

	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return this.beanDefinitionMap.containsKey(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		String[] frozenNames = this.frozenBeanDefinitionNames;
		if (frozenNames != null) {
			return frozenNames.clone();
		}
		else {
			return StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		Assert.notNull(requiredType, "Required type must not be null");
		return getBeanProvider(ResolvableType.forRawClass(requiredType), allowEagerInit);
	}

	/**
	 * 返回按类型延迟获取的 {@link ObjectProvider}；stream/orderedStream 会按需解析全部匹配 Bean。
	 */
	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		return new BeanObjectProvider<>() {
			@Override
			public T getObject() throws BeansException {
				T resolved = resolveBean(requiredType, null, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}
			@Override
			public T getObject(@Nullable Object... args) throws BeansException {
				T resolved = resolveBean(requiredType, args, false);
				if (resolved == null) {
					throw new NoSuchBeanDefinitionException(requiredType);
				}
				return resolved;
			}
			@Override
			public @Nullable T getIfAvailable() throws BeansException {
				try {
					return resolveBean(requiredType, null, false);
				}
				catch (ScopeNotActiveException ex) {
					// 作用域未激活，当作不可用
					return null;
				}
			}
			@Override
			public void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
				T dependency = getIfAvailable();
				if (dependency != null) {
					try {
						dependencyConsumer.accept(dependency);
					}
					catch (ScopeNotActiveException ex) {
						// 调用 scoped 代理时作用域也可能未激活，忽略
					}
				}
			}
			@Override
			public @Nullable T getIfUnique() throws BeansException {
				try {
					return resolveBean(requiredType, null, true);
				}
				catch (ScopeNotActiveException ex) {
					// 作用域未激活，当作不可用
					return null;
				}
			}
			@Override
			public void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
				T dependency = getIfUnique();
				if (dependency != null) {
					try {
						dependencyConsumer.accept(dependency);
					}
					catch (ScopeNotActiveException ex) {
						// 调用 scoped 代理时作用域也可能未激活，忽略
					}
				}
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> stream() {
				return Arrays.stream(beanNamesForStream(requiredType, true, allowEagerInit))
						.map(name -> (T) resolveBean(name, requiredType))
						.filter(bean -> !(bean instanceof NullBean));
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> orderedStream() {
				String[] beanNames = beanNamesForStream(requiredType, true, allowEagerInit);
				if (beanNames.length == 0) {
					return Stream.empty();
				}
				Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
				for (String beanName : beanNames) {
					Object beanInstance = resolveBean(beanName, requiredType);
					if (!(beanInstance instanceof NullBean)) {
						matchingBeans.put(beanName, (T) beanInstance);
					}
				}
				Stream<T> stream = matchingBeans.values().stream();
				return stream.sorted(adaptOrderComparator(matchingBeans));
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> stream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
				return Arrays.stream(beanNamesForStream(requiredType, includeNonSingletons, allowEagerInit))
						.filter(name -> customFilter.test(getType(name)))
						.map(name -> (T) resolveBean(name, requiredType))
						.filter(bean -> !(bean instanceof NullBean));
			}
			@SuppressWarnings("unchecked")
			@Override
			public Stream<T> orderedStream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
				String[] beanNames = beanNamesForStream(requiredType, includeNonSingletons, allowEagerInit);
				if (beanNames.length == 0) {
					return Stream.empty();
				}
				Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
				for (String beanName : beanNames) {
					if (customFilter.test(getType(beanName))) {
						Object beanInstance = resolveBean(beanName, requiredType);
						if (!(beanInstance instanceof NullBean)) {
							matchingBeans.put(beanName, (T) beanInstance);
						}
					}
				}
				return matchingBeans.values().stream().sorted(adaptOrderComparator(matchingBeans));
			}
		};
	}

	/**
	 * 按类型解析 Bean：本工厂 {@link #resolveNamedBean}，找不到再委托父工厂。
	 * @param nonUniqueAsNull 多候选且无法决出唯一时返回 {@code null}（而非抛异常）
	 */
	private <T> @Nullable T resolveBean(ResolvableType requiredType, @Nullable Object @Nullable [] args, boolean nonUniqueAsNull) {
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
		if (namedBean != null) {
			return namedBean.getBeanInstance();
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory dlbf) {
			return dlbf.resolveBean(requiredType, args, nonUniqueAsNull);
		}
		else if (parent != null) {
			ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
			if (args != null) {
				return parentProvider.getObject(args);
			}
			else {
				return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
			}
		}
		return null;
	}

	private String[] beanNamesForStream(ResolvableType requiredType, boolean includeNonSingletons, boolean allowEagerInit) {
		return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		return getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		Class<?> resolved = type.resolve();
		if (resolved != null && !type.hasGenerics()) {
			return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
		}
		else {
			return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		}
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}

	/**
	 * 按 Class 查 Bean 名：配置已冻结且允许急切初始化时走类型缓存。
	 */
	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
		}
		Map<Class<?>, String[]> cache =
				(includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
		String[] resolvedBeanNames = cache.get(type);
		if (resolvedBeanNames != null) {
			return resolvedBeanNames;
		}
		resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
		if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
			cache.put(type, resolvedBeanNames);
		}
		return resolvedBeanNames;
	}

	/**
	 * 真正按类型扫描：先遍历 beanDefinitionNames，再补上手动单例。
	 * FactoryBean 会先尝试匹配 getObject 产物类型，再尝试匹配 FactoryBean 自身（加 & 前缀）。
	 */
	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();

		// 1) 扫描全部 BeanDefinition（跳过别名，避免重复）
		for (String beanName : this.beanDefinitionNames) {
			// 别名不参与类型匹配，只处理规范名
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// 抽象定义跳过；不允许急切初始化时还要满足「已有 Class / 非懒 / 允许急切加载类」等条件
					if (!mbd.isAbstract() && (allowEagerInit ||
							(mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) &&
									!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
						boolean matchFound = false;
						boolean allowFactoryBeanInit = (allowEagerInit || containsSingleton(beanName));
						boolean isNonLazyDecorated = (dbd != null && !mbd.isLazyInit());
						if (!isFactoryBean) {
							if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
						}
						else {
							if (includeNonSingletons || isNonLazyDecorated) {
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
							}
							else if (allowFactoryBeanInit) {
								// 先做类型匹配再问 isSingleton，避免对明显不匹配的 FactoryBean 提前实例化
								matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit) &&
										isSingleton(beanName, mbd, dbd);
							}
							if (!matchFound) {
								// 产品类型不匹配时，再试 FactoryBean 本身（&beanName）
								beanName = FACTORY_BEAN_PREFIX + beanName;
								if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
									matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
								}
							}
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// 多半是占位符尚未解析，按类型匹配时忽略
					LogMessage message = (ex instanceof CannotLoadBeanClassException ?
							LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
							LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName));
					logger.trace(message, ex);
					// 记下被压制的异常，便于后续诊断「其实定义有问题」
					onSuppressedException(ex);
				}
				catch (NoSuchBeanDefinitionException ex) {
					// 迭代过程中定义被移除，忽略即可
				}
			}
		}

		// 2) 再检查手动 registerSingleton 的实例
		for (String beanName : this.manualSingletonNames) {
			try {
				// FactoryBean：先匹配其 getObject 创建的对象
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// 已匹配产品，不再匹配 FactoryBean 自身
						continue;
					}
					// 产品不匹配，再试 FactoryBean 本身
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// 匹配原始实例（也可能是裸 FactoryBean）
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// 理论上少见，多与循环引用解析过程有关
				logger.trace(LogMessage.format(
						"Failed to check manually registered singleton with name '%s'", beanName), ex);
			}
		}

		return StringUtils.toStringArray(result);
	}

	private boolean isSingleton(String beanName, RootBeanDefinition mbd, @Nullable BeanDefinitionHolder dbd) {
		return (dbd != null ? mbd.isSingleton() : isSingleton(beanName));
	}

	/**
	 * 判断为得到某 Bean 的类型，是否必须先急切初始化其 factoryBean。
	 * @param factoryBeanName 定义上声明的 factory-bean 引用
	 * @return 是否必须急切初始化
	 */
	private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
		return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfType(
			@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {

		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			try {
				Object beanInstance = (type != null ? getBean(beanName, type) : getBean(beanName));
				if (!(beanInstance instanceof NullBean)) {
					result.put(beanName, (T) beanInstance);
				}
			}
			catch (BeanNotOfRequiredTypeException ex) {
				// 忽略：多半是 NullBean
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException bce) {
					String exBeanName = bce.getBeanName();
					if (exBeanName != null && isCurrentlyInCreation(exBeanName)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Ignoring match to currently created bean '" + exBeanName + "': " +
									ex.getMessage());
						}
						onSuppressedException(ex);
						// 构造器自动注入遇到循环引用：跳过「正在创建中」的那个，继续找其他匹配
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		List<String> result = new ArrayList<>();
		for (String beanName : this.beanDefinitionNames) {
			BeanDefinition bd = this.beanDefinitionMap.get(beanName);
			if (bd != null && !bd.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		for (String beanName : this.manualSingletonNames) {
			if (!result.contains(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		return StringUtils.toStringArray(result);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		String[] beanNames = getBeanNamesForAnnotation(annotationType);
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(beanNames.length);
		for (String beanName : beanNames) {
			Object beanInstance = getBean(beanName);
			if (!(beanInstance instanceof NullBean)) {
				result.put(beanName, beanInstance);
			}
		}
		return result;
	}

	@Override
	public <A extends Annotation> @Nullable A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {

		return findAnnotationOnBean(beanName, annotationType, true);
	}

	@Override
	public <A extends Annotation> @Nullable A findAnnotationOnBean(
			String beanName, Class<A> annotationType, boolean allowFactoryBeanInit)
			throws NoSuchBeanDefinitionException {

		Class<?> beanType = getType(beanName, allowFactoryBeanInit);
		if (beanType != null) {
			MergedAnnotation<A> annotation =
					MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
			if (annotation.isPresent()) {
				return annotation.synthesize();
			}
		}
		if (containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			// 代理场景下再查原始 beanClass
			if (bd.hasBeanClass() && bd.getFactoryMethodName() == null) {
				Class<?> beanClass = bd.getBeanClass();
				if (beanClass != beanType) {
					MergedAnnotation<A> annotation =
							MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
					if (annotation.isPresent()) {
						return annotation.synthesize();
					}
				}
			}
			// 再查工厂方法上声明的注解
			Method factoryMethod = bd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				MergedAnnotation<A> annotation =
						MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
				if (annotation.isPresent()) {
					return annotation.synthesize();
				}
			}
		}
		return null;
	}

	@Override
	public <A extends Annotation> Set<A> findAllAnnotationsOnBean(
			String beanName, Class<A> annotationType, boolean allowFactoryBeanInit)
			throws NoSuchBeanDefinitionException {

		Set<A> annotations = new LinkedHashSet<>();
		Class<?> beanType = getType(beanName, allowFactoryBeanInit);
		if (beanType != null) {
			MergedAnnotations.from(beanType, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
					.stream(annotationType)
					.filter(MergedAnnotation::isPresent)
					.forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
		}
		if (containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			// 代理场景下再查原始 beanClass
			if (bd.hasBeanClass() && bd.getFactoryMethodName() == null) {
				Class<?> beanClass = bd.getBeanClass();
				if (beanClass != beanType) {
					MergedAnnotations.from(beanClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
							.stream(annotationType)
							.filter(MergedAnnotation::isPresent)
							.forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
				}
			}
			// 再查工厂方法上声明的注解
			Method factoryMethod = bd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				MergedAnnotations.from(factoryMethod, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
						.stream(annotationType)
						.filter(MergedAnnotation::isPresent)
						.forEach(mergedAnnotation -> annotations.add(mergedAnnotation.synthesize()));
			}
		}
		return annotations;
	}


	//---------------------------------------------------------------------
	// ConfigurableListableBeanFactory 接口实现
	//---------------------------------------------------------------------

	/**
	 * 注册「按类型可直接解析」的特殊依赖（如 BeanFactory 自身），写入 {@link #resolvableDependencies}。
	 */
	@Override
	public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
		Assert.notNull(dependencyType, "Dependency type must not be null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
				throw new IllegalArgumentException("Value [" + autowiredValue +
						"] does not implement specified dependency type [" + dependencyType.getName() + "]");
			}
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}

	@Override
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {

		return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
	}

	/**
	 * 判断指定 Bean 是否可作为自动注入候选（本工厂无定义则委托父工厂）。
	 * @param beanName 待检查的 Bean 名
	 * @param descriptor 待解析的依赖描述
	 * @param resolver 实际执行候选算法的解析器
	 * @return 是否应视为自动注入候选
	 */
	protected boolean isAutowireCandidate(
			String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver)
			throws NoSuchBeanDefinitionException {

		String bdName = transformedBeanName(beanName);
		if (containsBeanDefinition(bdName)) {
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(bdName), descriptor, resolver);
		}
		else if (containsSingleton(beanName)) {
			// 仅有单例实例、无定义时，用类型包一层 RootBeanDefinition 再判
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
		}

		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory dlbf) {
			// 本工厂没有该定义 → 交给父工厂，并带上同一个 resolver
			return dlbf.isAutowireCandidate(beanName, descriptor, resolver);
		}
		else if (parent instanceof ConfigurableListableBeanFactory clbf) {
			// 父工厂不是本类型，无法透传 resolver
			return clbf.isAutowireCandidate(beanName, descriptor);
		}
		else {
			return true;
		}
	}

	/**
	 * 基于合并后的 RootBeanDefinition，交给 {@link AutowireCandidateResolver} 做最终判定
	 *（含 {@code autowireCandidate} 标志、限定符、泛型等）。
	 * @param beanName 待检查的 Bean 名
	 * @param mbd 合并后的 Bean 定义
	 * @param descriptor 待解析的依赖描述
	 * @param resolver 实际执行候选算法的解析器
	 * @return 是否应视为自动注入候选
	 */
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {

		String bdName = transformedBeanName(beanName);
		resolveBeanClass(mbd, bdName);
		if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
		BeanDefinitionHolder holder = (beanName.equals(bdName) ?
				this.mergedBeanDefinitionHolders.computeIfAbsent(beanName,
						key -> new BeanDefinitionHolder(mbd, beanName, getAliases(bdName))) :
				new BeanDefinitionHolder(mbd, beanName, getAliases(bdName)));
		return resolver.isAutowireCandidate(holder, descriptor);
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<>();
		iterator.add(this.beanDefinitionNames.iterator());
		iterator.add(this.manualSingletonNames.iterator());
		return iterator;
	}

	@Override
	protected void clearMergedBeanDefinition(String beanName) {
		super.clearMergedBeanDefinition(beanName);
		this.mergedBeanDefinitionHolders.remove(beanName);
	}

	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		this.mergedBeanDefinitionHolders.clear();
		clearByTypeCache();
	}

	@Override
	public void freezeConfiguration() {
		clearMetadataCache();
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	/**
	 * 配置已冻结时，所有 Bean 都允许缓存合并元数据。
	 * @see #freezeConfiguration()
	 */
	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
	}

	@Override
	protected @Nullable Object obtainInstanceFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd)
			throws Exception {

		if (supplier instanceof InstanceSupplier<?> instanceSupplier) {
			return instanceSupplier.get(RegisteredBean.of(this, beanName, mbd));
		}
		return super.obtainInstanceFromSupplier(supplier, beanName, mbd);
	}

	/** 缓存合并定义时同步维护 primary 索引。 */
	@Override
	protected void cacheMergedBeanDefinition(RootBeanDefinition mbd, String beanName) {
		super.cacheMergedBeanDefinition(mbd, beanName);
		if (mbd.isPrimary()) {
			this.primaryBeanNamesWithType.put(beanName, Void.class);
		}
	}

	/**
	 * 校验合并定义：backgroundInit 与主/后台引导线程不能交叉「硬」拉取对方负责的 Bean。
	 */
	@Override
	protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, @Nullable Object @Nullable [] args) {
		super.checkMergedBeanDefinition(mbd, beanName, args);

		if (mbd.isBackgroundInit()) {
			if (this.preInstantiationThread.get() == PreInstantiation.MAIN && getBootstrapExecutor() != null) {
				throw new BeanCurrentlyInCreationException(beanName, "Bean marked for background " +
						"initialization but requested in mainline thread - declare ObjectProvider " +
						"or lazy injection point in dependent mainline beans");
			}
		}
		else {
			// 该 Bean 应在主引导线程初始化
			if (this.preInstantiationThread.get() == PreInstantiation.BACKGROUND) {
				throw new BeanCurrentlyInCreationException(beanName, "Bean marked for mainline initialization " +
						"but requested in background thread - enforce early instantiation in mainline thread " +
						"through depends-on '" + beanName + "' declaration for dependent background beans");
			}
		}
	}

	/**
	 * 预实例化阶段的单例锁策略：
	 * {@code null}=强制拿完整锁；{@code true}=允许持锁（可宽松）；{@code false}=禁止持锁（后台线程）。
	 */
	@Override
	protected @Nullable Boolean isCurrentThreadAllowedToHoldSingletonLock() {
		String mainThreadPrefix = this.mainThreadPrefix;
		if (mainThreadPrefix != null) {
			// 仅在 preInstantiateSingletons 阶段区分；mainThreadPrefix 非空即表示处于该阶段

			PreInstantiation preInstantiation = this.preInstantiationThread.get();
			if (preInstantiation != null) {
				// Spring 管理的引导线程：
				// MAIN 可持锁(true)，严格模式下强制完整锁(null)；BACKGROUND 永不持锁(false)
				return switch (preInstantiation) {
					case MAIN -> (Boolean.TRUE.equals(this.strictLocking) ? null : true);
					case BACKGROUND -> false;
				};
			}

			// 非 Spring 管理的引导线程
			if (Boolean.FALSE.equals(this.strictLocking)) {
				// 显式配置为尽可能宽松加锁
				return true;
			}
			else if (this.strictLocking == null) {
				// 未显式配置 → 按线程名前缀推断
				if (!getThreadNamePrefix().equals(mainThreadPrefix)) {
					// 与主引导线程池前缀不同，视为应用内部线程，可用宽松锁
					return true;
				}
			}
		}

		// 传统行为：始终强制持完整锁
		return null;
	}

	@Override
	public void prepareSingletonBootstrap() {
		this.mainThreadPrefix = getThreadNamePrefix();
	}

	/**
	 * 启动阶段预实例化所有非懒加载单例：先创建实例，再回调 {@link SmartInitializingSingleton}。
	 * <p>与 {@link DefaultSingletonBeanRegistry} 协作：真正创建走 {@code getBean} → 父类单例缓存/三级缓存；
	 * {@code backgroundInit} 会先 {@link #addSingletonFactory} 占坑，供其他线程等待。
	 */
	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isTraceEnabled()) {
			logger.trace("Pre-instantiating singletons in " + this);
		}

		// 拷贝一份名字列表：初始化过程中可能又 register 新定义，避免 ConcurrentModification
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// 标记进入主引导预实例化阶段
		this.preInstantiationThread.set(PreInstantiation.MAIN);
		if (this.mainThreadPrefix == null) {
			this.mainThreadPrefix = getThreadNamePrefix();
		}
		try {
			List<CompletableFuture<?>> futures = new ArrayList<>();
			for (String beanName : beanNames) {
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				// 只处理非抽象单例；prototype / 抽象定义跳过
				if (!mbd.isAbstract() && mbd.isSingleton()) {
					CompletableFuture<?> future = preInstantiateSingleton(beanName, mbd);
					if (future != null) {
						futures.add(future);
					}
				}
			}
			// 等待所有后台预实例化完成
			if (!futures.isEmpty()) {
				try {
					CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
				}
				catch (CompletionException ex) {
					ReflectionUtils.rethrowRuntimeException(ex.getCause());
				}
			}
		}
		finally {
			this.mainThreadPrefix = null;
			this.preInstantiationThread.remove();
		}

		// 全部单例起来后，触发 SmartInitializingSingleton 回调（如 EventListenerMethodProcessor）
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName, false);
			if (singletonInstance instanceof SmartInitializingSingleton smartSingleton) {
				StartupStep smartInitialize = getApplicationStartup().start("spring.beans.smart-initialize")
						.tag("beanName", beanName);
				smartSingleton.afterSingletonsInstantiated();
				smartInitialize.end();
			}
		}
	}

	/**
	 * 预实例化单个单例：backgroundInit 走异步；否则非 lazy 的在主线程 {@link #instantiateSingleton}。
	 */
	private @Nullable CompletableFuture<?> preInstantiateSingleton(String beanName, RootBeanDefinition mbd) {
		if (mbd.isBackgroundInit()) {
			Executor executor = getBootstrapExecutor();
			if (executor != null) {
				// depends-on 必须在主线程先建好，避免后台线程去硬拉主线程 Bean
				String[] dependsOn = mbd.getDependsOn();
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						getBean(dep);
					}
				}
				// factoryBean 引用同样先在主线程初始化
				String factoryBeanName = mbd.getFactoryBeanName();
				if (factoryBeanName != null) {
					getBean(factoryBeanName);
				}
				// 当前 Bean 丢到后台线程创建
				CompletableFuture<?> future = CompletableFuture.runAsync(
						() -> instantiateSingletonInBackgroundThread(beanName), executor);
				// 三级缓存占坑：其他线程 getSingleton 时 join 等待后台完成
				addSingletonFactory(beanName, () -> {
					try {
						future.join();
					}
					catch (CompletionException ex) {
						ReflectionUtils.rethrowRuntimeException(ex.getCause());
					}
					return future;  // 不应被当作 Bean 暴露；类型不对会 ClassCastException，便于暴露误用
				});
				return (!mbd.isLazyInit() ? future : null);
			}
			else if (logger.isInfoEnabled()) {
				logger.info("Bean '" + beanName + "' marked for background initialization " +
						"without bootstrap executor configured - falling back to mainline initialization");
			}
		}

		// 普通路径：非 lazy-init 单例在主线程立刻 getBean
		if (!mbd.isLazyInit()) {
			try {
				instantiateSingleton(beanName);
			}
			catch (BeanCurrentlyInCreationException ex) {
				logger.info("Bean '" + beanName + "' marked for pre-instantiation (not lazy-init) " +
						"but currently initialized by other thread - skipping it in mainline thread");
			}
		}
		return null;
	}

	/** 后台线程中实例化单例，并标记 {@link PreInstantiation#BACKGROUND}。 */
	private void instantiateSingletonInBackgroundThread(String beanName) {
		this.preInstantiationThread.set(PreInstantiation.BACKGROUND);
		try {
			instantiateSingleton(beanName);
		}
		catch (RuntimeException | Error ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to instantiate singleton bean '" + beanName + "' in background thread", ex);
			}
			throw ex;
		}
		finally {
			this.preInstantiationThread.remove();
		}
	}

	/**
	 * 真正触发单例创建：FactoryBean 先拿工厂本身，仅当 {@link SmartFactoryBean#isEagerInit()} 再创建产品。
	 */
	private void instantiateSingleton(String beanName) {
		if (isFactoryBean(beanName)) {
			Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
			if (bean instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isEagerInit()) {
				getBean(beanName);
			}
		}
		else {
			getBean(beanName);
		}
	}

	private Object resolveBean(String beanName, ResolvableType requiredType) {
		try {
			// 带上 requiredType，便于 SmartFactoryBean 按类型决策
			return getBean(beanName, requiredType.toClass());
		}
		catch (BeanNotOfRequiredTypeException ex) {
			// 多半是 NullBean 场景，退回不带类型的 getBean
			return getBean(beanName);
		}
	}

	private static String getThreadNamePrefix() {
		String name = Thread.currentThread().getName();
		int numberSeparator = name.lastIndexOf('-');
		return (numberSeparator >= 0 ? name.substring(0, numberSeparator) : name);
	}


	//---------------------------------------------------------------------
	// BeanDefinitionRegistry 接口实现（注册 / 覆盖 / 移除）
	//---------------------------------------------------------------------

	/**
	 * 注册 BeanDefinition：校验 → 处理同名覆盖/别名冲突 → 写入 map 与有序列表 → 必要时重置缓存。
	 * <p>覆盖策略由 {@link #isBeanDefinitionOverridable} / {@link #allowBeanDefinitionOverriding} 控制；
	 * Spring Boot 默认常关闭覆盖，重复注册会抛 {@link BeanDefinitionOverrideException}。
	 */
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		// AbstractBeanDefinition 先做方法重写等合法性校验
		if (beanDefinition instanceof AbstractBeanDefinition abd) {
			try {
				abd.validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
		if (existingDefinition != null) {
			// 同名已存在：不允许覆盖则失败，允许则替换 map 中的定义
			if (!isBeanDefinitionOverridable(beanName)) {
				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
			}
			else {
				logBeanDefinitionOverriding(beanName, beanDefinition, existingDefinition);
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
		else {
			// 名称当前是别名：要么覆盖别名指向的定义，要么去掉别名再注册
			if (isAlias(beanName)) {
				String aliasedName = canonicalName(beanName);
				if (!isBeanDefinitionOverridable(aliasedName)) {
					if (containsBeanDefinition(aliasedName)) {  // 别名指向已有定义
						throw new BeanDefinitionOverrideException(
								beanName, beanDefinition, getBeanDefinition(aliasedName));
					}
					else {  // 别名指向尚不存在的 Bean
						throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
								"Cannot register bean definition for bean '" + beanName +
								"' since there is already an alias for bean '" + aliasedName + "' bound.");
					}
				}
				else {
					if (logger.isInfoEnabled()) {
						logger.info("Removing alias '" + beanName + "' for bean '" + aliasedName +
								"' due to registration of bean definition for bean '" + beanName + "': [" +
								beanDefinition + "]");
					}
					removeAlias(beanName);
				}
			}
			if (hasBeanCreationStarted()) {
				// 已开始创建 Bean：不能直接改启动期集合，拷贝后整体替换以保证迭代稳定
				synchronized (this.beanDefinitionMap) {
					this.beanDefinitionMap.put(beanName, beanDefinition);
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					removeManualSingletonName(beanName);
				}
			}
			else {
				// 仍处于启动注册阶段，直接追加即可
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				removeManualSingletonName(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}

		// 覆盖已有定义或已有单例实例时，清合并定义/销毁单例/通知后处理器
		if (existingDefinition != null || containsSingleton(beanName)) {
			resetBeanDefinition(beanName);
		}
		else if (isConfigurationFrozen()) {
			clearByTypeCache();
		}

		// 维护 primary 索引，供依赖决策快速排查冲突
		if (beanDefinition.isPrimary()) {
			this.primaryBeanNamesWithType.put(beanName, Void.class);
		}
	}

	/** 按角色/是否等价，用不同日志级别记录 BeanDefinition 覆盖。 */
	private void logBeanDefinitionOverriding(String beanName, BeanDefinition beanDefinition,
			BeanDefinition existingDefinition) {

		boolean explicitBeanOverride = (this.allowBeanDefinitionOverriding != null);
		if (existingDefinition.getRole() < beanDefinition.getRole()) {
			// 例如原 ROLE_APPLICATION 被 ROLE_SUPPORT / ROLE_INFRASTRUCTURE 覆盖
			if (logger.isInfoEnabled()) {
				logger.info("Overriding user-defined bean definition for bean '" + beanName +
						"' with a framework-generated bean definition: replacing [" +
						existingDefinition + "] with [" + beanDefinition + "]");
			}
		}
		else if (!beanDefinition.equals(existingDefinition)) {
			if (explicitBeanOverride && logger.isInfoEnabled()) {
				logger.info("Overriding bean definition for bean '" + beanName +
						"' with a different definition: replacing [" + existingDefinition +
						"] with [" + beanDefinition + "]");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Overriding bean definition for bean '" + beanName +
						"' with a different definition: replacing [" + existingDefinition +
						"] with [" + beanDefinition + "]");
			}
		}
		else {
			if (explicitBeanOverride && logger.isInfoEnabled()) {
				logger.info("Overriding bean definition for bean '" + beanName +
						"' with an equivalent definition: replacing [" + existingDefinition +
						"] with [" + beanDefinition + "]");
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Overriding bean definition for bean '" + beanName +
						"' with an equivalent definition: replacing [" + existingDefinition +
						"] with [" + beanDefinition + "]");
			}
		}
	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}

		if (hasBeanCreationStarted()) {
			// 创建已开始：拷贝列表后替换，保证迭代稳定
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
				updatedDefinitions.remove(beanName);
				this.beanDefinitionNames = updatedDefinitions;
			}
		}
		else {
			// 仍在启动注册阶段
			this.beanDefinitionNames.remove(beanName);
		}
		this.frozenBeanDefinitionNames = null;

		resetBeanDefinition(beanName);
	}

	/**
	 * 重置给定 Bean 的全部相关缓存，并递归重置以它为 parent 的子定义。
	 * <p>在定义被替换或移除后调用：会触发 {@link #clearMergedBeanDefinition}、
	 * {@link #destroySingleton} 以及 {@link MergedBeanDefinitionPostProcessor#resetBeanDefinition}。
	 * @param beanName 要重置的 Bean 名
	 * @see #registerBeanDefinition
	 * @see #removeBeanDefinition
	 */
	protected void resetBeanDefinition(String beanName) {
		// 清掉已合并的 RootBeanDefinition
		clearMergedBeanDefinition(beanName);

		// 若单例缓存里还有实例一并销毁（常见于覆盖容器默认 Bean）
		destroySingleton(beanName);

		// 去掉 primary 索引
		this.primaryBeanNamesWithType.remove(beanName);

		// 通知 MergedBeanDefinitionPostProcessor
		for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
			processor.resetBeanDefinition(beanName);
		}

		// 递归重置 parentName 指向本 Bean 的子定义
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				// beanDefinitionMap 可能并发修改，bd 需判空
				if (bd != null && beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}

	/**
	 * 本实现：只要全局允许覆盖，任意 Bean 名都可覆盖。
	 * @see #setAllowBeanDefinitionOverriding
	 */
	@Override
	public boolean isBeanDefinitionOverridable(String beanName) {
		return isAllowBeanDefinitionOverriding();
	}

	/**
	 * 别名覆盖与 BeanDefinition 覆盖开关保持一致。
	 * @see #setAllowBeanDefinitionOverriding
	 */
	@Override
	protected boolean allowAliasOverriding() {
		return isAllowBeanDefinitionOverriding();
	}

	/**
	 * 额外检查：别名不能在不允许覆盖时盖住同名 BeanDefinition。
	 */
	@Override
	protected void checkForAliasCircle(String name, String alias) {
		super.checkForAliasCircle(name, alias);
		if (!isBeanDefinitionOverridable(alias) && containsBeanDefinition(alias)) {
			throw new IllegalStateException("Cannot register alias '" + alias +
					"' for name '" + name + "': Alias would override bean definition '" + alias + "'");
		}
	}

	/**
	 * 单例放入父类缓存后：失效相关按类型缓存，并补全 primary 的运行时类型。
	 */
	@Override
	protected void addSingleton(String beanName, Object singletonObject) {
		super.addSingleton(beanName, singletonObject);

		// 该实例可能改变「某类型有哪些 Bean」的缓存结论
		Predicate<Class<?>> filter = (beanType -> beanType != Object.class && beanType.isInstance(singletonObject));
		this.allBeanNamesByType.keySet().removeIf(filter);
		this.singletonBeanNamesByType.keySet().removeIf(filter);

		if (this.primaryBeanNamesWithType.containsKey(beanName) && singletonObject.getClass() != NullBean.class) {
			Class<?> beanType = (singletonObject instanceof FactoryBean<?> fb ?
					getTypeForFactoryBean(fb) : singletonObject.getClass());
			if (beanType != null) {
				this.primaryBeanNamesWithType.put(beanName, beanType);
			}
		}
	}

	/**
	 * 手动注册单例：交给 {@link DefaultSingletonBeanRegistry}，并记入 manualSingletonNames。
	 */
	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);

		updateManualSingletonNames(set -> set.add(beanName), set -> !this.beanDefinitionMap.containsKey(beanName));
		this.allBeanNamesByType.remove(Object.class);
		this.singletonBeanNamesByType.remove(Object.class);
	}

	@Override
	public void destroySingletons() {
		super.destroySingletons();
		updateManualSingletonNames(Set::clear, set -> !set.isEmpty());
		clearByTypeCache();
	}

	@Override
	public void destroySingleton(String beanName) {
		super.destroySingleton(beanName);
		removeManualSingletonName(beanName);
		clearByTypeCache();
	}

	private void removeManualSingletonName(String beanName) {
		updateManualSingletonNames(set -> set.remove(beanName), set -> set.contains(beanName));
	}

	/**
	 * 更新手动单例名集合；创建已开始后用拷贝替换，保证迭代稳定。
	 * @param action 修改动作
	 * @param condition 前置条件（不满足则跳过）
	 */
	private void updateManualSingletonNames(Consumer<Set<String>> action, Predicate<Set<String>> condition) {
		if (hasBeanCreationStarted()) {
			// 创建已开始：不能直接改启动期集合元素
			synchronized (this.beanDefinitionMap) {
				if (condition.test(this.manualSingletonNames)) {
					Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
					action.accept(updatedSingletons);
					this.manualSingletonNames = updatedSingletons;
				}
			}
		}
		else {
			// 仍在启动注册阶段
			if (condition.test(this.manualSingletonNames)) {
				action.accept(this.manualSingletonNames);
			}
		}
	}

	/**
	 * 清空按类型查找缓存（注册/销毁单例后类型映射可能失效）。
	 */
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}


	//---------------------------------------------------------------------
	// 依赖解析核心（resolveDependency / doResolveDependency / 多候选决策）
	//---------------------------------------------------------------------

	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		NamedBeanHolder<T> namedBean = resolveNamedBean(ResolvableType.forRawClass(requiredType), null, false);
		if (namedBean != null) {
			return namedBean;
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof AutowireCapableBeanFactory acbf) {
			return acbf.resolveNamedBean(requiredType);
		}
		throw new NoSuchBeanDefinitionException(requiredType);
	}

	/**
	 * 按类型解析出「唯一」命名 Bean：过滤非候选 → Primary → Priority → default-candidate。
	 * @param nonUniqueAsNull 无法决出唯一时返回 {@code null} 而非抛 {@link NoUniqueBeanDefinitionException}
	 */
	@SuppressWarnings("unchecked")
	private <T> @Nullable NamedBeanHolder<T> resolveNamedBean(
			ResolvableType requiredType, @Nullable Object @Nullable [] args, boolean nonUniqueAsNull) throws BeansException {

		Assert.notNull(requiredType, "Required type must not be null");
		String[] candidateNames = getBeanNamesForType(requiredType);

		// 多个候选时优先收窄到 autowireCandidate=true 的
		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = StringUtils.toStringArray(autowireCandidates);
			}
		}

		if (candidateNames.length == 1) {
			return resolveNamedBean(candidateNames[0], requiredType, args);
		}
		else if (candidateNames.length > 1) {
			// 已创建的单例放入实例，否则只放 Class，避免过早实例化影响 Primary 选择
			Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.length);
			for (String beanName : candidateNames) {
				if (containsSingleton(beanName) && args == null) {
					Object beanInstance = resolveBean(beanName, requiredType);
					candidates.put(beanName, (beanInstance instanceof NullBean ? null : beanInstance));
				}
				else {
					candidates.put(beanName, getType(beanName));
				}
			}
			// 决策顺序：@Primary（及非 fallback）→ @Priority → 唯一 default-candidate
			String candidateName = determinePrimaryCandidate(candidates, requiredType.toClass());
			if (candidateName == null) {
				candidateName = determineHighestPriorityCandidate(candidates, requiredType.toClass());
			}
			if (candidateName == null) {
				candidateName = determineDefaultCandidate(candidates);
			}
			if (candidateName != null) {
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance == null) {
					return null;
				}
				if (beanInstance instanceof Class) {
					return resolveNamedBean(candidateName, requiredType, args);
				}
				return new NamedBeanHolder<>(candidateName, (T) beanInstance);
			}
			if (!nonUniqueAsNull) {
				throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
			}
		}

		return null;
	}

	private <T> @Nullable NamedBeanHolder<T> resolveNamedBean(
			String beanName, ResolvableType requiredType, @Nullable Object @Nullable [] args) throws BeansException {

		Object bean = (args != null ? getBean(beanName, args) : resolveBean(beanName, requiredType));
		if (bean instanceof NullBean) {
			return null;
		}
		return new NamedBeanHolder<>(beanName, adaptBeanInstance(beanName, bean, requiredType.toClass()));
	}

	/**
	 * 解析依赖入口：先处理 Optional / ObjectProvider / JSR-330 Provider / {@code @Lazy} 代理，
	 * 其余交给 {@link #doResolveDependency}。
	 */
	@Override
	public @Nullable Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		if (Optional.class == descriptor.getDependencyType()) {
			return createOptionalDependency(descriptor, requestingBeanName, autowiredBeanNames, null);
		}
		else if (ObjectFactory.class == descriptor.getDependencyType() ||
				ObjectProvider.class == descriptor.getDependencyType()) {
			// 延迟查找：注入的是 provider，真正 getObject 时再解析
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		}
		else if (jakartaInjectProviderClass == descriptor.getDependencyType()) {
			return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
		}
		else if (descriptor.supportsLazyResolution()) {
			// @Lazy：必要时生成延迟解析代理
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
					descriptor, requestingBeanName);
			if (result != null) {
				return result;
			}
		}
		return doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
	}

	/**
	 * 依赖解析主流程（注入点真正取值时走这里）：
	 * shortcut → {@code @Value} → 按名捷径 → 多元素集合 → 找候选 → Primary/名称/Priority 决胜 → 校验。
	 */
	@SuppressWarnings("NullAway")  // Dataflow analysis limitation
	public @Nullable Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			// 步骤1：已解析的快捷路径（例如 @Autowired 字段在注入时已记住唯一 Bean 名）
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}

			Class<?> type = descriptor.getDependencyType();

			// 步骤2：@Value 等建议值/表达式 → 占位符解析 → SpEL → 类型转换
			Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				if (value instanceof String strValue) {
					String resolvedValue = resolveEmbeddedValue(strValue);
					BeanDefinition bd = (beanName != null && containsBean(beanName) ?
							getMergedBeanDefinition(beanName) : null);
					value = evaluateBeanDefinitionString(resolvedValue, bd);
				}
				TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
				try {
					return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
				}
				catch (UnsupportedOperationException ex) {
					// 自定义 TypeConverter 可能不支持 TypeDescriptor，退回 Field/MethodParameter
					return (descriptor.getField() != null ?
							converter.convertIfNecessary(value, type, descriptor.getField()) :
							converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
				}
			}

			// 步骤3：参数名 / 限定符建议名与容器中 Bean 名一致时走捷径（仍要过类型、候选、非 fallback、无 primary 冲突、非自引用）
			if (descriptor.usesStandardBeanLookup()) {
				String dependencyName = descriptor.getDependencyName();
				if (dependencyName == null || !containsBean(dependencyName)) {
					String suggestedName = getAutowireCandidateResolver().getSuggestedName(descriptor);
					dependencyName = (suggestedName != null && containsBean(suggestedName) ? suggestedName : null);
				}
				if (dependencyName != null) {
					dependencyName = canonicalName(dependencyName);  // 依赖名可能是别名，归一到规范名
					if (isTypeMatch(dependencyName, type) && isAutowireCandidate(dependencyName, descriptor) &&
							!isFallback(dependencyName) && !hasPrimaryConflict(dependencyName, type) &&
							!isSelfReference(beanName, dependencyName)) {
						if (autowiredBeanNames != null) {
							autowiredBeanNames.add(dependencyName);
						}
						Object dependencyBean = resolveBean(dependencyName, descriptor.getResolvableType());
						return resolveInstance(dependencyBean, descriptor, type, dependencyName);
					}
				}
			}

			// 步骤4a：Stream / 数组 / List|Set|Collection / Map 等多元素注入
			Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
			if (multipleBeans != null) {
				return multipleBeans;
			}
			// 步骤4b：按类型找单个依赖的候选（也可能直接匹配到 Collection/Map 类型的 Bean）
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				// 步骤4c：回退——把自定义 Collection/Map 接口声明当成「收集多个 Bean」
				multipleBeans = resolveMultipleBeansFallback(descriptor, beanName, autowiredBeanNames, typeConverter);
				if (multipleBeans != null) {
					return multipleBeans;
				}
				// 必选注入点找不到任何候选 → 抛错
				if (isRequired(descriptor)) {
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				return null;
			}

			String autowiredBeanName;
			Object instanceCandidate;

			// 步骤5：多候选时决出唯一注入目标
			if (matchingBeans.size() > 1) {
				autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (autowiredBeanName == null) {
					if (isRequired(descriptor) || !indicatesArrayCollectionOrMap(type)) {
						// 必选或非集合类型：无法唯一则交给 descriptor 抛 NoUnique...
						return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
					}
					else {
						// 可选的 Collection/Map：多候选且无法决胜时当作「不是要注入集合 Bean」而返回 null
						return null;
					}
				}
				instanceCandidate = matchingBeans.get(autowiredBeanName);
			}
			else {
				// 恰好一个候选
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				autowiredBeanName = entry.getKey();
				instanceCandidate = entry.getValue();
			}

			// 步骤6：记录依赖名；若 map 里只存了 Class 则此时再真正 getBean；校验 NullBean/类型
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(autowiredBeanName);
			}
			if (instanceCandidate instanceof Class) {
				instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
			}
			return resolveInstance(instanceCandidate, descriptor, type, autowiredBeanName);
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	/** 校验候选实例：NullBean 对必选注入点要报错；类型不兼容抛 {@link BeanNotOfRequiredTypeException}。 */
	private @Nullable Object resolveInstance(Object candidate, DependencyDescriptor descriptor, Class<?> type, String name) {
		Object result = candidate;
		if (result instanceof NullBean) {
			// 必选注入点遇到 NullBean → 视为找不到
			if (isRequired(descriptor)) {
				raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
			}
			result = null;
		}
		if (!ClassUtils.isAssignableValue(type, result)) {
			throw new BeanNotOfRequiredTypeException(name, type, candidate.getClass());
		}
		return result;
	}

	private @Nullable Object resolveMultipleBeans(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		Class<?> type = descriptor.getDependencyType();

		if (descriptor instanceof StreamDependencyDescriptor streamDependencyDescriptor) {
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			Stream<Object> stream = matchingBeans.keySet().stream()
					.map(name -> descriptor.resolveCandidate(name, type, this))
					.filter(bean -> !(bean instanceof NullBean));
			if (streamDependencyDescriptor.isOrdered()) {
				stream = stream.sorted(adaptOrderComparator(matchingBeans));
			}
			return stream;
		}
		else if (type.isArray()) {
			Class<?> componentType = type.componentType();
			ResolvableType resolvableType = descriptor.getResolvableType();
			Class<?> resolvedArrayType = resolvableType.resolve(type);
			if (resolvedArrayType != type) {
				componentType = resolvableType.getComponentType().resolve();
			}
			if (componentType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			Object result = converter.convertIfNecessary(matchingBeans.values(), resolvedArrayType);
			if (result instanceof Object[] array && array.length > 1) {
				Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
				if (comparator != null) {
					Arrays.sort(array, comparator);
				}
			}
			return result;
		}
		else if (Collection.class == type || Set.class == type || List.class == type) {
			return resolveMultipleBeanCollection(descriptor, beanName, autowiredBeanNames, typeConverter);
		}
		else if (Map.class == type) {
			return resolveMultipleBeanMap(descriptor, beanName, autowiredBeanNames, typeConverter);
		}
		return null;
	}


	private @Nullable Object resolveMultipleBeansFallback(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		Class<?> type = descriptor.getDependencyType();

		if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			return resolveMultipleBeanCollection(descriptor, beanName, autowiredBeanNames, typeConverter);
		}
		else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			return resolveMultipleBeanMap(descriptor, beanName, autowiredBeanNames, typeConverter);
		}
		return null;
	}

	private @Nullable Object resolveMultipleBeanCollection(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
		if (elementType == null) {
			return null;
		}
		Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType,
				new MultiElementDescriptor(descriptor));
		if (matchingBeans.isEmpty()) {
			return null;
		}
		if (autowiredBeanNames != null) {
			autowiredBeanNames.addAll(matchingBeans.keySet());
		}
		TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
		Object result = converter.convertIfNecessary(matchingBeans.values(), descriptor.getDependencyType());
		if (result instanceof List<?> list && list.size() > 1) {
			Comparator<Object> comparator = adaptDependencyComparator(matchingBeans);
			if (comparator != null) {
				list.sort(comparator);
			}
		}
		return result;
	}

	private @Nullable Object resolveMultipleBeanMap(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) {

		ResolvableType mapType = descriptor.getResolvableType().asMap();
		Class<?> keyType = mapType.resolveGeneric(0);
		if (String.class != keyType) {
			return null;
		}
		Class<?> valueType = mapType.resolveGeneric(1);
		if (valueType == null) {
			return null;
		}
		Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType,
				new MultiElementDescriptor(descriptor));
		if (matchingBeans.isEmpty()) {
			return null;
		}
		if (autowiredBeanNames != null) {
			autowiredBeanNames.addAll(matchingBeans.keySet());
		}
		TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
		return converter.convertIfNecessary(matchingBeans, descriptor.getDependencyType());
	}

	private boolean indicatesArrayCollectionOrMap(Class<?> type) {
		return (type.isArray() || (type.isInterface() &&
				(Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))));
	}

	private boolean isRequired(DependencyDescriptor descriptor) {
		return getAutowireCandidateResolver().isRequired(descriptor);
	}

	private @Nullable Comparator<Object> adaptDependencyComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator orderComparator) {
			return orderComparator.withSourceProvider(
					createFactoryAwareOrderSourceProvider(matchingBeans));
		}
		else {
			return comparator;
		}
	}

	private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
		Comparator<Object> dependencyComparator = getDependencyComparator();
		OrderComparator comparator = (dependencyComparator instanceof OrderComparator orderComparator ?
				orderComparator : OrderComparator.INSTANCE);
		return comparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
	}

	private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
		beans.forEach((beanName, instance) -> instancesToBeanNames.put(instance, beanName));
		return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
	}

	/**
	 * 找出匹配所需类型的自动注入候选。
	 * 顺序：resolvableDependencies → 常规候选 → fallback 匹配 → 最后才考虑自引用。
	 * @param beanName 即将被注入的 Bean 名（用于排除自引用）
	 * @param requiredType 要查找的实际类型（可能是数组组件或集合元素类型）
	 * @param descriptor 依赖描述符
	 * @return 候选名 → 实例或 Class 的 Map（永不为 {@code null}）
	 * @throws BeansException 出错时
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected Map<String, Object> findAutowireCandidates(
			@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

		// 含祖先工厂在内的按类型 Bean 名
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager());
		Map<String, Object> result = CollectionUtils.newLinkedHashMap(candidateNames.length);
		// 先塞 resolvableDependencies（BeanFactory、ApplicationContext 等）
		for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
			Class<?> autowiringType = classObjectEntry.getKey();
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = classObjectEntry.getValue();
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		// 第一轮：非自引用 + 通过 AutowireCandidateResolver
		for (String candidate : candidateNames) {
			if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
				addCandidateEntry(result, candidate, descriptor, requiredType);
			}
		}
		if (result.isEmpty()) {
			boolean multiple = indicatesArrayCollectionOrMap(requiredType);
			// 第二轮：放宽限定符等条件的 fallback 匹配
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for (String candidate : candidateNames) {
				if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor) &&
						(!multiple || matchesBeanName(candidate, descriptor.getDependencyName()) ||
								getAutowireCandidateResolver().hasQualifier(descriptor))) {
					addCandidateEntry(result, candidate, descriptor, requiredType);
				}
			}
			if (result.isEmpty() && !multiple) {
				// 第三轮：才考虑自引用（多元素注入时仍排除「自己注入自己」）
				for (String candidate : candidateNames) {
					if (isSelfReference(beanName, candidate) &&
							(!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) &&
							isAutowireCandidate(candidate, fallbackDescriptor)) {
						addCandidateEntry(result, candidate, descriptor, requiredType);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 写入候选表：多元素/已有单例/有序 Stream 放实例；否则只放 Class，避免在 Primary 决胜前过早创建。
	 */
	private void addCandidateEntry(Map<String, Object> candidates, String candidateName,
			DependencyDescriptor descriptor, Class<?> requiredType) {

		if (descriptor instanceof MultiElementDescriptor) {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			if (!(beanInstance instanceof NullBean)) {
				candidates.put(candidateName, beanInstance);
			}
		}
		else if (containsSingleton(candidateName) ||
				(descriptor instanceof StreamDependencyDescriptor streamDescriptor && streamDescriptor.isOrdered())) {
			Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
			candidates.put(candidateName, beanInstance);
		}
		else {
			candidates.put(candidateName, getType(candidateName));
		}
	}

	/**
	 * 在多个候选中决出唯一自动注入目标。
	 * <p>顺序：{@code @Primary}/非 fallback → 参数名 → 限定符建议名 → {@code @Priority} →
	 * 唯一 default-candidate → resolvableDependencies 中的直接注册值。
	 * @param candidates {@link #findAutowireCandidates} 返回的候选 Map
	 * @param descriptor 目标依赖
	 * @return 选中的 Bean 名，无法决胜则 {@code null}
	 */
	protected @Nullable String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		Class<?> requiredType = descriptor.getDependencyType();
		// 1) @Primary（内部还会在无 primary 时尝试「唯一非 fallback」）
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			return primaryCandidate;
		}
		// 2a) Bean 名 / 别名 == 依赖参数名
		String dependencyName = descriptor.getDependencyName();
		if (dependencyName != null) {
			for (String beanName : candidates.keySet()) {
				if (matchesBeanName(beanName, dependencyName)) {
					return beanName;
				}
			}
		}
		// 2b) Bean 名 / 别名 == 限定符建议名（如 @Qualifier）
		String suggestedName = getAutowireCandidateResolver().getSuggestedName(descriptor);
		if (suggestedName != null) {
			for (String beanName : candidates.keySet()) {
				if (matchesBeanName(beanName, suggestedName)) {
					return beanName;
				}
			}
		}
		// 3) @Priority 数值最小者胜出
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			return priorityCandidate;
		}
		// 4) 唯一的 default-candidate
		String defaultCandidate = determineDefaultCandidate(candidates);
		if (defaultCandidate != null) {
			return defaultCandidate;
		}
		// 5) 命中 resolvableDependencies 里直接注册的值
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) {
				return candidateName;
			}
		}
		return null;
	}

	/**
	 * 在候选集中找出 primary；本地 primary 优先于父工厂。
	 * 若没有 primary，则尝试「唯一的非 fallback」候选。
	 * @param candidates 候选名 → 实例或 Class
	 * @param requiredType 目标依赖类型
	 * @return primary（或唯一非 fallback）候选名；找不到则 {@code null}
	 * @see #isPrimary(String, Object)
	 */
	protected @Nullable String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		String primaryBeanName = null;
		// 第一遍：找出唯一 @Primary；父子工厂各有一个时本地胜出
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					if (candidateLocal == primaryLocal) {
						String message = "more than one 'primary' bean found among candidates: " + candidates.keySet();
						logger.trace(message);
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(), message);
					}
					else if (candidateLocal) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					primaryBeanName = candidateBeanName;
				}
			}
		}
		// 第二遍：没有 primary 时，若只剩一个非 fallback，也当作「准 primary」
		if (primaryBeanName == null) {
			for (String candidateBeanName : candidates.keySet()) {
				if (!isFallback(candidateBeanName)) {
					if (primaryBeanName != null) {
						return null;
					}
					primaryBeanName = candidateBeanName;
				}
			}
		}
		return primaryBeanName;
	}

	/**
	 * 按 {@code @jakarta.annotation.Priority} 选出优先级最高（数值最小）的候选。
	 * @param candidates 候选名 → 实例或 Class
	 * @param requiredType 目标依赖类型
	 * @return 最高优先级候选名；没有带 Priority 的则 {@code null}
	 * @throws NoUniqueBeanDefinitionException 多个候选并列最高优先级时
	 * @see #getPriority(Object)
	 */
	protected @Nullable String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		String highestPriorityBeanName = null;
		Integer highestPriority = null;
		boolean highestPriorityConflictDetected = false;
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (beanInstance != null) {
				Integer candidatePriority = getPriority(beanInstance);
				if (candidatePriority != null) {
					if (highestPriority != null) {
						if (candidatePriority.equals(highestPriority)) {
							highestPriorityConflictDetected = true;
						}
						else if (candidatePriority < highestPriority) {
							highestPriorityBeanName = candidateBeanName;
							highestPriority = candidatePriority;
							highestPriorityConflictDetected = false;
						}
					}
					else {
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				}
			}
		}

		if (highestPriorityConflictDetected) {
			throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
					"Multiple beans found with the same highest priority (" + highestPriority +
					") among candidates: " + candidates.keySet());

		}
		return highestPriorityBeanName;
	}

	/**
	 * 给定 Bean 是否标记为 primary。
	 * @param beanName Bean 名
	 * @param beanInstance 对应实例（可为 {@code null}）
	 * @return 是否为 primary
	 */
	protected boolean isPrimary(String beanName, Object beanInstance) {
		String transformedBeanName = transformedBeanName(beanName);
		if (containsBeanDefinition(transformedBeanName)) {
			return getMergedLocalBeanDefinition(transformedBeanName).isPrimary();
		}
		return (getParentBeanFactory() instanceof DefaultListableBeanFactory parent &&
				parent.isPrimary(transformedBeanName, beanInstance));
	}

	/**
	 * 给定 Bean 是否标记为 fallback。
	 * @param beanName Bean 名
	 * @since 6.2
	 */
	private boolean isFallback(String beanName) {
		String transformedBeanName = transformedBeanName(beanName);
		if (containsBeanDefinition(transformedBeanName)) {
			return getMergedLocalBeanDefinition(transformedBeanName).isFallback();
		}
		return (getParentBeanFactory() instanceof DefaultListableBeanFactory parent &&
				parent.isFallback(transformedBeanName));
	}

	/**
	 * 读取实例上 {@code jakarta.annotation.Priority} 的优先级。
	 * <p>默认委托给 {@link #setDependencyComparator dependency comparator}；
	 * 若是 {@link OrderComparator} 子类（通常为
	 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}）则调其
	 * {@link OrderComparator#getPriority}；否则返回 {@code null}。
	 * @param beanInstance 待检查实例（可为 {@code null}）
	 * @return 优先级数值，未设置则为 {@code null}
	 */
	protected @Nullable Integer getPriority(Object beanInstance) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator orderComparator) {
			return orderComparator.getPriority(beanInstance);
		}
		return null;
	}

	/**
	 * 在候选里找出唯一的 default-candidate（{@link AbstractBeanDefinition#isDefaultCandidate()}）。
	 * @param candidates 候选名 → 实例或 Class
	 * @return 唯一 default 候选名；没有或不止一个则 {@code null}
	 * @since 6.2.4
	 * @see AbstractBeanDefinition#isDefaultCandidate()
	 */
	@Nullable
	private String determineDefaultCandidate(Map<String, Object> candidates) {
		String defaultBeanName = null;
		for (String candidateBeanName : candidates.keySet()) {
			if (AutowireUtils.isDefaultCandidate(this, candidateBeanName)) {
				if (defaultBeanName != null) {
					return null;
				}
				defaultBeanName = candidateBeanName;
			}
		}
		return defaultBeanName;
	}

	/**
	 * 依赖名是否等于 Bean 名或其任一别名。
	 */
	protected boolean matchesBeanName(String beanName, @Nullable String dependencyName) {
		return (dependencyName != null &&
				(dependencyName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), dependencyName)));
	}

	/**
	 * 是否自引用：候选就是正在创建的 Bean，或候选的 factoryBeanName 指向它。
	 */
	@Contract("null, _ -> false; _, null -> false;")
	private boolean isSelfReference(@Nullable String beanName, @Nullable String candidateName) {
		return (beanName != null && candidateName != null &&
				(beanName.equals(candidateName) || (containsBeanDefinition(candidateName) &&
						beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
	}

	/**
	 * 是否存在「其他」primary Bean 与当前依赖类型冲突（用于按名捷径时否决）。
	 */
	private boolean hasPrimaryConflict(String beanName, Class<?> dependencyType) {
		for (Map.Entry<String, Class<?>> candidate : this.primaryBeanNamesWithType.entrySet()) {
			String candidateName = candidate.getKey();
			Class<?> candidateType = candidate.getValue();
			if (!candidateName.equals(beanName) && (candidateType != Void.class ?
					dependencyType.isAssignableFrom(candidateType) :  // primary 单例类型已缓存
					isTypeMatch(candidateName, dependencyType))) {  // 尚未实例化或非单例
				return true;
			}
		}
		return (getParentBeanFactory() instanceof DefaultListableBeanFactory parent &&
				parent.hasPrimaryConflict(beanName, dependencyType));
	}

	/**
	 * 找不到匹配候选时抛 {@link NoSuchBeanDefinitionException}；
	 * 若实际是代理类型不符则先抛 {@link BeanNotOfRequiredTypeException}。
	 */
	private void raiseNoMatchingBeanFound(
			Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor) throws BeansException {

		checkBeanNotOfRequiredType(type, descriptor);

		throw new NoSuchBeanDefinitionException(resolvableType,
				"expected at least 1 bean which qualifies as autowire candidate. " +
				"Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	/**
	 * 目标类型本可匹配，但暴露出来的代理类型不匹配时，抛更明确的
	 * {@link BeanNotOfRequiredTypeException}。
	 */
	private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
		for (String beanName : this.beanDefinitionNames) {
			try {
				RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				Class<?> targetType = mbd.getTargetType();
				if (targetType != null && type.isAssignableFrom(targetType) &&
						isAutowireCandidate(beanName, mbd, descriptor, getAutowireCandidateResolver())) {
					// 多半是代理挡住了目标类型匹配 → 抛出有意义的异常
					Object beanInstance = getSingleton(beanName, false);
					Class<?> beanType = (beanInstance != null && beanInstance.getClass() != NullBean.class ?
							beanInstance.getClass() : predictBeanType(beanName, mbd));
					if (beanType != null && !type.isAssignableFrom(beanType)) {
						throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// 迭代中定义被移除，忽略
			}
		}

		if (getParentBeanFactory() instanceof DefaultListableBeanFactory parent) {
			parent.checkBeanNotOfRequiredType(type, descriptor);
		}
	}

	/**
	 * 为指定依赖创建 {@link Optional} 包装（内部把 required 视为 false）。
	 */
	private Optional<?> createOptionalDependency(DependencyDescriptor descriptor, @Nullable String beanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable Object @Nullable [] args) {

		DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {
			@Override
			public boolean isRequired() {
				return false;
			}
			@Override
			public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
				return (!ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, args) :
						super.resolveCandidate(beanName, requiredType, beanFactory));
			}
			@Override
			public boolean usesStandardBeanLookup() {
				return ObjectUtils.isEmpty(args);
			}
		};
		Object result = doResolveDependency(descriptorToUse, beanName, autowiredBeanNames, null);
		return (result instanceof Optional<?> optional ? optional : Optional.ofNullable(result));
	}

	/**
	 * 公开 API：计算给定 Bean 的 order 值（会先从本工厂取出实例）。
	 * @param beanName Bean 名
	 * @return order 值（默认 {@link Ordered#LOWEST_PRECEDENCE}）
	 * @since 7.0
	 * @see #getOrder(String, Object)
	 */
	public int getOrder(String beanName) {
		return getOrder(beanName, getBean(beanName));
	}

	/**
	 * 公开 API：根据已有实例计算 Bean 的 order 值。
	 * @param beanName Bean 名
	 * @param beanInstance 待检查的实例
	 * @return order 值（默认 {@link Ordered#LOWEST_PRECEDENCE}）
	 * @since 7.0
	 * @see #getOrder(String)
	 */
	public int getOrder(String beanName, Object beanInstance) {
		OrderComparator comparator = (getDependencyComparator() instanceof OrderComparator orderComparator ?
				orderComparator : OrderComparator.INSTANCE);
		return comparator.getOrder(beanInstance,
				new FactoryAwareOrderSourceProvider(Collections.singletonMap(beanInstance, beanName)));
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		}
		else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	@Serial
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - " +
				"just a SerializedBeanFactoryReference is");
	}

	@Serial
	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		}
		else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}


	/**
	 * 仅持有工厂 id 的轻量引用；反序列化时再解析回真实工厂实例。
	 */
	private static class SerializedBeanFactoryReference implements Serializable {

		/** 工厂的 serializationId。 */
		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get(this.id);
			if (ref != null) {
				Object result = ref.get();
				if (result != null) {
					return result;
				}
			}
			// 宽松回退：原工厂找不到时返回带同一 id 的空壳工厂
			DefaultListableBeanFactory dummyFactory = new DefaultListableBeanFactory();
			dummyFactory.serializationId = this.id;
			return dummyFactory;
		}
	}


	/**
	 * 嵌套依赖描述符标记（如 Optional 解包后的内层依赖）。
	 */
	private static class NestedDependencyDescriptor extends DependencyDescriptor {

		public NestedDependencyDescriptor(DependencyDescriptor original) {
			super(original);
			increaseNestingLevel();
		}

		@Override
		public boolean usesStandardBeanLookup() {
			return true;
		}
	}


	/**
	 * 多元素声明（数组/集合元素）用的嵌套依赖描述符。
	 */
	private static class MultiElementDescriptor extends NestedDependencyDescriptor {

		public MultiElementDescriptor(DependencyDescriptor original) {
			super(original);
		}
	}


	/**
	 * Stream 访问多元素时使用的依赖描述符，可标记是否需要排序。
	 */
	private static class StreamDependencyDescriptor extends DependencyDescriptor {

		/** 是否按 Order 排序后再组成 Stream。 */
		private final boolean ordered;

		public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
			super(original);
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}


	private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
	}


	/**
	 * 可序列化的 ObjectFactory/ObjectProvider，用于延迟解析某个依赖。
	 */
	private class DependencyObjectProvider implements BeanObjectProvider<Object> {

		/** 缓存哨兵：解析结果不可缓存。 */
		private static final Object NOT_CACHEABLE = new Object();

		/** 缓存哨兵：解析结果为 {@code null}。 */
		private static final Object NULL_VALUE = new Object();

		/** 原始依赖描述（已包成 NestedDependencyDescriptor）。 */
		private final DependencyDescriptor descriptor;

		/** 依赖类型是否为 {@link Optional}。 */
		private final boolean optional;

		/** 请求注入的 Bean 名。 */
		private final @Nullable String beanName;

		/** 配置冻结后可缓存的解析结果。 */
		private transient volatile @Nullable Object cachedValue;

		public DependencyObjectProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			this.descriptor = new NestedDependencyDescriptor(descriptor);
			this.optional = (this.descriptor.getDependencyType() == Optional.class);
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			Object result = getValue();
			if (result == null) {
				throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
			}
			return result;
		}

		@Override
		public Object getObject(final @Nullable Object... args) throws BeansException {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName, null, args);
			}
			else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
					@Override
					public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
						return beanFactory.getBean(beanName, args);
					}
				};
				Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
				if (result == null) {
					throw new NoSuchBeanDefinitionException(this.descriptor.getResolvableType());
				}
				return result;
			}
		}

		@Override
		public @Nullable Object getIfAvailable() throws BeansException {
			try {
				if (this.optional) {
					return createOptionalDependency(this.descriptor, this.beanName, null, null);
				}
				else {
					DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
						@Override
						public boolean isRequired() {
							return false;
						}
						@Override
						public boolean usesStandardBeanLookup() {
							return true;
						}
					};
					return doResolveDependency(descriptorToUse, this.beanName, null, null);
				}
			}
			catch (ScopeNotActiveException ex) {
				// 作用域未激活，当作不可用
				return null;
			}
		}

		@Override
		public void ifAvailable(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = getIfAvailable();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				}
				catch (ScopeNotActiveException ex) {
					// 调用 scoped 代理时作用域也可能未激活，忽略
				}
			}
		}

		@Override
		public @Nullable Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}
				@Override
				public boolean usesStandardBeanLookup() {
					return true;
				}
				@Override
				public @Nullable Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) {
					return null;
				}
			};
			try {
				if (this.optional) {
					return createOptionalDependency(descriptorToUse, this.beanName, null, null);
				}
				else {
					return doResolveDependency(descriptorToUse, this.beanName, null, null);
				}
			}
			catch (ScopeNotActiveException ex) {
				// 作用域未激活，当作不可用
				return null;
			}
		}

		@Override
		public void ifUnique(Consumer<Object> dependencyConsumer) throws BeansException {
			Object dependency = getIfUnique();
			if (dependency != null) {
				try {
					dependencyConsumer.accept(dependency);
				}
				catch (ScopeNotActiveException ex) {
					// 调用 scoped 代理时作用域也可能未激活，忽略
				}
			}
		}

		/**
		 * 取值并在配置冻结后尝试缓存：仅当依赖的全是仍存在的单例时才缓存。
		 */
		protected @Nullable Object getValue() throws BeansException {
			Object value = this.cachedValue;
			if (value == null) {
				if (isConfigurationFrozen()) {
					Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
					value = resolveValue(autowiredBeanNames);
					boolean cacheable = false;
					if (!autowiredBeanNames.isEmpty()) {
						cacheable = true;
						for (String autowiredBeanName : autowiredBeanNames) {
							if (!containsBean(autowiredBeanName) || !isSingleton(autowiredBeanName)) {
								cacheable = false;
							}
						}
					}
					this.cachedValue = (cacheable ? (value != null ? value : NULL_VALUE) : NOT_CACHEABLE);
					return value;
				}
			}
			else if (value == NULL_VALUE) {
				return null;
			}
			else if (value != NOT_CACHEABLE) {
				return value;
			}

			// 不可缓存 → 每次重新解析
			return resolveValue(null);
		}

		private @Nullable Object resolveValue(@Nullable Set<String> autowiredBeanNames) {
			if (this.optional) {
				return createOptionalDependency(this.descriptor, this.beanName, autowiredBeanNames, null);
			}
			else {
				return doResolveDependency(this.descriptor, this.beanName, autowiredBeanNames, null);
			}
		}

		@Override
		public Stream<Object> stream() {
			return resolveStream(false);
		}

		@Override
		public Stream<Object> orderedStream() {
			return resolveStream(true);
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private Stream<Object> resolveStream(boolean ordered) {
			DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
			Object result = doResolveDependency(descriptorToUse, this.beanName, null, null);
			return (result instanceof Stream stream ? stream : Stream.of(result));
		}

		@Override
		public Stream<Object> stream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
			ResolvableType type = this.descriptor.getResolvableType();
			return Arrays.stream(beanNamesForStream(type, includeNonSingletons, true))
					.filter(name -> AutowireUtils.isAutowireCandidate(DefaultListableBeanFactory.this, name))
					.filter(name -> customFilter.test(getType(name)))
					.map(name -> resolveBean(name, type))
					.filter(bean -> !(bean instanceof NullBean));
		}

		@Override
		public Stream<Object> orderedStream(Predicate<Class<?>> customFilter, boolean includeNonSingletons) {
			ResolvableType type = this.descriptor.getResolvableType();
			String[] beanNames = beanNamesForStream(type, includeNonSingletons, true);
			if (beanNames.length == 0) {
				return Stream.empty();
			}
			Map<String, Object> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.length);
			for (String beanName : beanNames) {
				if (AutowireUtils.isAutowireCandidate(DefaultListableBeanFactory.this, beanName) &&
						customFilter.test(getType(beanName))) {
					Object beanInstance = resolveBean(beanName, type);
					if (!(beanInstance instanceof NullBean)) {
						matchingBeans.put(beanName, beanInstance);
					}
				}
			}
			return matchingBeans.values().stream().sorted(adaptOrderComparator(matchingBeans));
		}
	}


	/**
	 * 独立内部类，避免对 {@code jakarta.inject} API 产生硬依赖。
	 * 真正的 {@code jakarta.inject.Provider} 实现再嵌一层，使 Graal 内省
	 * DefaultListableBeanFactory 的嵌套类时看不到它。
	 */
	private class Jsr330Factory implements Serializable {

		public Object createDependencyProvider(DependencyDescriptor descriptor, @Nullable String beanName) {
			return new Jsr330Provider(descriptor, beanName);
		}

		/** 把 {@link DependencyObjectProvider} 适配成 JSR-330 {@link Provider}。 */
		private class Jsr330Provider extends DependencyObjectProvider implements Provider<Object> {

			public Jsr330Provider(DependencyDescriptor descriptor, @Nullable String beanName) {
				super(descriptor, beanName);
			}

			@Override
			public @Nullable Object get() throws BeansException {
				return getValue();
			}
		}
	}


	/**
	 * 感知 Bean 元数据的 {@link org.springframework.core.OrderComparator.OrderSourceProvider}。
	 * <p>排序时会查找实例对应的工厂方法，让比较器读取其上的
	 * {@link org.springframework.core.annotation.Order}；自 6.1.2 起还考虑
	 * {@link AbstractBeanDefinition#ORDER_ATTRIBUTE}。
	 */
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {

		/** 实例 → Bean 名，用于反查定义上的 order 元数据。 */
		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		public @Nullable Object getOrderSource(Object obj) {
			String beanName = this.instancesToBeanNames.get(obj);
			if (beanName == null) {
				return null;
			}
			try {
				BeanDefinition beanDefinition = getMergedBeanDefinition(beanName);
				List<Object> sources = new ArrayList<>(3);
				// 1) BeanDefinition 上的 ORDER_ATTRIBUTE
				Object orderAttribute = beanDefinition.getAttribute(AbstractBeanDefinition.ORDER_ATTRIBUTE);
				if (orderAttribute != null) {
					if (orderAttribute instanceof Integer order) {
						sources.add((Ordered) () -> order);
					}
					else {
						throw new IllegalStateException("Invalid value type for attribute '" +
								AbstractBeanDefinition.ORDER_ATTRIBUTE + "': " + orderAttribute.getClass().getName());
					}
				}
				if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
					// 2) @Bean 工厂方法上的 @Order
					Method factoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
					if (factoryMethod != null) {
						sources.add(factoryMethod);
					}
					// 3) 目标类型（代理场景下可能与运行时 class 不同）
					Class<?> targetType = rootBeanDefinition.getTargetType();
					if (targetType != null && targetType != obj.getClass()) {
						sources.add(targetType);
					}
				}
				return sources.toArray();
			}
			catch (NoSuchBeanDefinitionException ex) {
				return null;
			}
		}
	}


	/** 预实例化阶段线程角色：主引导线程 / 后台引导线程。 */
	private enum PreInstantiation {

		MAIN, BACKGROUND
	}

}
