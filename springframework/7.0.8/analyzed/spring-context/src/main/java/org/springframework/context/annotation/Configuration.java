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
 * <h2>引导 {@code @Configuration} 类</h2>
 *
 * <h3>通过 {@code AnnotationConfigApplicationContext}</h3>
 *
 * <p>{@code @Configuration} 类通常使用 {@link AnnotationConfigApplicationContext}
 * 或其支持 Web 的变体
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} 引导。前者示例如下：
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * <p>更多细节请参阅 {@link AnnotationConfigApplicationContext} 的 Javadoc；
 * 在 {@code Servlet} 容器中进行 Web 配置请参阅
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext}。
 *
 * <h3>通过 Spring {@code <beans>} XML</h3>
 *
 * <p>除直接向 {@code AnnotationConfigApplicationContext} 注册 {@code @Configuration} 类外，
 * 也可在 Spring XML 文件中将 {@code @Configuration} 类声明为普通 {@code <bean>} 定义：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *    &lt;context:annotation-config/&gt;
 *    &lt;bean class="com.acme.AppConfig"/&gt;
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>上例中，需要 {@code <context:annotation-config/>} 以启用
 * {@link ConfigurationClassPostProcessor} 及其他便于处理 {@code @Configuration} 类的
 * 注解相关后处理器。
 *
 * <h3>通过组件扫描</h3>
 *
 * <p>由于 {@code @Configuration} 以 {@link Component @Component} 为元注解，
 * {@code @Configuration} 类可作为组件扫描的候选——例如使用 {@link ComponentScan @ComponentScan} or Spring XML's
 * {@code <context:component-scan/>} 元素——因此也可像普通 {@code @Component} 一样使用
 * {@link Autowired @Autowired}/{@link jakarta.inject.Inject @Inject}。
 * 特别地，若存在唯一构造函数，将自动对该构造函数应用自动装配语义：
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
 * <p>{@code @Configuration} 类不仅可通过组件扫描引导，还可使用
 * {@link ComponentScan @ComponentScan} 注解<em>配置</em>组件扫描：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 *
 * <p>详见 {@link ComponentScan @ComponentScan} 的 Javadoc。
 *
 * <h2>使用外部化值</h2>
 *
 * <h3>使用 {@code Environment} API</h3>
 *
 * <p>可将 Spring {@link org.springframework.core.env.Environment} 注入
 * {@code @Configuration} 类以查找外部化值——例如使用 {@code @Autowired} 注解：
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
 * <p>通过 {@code Environment} 解析的属性位于一个或多个“属性源”对象中，
 * {@code @Configuration} 类可使用 {@link PropertySource @PropertySource} 注解
 * 向 {@code Environment} 贡献属性源：
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
 * <p>更多细节请参阅 {@link org.springframework.core.env.Environment Environment}
 * 和 {@link PropertySource @PropertySource} 的 Javadoc。
 *
 * <h3>使用 {@code @Value} 注解</h3>
 *
 * <p>可使用 {@link Value @Value} 注解将外部化值注入 {@code @Configuration} 类：
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
 * <p>此方式常与 Spring 的
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer} 配合使用，后者可通过 XML 配置中的
 * {@code <context:property-placeholder/>}<em>自动</em>启用，或在 {@code @Configuration} 类中
 * 通过专用 {@code static} {@code @Bean} 方法<em>显式</em>启用
 * （详见 {@link Bean @Bean} Javadoc 中关于返回 BeanFactoryPostProcessor 的 {@code @Bean} 方法的说明）。
 * 但请注意，通常仅在需要自定义占位符语法等配置时，才需要通过 {@code static} {@code @Bean}
 * 方法显式注册 {@code PropertySourcesPlaceholderConfigurer}。
 * 具体而言，若没有 Bean 后处理器（如 {@code PropertySourcesPlaceholderConfigurer}）
 * 为 {@code ApplicationContext} 注册<em>嵌入式值解析器</em>，Spring 将注册默认
 * <em>嵌入式值解析器</em>，针对 {@code Environment} 中注册的属性源解析占位符。
 * 请参阅下文关于使用 {@code @ImportResource} 将 {@code @Configuration} 类与 Spring XML 组合的章节；
 * 请参阅 {@link Value @Value} 和 {@link Bean @Bean} 的 Javadoc，
 * 了解如何与 {@code PropertySourcesPlaceholderConfigurer} 等
 * {@code BeanFactoryPostProcessor} 类型配合使用。
 *
 * <h2>组合 {@code @Configuration} 类</h2>
 *
 * <h3>使用 {@code @Import} 注解</h3>
 *
 * <p>可使用 {@link Import @Import} 注解组合 {@code @Configuration} 类，
 * 类似于 Spring XML 中 {@code <import>} 的用法。由于 {@code @Configuration} 对象
 * 在容器内作为 Spring Bean 管理，导入的配置可被注入——例如通过构造函数注入：
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
