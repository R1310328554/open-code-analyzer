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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.annotation.AliasFor;

/**
 * 指示方法产生由 Spring 容器管理的 Bean。
 *
 * <h3>概述</h3>
 *
 * <p>本注解属性的名称和语义有意与 Spring XML 模式中 {@code <bean/>} 元素相似。例如：
 *
 * <pre class="code">
 * &#064;Bean
 * public MyBean myBean() {
 *     // instantiate and configure MyBean obj
 *     return obj;
 * }</pre>
 *
 * <h3>Bean 名称</h3>
 *
 * <p>虽然提供了 {@link #name} 属性，但确定 Bean 名称的默认策略是使用 {@code @Bean} 方法名。
 * 可通过配置 {@link ConfigurationBeanNameGenerator} 覆盖此默认行为——例如使用
 * {@link FullyQualifiedConfigurationBeanNameGenerator} 生成全限定名称。
 * 若需为单个 Bean 显式命名，可使用 {@code name} 属性（或其别名 {@link #value}）。
 * 另请注意 {@code name} 接受 String 数组，允许单个 Bean 拥有多个名称（即主 Bean 名称加一个或多个别名）。
 *
 * <pre class="code">
 * &#064;Bean({"b1", "b2"}) // bean available as 'b1' and 'b2', but not 'myBean'
 * public MyBean myBean() {
 *     // instantiate and configure MyBean obj
 *     return obj;
 * }</pre>
 *
 * <h3>Profile、Scope、Lazy、DependsOn、Primary、Fallback、Order</h3>
 *
 * <p>注意 {@code @Bean} 注解不提供 profile、scope、lazy、depends-on 或 primary 属性。
 * 应配合 {@link Scope @Scope}、{@link Lazy @Lazy}、{@link DependsOn @DependsOn} 和
 * {@link Primary @Primary} 注解声明这些语义。例如：
 *
 * <pre class="code">
 * &#064;Bean
 * &#064;Profile("production")
 * &#064;Scope("prototype")
 * public MyBean myBean() {
 *     // instantiate and configure MyBean obj
 *     return obj;
 * }</pre>
 *
 * 上述注解的语义与在组件类级别的用法一致：{@code @Profile} 允许选择性包含特定 Bean。
 * {@code @Scope} 将 Bean 作用域从 singleton 改为指定作用域。
 * {@code @Lazy} 仅在默认 singleton 作用域下才有实际效果。
 * {@code @DependsOn} 强制在本 Bean 创建之前先创建特定其他 Bean，
 * 除 Bean 通过直接引用表达的依赖外，通常有助于单例启动。
 * {@code @Primary} 是在注入点需要注入单个目标组件但多个 Bean 按类型匹配时解决歧义的机制。
 * {@link Fallback @Fallback} 在此类场景中将 Bean 标记为后备候选；
 * 若多个匹配候选中除一个外均标记为 fallback，则选择剩余的那个。
 *
 * <p>此外，{@code @Bean} 方法也可声明限定符注解和
 * {@link org.springframework.core.annotation.Order @Order} 值，
 * 在注入点解析时予以考虑，类似于对应组件类上的注解，
 * 但每个 Bean 定义可能非常个性化（多个定义共享同一 Bean 类时）。
 * 限定符在初始类型匹配后缩小候选集；
 * order 值在集合注入点（多个目标 Bean 按类型和限定符匹配）时决定解析元素的顺序。
 *
 * <p><b>注意：</b>{@code @Order} 值可能影响注入点优先级，
 * 但请注意它们不影响单例启动顺序——后者是由依赖关系和上文所述 {@code @DependsOn}
 * 声明决定的正交关注点。此外，{@link jakarta.annotation.Priority} 在此级别不可用，
 * 因为无法声明在方法上；其语义可通过 {@code @Order} 值结合每个类型单个 Bean 上的
 * {@code @Primary} 或 {@code @Fallback} 来建模。
 *
 * <h3>{@code @Configuration} 类中的 {@code @Bean} 方法</h3>
 *
 * <p>通常，{@code @Bean} 方法在 {@code @Configuration} 类中声明。
 * 此时，Bean 方法可通过<em>直接</em>调用同类的其他 {@code @Bean} 方法来引用它们。
 * 这确保 Bean 之间的引用是强类型且可导航的。此类所谓的<em>“Bean 间引用”</em>
 * 保证尊重作用域和 AOP 语义，就像 {@code getBean()} 查找一样。
 * 这些是原始“Spring JavaConfig”项目所知的语义，要求在运行时为每个此类配置类
 * 进行 CGLIB 子类化。因此，在此模式下 {@code @Configuration} 类及其工厂方法
 * 不得标记为 final 或 private。例如：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *    &#064;Bean
 *    public FooService fooService() {
 *        return new FooService(fooRepository());
 *    }
 *
 *    &#064;Bean
 *    public FooRepository fooRepository() {
 *        return new JdbcFooRepository(dataSource());
 *    }
 *
 *    // ...
 * }</pre>
 *
 * <h3>{@code @Bean} <em>Lite</em> 模式</h3>
 *
 * <p>{@code @Bean} 方法也可在未标注 {@code @Configuration} 的类中声明。
 * 若在未标注 {@code @Configuration} 的 Bean 上声明 Bean 方法，
 * 则将以所谓的<em>“lite”</em>模式处理。
 *
 * <p><em>lite</em> 模式下的 Bean 方法将被容器视为普通<em>工厂方法</em>
 * （类似于 XML 中的 {@code factory-method} 声明），并正确应用作用域和生命周期回调。
 * 此情况下包含类保持不变，对包含类或工厂方法无特殊约束。
 *
 * <p>与 {@code @Configuration} 类中 Bean 方法的语义不同，
 * <em>lite</em> 模式不支持<em>“Bean 间引用”</em>。
 * 当 <em>lite</em> 模式下一个 {@code @Bean} 方法调用另一个 {@code @Bean} 方法时，
 * 调用是标准 Java 方法调用；Spring 不会通过 CGLIB 代理拦截调用。
 * 这类似于 {@code @Transactional} 方法间调用：在代理模式下 Spring 不拦截调用——
 * 仅在 AspectJ 模式下才拦截。
 *
 * <p>例如：
 *
 * <pre class="code">
 * &#064;Component
 * public class Calculator {
 *    public int sum(int a, int b) {
 *        return a+b;
 *    }
 *
 *    &#064;Bean
 *    public MyBean myBean() {
 *        return new MyBean();
 *    }
 * }</pre>
 *
 * <h3>引导</h3>
 *
 * <p>有关如何使用 {@link AnnotationConfigApplicationContext} 等引导容器的更多细节，
 * 请参阅 {@link Configuration @Configuration} 的 Javadoc。
 *
 * <h3>返回 {@code BeanFactoryPostProcessor} 的 {@code @Bean} 方法</h3>
 *
 * <p>对于返回 Spring
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * （{@code BFPP}）类型的 {@code @Bean} 方法需特别考虑。
 * 由于 {@code BFPP} 对象必须在容器生命周期极早实例化，
 * 它们可能干扰 {@code @Configuration} 类中 {@code @Autowired}、{@code @Value}
 * 和 {@code @PostConstruct} 等注解的处理。为避免这些生命周期问题，
 * 将返回 {@code BFPP} 的 {@code @Bean} 方法标记为 {@code static}。例如：
 *
 * <pre class="code">
 * &#064;Bean
 * public static PropertySourcesPlaceholderConfigurer pspc() {
 *     // instantiate, configure and return pspc ...
 * }</pre>
 *
 * 将此方法标记为 {@code static} 后，可在不实例化其声明的 {@code @Configuration} 类的情况下调用，
 * 从而避免上述生命周期冲突。但请注意，{@code static} {@code @Bean} 方法不会像上文所述
 * 那样增强作用域和 AOP 语义。这在 {@code BFPP} 场景下可行，因为它们通常不被其他
 * {@code @Bean} 方法引用。提醒：对于返回类型可赋值给 {@code BeanFactoryPostProcessor}
 * 的任何非 static {@code @Bean} 方法，将发出 INFO 级别日志消息。
 *
 * <h3>返回 {@code BeanPostProcessor} 的 {@code @Bean} 方法</h3>
 *
 * <p>类似地，对于返回 Spring
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
 * （{@code BPP}）类型的 {@code @Bean} 方法也需特别考虑。
 * 由于 {@code BPP} 对象必须在容器生命周期早期实例化，
 * 返回 {@code BPP} 的非 static {@code @Bean} 方法将导致其声明的 {@code @Configuration} 类
 * 被急切初始化，这可能使 {@code @Configuration} 类中的其他 Bean（以及这些 Bean 的依赖）
 * 无法获得完整后处理。为避免这些生命周期问题，将返回 {@code BPP} 的 {@code @Bean}
 * 方法标记为 {@code static}。例如：
 *
 * <pre class="code">
 * &#064;Bean
 * public static MyBeanPostProcessor myBeanPostProcessor() {
 *     return new MyBeanPostProcessor();
 * }</pre>
 *
 * 将此方法标记为 {@code static} 后，可在不实例化其声明的 {@code @Configuration} 类的情况下调用。
 * 此外，该方法理想情况下不应声明任何依赖，以免容器需要实例化其他 Bean 来创建后处理器，
 * 从而使那些 Bean 也无法获得后处理。对于此类 Bean，应看到类似以下的 WARN 级别日志消息：
 * "Bean 'someBean' of type [org.example.SomeType] is not eligible for getting processed by all
 * BeanPostProcessors (for example: not eligible for auto-proxying)"。
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see Configuration
 * @see Scope
 * @see DependsOn
 * @see Lazy
 * @see Primary
 * @see Fallback
 * @see org.springframework.stereotype.Component
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.beans.factory.annotation.Value
 * @see FullyQualifiedConfigurationBeanNameGenerator
 * @see AnnotationConfigApplicationContext#setBeanNameGenerator
 * @see ComponentScan#nameGenerator()
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

	/**
	 * {@link #name} 的别名。
	 * <p>适用于无需其他属性时，例如：{@code @Bean("customBeanName")}。
	 * @since 4.3.3
	 * @see #name
	 */
	@AliasFor("name")
	String[] value() default {};

	/**
	 * 本 Bean 的名称；若有多个名称，则为主 Bean 名称加别名。
	 * <p>若未指定本属性，Bean 名称如何确定的细节请参阅
	 * {@linkplain Bean 类级文档}中的“Bean 名称”一节。
	 * <p>也可通过 {@link #value} 属性配置 Bean 名称和别名。
	 * @see #value
	 */
	@AliasFor("value")
	String[] name() default {};

	/**
	 * 本 Bean 是否可作为自动装配候选注入到其他 Bean。
	 * <p>默认为 {@code true}；对于不应妨碍其他位置同类型 Bean 的内部委托，
	 * 可设为 {@code false}。
	 * @since 5.1
	 * @see #defaultCandidate()
	 */
	boolean autowireCandidate() default true;

	/**
	 * 本 Bean 是否可作为仅基于纯类型（无进一步指示如限定符匹配）的自动装配候选。
	 * <p>默认为 {@code true}；对于应在特定区域可注入但不应妨碍其他位置同类型 Bean 的受限委托，
	 * 可设为 {@code false}。
	 * <p>这是 {@link #autowireCandidate()} 的变体，不会完全禁用注入，
	 * 仅强制要求额外指示（如限定符）。
	 * @since 6.2
	 * @see #autowireCandidate()
	 */
	boolean defaultCandidate() default true;

	/**
	 * 本 Bean 的引导模式：默认对非延迟单例 Bean 使用主预实例化线程，
	 * 对 prototype Bean 使用调用方线程。
	 * <p>设为 {@link Bootstrap#BACKGROUND} 可在后台线程实例化本 Bean。
	 * 对于非延迟单例，可使用后台预实例化线程，同时仍强制在
	 * {@link org.springframework.context.ConfigurableApplicationContext#refresh()} 结束时完成。
	 * 对于延迟单例，也可使用后台预实例化线程——允许稍后完成，在实际访问时强制完成。
	 * @since 6.2
	 * @see Lazy
	 */
	Bootstrap bootstrap() default Bootstrap.DEFAULT;

	/**
	 * 初始化期间在 Bean 实例上调用的可选方法名。
	 * 不常用，因为该方法可在 {@code @Bean} 方法体内直接以编程方式调用。
	 * <p>默认值为 {@code ""}，表示不调用初始化方法。
	 * @see org.springframework.beans.factory.InitializingBean
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	String initMethod() default "";

	/**
	 * 关闭应用上下文时在 Bean 实例上调用的可选方法名，
	 * 例如 JDBC {@code DataSource} 实现上的 {@code close()} 方法，
	 * 或 Hibernate {@code SessionFactory} 对象上的方法。
	 * 方法必须无参数，但可抛出任何异常。
	 * <p>为方便用户，容器将尝试对 {@code @Bean} 方法返回的对象推断销毁方法。
	 * 例如，给定返回 Apache Commons DBCP {@code BasicDataSource} 的 {@code @Bean} 方法，
	 * 容器会注意到该对象上的 {@code close()} 方法，并自动将其注册为 {@code destroyMethod}。
	 * 此“销毁方法推断”目前仅限于检测名为 {@code close} 或 {@code shutdown} 的
	 * public 无参方法。方法可在继承层次任何级别声明，无论 {@code @Bean} 方法的返回类型
	 * （即在创建时针对 Bean 实例本身进行反射检测）。
	 * <p>要对特定 {@code @Bean} 禁用销毁方法推断，请指定空字符串作为值，
	 * 例如 {@code @Bean(destroyMethod="")}。请注意，
	 * {@link org.springframework.beans.factory.DisposableBean} 回调接口仍会被检测
	 * 并调用相应销毁方法：换言之，{@code destroyMethod=""} 仅影响自定义 close/shutdown 方法
	 * 以及 {@link java.io.Closeable}/{@link java.lang.AutoCloseable} 声明的 close 方法。
	 * <p>注意：仅对生命周期完全由工厂控制的 Bean 调用；
	 * 单例始终满足此条件，其他作用域则不保证。
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	String destroyMethod() default AbstractBeanDefinition.INFER_METHOD;


	/**
	 * 引导模式的本地枚举。
	 * @since 6.2
	 * @see #bootstrap()
	 */
	enum Bootstrap {

		/**
		 * 指示对非延迟单例 Bean 使用主预实例化线程，对 prototype Bean 使用调用方线程。
		 */
		DEFAULT,

		/**
		 * 允许在后台线程实例化 Bean。
		 * <p>对于非延迟单例，可使用后台预实例化线程，同时仍强制在上下文刷新时完成。
		 * 对于延迟单例，可使用后台预实例化线程，允许稍后完成（实际访问时）。
		 */
		BACKGROUND,
	}

}
