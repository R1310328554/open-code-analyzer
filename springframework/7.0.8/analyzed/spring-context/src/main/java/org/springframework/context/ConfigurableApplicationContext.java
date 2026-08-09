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

package org.springframework.context;

import java.io.Closeable;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.metrics.ApplicationStartup;

/**
 * SPI 接口，绝大多数应用上下文应实现此接口。
 * 除 {@link org.springframework.context.ApplicationContext} 中的
 * 应用上下文客户端方法外，还提供配置应用上下文的设施。
 *
 * <p>配置与生命周期方法封装于此，以免对 ApplicationContext 客户端代码过于显眼。
 * 本接口中的方法仅应由启动与关闭代码使用。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {

	/**
	 * 单个 {@code String} 值中多个上下文配置路径之间的分隔符
	 * （任意数量均可）：{@value}。
	 * @see org.springframework.context.support.AbstractXmlApplicationContext#setConfigLocation
	 * @see org.springframework.web.context.ContextLoader#CONFIG_LOCATION_PARAM
	 * @see org.springframework.web.servlet.FrameworkServlet#setContextConfigLocation
	 */
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	/**
	 * 上下文中 {@linkplain java.util.concurrent.Executor 引导执行器}
	 * Bean 的名称：{@value}。
	 * <p>若未提供，则不会启用后台引导。
	 * @since 6.2
	 * @see java.util.concurrent.Executor
	 * @see org.springframework.core.task.TaskExecutor
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setBootstrapExecutor
	 */
	String BOOTSTRAP_EXECUTOR_BEAN_NAME = "bootstrapExecutor";

	/**
	 * 工厂中 {@code ConversionService} Bean 的名称：{@value}。
	 * <p>若未提供，则应用默认转换规则。
	 * @since 3.0
	 * @see org.springframework.core.convert.ConversionService
	 */
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	/**
	 * 工厂中 {@code LoadTimeWeaver} Bean 的名称：{@value}。
	 * <p>若提供了此类 Bean，上下文将使用临时 {@link ClassLoader}
	 * 进行类型匹配，以便 {@code LoadTimeWeaver} 处理所有实际 Bean 类。
	 * @since 2.5
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver
	 */
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";

	/**
	 * 工厂中 {@link org.springframework.core.env.Environment Environment}
	 * Bean 的名称：{@value}。
	 * @since 3.1
	 */
	String ENVIRONMENT_BEAN_NAME = "environment";

	/**
	 * 工厂中 JVM 系统属性 Bean 的名称：{@value}。
	 * @see java.lang.System#getProperties()
	 */
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";

	/**
	 * 工厂中操作系统环境 Bean 的名称：{@value}。
	 * @see java.lang.System#getenv()
	 */
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";

	/**
	 * 工厂中 {@link ApplicationStartup} Bean 的名称：{@value}。
	 * @since 5.3
	 */
	String APPLICATION_STARTUP_BEAN_NAME = "applicationStartup";

	/**
	 * {@linkplain #registerShutdownHook() 关闭钩子}线程的
	 * {@linkplain Thread#getName() 名称}：{@value}。
	 * @since 5.2
	 * @see #registerShutdownHook()
	 */
	String SHUTDOWN_HOOK_THREAD_NAME = "SpringContextShutdownHook";


	/**
	 * 设置本应用上下文的唯一 ID。
	 * @since 3.0
	 */
	void setId(String id);

	/**
	 * 设置本应用上下文的父级。
	 * <p>注意不应更改父级：仅当创建本类对象时父级尚不可用
	 * （例如 WebApplicationContext 设置场景）时，才应在构造器外设置。
	 * @param parent the parent context
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(@Nullable ApplicationContext parent);

	/**
	 * 为本应用上下文设置 {@code Environment}。
	 * @param environment the new environment
	 * @since 3.1
	 */
	void setEnvironment(ConfigurableEnvironment environment);

	/**
	 * 以可配置形式返回本应用上下文的 {@code Environment}，允许进一步定制。
	 * @since 3.1
	 */
	@Override
	ConfigurableEnvironment getEnvironment();

	/**
	 * 为本应用上下文设置 {@link ApplicationStartup}。
	 * <p>这使应用上下文能在启动期间记录指标。
	 * @param applicationStartup the new context event factory
	 * @since 5.3
	 */
	void setApplicationStartup(ApplicationStartup applicationStartup);

	/**
	 * 返回本应用上下文的 {@link ApplicationStartup}。
	 * @since 5.3
	 */
	ApplicationStartup getApplicationStartup();

	/**
	 * 添加新的 BeanFactoryPostProcessor，在刷新时于任何 Bean 定义求值之前
	 * 应用于本应用上下文的内部 Bean 工厂。应在上下文配置期间调用。
	 * @param postProcessor the factory processor to register
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

	/**
	 * 添加新的 ApplicationListener，在上下文刷新、关闭等事件时收到通知。
	 * <p>注意：此处注册的 ApplicationListener 若上下文尚未激活，
	 * 将在刷新时应用；若上下文已激活，则通过当前事件多播器即时应用。
	 * @param listener the ApplicationListener to register
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * 从本上下文的监听器集合中移除给定 ApplicationListener，
	 * 假定其此前已通过 {@link #addApplicationListener} 注册。
	 * @param listener the ApplicationListener to deregister
	 * @since 6.0
	 */
	void removeApplicationListener(ApplicationListener<?> listener);

	/**
	 * 指定用于加载类路径资源和 Bean 类的 ClassLoader。
	 * <p>此上下文类加载器将传递给内部 Bean 工厂。
	 * @since 5.2.7
	 * @see org.springframework.core.io.DefaultResourceLoader#DefaultResourceLoader(ClassLoader)
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setBeanClassLoader
	 */
	void setClassLoader(ClassLoader classLoader);

	/**
	 * 向本应用上下文注册给定协议解析器，以支持额外的资源协议处理。
	 * <p>此类解析器将在本上下文标准解析规则之前调用，
	 * 因此也可覆盖任何默认规则。
	 * @since 4.3
	 */
	void addProtocolResolver(ProtocolResolver resolver);

	/**
	 * 加载或刷新配置的持久表示，可能来自 Java 配置、XML 文件、
	 * 属性文件、关系数据库模式或其他格式。
	 * <p>作为启动方法，若失败应销毁已创建的单例，以避免悬空资源。
	 * 换言之，调用本方法后，应要么全部实例化单例，要么一个都不实例化。
	 * @throws BeansException if the bean factory could not be initialized
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * 必要时暂停本应用上下文中的所有 Bean，随后重启所有自动启动 Bean，
	 * 从而在 {@link #refresh()} 之后（通常在先前调用 {@link #pause()} 且
	 * 需避免对惰性启动 Bean 执行完整 {@link #start()} 时）恢复生命周期状态。
	 * @since 7.0
	 * @see #pause()
	 * @see #start()
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void restart();

	/**
	 * 停止本应用上下文中的所有 Bean，除非其通过
	 * {@link SmartLifecycle#isPauseable()} 返回 {@code false} 明确拒绝暂停。
	 * @since 7.0
	 * @see #restart()
	 * @see #stop()
	 * @see SmartLifecycle#isPauseable()
	 */
	void pause();

	/**
	 * 向 JVM 运行时注册关闭钩子，在 JVM 关闭时关闭本上下文
	 * （除非届时已关闭）。
	 * <p>可多次调用。每个上下文实例最多注册一个关闭钩子。
	 * <p>关闭钩子线程的 {@linkplain Thread#getName() 名称}
	 * 应为 {@link #SHUTDOWN_HOOK_THREAD_NAME}。
	 * @see java.lang.Runtime#addShutdownHook
	 * @see #close()
	 */
	void registerShutdownHook();

	/**
	 * 关闭本应用上下文，释放实现可能持有的全部资源与锁。
	 * 包括销毁所有缓存的单例 Bean。
	 * <p>注意：不会对父上下文调用 {@code close}；
	 * 父上下文拥有各自独立的生命周期。
	 * <p>可多次调用而无副作用：对已关闭上下文再次调用 {@code close} 将被忽略。
	 */
	@Override
	void close();

	/**
	 * 返回本上下文是否已关闭，即是否已在活动上下文上调用
	 * {@link #close()} 以启动关闭流程。
	 * <p>注意：这不表示上下文关闭是否已完成。
	 * 使用 {@link #isActive()} 区分这两种情况：上下文在完全关闭且
	 * 原始 {@code close()} 调用返回后变为非活动。
	 * @since 6.2
	 */
	boolean isClosed();

	/**
	 * 判断本应用上下文是否处于活动状态，即是否至少刷新过一次且尚未关闭。
	 * @return whether the context is still active
	 * @see #refresh()
	 * @see #close()
	 * @see #getBeanFactory()
	 */
	boolean isActive();

	/**
	 * 返回本应用上下文的内部 Bean 工厂。
	 * 可用于访问底层工厂的特定功能。
	 * <p>注意：不要用此方法后处理 Bean 工厂；单例在调用前通常已实例化。
	 * 应使用 BeanFactoryPostProcessor 在 Bean 被触及之前拦截 BeanFactory 设置过程。
	 * <p>通常，此内部工厂仅在上下文处于活动状态时（即 {@link #refresh()}
	 * 与 {@link #close()} 之间）可访问。可用 {@link #isActive()} 检查
	 * 上下文是否处于适当状态。
	 * @return the underlying bean factory
	 * @throws IllegalStateException if the context does not hold an internal
	 * bean factory (usually if {@link #refresh()} hasn't been called yet or
	 * if {@link #close()} has already been called)
	 * @see #isActive()
	 * @see #refresh()
	 * @see #close()
	 * @see #addBeanFactoryPostProcessor
	 */
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}
