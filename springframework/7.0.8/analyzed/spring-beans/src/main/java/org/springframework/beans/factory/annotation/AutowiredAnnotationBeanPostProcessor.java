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

package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.AccessControl;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.support.ClassHintUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.aot.AutowiredArgumentsCodeGenerator;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.aot.AutowiredMethodArgumentsResolver;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.aot.CodeWarnings;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
 * 实现：对标注了注入注解的字段、setter 以及任意配置方法进行自动装配。
 * 默认识别 Spring 的 {@link Autowired @Autowired} 与 {@link Value @Value}。
 *
 * <p>若类路径上存在 Jakarta Inject，也支持通用的
 * {@link jakarta.inject.Inject @Inject}，可作为 {@code @Autowired} 的直接替代。
 *
 * <h3>自动装配构造器</h3>
 * <p>同一 Bean 类中，最多只能有一个构造器将注解的 {@code required} 设为 {@code true}，
 * 表示这就是创建 Spring Bean 时要自动装配的构造器。若存在多个带注解且
 * {@code required=false} 的构造器，它们都会作为候选；容器会选择「能被匹配到的依赖最多」
 * 的那个。若所有候选都无法满足，则回退到主构造器/默认无参构造器（若存在）。
 * 若类本身只有一个构造器，即使未标注也会始终使用。带注解的构造器不必是 public。
 *
 * <h3>自动装配字段</h3>
 * <p>字段在 Bean 构造完成之后、任何配置方法调用之前注入。配置字段不必是 public。
 *
 * <h3>自动装配方法</h3>
 * <p>配置方法可以任意命名、参数个数不限；每个参数都会按类型从容器中匹配 Bean。
 * 属性 setter 只是这类通用配置方法的特例。配置方法也不必是 public。
 *
 * <h3>注解配置 vs XML 配置</h3>
 * <p>使用 XML 的 {@code context:annotation-config} 与 {@code context:component-scan}
 * 时，会默认注册一个 {@code AutowiredAnnotationBeanPostProcessor}。
 * 若要自定义该后置处理器的 Bean 定义，需移除或关闭上述默认注解配置。
 *
 * <p><b>注意：</b>注解注入发生在 XML 注入<strong>之前</strong>；
 * 因此对同一属性两种方式都配置时，XML 会覆盖注解注入的结果。
 *
 * <h3>{@literal @}Lookup 方法</h3>
 * <p>除上述常规注入点外，本后置处理器还处理 Spring 的 {@link Lookup @Lookup}：
 * 标识需在运行时由容器替换的查找方法，本质上是类型安全的
 * {@code getBean(Class, args)} / {@code getBean(String, args)}。
 * 详见 {@link Lookup @Lookup} 的 JavaDoc。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 2.5
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see Value
 */
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, BeanFactoryAware {

	/** 空构造器数组常量，避免重复分配 */
	private static final Constructor<?>[] EMPTY_CONSTRUCTOR_ARRAY = new Constructor<?>[0];


	/** 日志记录器 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 视为「自动装配」标记的注解类型集合（默认含 @Autowired、@Value，以及可选的 @Inject） */
	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = CollectionUtils.newLinkedHashSet(4);

	/** 注解上表示「是否必须」的属性名，默认为 {@code required} */
	private String requiredParameterName = "required";

	/** 与 {@link #requiredParameterName} 对应、表示「依赖必须存在」的布尔值，默认为 {@code true} */
	private boolean requiredParameterValue = true;

	/** 后置处理器排序值（PriorityOrdered，默认略高于最低优先级） */
	private int order = Ordered.LOWEST_PRECEDENCE - 2;

	/** 当前所属的可配置 BeanFactory，用于 resolveDependency / 注册依赖关系 */
	private @Nullable ConfigurableListableBeanFactory beanFactory;

	/** 读取类文件元数据（ASM），用于稳定排序 @Autowired 方法声明顺序 */
	private @Nullable MetadataReaderFactory metadataReaderFactory;

	/** 已检查过 @Lookup 方法的 Bean 名称集合，避免重复扫描 */
	private final Set<String> lookupMethodsChecked = ConcurrentHashMap.newKeySet(256);

	/** 按 Bean 类型缓存的构造器候选结果 */
	private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

	/** 按 Bean 名称（或类名）缓存的字段/方法注入元数据 */
	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


	/**
	 * 创建新的 {@code AutowiredAnnotationBeanPostProcessor}，支持 Spring 标准的
	 * {@link Autowired @Autowired} 与 {@link Value @Value}。
	 * <p>若类路径可用，也一并支持通用的 {@link jakarta.inject.Inject @Inject}。
	 */
	@SuppressWarnings("unchecked")
	public AutowiredAnnotationBeanPostProcessor() {
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);

		ClassLoader classLoader = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
		try {
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("jakarta.inject.Inject", classLoader));
			logger.trace("'jakarta.inject.Inject' annotation found and supported for autowiring");
		}
		catch (ClassNotFoundException ex) {
			// 类路径无 jakarta.inject API，直接跳过
		}
	}


	/**
	 * 设置单一的「自动装配」注解类型，用于构造器、字段、setter 及任意配置方法。
	 * <p>默认类型为 Spring 提供的 {@link Autowired @Autowired}、{@link Value @Value}，
	 * 以及（若可用）通用的 {@code @Inject}。
	 * <p>提供此 setter，便于开发者使用自定义（非 Spring 专属）注解来标记需自动装配的成员。
	 */
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	/**
	 * 设置一组「自动装配」注解类型，用于构造器、字段、setter 及任意配置方法。
	 * <p>默认类型为 Spring 提供的 {@link Autowired @Autowired}、{@link Value @Value}，
	 * 以及（若可用）通用的 {@code @Inject}。
	 * <p>提供此 setter，便于开发者使用自定义（非 Spring 专属）注解类型来标记需自动装配的成员。
	 */
	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}

	/**
	 * 设置注解中表示「依赖是否必须」的属性名。
	 * @see #setRequiredParameterValue(boolean)
	 */
	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	/**
	 * 设置表示「依赖必须存在」的布尔取值。
	 * <p>例如使用 {@code required=true}（默认）时，该值应为 {@code true}；
	 * 若改用 {@code optional=false} 这类语义，则应设为 {@code false}。
	 * @see #setRequiredParameterName(String)
	 */
	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		this.beanFactory = clbf;
		this.metadataReaderFactory = MetadataReaderFactory.create(clbf.getBeanClassLoader());
	}


	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		// 在合并后的 BeanDefinition 上登记外部管理的配置成员（字段/方法注入点）
		findInjectionMetadata(beanName, beanType, beanDefinition);

		// 单例实例化后可清理部分缓存；injectionMetadataCache 需保留，
		// 否则无法可靠还原「外部管理配置成员」信息
		if (beanDefinition.isSingleton()) {
			this.candidateConstructorsCache.remove(beanType);
			// 若存在真正的方法覆盖（如 @Lookup），则与 BeanDefinition 一并保留
			if (!beanDefinition.hasMethodOverrides()) {
				this.lookupMethodsChecked.remove(beanName);
			}
		}
	}

	@Override
	public void resetBeanDefinition(String beanName) {
		this.lookupMethodsChecked.remove(beanName);
		this.injectionMetadataCache.remove(beanName);
	}

	@Override
	public @Nullable BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		Class<?> beanClass = registeredBean.getBeanClass();
		String beanName = registeredBean.getBeanName();
		RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
		InjectionMetadata metadata = findInjectionMetadata(beanName, beanClass, beanDefinition);
		Collection<AutowiredElement> autowiredElements = getAutowiredElements(metadata,
				beanDefinition.getPropertyValues());
		if (!ObjectUtils.isEmpty(autowiredElements)) {
			return new AotContribution(beanClass, autowiredElements, getAutowireCandidateResolver());
		}
		return null;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<AutowiredElement> getAutowiredElements(InjectionMetadata metadata, PropertyValues propertyValues) {
		return (Collection) metadata.getInjectedElements(propertyValues);
	}

	private @Nullable AutowireCandidateResolver getAutowireCandidateResolver() {
		if (this.beanFactory instanceof DefaultListableBeanFactory lbf) {
			return lbf.getAutowireCandidateResolver();
		}
		return null;
	}

	private InjectionMetadata findInjectionMetadata(String beanName, Class<?> beanType, RootBeanDefinition beanDefinition) {
		InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
		// 将注入点登记为 BeanDefinition 的外部管理配置成员，避免被其他逻辑误清理
		metadata.checkConfigMembers(beanDefinition);
		return metadata;
	}

	@Override
	public Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeanCreationException {
		checkLookupMethods(beanClass, beanName);

		// 上方可能已写入 LookupOverride，此处取实例化策略给出的实际（可能被 CGLIB 增强的）类型
		if (this.beanFactory instanceof AbstractAutowireCapableBeanFactory aacBeanFactory) {
			RootBeanDefinition mbd = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
			if (mbd.getFactoryMethodName() == null && mbd.hasBeanClass()) {
				return aacBeanFactory.getInstantiationStrategy().getActualBeanClass(mbd, beanName, aacBeanFactory);
			}
		}
		return beanClass;
	}

	@Override
	public Constructor<?> @Nullable [] determineCandidateConstructors(Class<?> beanClass, final String beanName)
			throws BeanCreationException {

		// 构造器解析前先处理 @Lookup，确保方法覆盖已就绪
		checkLookupMethods(beanClass, beanName);

		// 先无锁读缓存；未命中再进入同步块完整解析
		Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			// 加锁后双重检查，避免并发重复解析
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					Constructor<?>[] rawCandidates;
					try {
						rawCandidates = beanClass.getDeclaredConstructors();
					}
					catch (Throwable ex) {
						throw new BeanCreationException(beanName,
								"Resolution of declared constructors on bean Class [" + beanClass.getName() +
								"] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
					}
					List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
					Constructor<?> requiredConstructor = null;
					Constructor<?> defaultConstructor = null;
					Constructor<?> primaryConstructor = BeanUtils.findPrimaryConstructor(beanClass);
					int nonSyntheticConstructors = 0;
					for (Constructor<?> candidate : rawCandidates) {
						if (!candidate.isSynthetic()) {
							nonSyntheticConstructors++;
						}
						else if (primaryConstructor != null) {
							// 已有主构造器时跳过合成构造器（如编译器生成）
							continue;
						}
						MergedAnnotation<?> ann = findAutowiredAnnotation(candidate);
						if (ann == null) {
							// CGLIB 等增强类：回退到用户原始类上的同签名构造器再找注解
							Class<?> userClass = ClassUtils.getUserClass(beanClass);
							if (userClass != beanClass) {
								try {
									Constructor<?> superCtor =
											userClass.getDeclaredConstructor(candidate.getParameterTypes());
									ann = findAutowiredAnnotation(superCtor);
								}
								catch (NoSuchMethodException ex) {
									// 用户类无等价构造器，继续
								}
							}
						}
						if (ann != null) {
							if (requiredConstructor != null) {
								// 已存在 required=true 的构造器，不允许再出现任何带注解构造器
								throw new BeanCreationException(beanName,
										"Invalid autowire-marked constructor: " + candidate +
										". Found constructor with 'required' Autowired annotation already: " +
										requiredConstructor);
							}
							boolean required = determineRequiredStatus(ann);
							if (required) {
								if (!candidates.isEmpty()) {
									// required=true 的构造器必须是唯一候选
									throw new BeanCreationException(beanName,
											"Invalid autowire-marked constructors: " + candidates +
											". Found constructor with 'required' Autowired annotation: " +
											candidate);
								}
								requiredConstructor = candidate;
							}
							candidates.add(candidate);
						}
						else if (candidate.getParameterCount() == 0) {
							defaultConstructor = candidate;
						}
					}
					if (!candidates.isEmpty()) {
						// 全是可选构造器时，把无参构造器加入列表作为回退
						if (requiredConstructor == null) {
							if (defaultConstructor != null) {
								candidates.add(defaultConstructor);
							}
							else if (candidates.size() == 1 && logger.isInfoEnabled()) {
								logger.info("Inconsistent constructor declaration on bean with name '" + beanName +
										"': single autowire-marked constructor flagged as optional - " +
										"this constructor is effectively required since there is no " +
										"default constructor to fall back to: " + candidates.get(0));
							}
						}
						candidateConstructors = candidates.toArray(EMPTY_CONSTRUCTOR_ARRAY);
					}
					else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0) {
						// 仅一个有参构造器：即使无注解也作为候选
						candidateConstructors = new Constructor<?>[] {rawCandidates[0]};
					}
					else if (nonSyntheticConstructors == 2 && primaryConstructor != null &&
							defaultConstructor != null && !primaryConstructor.equals(defaultConstructor)) {
						// Kotlin 等场景：主构造器 + 无参构造器
						candidateConstructors = new Constructor<?>[] {primaryConstructor, defaultConstructor};
					}
					else if (nonSyntheticConstructors == 1 && primaryConstructor != null) {
						candidateConstructors = new Constructor<?>[] {primaryConstructor};
					}
					else {
						candidateConstructors = EMPTY_CONSTRUCTOR_ARRAY;
					}
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return (candidateConstructors.length > 0 ? candidateConstructors : null);
	}

	private void checkLookupMethods(Class<?> beanClass, final String beanName) throws BeanCreationException {
		if (!this.lookupMethodsChecked.contains(beanName)) {
			if (AnnotationUtils.isCandidateClass(beanClass, Lookup.class)) {
				try {
					Class<?> targetClass = beanClass;
					do {
						ReflectionUtils.doWithLocalMethods(targetClass, method -> {
							Lookup lookup = method.getAnnotation(Lookup.class);
							if (lookup != null) {
								Assert.state(this.beanFactory != null, "No BeanFactory available");
								// 将 @Lookup 方法登记为 LookupOverride，运行时由容器动态解析
								LookupOverride override = new LookupOverride(method, lookup.value());
								try {
									RootBeanDefinition mbd = (RootBeanDefinition)
											this.beanFactory.getMergedBeanDefinition(beanName);
									mbd.getMethodOverrides().addOverride(override);
								}
								catch (NoSuchBeanDefinitionException ex) {
									throw new BeanCreationException(beanName,
											"Cannot apply @Lookup to beans without corresponding bean definition");
								}
							}
						});
						targetClass = targetClass.getSuperclass();
					}
					while (targetClass != null && targetClass != Object.class);

				}
				catch (IllegalStateException ex) {
					throw new BeanCreationException(beanName, "Lookup method resolution failed", ex);
				}
			}
			this.lookupMethodsChecked.add(beanName);
		}
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
		// 属性填充阶段：按缓存的注入元数据对字段/方法执行自动装配
		InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
		}
		return pvs;
	}

	/**
	 * <em>原生</em>注入入口：对任意目标实例直接调用，解析并注入所有带已配置
	 * 「自动装配」注解的字段与方法。
	 * @param bean 待处理的目标实例
	 * @throws BeanCreationException 自动装配失败时抛出
	 * @see #setAutowiredAnnotationTypes(Set)
	 */
	public void processInjection(Object bean) throws BeanCreationException {
		Class<?> clazz = bean.getClass();
		// 无 beanName 时以类名作为缓存键（兼容外部直接调用）
		InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
		try {
			metadata.inject(bean, null, null);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					"Injection of autowired dependencies failed for class [" + clazz + "]", ex);
		}
	}

	private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
		// 无 beanName 时回退到类名，兼容自定义调用方
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// 先无锁读；类型变化或首次构建时再加锁刷新
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					metadata = buildAutowiringMetadata(clazz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
		if (!AnnotationUtils.isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
			return InjectionMetadata.EMPTY;
		}

		final List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = ClassUtils.getUserClass(clazz);

		do {
			// 自下而上（子类优先）收集：先字段后方法；每层都插到列表头部，最终父类在前、子类在后
			final List<InjectionMetadata.InjectedElement> fieldElements = new ArrayList<>();
			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				MergedAnnotation<?> ann = findAutowiredAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static fields: " + field);
						}
						return;
					}
					boolean required = determineRequiredStatus(ann);
					fieldElements.add(new AutowiredFieldElement(field, required));
				}
			});

			final List<InjectionMetadata.InjectedElement> methodElements = new ArrayList<>();
			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				if (method.isBridge()) {
					return;
				}
				MergedAnnotation<?> ann = findAutowiredAnnotation(method);
				if (ann != null && method.equals(BridgeMethodResolver.getMostSpecificMethod(method, clazz))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static methods: " + method);
						}
						return;
					}
					if (method.getParameterCount() == 0) {
						if (method.getDeclaringClass().isRecord()) {
							// record 紧凑构造器参数上的注解会出现在访问器上，忽略
							return;
						}
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation should only be used on methods with parameters: " +
									method);
						}
					}
					boolean required = determineRequiredStatus(ann);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method, clazz);
					methodElements.add(new AutowiredMethodElement(method, required, pd));
				}
			});

			elements.addAll(0, sortMethodElements(methodElements, targetClass));
			elements.addAll(0, fieldElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return InjectionMetadata.forElements(elements, clazz);
	}

	private @Nullable MergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao) {
		MergedAnnotations annotations = MergedAnnotations.from(ao);
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			MergedAnnotation<?> annotation = annotations.get(type);
			if (annotation.isPresent()) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * 判断带注解的字段或方法是否要求依赖必须存在。
	 * <p>{@code required} 为真时，找不到 Bean 应导致自动装配失败；
	 * 否则找不到 Bean 时会跳过该注入点。
	 * @param ann 表示 Autowired（或同类）注解的 {@link MergedAnnotation}
	 * @return 注解是否表明该依赖为必须
	 */
	protected boolean determineRequiredStatus(MergedAnnotation<?> ann) {
		Optional<Boolean> requiredAttribute = ann.getValue(this.requiredParameterName, Boolean.class);
		return (requiredAttribute.isEmpty() || this.requiredParameterValue == requiredAttribute.get());
	}

	/**
	 * 尽可能借助 ASM 按源码声明顺序排序方法注入点，保证顺序稳定可复现。
	 */
	private List<InjectionMetadata.InjectedElement> sortMethodElements(
			List<InjectionMetadata.InjectedElement> methodElements, Class<?> targetClass) {

		if (this.metadataReaderFactory != null && methodElements.size() > 1) {
			// JVM 反射返回的方法顺序不确定；用 ASM 读 class 文件拿声明顺序
			try {
				AnnotationMetadata asm =
						this.metadataReaderFactory.getMetadataReader(targetClass.getName()).getAnnotationMetadata();
				Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Autowired.class.getName());
				if (asmMethods.size() >= methodElements.size()) {
					List<InjectionMetadata.InjectedElement> candidateMethods = new ArrayList<>(methodElements);
					List<InjectionMetadata.InjectedElement> selectedMethods = new ArrayList<>(asmMethods.size());
					for (MethodMetadata asmMethod : asmMethods) {
						for (Iterator<InjectionMetadata.InjectedElement> it = candidateMethods.iterator(); it.hasNext();) {
							InjectionMetadata.InjectedElement element = it.next();
							if (element.getMember().getName().equals(asmMethod.getMethodName())) {
								selectedMethods.add(element);
								it.remove();
								break;
							}
						}
					}
					if (selectedMethods.size() == methodElements.size()) {
						// 反射检测到的方法都能在 ASM 集合中对上，采用 ASM 顺序
						return selectedMethods;
					}
				}
			}
			catch (IOException ex) {
				logger.debug("Failed to read class file via ASM for determining @Autowired method order", ex);
				// 读失败则继续使用原先的反射顺序
			}
		}
		return methodElements;
	}

	/**
	 * 将被注入 Bean 登记为当前 Bean 的依赖（销毁顺序等依赖图）。
	 */
	private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Autowiring by type from bean name '" + beanName +
							"' to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}

	/**
	 * 解析已缓存的方法参数或字段值：若为 {@link DependencyDescriptor} 则委托工厂 resolveDependency。
	 */
	private @Nullable Object resolveCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor descriptor) {
			Assert.state(this.beanFactory != null, "No BeanFactory available");
			// 与首次注入相同路径：DefaultListableBeanFactory#resolveDependency
			return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
		}
		else {
			return cachedArgument;
		}
	}


	/**
	 * 注入信息基类：在 {@link InjectionMetadata.InjectedElement} 上增加 required 标记。
	 */
	private abstract static class AutowiredElement extends InjectionMetadata.InjectedElement {

		/** 该注入点是否要求依赖必须存在 */
		protected final boolean required;

		protected AutowiredElement(Member member, @Nullable PropertyDescriptor pd, boolean required) {
			super(member, pd);
			this.required = required;
		}
	}


	/**
	 * 带注解字段的注入信息：首次通过 resolveDependency 解析，成功后可缓存快捷描述符。
	 */
	private class AutowiredFieldElement extends AutowiredElement {

		/** 是否已缓存解析结果（或快捷 DependencyDescriptor） */
		private volatile boolean cached;

		/** 缓存的字段值描述：DependencyDescriptor / ShortcutDependencyDescriptor / null */
		private volatile @Nullable Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null, required);
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value;
			if (this.cached) {
				try {
					value = resolveCachedArgument(beanName, this.cachedFieldValue);
				}
				catch (BeansException ex) {
					// 缓存参数与目标 Bean 不匹配，失效后重新完整解析
					this.cached = false;
					logger.debug("Failed to resolve cached argument", ex);
					value = resolveFieldValue(field, bean, beanName);
				}
			}
			else {
				value = resolveFieldValue(field, bean, beanName);
			}
			if (value != null) {
				ReflectionUtils.makeAccessible(field);
				field.set(bean, value);
			}
		}

		private @Nullable Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
			DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
			desc.setContainingClass(bean.getClass());
			Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
			Assert.state(beanFactory != null, "No BeanFactory available");
			TypeConverter typeConverter = beanFactory.getTypeConverter();
			Object value;
			try {
				// 核心协作点：交给 BeanFactory（通常是 DefaultListableBeanFactory）按类型/限定符解析依赖
				value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
			}
			synchronized (this) {
				if (!this.cached) {
					if (value != null || this.required) {
						Object cachedFieldValue = desc;
						registerDependentBeans(beanName, autowiredBeanNames);
						// 唯一候选且类型匹配时，缓存 ShortcutDependencyDescriptor，后续可直接 getBean
						if (value != null && autowiredBeanNames.size() == 1) {
							String autowiredBeanName = autowiredBeanNames.iterator().next();
							if (beanFactory.containsBean(autowiredBeanName) &&
									beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
								cachedFieldValue = new ShortcutDependencyDescriptor(desc, autowiredBeanName);
							}
						}
						this.cachedFieldValue = cachedFieldValue;
						this.cached = true;
					}
					else {
						// 可选依赖且解析为 null：不标记 cached，下次仍可再试
						this.cachedFieldValue = null;
						// cached 标志保持 false
					}
				}
			}
			return value;
		}
	}


	/**
	 * 带注解方法的注入信息：解析每个参数后反射调用；可按参数缓存快捷描述符。
	 */
	private class AutowiredMethodElement extends AutowiredElement {

		/** 是否已缓存方法参数的解析描述 */
		private volatile boolean cached;

		/** 每个参数对应的 DependencyDescriptor（或 Shortcut）；整体为 null 表示可选且未解析到 */
		private volatile Object @Nullable [] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
			super(method, pd, required);
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			// 若该属性已在 PropertyValues 中显式给出，则跳过方法注入（避免覆盖）
			if (!shouldInject(pvs)) {
				return;
			}
			Method method = (Method) this.member;
			@Nullable Object[] arguments;
			if (this.cached) {
				try {
					arguments = resolveCachedArguments(beanName, this.cachedMethodArguments);
				}
				catch (BeansException ex) {
					// 缓存参数与目标 Bean 不匹配，失效后重新完整解析
					this.cached = false;
					logger.debug("Failed to resolve cached argument", ex);
					arguments = resolveMethodArguments(method, bean, beanName);
				}
			}
			else {
				arguments = resolveMethodArguments(method, bean, beanName);
			}
			if (arguments != null) {
				try {
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		private @Nullable Object @Nullable [] resolveCachedArguments(@Nullable String beanName, Object @Nullable [] cachedMethodArguments) {
			if (cachedMethodArguments == null) {
				return null;
			}
			@Nullable Object[] arguments = new Object[cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolveCachedArgument(beanName, cachedMethodArguments[i]);
			}
			return arguments;
		}

		private @Nullable Object @Nullable [] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
			int argumentCount = method.getParameterCount();
			@Nullable Object[] arguments = new Object[argumentCount];
			DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
			Set<String> autowiredBeanNames = CollectionUtils.newLinkedHashSet(argumentCount);
			Assert.state(beanFactory != null, "No BeanFactory available");
			TypeConverter typeConverter = beanFactory.getTypeConverter();
			for (int i = 0; i < arguments.length; i++) {
				MethodParameter methodParam = new MethodParameter(method, i);
				DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required);
				currDesc.setContainingClass(bean.getClass());
				descriptors[i] = currDesc;
				try {
					Object arg = beanFactory.resolveDependency(currDesc, beanName, autowiredBeanNames, typeConverter);
					if (arg == null && !this.required && !methodParam.isOptional()) {
						// 可选方法：任一非 Optional 参数解析为 null 则整次注入放弃
						arguments = null;
						break;
					}
					arguments[i] = arg;
				}
				catch (BeansException ex) {
					throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
				}
			}
			synchronized (this) {
				if (!this.cached) {
					if (arguments != null) {
						DependencyDescriptor[] cachedMethodArguments = Arrays.copyOf(descriptors, argumentCount);
						registerDependentBeans(beanName, autowiredBeanNames);
						// 参数个数与匹配到的 Bean 名一一对应时，尝试为每个参数建立快捷描述符
						if (autowiredBeanNames.size() == argumentCount) {
							Iterator<String> it = autowiredBeanNames.iterator();
							Class<?>[] paramTypes = method.getParameterTypes();
							for (int i = 0; i < paramTypes.length; i++) {
								String autowiredBeanName = it.next();
								if (arguments[i] != null && beanFactory.containsBean(autowiredBeanName) &&
										beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
									cachedMethodArguments[i] = new ShortcutDependencyDescriptor(
											descriptors[i], autowiredBeanName);
								}
							}
						}
						this.cachedMethodArguments = cachedMethodArguments;
						this.cached = true;
					}
					else {
						this.cachedMethodArguments = null;
						// cached 标志保持 false
					}
				}
			}
			return arguments;
		}
	}


	/**
	 * {@link DependencyDescriptor} 变体：已预先解析出目标 Bean 名称，后续走快捷 getBean。
	 */
	@SuppressWarnings("serial")
	private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

		/** 预解析的目标 Bean 名称 */
		private final String shortcut;

		public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut) {
			super(original);
			this.shortcut = shortcut;
		}

		@Override
		public Object resolveShortcut(BeanFactory beanFactory) {
			return beanFactory.getBean(this.shortcut, getDependencyType());
		}
	}


	/**
	 * AOT 贡献：为字段与方法注入生成运行时装配代码，并注册必要的 RuntimeHints。
	 */
	private static class AotContribution implements BeanRegistrationAotContribution {

		private static final String REGISTERED_BEAN_PARAMETER = "registeredBean";

		private static final String INSTANCE_PARAMETER = "instance";

		/** 目标 Bean 类型 */
		private final Class<?> target;

		/** 需要生成代码的自动装配元素 */
		private final Collection<AutowiredElement> autowiredElements;

		/** 用于懒加载代理等提示注册的候选解析器（可为空） */
		private final @Nullable AutowireCandidateResolver candidateResolver;

		AotContribution(Class<?> target, Collection<AutowiredElement> autowiredElements,
				@Nullable AutowireCandidateResolver candidateResolver) {

			this.target = target;
			this.autowiredElements = autowiredElements;
			this.candidateResolver = candidateResolver;
		}

		@Override
		public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
			GeneratedClass generatedClass = generationContext.getGeneratedClasses()
					.addForFeatureComponent("Autowiring", this.target, type -> {
						type.addJavadoc("Autowiring for {@link $T}.", this.target);
						type.addModifiers(javax.lang.model.element.Modifier.PUBLIC);
					});
			GeneratedMethod generateMethod = generatedClass.getMethods().add("apply", method -> {
				method.addJavadoc("Apply the autowiring.");
				method.addModifiers(javax.lang.model.element.Modifier.PUBLIC,
						javax.lang.model.element.Modifier.STATIC);
				method.addParameter(RegisteredBean.class, REGISTERED_BEAN_PARAMETER);
				method.addParameter(this.target, INSTANCE_PARAMETER);
				method.returns(this.target);
				CodeWarnings codeWarnings = new CodeWarnings();
				codeWarnings.detectDeprecation(this.target);
				method.addCode(generateMethodCode(codeWarnings,
						generatedClass.getName(), generationContext.getRuntimeHints()));
				codeWarnings.suppress(method);
			});
			beanRegistrationCode.addInstancePostProcessor(generateMethod.toMethodReference());

			if (this.candidateResolver != null) {
				registerHints(generationContext.getRuntimeHints());
			}
		}

		private CodeBlock generateMethodCode(CodeWarnings codeWarnings,
				ClassName targetClassName, RuntimeHints hints) {

			CodeBlock.Builder code = CodeBlock.builder();
			for (AutowiredElement autowiredElement : this.autowiredElements) {
				code.addStatement(generateMethodStatementForElement(
						codeWarnings, targetClassName, autowiredElement, hints));
			}
			code.addStatement("return $L", INSTANCE_PARAMETER);
			return code.build();
		}

		private CodeBlock generateMethodStatementForElement(CodeWarnings codeWarnings,
				ClassName targetClassName, AutowiredElement autowiredElement, RuntimeHints hints) {

			Member member = autowiredElement.getMember();
			boolean required = autowiredElement.required;
			if (member instanceof Field field) {
				return generateMethodStatementForField(
						codeWarnings, targetClassName, field, required, hints);
			}
			if (member instanceof Method method) {
				return generateMethodStatementForMethod(
						codeWarnings, targetClassName, method, required, hints);
			}
			throw new IllegalStateException(
					"Unsupported member type " + member.getClass().getName());
		}

		private CodeBlock generateMethodStatementForField(CodeWarnings codeWarnings,
				ClassName targetClassName, Field field, boolean required, RuntimeHints hints) {

			hints.reflection().registerField(field);
			CodeBlock resolver = CodeBlock.of("$T.$L($S)",
					AutowiredFieldValueResolver.class,
					(!required ? "forField" : "forRequiredField"), field.getName());
			AccessControl accessControl = AccessControl.forMember(field);
			if (!accessControl.isAccessibleFrom(targetClassName)) {
				// 不可见字段：运行时通过反射 resolveAndSet
				return CodeBlock.of("$L.resolveAndSet($L, $L)", resolver,
						REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
			}
			else {
				codeWarnings.detectDeprecation(field);
				return CodeBlock.of("$L.$L = $L.resolve($L)", INSTANCE_PARAMETER,
						field.getName(), resolver, REGISTERED_BEAN_PARAMETER);
			}
		}

		private CodeBlock generateMethodStatementForMethod(CodeWarnings codeWarnings,
				ClassName targetClassName, Method method, boolean required, RuntimeHints hints) {

			CodeBlock.Builder code = CodeBlock.builder();
			code.add("$T.$L", AutowiredMethodArgumentsResolver.class,
					(!required ? "forMethod" : "forRequiredMethod"));
			code.add("($S", method.getName());
			if (method.getParameterCount() > 0) {
				codeWarnings.detectDeprecation(method.getParameterTypes());
				code.add(", $L", generateParameterTypesCode(method.getParameterTypes()));
			}
			code.add(")");
			AccessControl accessControl = AccessControl.forMember(method);
			if (!accessControl.isAccessibleFrom(targetClassName)) {
				hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				code.add(".resolveAndInvoke($L, $L)", REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
			}
			else {
				codeWarnings.detectDeprecation(method);
				hints.reflection().registerType(method.getDeclaringClass());
				CodeBlock arguments = new AutowiredArgumentsCodeGenerator(this.target,
						method).generateCode(method.getParameterTypes());
				CodeBlock injectionCode = CodeBlock.of("args -> $L.$L($L)",
						INSTANCE_PARAMETER, method.getName(), arguments);
				code.add(".resolve($L, $L)", REGISTERED_BEAN_PARAMETER, injectionCode);
			}
			return code.build();
		}

		private CodeBlock generateParameterTypesCode(Class<?>[] parameterTypes) {
			return CodeBlock.join(Arrays.stream(parameterTypes)
					.map(parameterType -> CodeBlock.of("$T.class", parameterType))
					.toList(), ", ");
		}

		private void registerHints(RuntimeHints runtimeHints) {
			this.autowiredElements.forEach(autowiredElement -> {
				boolean required = autowiredElement.required;
				Member member = autowiredElement.getMember();
				if (member instanceof Field field) {
					DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, required);
					registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
				}
				if (member instanceof Method method) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					for (int i = 0; i < parameterTypes.length; i++) {
						MethodParameter methodParam = new MethodParameter(method, i);
						DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParam, required);
						registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
					}
				}
			});
		}

		private void registerProxyIfNecessary(RuntimeHints runtimeHints, DependencyDescriptor dependencyDescriptor) {
			if (this.candidateResolver != null) {
				Class<?> proxyClass =
						this.candidateResolver.getLazyResolutionProxyClass(dependencyDescriptor, null);
				if (proxyClass != null) {
					ClassHintUtils.registerProxyIfNecessary(proxyClass, runtimeHints);
				}
			}
		}
	}

}
