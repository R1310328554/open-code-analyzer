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

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用 Spring 注解驱动的事务管理能力，类似于 Spring {@code <tx:*>} XML 命名空间中的支持。
 * 用于 {@link org.springframework.context.annotation.Configuration @Configuration}
 * 类，以配置传统命令式事务管理或响应式事务管理。
 *
 * <p>以下示例演示使用
 * {@link org.springframework.transaction.PlatformTransactionManager
 * PlatformTransactionManager} 的命令式事务管理。
 * 响应式事务管理请改为配置
 * {@link org.springframework.transaction.ReactiveTransactionManager
 * ReactiveTransactionManager}。
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 * }</pre>
 *
 * <p>作为参考，上述示例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;tx:annotation-driven/&gt;
 *
 *     &lt;bean id="fooRepository" class="com.foo.JdbcFooRepository"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id="dataSource" class="com.vendor.VendorDataSource"/&gt;
 *
 *     &lt;bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * 在上述两种场景中，{@code @EnableTransactionManagement} 和 {@code
 * <tx:annotation-driven/>} 负责注册支撑注解驱动事务管理所需的 Spring 组件，
 * 例如 TransactionInterceptor 以及基于代理或 AspectJ 的通知，
 * 在调用 {@code JdbcFooRepository} 的 {@code @Transactional} 方法时
 * 将拦截器织入调用栈。
 *
 * <p>两示例的细微差别在于 {@code TransactionManager} Bean 的命名：
 * {@code @Bean} 情形下名称为 <em>"txManager"</em>（按方法名）；
 * XML 情形下为 <em>"transactionManager"</em>。
 * {@code <tx:annotation-driven/>} 默认硬编码查找名为 "transactionManager" 的 Bean，
 * 而 {@code @EnableTransactionManagement} 更灵活，
 * 会回退到按类型查找容器中任意 {@code TransactionManager} Bean。
 * 因此名称可以是 "txManager"、"transactionManager" 或 "tm"，并无影响。
 *
 * <p>若希望在 {@code @EnableTransactionManagement} 与将使用的
 * 具体事务管理器 Bean 之间建立更直接的关系，
 * 可实现 {@link TransactionManagementConfigurer} 回调接口——
 * 注意下面的 {@code implements} 子句和 {@code @Override} 标注的方法：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig implements TransactionManagementConfigurer {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 *
 *     &#064;Override
 *     public PlatformTransactionManager annotationDrivenTransactionManager() {
 *         return txManager();
 *     }
 * }</pre>
 *
 * <p>此方式可能仅因更明确而 desirable，
 * 或在同一容器中存在两个 {@code TransactionManager} Bean 时必需。
 * 顾名思义，{@code annotationDrivenTransactionManager()} 将用于处理
 * {@code @Transactional} 方法。详见 {@link TransactionManagementConfigurer} Javadoc。
 *
 * <p>{@link #mode} 属性控制通知如何应用：若模式为
 * {@link AdviceMode#PROXY}（默认），则其他属性控制代理行为。
 * 请注意代理模式仅能通过代理拦截调用；
 * 同类内的本地调用无法被拦截。
 *
 * <p>若 {@linkplain #mode} 设为 {@link AdviceMode#ASPECTJ}，
 * 则 {@link #proxyTargetClass} 属性值将被忽略。
 * 此外此时 classpath 上须有 {@code spring-aspects} 模块 JAR，
 * 通过编译期或加载期织入将切面应用于受影响类。
 * 此场景不涉及代理；本地调用也会被拦截。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see TransactionManagementConfigurer
 * @see TransactionManagementConfigurationSelector
 * @see ProxyTransactionManagementConfiguration
 * @see org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {

	/**
	 * 指示是否创建基于子类（CGLIB）的代理（{@code true}），
	 * 而非标准 Java 基于接口的代理（{@code false}）。
	 * 默认为 {@code false}。<strong>仅当 {@link #mode()}
	 * 设为 {@link AdviceMode#PROXY} 时适用</strong>。
	 * <p>将此属性设为 {@code true} 会影响<em>所有</em>需要代理的 Spring 管理 Bean，
	 * 不仅限于 {@code @Transactional} 标注的 Bean。
	 * 例如同时带 Spring {@code @Async} 注解的其他 Bean
	 * 也会升级为子类代理。除非明确期望某种代理类型
	 *（例如测试中），否则实践中无负面影响。
	 * <p>通常建议依赖全局默认代理配置，
	 * 对特定 Bean 的代理需求通过受影响 Bean 类上的
	 * {@link org.springframework.context.annotation.Proxyable} 注解表达。
	 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 指示事务通知应如何应用。
	 * <p><b>默认为 {@link AdviceMode#PROXY}。</b>
	 * 请注意代理模式仅能通过代理拦截调用。
	 * 同类内的本地调用无法被拦截；本地调用中此类方法上的
	 * {@link Transactional} 注解会被忽略，
	 * 因为 Spring 拦截器在此运行时场景下根本不会生效。
	 * 如需更高级的拦截模式，请考虑切换为 {@link AdviceMode#ASPECTJ}。
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 指示在特定连接点应用多个通知时事务顾问的执行顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

	/**
	 * 指示无自定义回滚规则的基于规则事务的回滚行为：
	 * 默认为非受检异常回滚，可切换为任意异常（含受检）回滚。
	 * <p>事务特定回滚规则覆盖默认行为，
	 * 但对未指定异常仍保留所选默认值。
	 * Spring {@link Transactional} 及此处与 Spring 配合使用的
	 * JTA {@link jakarta.transaction.Transactional} 均如此。
	 * <p>除非依赖 EJB 风格带提交行为的业务异常，
	 * 建议切换为 {@link RollbackOn#ALL_EXCEPTIONS}，
	 * 以便在（可能意外的）受检异常时也一致回滚。
	 * 对 Kotlin 应用（完全不强制受检异常）也建议切换。
	 * @since 6.2
	 * @see Transactional#rollbackFor()
	 * @see Transactional#noRollbackFor()
	 * @see jakarta.transaction.Transactional#rollbackOn()
	 * @see jakarta.transaction.Transactional#dontRollbackOn()
	 */
	RollbackOn rollbackOn() default RollbackOn.RUNTIME_EXCEPTIONS;

}
