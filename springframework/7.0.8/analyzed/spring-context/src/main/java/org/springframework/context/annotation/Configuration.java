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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * 表明类声明了一个或多个 {@link Bean @Bean} 方法，可由 Spring 容器在运行时处理，
 * 为这些 Bean 生成 Bean 定义并满足服务请求，例如：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // instantiate, configure and return bean ...
 *     }
 * }</pre>
 *
 * <h2>Bootstrapping {@code @Configuration} classes</h2>
 *
 * <h3>Via {@code AnnotationConfigApplicationContext}</h3>
 *
 * <p>{@code @Configuration} classes are typically bootstrapped using either
 * {@link AnnotationConfigApplicationContext} or its web-capable variant,
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext}. A simple example with the former follows:
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * <p>See the {@link AnnotationConfigApplicationContext} javadocs for further details, and see
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} for web configuration instructions in a
 * {@code Servlet} container.
 *
 * <h3>Via Spring {@code <beans>} XML</h3>
 *
 * <p>As an alternative to registering {@code @Configuration} classes directly against an
 * {@code AnnotationConfigApplicationContext}, {@code @Configuration} classes may be
 * declared as normal {@code <bean>} definitions within Spring XML files:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *    &lt;context:annotation-config/&gt;
 *    &lt;bean class="com.acme.AppConfig"/&gt;
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>In the example above, {@code <context:annotation-config/>} is required in order to
 * enable {@link ConfigurationClassPostProcessor} and other annotation-related
 * post processors that facilitate handling {@code @Configuration} classes.
 *
 * <h3>Via component scanning</h3>
 *
 * <p>Since {@code @Configuration} is meta-annotated with {@link Component @Component},
 * {@code @Configuration} classes are candidates for component scanning &mdash;
 * for example, using {@link ComponentScan @ComponentScan} or Spring XML's
 * {@code <context:component-scan/>} element &mdash; and therefore may also take
 * advantage of {@link Autowired @Autowired}/{@link jakarta.inject.Inject @Inject}
 * like any regular {@code @Component}. In particular, if a single constructor is
 * present, autowiring semantics will be applied transparently for that constructor:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     private final SomeBean someBean;
 *
 *     public AppConfig(SomeBean someBean) {
 *         this.someBean = someBean;
 *     }
 *
 *     // &#064;Bean definition using "SomeBean"
 *
 * }</pre>
 *
 * <p>{@code @Configuration} classes may not only be bootstrapped using component
 * scanning, but may also themselves <em>configure</em> component scanning using
 * the {@link ComponentScan @ComponentScan} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 *
 * <p>See the {@link ComponentScan @ComponentScan} javadocs for details.
 *
 * <h2>Working with externalized values</h2>
 *
 * <h3>Using the {@code Environment} API</h3>
 *
 * <p>Externalized values may be looked up by injecting the Spring
 * {@link org.springframework.core.env.Environment} into a {@code @Configuration}
 * class &mdash; for example, using the {@code @Autowired} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Autowired Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         MyBean myBean = new MyBean();
 *         myBean.setName(env.getProperty("bean.name"));
 *         return myBean;
 *     }
 * }</pre>
 *
 * <p>Properties resolved through the {@code Environment} reside in one or more "property
 * source" objects, and {@code @Configuration} classes may contribute property sources to
 * the {@code Environment} object using the {@link PropertySource @PropertySource}
 * annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064;Inject Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(env.getProperty("bean.name"));
 *     }
 * }</pre>
 *
 * <p>See the {@link org.springframework.core.env.Environment Environment}
 * and {@link PropertySource @PropertySource} javadocs for further details.
 *
 * <h3>Using the {@code @Value} annotation</h3>
 *
 * <p>Externalized values may be injected into {@code @Configuration} classes using
 * the {@link Value @Value} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064;Value("${bean.name}") String beanName;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(beanName);
 *     }
 * }</pre>
 *
 * <p>This approach is often used in conjunction with Spring's
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer} that can be enabled <em>automatically</em>
 * in XML configuration via {@code <context:property-placeholder/>} or <em>explicitly</em>
 * in a {@code @Configuration} class via a dedicated {@code static} {@code @Bean} method
 * (see "a note on BeanFactoryPostProcessor-returning {@code @Bean} methods" of
 * {@link Bean @Bean}'s javadocs for details). Note, however, that explicit registration
 * of a {@code PropertySourcesPlaceholderConfigurer} via a {@code static} {@code @Bean}
 * method is typically only required if you need to customize configuration such as the
 * placeholder syntax, etc. Specifically, if no bean post-processor (such as a
 * {@code PropertySourcesPlaceholderConfigurer}) has registered an <em>embedded value
 * resolver</em> for the {@code ApplicationContext}, Spring will register a default
 * <em>embedded value resolver</em> which resolves placeholders against property sources
 * registered in the {@code Environment}. See the section below on composing
 * {@code @Configuration} classes with Spring XML using {@code @ImportResource}; see
 * the {@link Value @Value} javadocs; and see the {@link Bean @Bean} javadocs for details
 * on working with {@code BeanFactoryPostProcessor} types such as
 * {@code PropertySourcesPlaceholderConfigurer}.
 *
 * <h2>Composing {@code @Configuration} classes</h2>
 *
 * <h3>With the {@code @Import} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be composed using the {@link Import @Import} annotation,
 * similar to the way that {@code <import>} works in Spring XML. Because
 * {@code @Configuration} objects are managed as Spring beans within the container,
 * imported configurations may be injected &mdash; for example, via constructor injection:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class DatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return DataSource
 *     }
 * }
 *
 * &#064;Configuration
 * &#064;Import(DatabaseConfig.class)
 * public class AppConfig {
 *
 *     private final DatabaseConfig dataConfig;
 *
 *     public AppConfig(DatabaseConfig dataConfig) {
 *         this.dataConfig = dataConfig;
 *     }
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // reference the dataSource() bean method
 *         return new MyBean(dataConfig.dataSource());
 *     }
 * }</pre>
 *
 * <p>此时只需向 Spring 上下文注册 {@code AppConfig}，即可同时引导
 * {@code AppConfig} 和导入的 {@code DatabaseConfig}：
 *
 * <pre class="code">
 * new AnnotationConfigApplicationContext(AppConfig.class);</pre>
 *
 * <h3>使用 {@code @Profile} 注解</h3>
 *
 * <p>可使用 {@link Profile @Profile} 注解标记 {@code @Configuration} 类，
 * 表示仅当给定 profile 处于<em>活动</em>状态时才处理：
 *
 * <pre class="code">
 * &#064;Profile("development")
 * &#064;Configuration
 * public class EmbeddedDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return embedded DataSource
 *     }
 * }
 *
 * &#064;Profile("production")
 * &#064;Configuration
 * public class ProductionDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return production DataSource
 *     }
 * }</pre>
 *
 * <p>也可在 {@code @Bean} 方法级别声明 profile 条件——例如在同一配置类中提供备选 Bean 变体：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class ProfileDatabaseConfig {
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("development")
 *     public DataSource embeddedDatabase() { ... }
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("production")
 *     public DataSource productionDatabase() { ... }
 * }</pre>
 *
 * <p>更多细节请参阅 {@link Profile @Profile} 和
 * {@link org.springframework.core.env.Environment} 的 Javadoc。
 *
 * <h3>使用 {@code @ImportResource} 注解导入 Spring XML</h3>
 *
 * <p>如前所述，{@code @Configuration} 类可在 Spring XML 文件中声明为普通 Spring
 * {@code <bean>} 定义。也可使用 {@link ImportResource @ImportResource} 注解
 * 将 Spring XML 配置文件导入 {@code @Configuration} 类。从 XML 导入的 Bean 定义
 * 可被注入——例如使用 {@code @Inject} 注解：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ImportResource("classpath:/com/acme/database-config.xml")
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource; // from XML
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // inject the XML-defined dataSource bean
 *         return new MyBean(this.dataSource);
 *     }
 * }</pre>
 *
 * <h3>使用嵌套 {@code @Configuration} 类</h3>
 *
 * <p>{@code @Configuration} 类可按如下方式相互嵌套：
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(dataSource);
 *     }
 *
 *     &#064;Configuration
 *     static class DatabaseConfig {
 *         &#064;Bean
 *         DataSource dataSource() {
 *             return new EmbeddedDatabaseBuilder().build();
 *         }
 *     }
 * }</pre>
 *
 * <p>引导此类结构时，只需向应用上下文注册 {@code AppConfig}。
 * 作为嵌套 {@code @Configuration} 类，{@code DatabaseConfig}
 * <em>将自动注册</em>。当 {@code AppConfig} 与 {@code DatabaseConfig} 的关系
 * 已隐含明确时，可避免使用 {@code @Import} 注解。
 *
 * <p>另请注意，嵌套 {@code @Configuration} 类可与 {@code @Profile} 注解配合，
 * 为外层 {@code @Configuration} 类提供同一 Bean 的两种备选方案。
 *
 * <p>在外层 {@code @Configuration} 类上声明的 {@link Conditional @Conditional} 注解，
 * 仅当解析器从其外层类递归到达嵌套 {@code @Configuration} 类，或通过
 * {@link Import @Import} 到达时，才应用于嵌套类的注册。
 * 若嵌套类独立于其外层类被发现——例如通过 {@link ComponentScan @ComponentScan}
 * 或直接注册到应用上下文——则仅使用其自身的 {@code @Conditional} 注解处理。
 * 因此，若希望在这些场景下应用相同的 {@code @Conditional} 注解，
 * 必须在嵌套类上重新声明相关注解，或将其提取为组合注解并同时应用于外层类和嵌套类。
 *
 * <h2>配置延迟初始化</h2>
 *
 * <p>默认情况下，{@code @Bean} 方法在容器引导时<em>急切实例化</em>。
 * 为避免此行为，可将 {@code @Configuration} 与 {@link Lazy @Lazy} 注解配合使用，
 * 表示类内声明的所有 {@code @Bean} 方法默认延迟初始化。
 * 也可在单个 {@code @Bean} 方法上使用 {@code @Lazy}。
 *
 * <h2>{@code @Configuration} 类的测试支持</h2>
 *
 * <p>{@code spring-test} 模块中的 Spring <em>TestContext 框架</em>提供
 * {@code @ContextConfiguration} 注解，可接受<em>组件类</em>引用数组——
 * 通常为 {@code @Configuration} 或 {@code @Component} 类。
 *
 * <pre class="code">
 * &#064;ExtendWith(SpringExtension.class)
 * &#064;ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
 * class MyTests {
 *
 *     &#064;Autowired MyBean myBean;
 *
 *     &#064;Autowired DataSource dataSource;
 *
 *     &#064;Test
 *     void test() {
 *         // assertions against myBean ...
 *     }
 * }</pre>
 *
 * <p>详见
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-framework">TestContext framework</a>
 * 参考文档。
 *
 * <h2>使用 {@code @Enable} 注解启用内置 Spring 功能</h2>
 *
 * <p>异步方法执行、定时任务执行、注解驱动事务管理乃至 Spring MVC 等 Spring 功能，
 * 均可通过各自的 "{@code @Enable}" 注解从 {@code @Configuration} 类启用和配置。详见
 * {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync},
 * {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling},
 * {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement},
 * {@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy},
 * and {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}
 * 和 {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}。
 *
 * <h2>编写 {@code @Configuration} 类的约束</h2>
 *
 * <ul>
 * <li>配置类必须以类形式提供（即不能是工厂方法返回的实例），
 * 以便通过生成的子类在运行时增强。
 * <li>配置类必须非 final（允许运行时子类化），除非将
 * {@link #proxyBeanMethods() proxyBeanMethods} 标志设为 {@code false}，
 * 此时无需运行时生成的子类。
 * <li>配置类必须非局部（即不能在方法内声明）。
 * <li>任何嵌套配置类必须声明为 {@code static}。
 * <li>{@code @Bean} 方法不得再创建其他配置类
 * （此类实例将视为普通 Bean，其配置注解不会被检测）。
 * </ul>
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Bean
 * @see Profile
 * @see Import
 * @see ImportResource
 * @see ComponentScan
 * @see Lazy
 * @see PropertySource
 * @see AnnotationConfigApplicationContext
 * @see ConfigurationClassPostProcessor
 * @see org.springframework.core.env.Environment
 * @see org.springframework.test.context.ContextConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

	/**
	 * 显式指定与 {@code @Configuration} 类关联的 Spring Bean 定义名称。
	 * 若未指定（常见情况），将自动生成 Bean 名称。
	 * <p>自定义名称仅当 {@code @Configuration} 类通过组件扫描发现，
	 * 或直接提供给 {@link AnnotationConfigApplicationContext} 时生效。
	 * 若 {@code @Configuration} 类作为传统 XML Bean 定义注册，
	 * 则以 bean 元素的 name/id 为准。
	 * <p>{@link Component#value} 的别名。
	 * @return 显式组件名称（若有），否则为空字符串
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 * @see FullyQualifiedConfigurationBeanNameGenerator
	 */
	@AliasFor(annotation = Component.class)
	String value() default "";

	/**
	 * 指定是否对 {@code @Bean} 方法进行代理以强制执行 Bean 生命周期行为，
	 * 例如即使用户代码中直接调用 {@code @Bean} 方法，也返回共享的单例 Bean 实例。
	 * 此功能需要方法拦截，通过运行时生成的 CGLIB 子类实现，
	 * 存在配置类及其方法不得声明 {@code final} 等限制。
	 * <p>默认为 {@code true}，允许在配置类内通过直接方法调用实现“Bean 间引用”，
	 * 也允许外部调用本配置的 {@code @Bean} 方法（例如从另一个配置类调用）。
	 * 若本配置的每个 {@code @Bean} 方法都是自包含的、设计为供容器使用的普通工厂方法，
	 * 则无需此功能，可将此标志设为 {@code false} 以避免 CGLIB 子类处理。
	 * <p>关闭 Bean 方法拦截后，{@code @Bean} 方法将像在未标注 {@code @Configuration} 的类上
	 * 声明时一样单独处理，即所谓的“@Bean Lite 模式”
	 * （见 {@link Bean @Bean} 的 Javadoc）。因此在行为上等价于移除 {@code @Configuration} 构造型。
	 * @since 5.2
	 */
	boolean proxyBeanMethods() default true;

	/**
	 * 指定 {@code @Bean} 方法是否必须具有唯一的方法名，
	 * 否则抛出异常以防止意外重载。
	 * <p>默认为 {@code true}，防止被解释为同一 Bean 定义的重载工厂方法的意外方法重载
	 * （而非具有各自条件等的独立 Bean 定义）。
	 * 若需按上述语义允许方法重载，可将此标志设为 {@code false}，但需承担意外重叠的风险。
	 * @since 6.0
	 * @deprecated 自 7.0 起已弃用，始终依赖 {@code @Bean} 方法唯一性，
	 * 参数可使用 {@code Optional}/{@code ObjectProvider}
	 */
	@Deprecated(since = "7.0")
	boolean enforceUniqueMethods() default true;

}
