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

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用 Spring 异步方法执行能力，类似于 Spring {@code <task:*>} XML 命名空间中的功能。
 *
 * <p>与 @{@link Configuration Configuration} 类配合使用，
 * 为整个 Spring 应用上下文启用注解驱动的异步处理：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig {
 *
 * }</pre>
 *
 * {@code MyAsyncBean} 为用户定义类型，其一个或多个方法标注 Spring {@code @Async}、
 * EJB 3.1 {@code @jakarta.ejb.Asynchronous} 或通过 {@link #annotation} 属性指定的自定义注解。
 * 切面对任何已注册 Bean 透明添加，例如通过以下配置：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AnotherAppConfig {
 *
 *     &#064;Bean
 *     public MyAsyncBean asyncBean() {
 *         return new MyAsyncBean();
 *     }
 * }</pre>
 *
 * <p>默认情况下，Spring 将查找关联的线程池定义：
 * 上下文中唯一的 {@link org.springframework.core.task.TaskExecutor} Bean，
 * 否则名为 "taskExecutor" 的 {@link java.util.concurrent.Executor} Bean。
 * 若两者均不可解析，将使用 {@link org.springframework.core.task.SimpleAsyncTaskExecutor}
 * 处理异步方法调用。此外，{@code void} 返回类型的标注方法无法将异常传回调用方，
 * 默认仅记录此类未捕获异常。
 *
 * <p>要自定义上述行为，实现 {@link AsyncConfigurer} 并提供：
 * <ul>
 * <li>通过 {@link AsyncConfigurer#getAsyncExecutor getAsyncExecutor()} 方法
 * 提供自定义 {@link java.util.concurrent.Executor Executor}，以及</li>
 * <li>通过 {@link AsyncConfigurer#getAsyncUncaughtExceptionHandler
 * getAsyncUncaughtExceptionHandler()} 方法提供自定义
 * {@link org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
 * AsyncUncaughtExceptionHandler}。</li>
 * </ul>
 *
 * <p><b>注意：{@link AsyncConfigurer} 配置类在应用上下文引导阶段较早初始化。
 * 若需依赖其他 Bean，请尽可能声明为 lazy，以便它们也能经过其他后处理器。</b>
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig implements AsyncConfigurer {
 *
 *     &#064;Override
 *     public Executor getAsyncExecutor() {
 *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *         executor.setCorePoolSize(7);
 *         executor.setMaxPoolSize(42);
 *         executor.setQueueCapacity(11);
 *         executor.setThreadNamePrefix("MyExecutor-");
 *         executor.initialize();
 *         return executor;
 *     }
 *
 *     &#064;Override
 *     public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
 *         return new MyAsyncUncaughtExceptionHandler();
 *     }
 * }</pre>
 *
 * <p>若只需自定义其中一项，可返回 {@code null} 以保持默认设置。
 *
 * <p>注意：上例中 {@code ThreadPoolTaskExecutor} 并非完全受管的 Spring Bean。
 * 若需要完全受管 Bean，请在 {@code getAsyncExecutor()} 方法上添加 {@code @Bean} 注解。
 * 此时无需手动调用 {@code executor.initialize()}，Bean 初始化时将自动调用。
 *
 * <p>作为参考，上例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;task:annotation-driven executor="myExecutor" exception-handler="exceptionHandler"/&gt;
 *
 *     &lt;task:executor id="myExecutor" pool-size="7-42" queue-capacity="11"/&gt;
 *
 *     &lt;bean id="asyncBean" class="com.foo.MyAsyncBean"/&gt;
 *
 *     &lt;bean id="exceptionHandler" class="com.foo.MyAsyncUncaughtExceptionHandler"/&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * 上述基于 XML 与 JavaConfig 的示例等价，仅 {@code Executor} 的<em>线程名前缀</em>设置不同；
 * 因为 {@code <task:executor>} 元素未暴露该属性。
 * 这展示了 JavaConfig 方式通过直接访问实际组件实现最大可配置性。
 *
 * <p>{@link #mode} 属性控制通知如何应用：若模式为 {@link AdviceMode#PROXY}（默认），
 * 则其他属性控制代理行为。请注意代理模式仅拦截通过代理的调用；
 * 同类内部本地调用无法被拦截。
 *
 * <p>若 {@linkplain #mode} 设为 {@link AdviceMode#ASPECTJ}，
 * 则 {@link #proxyTargetClass} 属性值将被忽略。
 * 此时类路径上须有 {@code spring-aspects} 模块 JAR，
 * 并通过编译时或加载时织入将切面应用于受影响类。
 * 此场景不涉及代理，本地调用也会被拦截。
 *
 * <p><b>注意：{@code @EnableAsync} 仅作用于其本地应用上下文，
 * 允许在不同层级选择性启用。</b> 若需在多个层级应用其行为，
 * 请在各独立上下文中重新声明 {@code @EnableAsync}，
 * 例如公共根 Web 应用上下文及独立的 {@code DispatcherServlet} 应用上下文。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see Async
 * @see AsyncConfigurer
 * @see AsyncConfigurationSelector
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncConfigurationSelector.class)
public @interface EnableAsync {

	/**
	 * 指定在类或方法级别检测的“异步”注解类型。
	 * <p>默认检测 Spring @{@link Async} 注解及 EJB 3.1 {@code @jakarta.ejb.Asynchronous} 注解。
	 * <p>此属性供开发者提供自定义注解类型，
	 * 以指示方法（或给定类的全部方法）应异步调用。
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

	/**
	 * 指示是否创建基于子类（CGLIB）的代理，而非标准 Java 接口代理。
	 * <p><strong>仅当 {@link #mode} 设为 {@link AdviceMode#PROXY} 时适用。</strong>
	 * <p>默认为 {@code false}。
	 * <p>将此属性设为 {@code true} 仅影响 {@link AsyncAnnotationBeanPostProcessor}。
	 * <p>通常建议依赖全局默认代理配置，
	 * 对特定 Bean 的代理需求通过受影响 Bean 类上的
	 * {@link org.springframework.context.annotation.Proxyable} 注解表达。
	 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 指示如何应用异步通知。
	 * <p><b>默认为 {@link AdviceMode#PROXY}。</b>
	 * 请注意代理模式仅拦截通过代理的调用；同类本地调用无法被拦截，
	 * 本地调用中此类方法上的 {@link Async} 注解将被忽略，
	 * 因为 Spring 拦截器在此运行时场景下不会生效。
	 * 如需更高级的拦截模式，可考虑切换为 {@link AdviceMode#ASPECTJ}。
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 指示 {@link AsyncAnnotationBeanPostProcessor} 的应用顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}，以便在所有其他后处理器之后运行，
	 * 从而向现有代理添加 Advisor 而非双重代理。
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

}
