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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.concurrent.Executor;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.util.StringValueResolver;

/**
 * 大多数 bean 工厂应实现的配置接口。
 * 除 {@link org.springframework.beans.factory.BeanFactory} 接口中的
 * bean 工厂客户端方法外，还提供配置 bean 工厂的设施。
 *
 * <p>此 bean 工厂接口不适用于普通应用代码：典型场景请使用
 * {@link org.springframework.beans.factory.BeanFactory} 或
 * {@link org.springframework.beans.factory.ListableBeanFactory}。
 * 此扩展接口仅用于框架内部即插即用，以及特殊访问 bean 工厂配置方法。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	/**
	 * 标准单例作用域的标识符：{@value}。
	 * <p>可通过 {@code registerScope} 添加自定义作用域。
	 * @see #registerScope
	 */
	String SCOPE_SINGLETON = "singleton";

	/**
	 * 标准原型作用域的标识符：{@value}。
	 * <p>可通过 {@code registerScope} 添加自定义作用域。
	 * @see #registerScope
	 */
	String SCOPE_PROTOTYPE = "prototype";


	/**
	 * 设置此 bean 工厂的父工厂。
	 * <p>注意父工厂不能更改：若工厂实例化时不可用，应仅在构造函数外设置。
	 * @param parentBeanFactory 父 BeanFactory
	 * @throws IllegalStateException 若此工厂已关联父 BeanFactory
	 * @see #getParentBeanFactory()
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	/**
	 * 设置用于加载 bean 类的类加载器。
	 * 默认为线程上下文类加载器。
	 * <p>注意此类加载器仅适用于尚未携带已解析 bean 类的 bean 定义。
	 * 自 Spring 2.0 起默认如此：bean 定义仅携带 bean 类名，
	 * 在工厂处理 bean 定义时解析。
	 * @param beanClassLoader 要使用的类加载器，或 {@code null} 表示使用默认类加载器
	 */
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

	/**
	 * 返回此工厂用于加载 bean 类的类加载器
	 * （仅当系统 ClassLoader 也不可访问时才为 {@code null}）。
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable ClassLoader getBeanClassLoader();

	/**
	 * 指定用于类型匹配目的的临时 ClassLoader。
	 * 默认为无，仅使用标准 bean ClassLoader。
	 * <p>通常仅在涉及<i>加载时织入</i>时指定临时 ClassLoader，
	 * 以确保实际 bean 类尽可能延迟加载。临时加载器在 BeanFactory 完成引导阶段后移除。
	 * @since 2.5
	 */
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

	/**
	 * 返回用于类型匹配目的的临时 ClassLoader（如有）。
	 * @since 2.5
	 */
	@Nullable ClassLoader getTempClassLoader();

	/**
	 * 设置是否缓存 bean 元数据（如给定的 bean 定义（合并后）和已解析的 bean 类）。
	 * 默认为开启。
	 * <p>关闭此标志以启用 bean 定义对象（特别是 bean 类）的热刷新。
	 * 若此标志关闭，任何 bean 实例的创建都将重新查询 bean 类加载器以获取新解析的类。
	 */
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	/**
	 * 返回是否缓存 bean 元数据（如给定的 bean 定义（合并后）和已解析的 bean 类）。
	 */
	boolean isCacheBeanMetadata();

	/**
	 * 指定 bean 定义值中表达式的解析策略。
	 * <p>BeanFactory 默认不激活表达式支持。
	 * ApplicationContext 通常会在此设置标准表达式策略，
	 * 支持 Unified EL 兼容风格的 "#{...}" 表达式。
	 * @since 3.0
	 */
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

	/**
	 * 返回 bean 定义值中表达式的解析策略。
	 * @since 3.0
	 */
	@Nullable BeanExpressionResolver getBeanExpressionResolver();

	/**
	 * 设置用于后台引导的 {@link Executor}（可能是 {@link org.springframework.core.task.TaskExecutor}）。
	 * @since 6.2
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setBackgroundInit
	 */
	void setBootstrapExecutor(@Nullable Executor executor);

	/**
	 * 返回用于后台引导的 {@link Executor}（可能是 {@link org.springframework.core.task.TaskExecutor}）（如有）。
	 * @since 6.2
	 */
	@Nullable Executor getBootstrapExecutor();

	/**
	 * 指定用于转换属性值的 {@link ConversionService}，作为 JavaBeans PropertyEditor 的替代。
	 * @since 3.0
	 */
	void setConversionService(@Nullable ConversionService conversionService);

	/**
	 * 返回关联的 ConversionService（如有）。
	 * @since 3.0
	 */
	@Nullable ConversionService getConversionService();

	/**
	 * 添加要应用于所有 bean 创建过程的 PropertyEditorRegistrar。
	 * <p>此类注册器为每次 bean 创建尝试创建新的 PropertyEditor 实例并注册到给定注册表，
	 * 避免自定义编辑器同步的需要；因此通常优先使用此方法而非 {@link #registerCustomEditor}。
	 * <p>若给定注册器实现
	 * {@link PropertyEditorRegistrar#overridesDefaultEditors()} 返回 {@code true}，
	 * 将延迟应用（仅在实际需要默认编辑器时）。
	 * @param registrar 要注册的 PropertyEditorRegistrar
	 * @see PropertyEditorRegistrar#overridesDefaultEditors()
	 */
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	/**
	 * 为给定类型的所有属性注册自定义属性编辑器。
	 * 在工厂配置期间调用。
	 * <p>注意此方法将注册共享的自定义编辑器实例；
	 * 对该实例的访问将同步以保证线程安全。通常优先使用
	 * {@link #addPropertyEditorRegistrar} 而非此方法，以避免自定义编辑器同步的需要。
	 * @param requiredType 属性类型
	 * @param propertyEditorClass 要注册的 {@link PropertyEditor} 类
	 */
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	/**
	 * 使用已注册到此 BeanFactory 的自定义编辑器初始化给定 PropertyEditorRegistry。
	 * @param registry 要初始化的 PropertyEditorRegistry
	 */
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	/**
	 * 设置此 BeanFactory 用于转换 bean 属性值、构造器参数值等的自定义类型转换器。
	 * <p>这将覆盖默认 PropertyEditor 机制，从而使任何自定义编辑器或自定义编辑器注册器失效。
	 * @since 2.5
	 * @see #addPropertyEditorRegistrar
	 * @see #registerCustomEditor
	 */
	void setTypeConverter(TypeConverter typeConverter);

	/**
	 * 获取此 BeanFactory 使用的类型转换器。通常每次调用返回新实例，
	 * 因为 TypeConverter 通常<i>不是</i>线程安全的。
	 * <p>若默认 PropertyEditor 机制处于活动状态，返回的 TypeConverter 将感知所有已注册的自定义编辑器。
	 * @since 2.5
	 */
	TypeConverter getTypeConverter();

	/**
	 * 添加用于嵌入式值（如注解属性）的字符串解析器。
	 * @param valueResolver 要应用于嵌入式值的字符串解析器
	 * @since 3.0
	 */
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * 判断是否已注册嵌入式值解析器，通过 {@link #resolveEmbeddedValue(String)} 应用。
	 * @since 4.3
	 */
	boolean hasEmbeddedValueResolver();

	/**
	 * 解析给定嵌入式值，例如注解属性。
	 * @param value 要解析的值
	 * @return 解析后的值（可能原样返回原始值）
	 * @since 3.0
	 */
	@Nullable String resolveEmbeddedValue(String value);

	/**
	 * 添加将应用于由此工厂创建的 bean 的新 BeanPostProcessor。
	 * 在工厂配置期间调用。
	 * <p>注意：此处提交的后处理器将按注册顺序应用；
	 * 通过实现 {@link org.springframework.core.Ordered} 接口表达的排序语义将被忽略。
	 * 注意自动检测的后处理器（例如 ApplicationContext 中的 bean）
	 * 将始终在编程注册的后处理器之后应用。
	 * @param beanPostProcessor 要注册的后处理器
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * 返回当前已注册的 BeanPostProcessor 数量（如有）。
	 */
	int getBeanPostProcessorCount();

	/**
	 * 注册给定作用域，由给定 Scope 实现支持。
	 * @param scopeName 作用域标识符
	 * @param scope 支持的 Scope 实现
	 */
	void registerScope(String scopeName, Scope scope);

	/**
	 * 返回所有当前已注册作用域的名称。
	 * <p>仅返回显式注册的作用域名称。
	 * 内置作用域如 "singleton" 和 "prototype" 不会暴露。
	 * @return 作用域名称数组，无则为空数组
	 * @see #registerScope
	 */
	String[] getRegisteredScopeNames();

	/**
	 * 返回给定作用域名称的 Scope 实现（如有）。
	 * <p>仅返回显式注册的作用域。
	 * 内置作用域如 "singleton" 和 "prototype" 不会暴露。
	 * @param scopeName 作用域名称
	 * @return 已注册的 Scope 实现，无则为 {@code null}
	 * @see #registerScope
	 */
	@Nullable Scope getRegisteredScope(String scopeName);

	/**
	 * 设置此 bean 工厂的 {@code ApplicationStartup}。
	 * <p>这允许应用上下文在应用启动期间记录指标。
	 * @param applicationStartup 新的应用启动器
	 * @since 5.3
	 */
	void setApplicationStartup(ApplicationStartup applicationStartup);

	/**
	 * 返回此 bean 工厂的 {@code ApplicationStartup}。
	 * @since 5.3
	 */
	ApplicationStartup getApplicationStartup();

	/**
	 * 从给定其他工厂复制所有相关配置。
	 * <p>应包括所有标准配置设置以及 BeanPostProcessor、Scope 和工厂特定的内部设置。
	 * 不应包括实际 bean 定义的元数据，如 BeanDefinition 对象和 bean 名称别名。
	 * @param otherFactory 要复制来源的其他 BeanFactory
	 */
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	/**
	 * 给定 bean 名称，创建别名。通常用此方法支持 XML id 中非法的名称（用于 bean 名称）。
	 * <p>通常在工厂配置期间调用，但也可用于运行时注册别名。
	 * 因此，工厂实现应同步别名访问。
	 * @param beanName 目标 bean 的规范名称
	 * @param alias 要为 bean 注册的别名
	 * @throws BeanDefinitionStoreException 若别名已被使用
	 */
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	/**
	 * 解析此工厂中注册的所有别名目标名称和别名，对它们应用给定 StringValueResolver。
	 * <p>值解析器可例如解析目标 bean 名称甚至别名名称中的占位符。
	 * @param valueResolver 要应用的 StringValueResolver
	 * @since 2.5
	 */
	void resolveAliases(StringValueResolver valueResolver);

	/**
	 * 返回给定 bean 名称的合并 BeanDefinition，必要时将子 bean 定义与父定义合并。
	 * 也会考虑祖先工厂中的 bean 定义。
	 * @param beanName 要检索合并定义的 bean 名称
	 * @return 给定 bean 的（可能合并的）BeanDefinition
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 bean 定义
	 * @since 2.5
	 */
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 判断给定名称的 bean 是否为 FactoryBean。
	 * @param name 要检查的 bean 名称
	 * @return bean 是否为 FactoryBean
	 * （{@code false} 表示 bean 存在但不是 FactoryBean）
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 bean
	 * @since 2.5
	 */
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 显式控制指定 bean 的当前创建中状态。仅供容器内部使用。
	 * @param beanName bean 的名称
	 * @param inCreation bean 是否当前正在创建中
	 * @since 3.1
	 */
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	/**
	 * 判断指定 bean 是否当前正在创建中。
	 * @param beanName bean 的名称
	 * @return bean 是否当前正在创建中
	 * @since 2.5
	 */
	boolean isCurrentlyInCreation(String beanName);

	/**
	 * 为给定 bean 注册依赖 bean，在销毁给定 bean 之前销毁依赖 bean。
	 * @param beanName bean 的名称
	 * @param dependentBeanName 依赖 bean 的名称
	 * @since 2.5
	 */
	void registerDependentBean(String beanName, String dependentBeanName);

	/**
	 * 返回所有依赖指定 bean 的 bean 名称（如有）。
	 * @param beanName bean 的名称
	 * @return 依赖 bean 名称数组，无则为空数组
	 * @since 2.5
	 */
	String[] getDependentBeans(String beanName);

	/**
	 * 返回指定 bean 依赖的所有 bean 名称（如有）。
	 * @param beanName bean 的名称
	 * @return bean 依赖的 bean 名称数组，无则为空数组
	 * @since 2.5
	 */
	String[] getDependenciesForBean(String beanName);

	/**
	 * 根据其 bean 定义销毁给定 bean 实例（通常是从此工厂获取的原型实例）。
	 * <p>销毁过程中产生的任何异常应被捕获并记录，而非传播给此方法的调用者。
	 * @param beanName bean 定义的名称
	 * @param beanInstance 要销毁的 bean 实例
	 */
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * 销毁当前目标作用域中指定的作用域 bean（如有）。
	 * <p>销毁过程中产生的任何异常应被捕获并记录，而非传播给此方法的调用者。
	 * @param beanName 作用域 bean 的名称
	 */
	void destroyScopedBean(String beanName);

	/**
	 * 销毁此工厂中所有单例 bean，包括已注册为可销毁的内部 bean。
	 * 在工厂关闭时调用。
	 * <p>销毁过程中产生的任何异常应被捕获并记录，而非传播给此方法的调用者。
	 */
	void destroySingletons();

}
