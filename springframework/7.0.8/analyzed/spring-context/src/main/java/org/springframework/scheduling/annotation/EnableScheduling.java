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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 启用 Spring 定时任务执行能力，类似于 Spring {@code <task:*>} XML 命名空间中的功能。
 * 在 {@link Configuration @Configuration} 类上按如下方式使用：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * public class AppConfig {
 *
 *     // various &#064;Bean definitions
 * }</pre>
 *
 * <p>这将在容器中任何 Spring 管理的 Bean 上检测 {@link Scheduled @Scheduled} 注解。
 * 例如给定类 {@code MyTask}：
 *
 * <pre class="code">
 * package com.myco.tasks;
 *
 * public class MyTask {
 *
 *     &#064;Scheduled(fixedRate=1000)
 *     public void work() {
 *         // task execution logic
 *     }
 * }</pre>
 *
 * <p>以下配置将确保每 1000 ms 调用一次 {@code MyTask.work()}：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyTask task() {
 *         return new MyTask();
 *     }
 * }</pre>
 *
 * <p>或者，若 {@code MyTask} 标注 {@code @Component}，
 * 以下配置将确保其 {@code @Scheduled} 方法按期望间隔调用：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * &#064;ComponentScan(basePackages="com.myco.tasks")
 * public class AppConfig {
 * }</pre>
 *
 * <p>标注 {@code @Scheduled} 的方法甚至可直接在 {@code @Configuration} 类中声明：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * public class AppConfig {
 *
 *     &#064;Scheduled(fixedRate=1000)
 *     public void work() {
 *         // task execution logic
 *     }
 * }</pre>
 *
 * <p>默认情况下，Spring 将查找关联的调度器定义：
 * 上下文中唯一的 {@link org.springframework.scheduling.TaskScheduler} Bean，
 * 否则名为 "taskScheduler" 的 {@code TaskScheduler} Bean；
 * 对 {@link java.util.concurrent.ScheduledExecutorService} Bean 也执行相同查找。
 * 若两者均不可解析，将在注册器内创建并使用本地单线程默认调度器。
 *
 * <p>需要更多控制时，{@code @Configuration} 类可实现 {@link SchedulingConfigurer}。
 * 这允许访问底层 {@link ScheduledTaskRegistrar} 实例。
 * 例如以下示例演示如何自定义执行定时任务的 {@link Executor}：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * public class AppConfig implements SchedulingConfigurer {
 *
 *     &#064;Override
 *     public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
 *         taskRegistrar.setScheduler(taskExecutor());
 *     }
 *
 *     &#064;Bean(destroyMethod="shutdown")
 *     public Executor taskExecutor() {
 *         return Executors.newScheduledThreadPool(100);
 *     }
 * }</pre>
 *
 * <p>注意上例使用 {@code @Bean(destroyMethod="shutdown")}，
 * 确保 Spring 应用上下文关闭时任务执行器正确关闭。
 *
 * <p>实现 {@code SchedulingConfigurer} 还可通过 {@code ScheduledTaskRegistrar}
 * 精细控制任务注册。例如以下配置按自定义 {@code Trigger} 实现
 * 执行特定 Bean 方法：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableScheduling
 * public class AppConfig implements SchedulingConfigurer {
 *
 *     &#064;Override
 *     public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
 *         taskRegistrar.setScheduler(taskScheduler());
 *         taskRegistrar.addTriggerTask(
 *             () -&gt; myTask().work(),
 *             new CustomTrigger()
 *         );
 *     }
 *
 *     &#064;Bean(destroyMethod="shutdown")
 *     public Executor taskScheduler() {
 *         return Executors.newScheduledThreadPool(42);
 *     }
 *
 *     &#064;Bean
 *     public MyTask myTask() {
 *         return new MyTask();
 *     }
 * }</pre>
 *
 * <p>作为参考，上例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;task:annotation-driven scheduler="taskScheduler"/&gt;
 *
 *     &lt;task:scheduler id="taskScheduler" pool-size="42"/&gt;
 *
 *     &lt;task:scheduled-tasks scheduler="taskScheduler"&gt;
 *         &lt;task:scheduled ref="myTask" method="work" fixed-rate="1000"/&gt;
 *     &lt;/task:scheduled-tasks&gt;
 *
 *     &lt;bean id="myTask" class="com.foo.MyTask"/&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>示例等价，仅 XML 使用<em>固定速率</em>周期而非自定义<em>{@code Trigger}</em> 实现；
 * 因为 {@code task:} 命名空间的 {@code scheduled} 不易暴露此类支持。
 * 这展示了基于代码的方式通过直接访问实际组件实现最大可配置性。
 *
 * <p><b>注意：{@code @EnableScheduling} 仅作用于其本地应用上下文，
 * 允许在不同层级选择性调度 Bean。</b> 若需在多个层级应用其行为，
 * 请在各独立上下文中重新声明 {@code @EnableScheduling}，
 * 例如公共根 Web 应用上下文及独立的 {@code DispatcherServlet} 应用上下文。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Scheduled
 * @see SchedulingConfiguration
 * @see SchedulingConfigurer
 * @see ScheduledTaskRegistrar
 * @see Trigger
 * @see ScheduledAnnotationBeanPostProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SchedulingConfiguration.class)
@Documented
public @interface EnableScheduling {

}
