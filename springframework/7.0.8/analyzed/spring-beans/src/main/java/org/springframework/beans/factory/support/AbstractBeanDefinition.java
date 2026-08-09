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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 具体、完整 {@link BeanDefinition} 类的抽象基类，
 * 抽取 {@link GenericBeanDefinition}、{@link RootBeanDefinition}
 * 和 {@link ChildBeanDefinition} 的公共属性。
 *
 * <p>自动装配常量与
 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * 接口中定义的常量一致。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Sebastien Deleuze
 * @see GenericBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
		implements BeanDefinition, Cloneable {

	/**
	 * 默认作用域名称常量：{@code ""}，等价于单例状态，除非被父 Bean 定义覆盖（若适用）。
	 */
	public static final String SCOPE_DEFAULT = "";

	/**
	 * 表示完全不进行外部自动装配的常量。
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

	/**
	 * 表示按名称自动装配 Bean 属性的常量。
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * 表示按类型自动装配 Bean 属性的常量。
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

	/**
	 * 表示自动装配构造函数的常量。
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

	/**
	 * 表示通过内省 Bean 类确定适当自动装配策略的常量。
	 * @see #setAutowireMode
	 * @deprecated If you are using mixed autowiring strategies, use
	 * annotation-based autowiring for clearer demarcation of autowiring needs.
	 */
	@Deprecated(since = "3.0")
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

	/**
	 * 表示完全不进行依赖检查的常量。
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_NONE = 0;

	/**
	 * 表示对对象引用进行依赖检查的常量。
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;

	/**
	 * 表示对"简单"属性进行依赖检查的常量。
	 * @see #setDependencyCheck
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;

	/**
	 * 表示对所有属性（对象引用和"简单"属性）进行依赖检查的常量。
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_ALL = 3;

	/**
	 * 可 {@link org.springframework.core.AttributeAccessor#setAttribute set} 在
	 * {@link org.springframework.beans.factory.config.BeanDefinition} 上的属性名，
	 * 用于指示一个或多个首选构造函数。类似于 Bean 类上
	 * {@code @Autowired} 注解的构造函数。
	 * <p>The attribute value may be a single {@link java.lang.reflect.Constructor}
	 * reference or an array thereof.
	 * @since 6.1
	 * @see org.springframework.beans.factory.annotation.Autowired
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#getPreferredConstructors()
	 */
	public static final String PREFERRED_CONSTRUCTORS_ATTRIBUTE = "preferredConstructors";

	/**
	 * 可 {@link org.springframework.core.AttributeAccessor#setAttribute set} 在
	 * {@link org.springframework.beans.factory.config.BeanDefinition} 上的属性名，
	 * 用于指示目标 Bean 的排序顺序。类似于 {@code @Order} 注解。
	 * @since 6.1.2
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.Ordered
	 */
	public static final String ORDER_ATTRIBUTE = "order";

	/**
	 * 表示容器应尝试推断 Bean 的 {@link #setDestroyMethodName 销毁方法名}，
	 * 而非显式指定方法名。值 {@value} 特意包含方法名中通常非法的字符，
	 * 确保不会与合法同名方法冲突。
	 * <p>当前，销毁方法推断检测的方法名为 "close" 和 "shutdown"（若存在于特定 Bean 类上）。
	 */
	public static final String INFER_METHOD = "(inferred)";


	/** Bean 类（Class 对象或类名字符串）。 */
	private volatile @Nullable Object beanClass;

	/** Bean 作用域名称。 */
	private @Nullable String scope = SCOPE_DEFAULT;

	/** 是否为抽象 Bean 定义。 */
	private boolean abstractFlag = false;

	/** 是否在后台线程引导初始化。 */
	private boolean backgroundInit = false;

	/** 是否懒加载初始化。 */
	private @Nullable Boolean lazyInit;

	/** 自动装配模式。 */
	private int autowireMode = AUTOWIRE_NO;

	/** 依赖检查模式。 */
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	/** 显式依赖的 Bean 名称数组。 */
	private String @Nullable [] dependsOn;

	/** 是否为自动装配候选。 */
	private boolean autowireCandidate = true;

	/** 是否为默认候选（按纯类型注入）。 */
	private boolean defaultCandidate = true;

	/** 是否为首选 Bean（@Primary）。 */
	private boolean primary = false;

	/** 是否为回退 Bean。 */
	private boolean fallback = false;

	/** 自动装配候选限定符映射。 */
	private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();

	/** 实例供应器回调。 */
	private @Nullable Supplier<?> instanceSupplier;

	/** 是否允许访问非 public 成员。 */
	private boolean nonPublicAccessAllowed = true;

	/** 是否以宽松模式解析构造函数。 */
	private boolean lenientConstructorResolution = true;

	/** 工厂 Bean 名称。 */
	private @Nullable String factoryBeanName;

	/** 工厂方法名称。 */
	private @Nullable String factoryMethodName;

	/** 构造函数参数值。 */
	private @Nullable ConstructorArgumentValues constructorArgumentValues;

	/** 属性值集合。 */
	private @Nullable MutablePropertyValues propertyValues;

	/** 方法覆盖信息。 */
	private MethodOverrides methodOverrides = new MethodOverrides();

	/** 初始化方法名称数组。 */
	private String @Nullable [] initMethodNames;

	/** 销毁方法名称数组。 */
	private String @Nullable [] destroyMethodNames;

	/** 是否强制存在初始化方法。 */
	private boolean enforceInitMethod = true;

	/** 是否强制存在销毁方法。 */
	private boolean enforceDestroyMethod = true;

	/** 是否为合成 Bean 定义。 */
	private boolean synthetic = false;

	/** Bean 角色。 */
	private int role = BeanDefinition.ROLE_APPLICATION;

	/** Bean 描述。 */
	private @Nullable String description;

	/** Bean 定义来源资源。 */
	private @Nullable Resource resource;


	/**
	 * 使用默认设置创建新的 AbstractBeanDefinition。
	 */
	protected AbstractBeanDefinition() {
		this(null, null);
	}

	/**
	 * 使用给定构造函数参数值和属性值创建新的 AbstractBeanDefinition。
	 */
	protected AbstractBeanDefinition(@Nullable ConstructorArgumentValues cargs, @Nullable MutablePropertyValues pvs) {
		this.constructorArgumentValues = cargs;
		this.propertyValues = pvs;
	}

	/**
	 * 将给定 Bean 定义深拷贝为新的 AbstractBeanDefinition。
	 * @param original the original bean definition to copy from
	 */
	protected AbstractBeanDefinition(BeanDefinition original) {
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setRole(original.getRole());
		setSource(original.getSource());
		copyAttributesFrom(original);

		if (original instanceof AbstractBeanDefinition originalAbd) {
			if (originalAbd.hasBeanClass()) {
				setBeanClass(originalAbd.getBeanClass());
			}
			if (originalAbd.hasConstructorArgumentValues()) {
				setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			}
			if (originalAbd.hasPropertyValues()) {
				setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			}
			if (originalAbd.hasMethodOverrides()) {
				setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			}
			setBackgroundInit(originalAbd.isBackgroundInit());
			Boolean lazyInit = originalAbd.getLazyInit();
			if (lazyInit != null) {
				setLazyInit(lazyInit);
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			setDefaultCandidate(originalAbd.isDefaultCandidate());
			setPrimary(originalAbd.isPrimary());
			setFallback(originalAbd.isFallback());
			copyQualifiersFrom(originalAbd);
			setInstanceSupplier(originalAbd.getInstanceSupplier());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodNames(originalAbd.getInitMethodNames());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodNames(originalAbd.getDestroyMethodNames());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		}
		else {
			setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			setLazyInit(original.isLazyInit());
			setResourceDescription(original.getResourceDescription());
		}
	}


	/**
	 * 从给定 Bean 定义（推测为子定义）覆盖本 Bean 定义（推测为从父子继承关系复制的父定义）中的设置。
	 * <ul>
	 * <li>Will override beanClass if specified in the given bean definition.
	 * <li>Will always take {@code abstract}, {@code scope},
	 * {@code lazyInit}, {@code autowireMode}, {@code dependencyCheck},
	 * and {@code dependsOn} from the given bean definition.
	 * <li>Will add {@code constructorArgumentValues}, {@code propertyValues},
	 * {@code methodOverrides} from the given bean definition to existing ones.
	 * <li>Will override {@code factoryBeanName}, {@code factoryMethodName},
	 * {@code initMethodName}, and {@code destroyMethodName} if specified
	 * in the given bean definition.
	 * </ul>
	 */
	public void overrideFrom(BeanDefinition other) {
		if (StringUtils.hasLength(other.getBeanClassName())) {
			setBeanClassName(other.getBeanClassName());
		}
		if (StringUtils.hasLength(other.getScope())) {
			setScope(other.getScope());
		}
		setAbstract(other.isAbstract());
		if (StringUtils.hasLength(other.getFactoryBeanName())) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if (StringUtils.hasLength(other.getFactoryMethodName())) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		setRole(other.getRole());
		setSource(other.getSource());
		copyAttributesFrom(other);

		if (other instanceof AbstractBeanDefinition otherAbd) {
			if (otherAbd.hasBeanClass()) {
				setBeanClass(otherAbd.getBeanClass());
			}
			if (otherAbd.hasConstructorArgumentValues()) {
				getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
			}
			if (otherAbd.hasPropertyValues()) {
				getPropertyValues().addPropertyValues(other.getPropertyValues());
			}
			if (otherAbd.hasMethodOverrides()) {
				getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
			}
			setBackgroundInit(otherAbd.isBackgroundInit());
			Boolean lazyInit = otherAbd.getLazyInit();
			if (lazyInit != null) {
				setLazyInit(lazyInit);
			}
			setAutowireMode(otherAbd.getAutowireMode());
			setDependencyCheck(otherAbd.getDependencyCheck());
			setDependsOn(otherAbd.getDependsOn());
			setAutowireCandidate(otherAbd.isAutowireCandidate());
			setDefaultCandidate(otherAbd.isDefaultCandidate());
			setPrimary(otherAbd.isPrimary());
			setFallback(otherAbd.isFallback());
			copyQualifiersFrom(otherAbd);
			setInstanceSupplier(otherAbd.getInstanceSupplier());
			setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
			if (otherAbd.getInitMethodNames() != null) {
				setInitMethodNames(otherAbd.getInitMethodNames());
				setEnforceInitMethod(otherAbd.isEnforceInitMethod());
			}
			if (otherAbd.getDestroyMethodNames() != null) {
				setDestroyMethodNames(otherAbd.getDestroyMethodNames());
				setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
			}
			setSynthetic(otherAbd.isSynthetic());
			setResource(otherAbd.getResource());
		}
		else {
			getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
			getPropertyValues().addPropertyValues(other.getPropertyValues());
			setLazyInit(other.isLazyInit());
			setResourceDescription(other.getResourceDescription());
		}
	}

	/**
	 * 将提供的默认值应用到本 Bean。
	 * @param defaults the default settings to apply
	 * @since 2.5
	 */
	public void applyDefaults(BeanDefinitionDefaults defaults) {
		Boolean lazyInit = defaults.getLazyInit();
		if (lazyInit != null) {
			setLazyInit(lazyInit);
		}
		setAutowireMode(defaults.getAutowireMode());
		setDependencyCheck(defaults.getDependencyCheck());
		setInitMethodName(defaults.getInitMethodName());
		setEnforceInitMethod(false);
		setDestroyMethodName(defaults.getDestroyMethodName());
		setEnforceDestroyMethod(false);
	}


	/**
	 * {@inheritDoc}
	 * @see #setBeanClass(Class)
	 */
	@Override
	public void setBeanClassName(@Nullable String beanClassName) {
		this.beanClass = beanClassName;
	}

	/**
	 * {@inheritDoc}
	 * @see #getBeanClass()
	 */
	@Override
	public @Nullable String getBeanClassName() {
		Object beanClassObject = this.beanClass;  // defensive access to volatile beanClass field
		return (beanClassObject instanceof Class<?> clazz ? clazz.getName() : (String) beanClassObject);
	}

	/**
	 * 指定本 Bean 的类。
	 * @see #setBeanClassName(String)
	 */
	public void setBeanClass(@Nullable Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * 返回 Bean 定义的指定类（假定已解析）。
	 * <p><b>NOTE:</b> This is an initial class reference as declared in the bean metadata
	 * definition, potentially combined with a declared factory method or a
	 * {@link org.springframework.beans.factory.FactoryBean} which may lead to a different
	 * runtime type of the bean, or not being set at all in case of an instance-level
	 * factory method (which is resolved via {@link #getFactoryBeanName()} instead).
	 * <b>Do not use this for runtime type introspection of arbitrary bean definitions.</b>
	 * The recommended way to find out about the actual runtime type of a particular bean
	 * is a {@link org.springframework.beans.factory.BeanFactory#getType} call for the
	 * specified bean name; this takes all of the above cases into account and returns the
	 * type of object that a {@link org.springframework.beans.factory.BeanFactory#getBean}
	 * call is going to return for the same bean name.
	 * @return the resolved bean class (never {@code null})
	 * @throws IllegalStateException if the bean definition does not define a bean class,
	 * or a specified bean class name has not been resolved into an actual Class yet
	 * @see #getBeanClassName()
	 * @see #hasBeanClass()
	 * @see #setBeanClass(Class)
	 * @see #resolveBeanClass(ClassLoader)
	 */
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = this.beanClass;  // defensive access to volatile beanClass field
		if (beanClassObject == null) {
			throw new IllegalStateException("No bean class specified on bean definition");
		}
		if (!(beanClassObject instanceof Class<?> clazz)) {
			throw new IllegalStateException(
					"Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
		}
		return clazz;
	}

	/**
	 * 返回本定义是否指定了 Bean 类。
	 * @see #getBeanClass()
	 * @see #setBeanClass(Class)
	 * @see #resolveBeanClass(ClassLoader)
	 */
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}

	/**
	 * 确定被包装 Bean 的类，必要时从指定类名解析。若 Bean 类已解析，调用时也会从名称重新加载指定 Class。
	 * @param classLoader the ClassLoader to use for resolving a (potential) class name
	 * @return the resolved bean class
	 * @throws ClassNotFoundException if the class name could be resolved
	 */
	public @Nullable Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
		this.beanClass = resolvedClass;
		return resolvedClass;
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation delegates to {@link #getBeanClass()}.
	 */
	@Override
	public ResolvableType getResolvableType() {
		return (hasBeanClass() ? ResolvableType.forClass(getBeanClass()) : ResolvableType.NONE);
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is singleton status, although this is only applied once
	 * a bean definition becomes active in the containing factory. A bean
	 * definition may eventually inherit its scope from a parent bean definition.
	 * For this reason, the default scope name is an empty string (i.e., {@code ""}),
	 * with singleton status being assumed until a resolved scope is set.
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	@Override
	public void setScope(@Nullable String scope) {
		this.scope = scope;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@link #SCOPE_DEFAULT}.
	 */
	@Override
	public @Nullable String getScope() {
		return this.scope;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code true}.
	 */
	@Override
	public boolean isSingleton() {
		return SCOPE_SINGLETON.equals(this.scope) || SCOPE_DEFAULT.equals(this.scope);
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public boolean isPrototype() {
		return SCOPE_PROTOTYPE.equals(this.scope);
	}

	/**
	 * 设置本 Bean 是否为"抽象"，即不打算自身实例化，
	 * 仅作为具体子 Bean 定义的父定义。
	 * <p>The default is "false". Specify {@code true} to tell the bean factory to
	 * not try to instantiate that particular bean in any case.
	 */
	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public boolean isAbstract() {
		return this.abstractFlag;
	}

	/**
	 * 指定本 Bean 的引导模式：默认为 {@code false}，非懒加载单例使用主预实例化线程，
	 * prototype 使用调用者线程。
	 * <p>Set this flag to {@code true} to allow for instantiating this bean on a
	 * background thread. For a non-lazy singleton, a background pre-instantiation
	 * thread can be used then, while still enforcing the completion at the end of
	 * {@link DefaultListableBeanFactory#preInstantiateSingletons()}.
	 * For a lazy singleton, a background pre-instantiation thread can be used as well
	 * - with completion allowed at a later point, enforcing it when actually accessed.
	 * <p>Note that this flag may be ignored by bean factories not set up for
	 * background bootstrapping, always applying single-threaded bootstrapping
	 * for non-lazy singleton beans.
	 * @since 6.2
	 * @see #setLazyInit
	 * @see DefaultListableBeanFactory#setBootstrapExecutor
	 */
	public void setBackgroundInit(boolean backgroundInit) {
		this.backgroundInit = backgroundInit;
	}

	/**
	 * 返回本 Bean 的引导模式：默认为 {@code false}，非懒加载单例使用主预实例化线程，
	 * prototype 使用调用者线程。
	 * @since 6.2
	 */
	public boolean isBackgroundInit() {
		return this.backgroundInit;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public boolean isLazyInit() {
		return (this.lazyInit != null && this.lazyInit);
	}

	/**
	 * 返回本 Bean 是否应懒加载初始化，即不在启动时急切实例化。仅适用于单例 Bean。
	 * @return the lazy-init flag if explicitly set, or {@code null} otherwise
	 * @since 5.2
	 */
	public @Nullable Boolean getLazyInit() {
		return this.lazyInit;
	}

	/**
	 * 设置自动装配模式。决定是否会自动检测和设置 Bean 引用。默认为 AUTOWIRE_NO，
	 * 即不会按名称或类型进行约定式自动装配（但可能仍有显式注解驱动的自动装配）。
	 * @param autowireMode the autowire mode to set.
	 * Must be one of the constants defined in this class.
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 */
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	/**
	 * 返回 Bean 定义中指定的自动装配模式。
	 */
	public int getAutowireMode() {
		return this.autowireMode;
	}

	/**
	 * 返回已解析的自动装配代码（将 AUTOWIRE_AUTODETECT 解析为 AUTOWIRE_CONSTRUCTOR 或 AUTOWIRE_BY_TYPE）。
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// 确定应用 setter 自动装配还是构造函数自动装配
			// 若有无参构造函数则视为 setter 自动装配，否则尝试构造函数自动装配
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterCount() == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		}
		else {
			return this.autowireMode;
		}
	}

	/**
	 * 设置依赖检查代码。
	 * @param dependencyCheck the code to set.
	 * Must be one of the four constants defined in this class.
	 * @see #DEPENDENCY_CHECK_NONE
	 * @see #DEPENDENCY_CHECK_OBJECTS
	 * @see #DEPENDENCY_CHECK_SIMPLE
	 * @see #DEPENDENCY_CHECK_ALL
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * 返回依赖检查代码。
	 */
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is no beans to explicitly depend on.
	 */
	@Override
	public void setDependsOn(String @Nullable ... dependsOn) {
		this.dependsOn = dependsOn;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is no beans to explicitly depend on.
	 */
	@Override
	public String @Nullable [] getDependsOn() {
		return this.dependsOn;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code true}, allowing injection by type at any injection point.
	 * Switch this to {@code false} in order to disable autowiring by type for this bean.
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_BY_NAME
	 */
	@Override
	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code true}.
	 */
	@Override
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}

	/**
	 * 设置本 Bean 是否可作为候选，基于纯类型自动装配到其他 Bean，
	 * 无需进一步指示（如限定符匹配）。
	 * <p>The default is {@code true}, allowing injection by type at any injection point.
	 * Switch this to {@code false} in order to restrict injection by default,
	 * effectively enforcing an additional indication such as a qualifier match.
	 * @since 6.2
	 */
	public void setDefaultCandidate(boolean defaultCandidate) {
		this.defaultCandidate = defaultCandidate;
	}

	/**
	 * 返回本 Bean 是否可作为候选，基于纯类型自动装配到其他 Bean，
	 * 无需进一步指示（如限定符匹配）？
	 * <p>The default is {@code true}.
	 * @since 6.2
	 */
	public boolean isDefaultCandidate() {
		return this.defaultCandidate;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public boolean isPrimary() {
		return this.primary;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public void setFallback(boolean fallback) {
		this.fallback = fallback;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code false}.
	 */
	@Override
	public boolean isFallback() {
		return this.fallback;
	}

	/**
	 * 注册用于自动装配候选解析的限定符，以限定符类型名作为键。
	 * @see AutowireCandidateQualifier#getTypeName()
	 */
	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}

	/**
	 * 返回本 Bean 是否具有指定限定符。
	 */
	public boolean hasQualifier(String typeName) {
		return this.qualifiers.containsKey(typeName);
	}

	/**
	 * 返回映射到所供类型名的限定符。
	 */
	public @Nullable AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}

	/**
	 * 返回所有已注册的限定符。
	 * @return the Set of {@link AutowireCandidateQualifier} objects.
	 */
	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<>(this.qualifiers.values());
	}

	/**
	 * 从所供 AbstractBeanDefinition 复制限定符到本 Bean 定义。
	 * @param source the AbstractBeanDefinition to copy from
	 */
	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "Source must not be null");
		this.qualifiers.putAll(source.qualifiers);
	}

	/**
	 * 指定创建 Bean 实例的回调，作为声明式工厂方法的替代。
	 * <p>If such a callback is set, it will override any other constructor
	 * or factory method metadata. However, bean property population and
	 * potential annotation-driven injection will still apply as usual.
	 * @since 5.0
	 * @see #setConstructorArgumentValues(ConstructorArgumentValues)
	 * @see #setPropertyValues(MutablePropertyValues)
	 */
	public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
		this.instanceSupplier = instanceSupplier;
	}

	/**
	 * 返回创建 Bean 实例的回调（若有）。
	 * @since 5.0
	 */
	public @Nullable Supplier<?> getInstanceSupplier() {
		return this.instanceSupplier;
	}

	/**
	 * 指定是否允许访问非 public 构造函数和方法（外部化元数据指向这些成员时）。
	 * 默认为 {@code true}；切换为 {@code false} 则仅允许 public 访问。
	 * <p>This applies to constructor resolution, factory method resolution,
	 * and also init/destroy methods. Bean property accessors have to be public
	 * in any case and are not affected by this setting.
	 * <p>Note that annotation-driven configuration will still access non-public
	 * members as far as they have been annotated. This setting applies to
	 * externalized metadata in this bean definition only.
	 */
	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}

	/**
	 * 返回是否允许访问非 public 构造函数和方法。
	 */
	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}

	/**
	 * 指定是否在宽松模式（{@code true}，默认）下解析构造函数，
	 * 或切换为严格模式（参数转换时所有匹配构造函数均模糊则抛异常，
	 * 宽松模式则使用类型匹配"最接近"的那个）。
	 */
	public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
		this.lenientConstructorResolution = lenientConstructorResolution;
	}

	/**
	 * 返回是否在宽松或严格模式下解析构造函数。
	 */
	public boolean isLenientConstructorResolution() {
		return this.lenientConstructorResolution;
	}

	/**
	 * {@inheritDoc}
	 * @see #setBeanClass
	 */
	@Override
	public void setFactoryBeanName(@Nullable String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	/**
	 * {@inheritDoc}
	 * @see #getBeanClass()
	 */
	@Override
	public @Nullable String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	/**
	 * {@inheritDoc}
	 * @see RootBeanDefinition#setUniqueFactoryMethodName
	 * @see RootBeanDefinition#setNonUniqueFactoryMethodName
	 * @see RootBeanDefinition#setResolvedFactoryMethod
	 */
	@Override
	public void setFactoryMethodName(@Nullable String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	/**
	 * {@inheritDoc}
	 * @see RootBeanDefinition#getResolvedFactoryMethod()
	 */
	@Override
	public @Nullable String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	/**
	 * 指定本 Bean 的构造函数参数值。
	 */
	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues = constructorArgumentValues;
	}

	/**
	 * {@inheritDoc}
	 * @see #setConstructorArgumentValues
	 */
	@Override
	public ConstructorArgumentValues getConstructorArgumentValues() {
		ConstructorArgumentValues cav = this.constructorArgumentValues;
		if (cav == null) {
			cav = new ConstructorArgumentValues();
			this.constructorArgumentValues = cav;
		}
		return cav;
	}

	/**
	 * {@inheritDoc}
	 * @see #setConstructorArgumentValues
	 */
	@Override
	public boolean hasConstructorArgumentValues() {
		return (this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty());
	}

	/**
	 * 指定本 Bean 的属性值（若有）。
	 */
	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = propertyValues;
	}

	/**
	 * {@inheritDoc}
	 * @see #setPropertyValues
	 */
	@Override
	public MutablePropertyValues getPropertyValues() {
		MutablePropertyValues pvs = this.propertyValues;
		if (pvs == null) {
			pvs = new MutablePropertyValues();
			this.propertyValues = pvs;
		}
		return pvs;
	}

	/**
	 * {@inheritDoc}
	 * @see #setPropertyValues
	 */
	@Override
	public boolean hasPropertyValues() {
		return (this.propertyValues != null && !this.propertyValues.isEmpty());
	}

	/**
	 * 指定 Bean 的方法覆盖（若有）。
	 */
	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = methodOverrides;
	}

	/**
	 * 返回 IoC 容器要覆盖的方法信息。若无方法覆盖则为空。
	 * <p>Never returns {@code null}.
	 */
	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
	}

	/**
	 * 返回本 Bean 是否定义了方法覆盖。
	 * @since 5.0.2
	 */
	public boolean hasMethodOverrides() {
		return !this.methodOverrides.isEmpty();
	}

	/**
	 * 指定多个初始化方法的名称。
	 * <p>The default is {@code null} in which case there are no initializer methods.
	 * @since 6.0
	 * @see #setInitMethodName
	 */
	public void setInitMethodNames(String @Nullable ... initMethodNames) {
		this.initMethodNames = initMethodNames;
	}

	/**
	 * 返回初始化方法的名称。
	 * @since 6.0
	 */
	public String @Nullable [] getInitMethodNames() {
		return this.initMethodNames;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code null} in which case there is no initializer method.
	 * @see #setInitMethodNames
	 */
	@Override
	public void setInitMethodName(@Nullable String initMethodName) {
		this.initMethodNames = (initMethodName != null ? new String[] {initMethodName} : null);
	}

	/**
	 * {@inheritDoc}
	 * <p>Use the first one in case of multiple methods.
	 */
	@Override
	public @Nullable String getInitMethodName() {
		return (!ObjectUtils.isEmpty(this.initMethodNames) ? this.initMethodNames[0] : null);
	}

	/**
	 * 指定配置的初始化方法是否为默认值。
	 * <p>The default value is {@code true} for a locally specified init method
	 * but switched to {@code false} for a shared setting in a defaults section
	 * (for example, {@code bean init-method} versus {@code beans default-init-method}
	 * level in XML) which might not apply to all contained bean definitions.
	 * @see #setInitMethodName
	 * @see #applyDefaults
	 */
	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}

	/**
	 * 指示配置的初始化方法是否为默认值。
	 * @see #getInitMethodName()
	 */
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	/**
	 * 指定多个销毁方法的名称。
	 * <p>The default is {@code null} in which case there are no destroy methods.
	 * @since 6.0
	 * @see #setDestroyMethodName
	 */
	public void setDestroyMethodNames(String @Nullable ... destroyMethodNames) {
		this.destroyMethodNames = destroyMethodNames;
	}

	/**
	 * 返回销毁方法的名称。
	 * @since 6.0
	 */
	public String @Nullable [] getDestroyMethodNames() {
		return this.destroyMethodNames;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@code null} in which case there is no destroy method.
	 * @see #setDestroyMethodNames
	 */
	@Override
	public void setDestroyMethodName(@Nullable String destroyMethodName) {
		this.destroyMethodNames = (destroyMethodName != null ? new String[] {destroyMethodName} : null);
	}

	/**
	 * {@inheritDoc}
	 * <p>Use the first one in case of multiple methods.
	 */
	@Override
	public @Nullable String getDestroyMethodName() {
		return (!ObjectUtils.isEmpty(this.destroyMethodNames) ? this.destroyMethodNames[0] : null);
	}

	/**
	 * 指定配置的销毁方法是否为默认值。
	 * <p>The default value is {@code true} for a locally specified destroy method
	 * but switched to {@code false} for a shared setting in a defaults section
	 * (for example, {@code bean destroy-method} versus {@code beans default-destroy-method}
	 * level in XML) which might not apply to all contained bean definitions.
	 * @see #setDestroyMethodName
	 * @see #applyDefaults
	 */
	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}

	/**
	 * 指示配置的销毁方法是否为默认值。
	 * @see #getDestroyMethodName()
	 */
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}

	/**
	 * 设置本 Bean 定义是否为"合成"，即非应用自身定义
	 * （例如通过 {@code <aop:config>} 创建的自动代理基础设施 Bean）。
	 */
	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	/**
	 * 返回本 Bean 定义是否为"合成"，即非应用自身定义。
	 */
	public boolean isSynthetic() {
		return this.synthetic;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@link #ROLE_APPLICATION}.
	 */
	@Override
	public void setRole(int role) {
		this.role = role;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is {@link #ROLE_APPLICATION}.
	 */
	@Override
	public int getRole() {
		return this.role;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is no description.
	 */
	@Override
	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 * <p>The default is no description.
	 */
	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	/**
	 * 设置本 Bean 定义的来源资源（用于出错时展示上下文）。
	 */
	public void setResource(@Nullable Resource resource) {
		this.resource = resource;
	}

	/**
	 * 返回本 Bean 定义的来源资源。
	 */
	public @Nullable Resource getResource() {
		return this.resource;
	}

	/**
	 * 设置本 Bean 定义来源资源的描述（用于出错时展示上下文）。
	 */
	public void setResourceDescription(@Nullable String resourceDescription) {
		this.resource = (resourceDescription != null ? new DescriptiveResource(resourceDescription) : null);
	}

	/**
	 * {@inheritDoc}
	 * @see #setResourceDescription
	 */
	@Override
	public @Nullable String getResourceDescription() {
		return (this.resource != null ? this.resource.getDescription() : null);
	}

	/**
	 * 设置原始（例如被装饰的）BeanDefinition（若有）。
	 */
	public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
		this.resource = new BeanDefinitionResource(originatingBd);
	}

	/**
	 * {@inheritDoc}
	 * @see #setOriginatingBeanDefinition
	 */
	@Override
	public @Nullable BeanDefinition getOriginatingBeanDefinition() {
		return (this.resource instanceof BeanDefinitionResource bdr ? bdr.getBeanDefinition() : null);
	}

	/**
	 * 验证本 Bean 定义。
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void validate() throws BeanDefinitionValidationException {
		if (hasMethodOverrides() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException(
					"Cannot combine factory method with container-generated method overrides: " +
					"the factory method must create the concrete bean instance.");
		}
		if (hasBeanClass()) {
			prepareMethodOverrides();
		}
	}

	/**
	 * 验证并准备本 Bean 定义的方法覆盖。检查指定名称的方法是否存在。
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		// 检查 lookup 方法是否存在并确定其重载状态
		if (hasMethodOverrides()) {
			getMethodOverrides().getOverrides().forEach(this::prepareMethodOverride);
		}
	}

	/**
	 * 验证并准备给定方法覆盖。检查指定名称的方法是否存在，
	 * 若未找到则标记为非重载。
	 * @param mo the MethodOverride object to validate
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException(
					"Invalid method override: no method with name '" + mo.getMethodName() +
					"' on class [" + getBeanClassName() + "]");
		}
		else if (count == 1) {
			// 标记覆盖为非重载，避免参数类型检查开销
			mo.setOverloaded(false);
		}
	}


	/**
	 * Object {@code clone()} 方法的公开声明。委托给 {@link #cloneBeanDefinition()}。
	 * @see Object#clone()
	 */
	@Override
	public Object clone() {
		return cloneBeanDefinition();
	}

	/**
	 * 克隆本 Bean 定义。由具体子类实现。
	 * @return the cloned bean definition object
	 */
	public abstract AbstractBeanDefinition cloneBeanDefinition();

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AbstractBeanDefinition that &&
				ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName()) &&
				ObjectUtils.nullSafeEquals(this.scope, that.scope) &&
				this.abstractFlag == that.abstractFlag &&
				this.lazyInit == that.lazyInit &&
				this.autowireMode == that.autowireMode &&
				this.dependencyCheck == that.dependencyCheck &&
				Arrays.equals(this.dependsOn, that.dependsOn) &&
				this.autowireCandidate == that.autowireCandidate &&
				ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers) &&
				this.primary == that.primary &&
				this.nonPublicAccessAllowed == that.nonPublicAccessAllowed &&
				this.lenientConstructorResolution == that.lenientConstructorResolution &&
				equalsConstructorArgumentValues(that) &&
				equalsPropertyValues(that) &&
				ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides) &&
				ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName) &&
				ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName) &&
				ObjectUtils.nullSafeEquals(this.initMethodNames, that.initMethodNames) &&
				this.enforceInitMethod == that.enforceInitMethod &&
				ObjectUtils.nullSafeEquals(this.destroyMethodNames, that.destroyMethodNames) &&
				this.enforceDestroyMethod == that.enforceDestroyMethod &&
				this.synthetic == that.synthetic &&
				this.role == that.role &&
				super.equals(other)));
	}

	private boolean equalsConstructorArgumentValues(AbstractBeanDefinition other) {
		if (!hasConstructorArgumentValues()) {
			return !other.hasConstructorArgumentValues();
		}
		return ObjectUtils.nullSafeEquals(this.constructorArgumentValues, other.constructorArgumentValues);
	}

	private boolean equalsPropertyValues(AbstractBeanDefinition other) {
		if (!hasPropertyValues()) {
			return !other.hasPropertyValues();
		}
		return ObjectUtils.nullSafeEquals(this.propertyValues, other.propertyValues);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
		if (hasConstructorArgumentValues()) {
			hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
		}
		if (hasPropertyValues()) {
			hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
		}
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
		hashCode = 29 * hashCode + super.hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class=").append(getBeanClassName());
		sb.append("; scope=").append(this.scope);
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowireMode=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; autowireCandidate=").append(this.autowireCandidate);
		sb.append("; primary=").append(this.primary);
		sb.append("; fallback=").append(this.fallback);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodNames=").append(Arrays.toString(this.initMethodNames));
		sb.append("; destroyMethodNames=").append(Arrays.toString(this.destroyMethodNames));
		if (this.resource != null) {
			sb.append("; defined in ").append(this.resource.getDescription());
		}
		return sb.toString();
	}

}
