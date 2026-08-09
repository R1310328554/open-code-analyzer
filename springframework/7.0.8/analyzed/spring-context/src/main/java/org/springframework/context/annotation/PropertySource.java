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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.io.support.PropertySourceFactory;

/**
 * 提供便捷、声明式机制，向 Spring {@link org.springframework.core.env.Environment Environment}
 * 添加 {@link org.springframework.core.env.PropertySource PropertySource}。
 * 与 @{@link Configuration} 类配合使用。
 *
 * <h3>用法示例</h3>
 *
 * <p>给定包含键值对 {@code testbean.name=myTestBean} 的文件 {@code app.properties}，
 * 下列 {@code @Configuration} 类通过 {@code @PropertySource} 将 {@code app.properties}
 * 贡献给 {@code Environment} 的 {@code PropertySources} 集合：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/app.properties")
 * public class AppConfig {
 *
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * <p>注意 {@code Environment} 通过
 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
 * 注入配置类，再用于填充 {@code TestBean}。在上述配置下，调用
 * {@code testBean.getName()} 将返回 "myTestBean"。
 *
 * <h3>在 {@code <bean>} 与 {@code @Value} 中解析 <code>${...}</code> 占位符</h3>
 *
 * <p>要在 {@code <bean>} 定义或 {@code @Value} 注解中使用 {@code PropertySource} 中的属性
 * 解析 ${...} 占位符，须确保在 {@code ApplicationContext} 使用的 {@code BeanFactory} 中
 * 注册了合适的<em>嵌入式值解析器</em>。XML 中使用 {@code <context:property-placeholder>}
 * 时会自动完成。使用 {@code @Configuration} 类时，可通过 {@code static} {@code @Bean}
 * 方法显式注册 {@code PropertySourcesPlaceholderConfigurer}。不过，通常仅在需要自定义
 * 占位符语法等配置时才需要这样做。详见 {@link Configuration @Configuration} JavaDoc
 * 的「处理外部化值」章节，以及 {@link Bean @Bean} JavaDoc 中关于返回
 * {@code BeanFactoryPostProcessor} 的 {@code @Bean} 方法的说明。
 *
 * <h3>在 {@code @PropertySource} 资源位置中解析 ${...} 占位符</h3>
 *
 * <p>{@code @PropertySource} {@linkplain #value() 资源位置}中的任意 ${...} 占位符，
 * 将针对环境中已注册的属性源集合解析。例如：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
 * public class AppConfig {
 *
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * <p>若 "my.placeholder" 已存在于某个已注册属性源（如系统属性或环境变量）中，
 * 占位符将解析为对应值；否则使用默认值 "default/path"。默认值（以冒号 ":" 分隔）为可选。
 * 若未指定默认值且属性无法解析，将抛出 {@code IllegalArgumentException}。
 *
 * <h3>关于 {@code @PropertySource} 属性覆盖的说明</h3>
 *
 * <p>当同一属性键存在于多个属性资源文件中时，最后处理的 {@code @PropertySource}
 * 注解将「胜出」，覆盖先前同名键。
 *
 * <p>例如，给定 {@code a.properties} 与 {@code b.properties}，考虑下列两个配置类：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/a.properties")
 * public class ConfigA { }
 *
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/b.properties")
 * public class ConfigB { }
 * </pre>
 *
 * <p>覆盖顺序取决于这些类注册到应用上下文的顺序：
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(ConfigA.class);
 * ctx.register(ConfigB.class);
 * ctx.refresh();
 * </pre>
 *
 * <p>上述场景中，{@code b.properties} 中的属性将覆盖 {@code a.properties} 中的重复项，
 * 因为 {@code ConfigB} 最后注册。
 *
 * <p>某些情况下，使用 {@code @PropertySource} 时难以严格控制属性源顺序（例如通过组件扫描
 * 注册上述 {@code @Configuration} 类时，顺序难以预测）。若覆盖很重要，建议改用编程式
 * {@code PropertySource} API。详见
 * {@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}
 * 与 {@link org.springframework.core.env.MutablePropertySources MutablePropertySources} JavaDoc。
 *
 * <p>{@code @PropertySource} 可作为<em>{@linkplain Repeatable 可重复}</em>注解使用。
 * 也可作为<em>元注解</em>创建带属性覆盖的自定义<em>组合注解</em>。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.1
 * @see PropertySources
 * @see Configuration
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.env.ConfigurableEnvironment#getPropertySources()
 * @see org.springframework.core.env.MutablePropertySources
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySources.class)
public @interface PropertySource {

	/**
	 * 指定本属性源的唯一名称。
	 * <p>若省略，{@link #factory} 将根据底层资源生成名称（对
	 * {@link org.springframework.core.io.support.DefaultPropertySourceFactory
	 * DefaultPropertySourceFactory}：通过无名称的
	 * {@link org.springframework.core.io.support.ResourcePropertySource
	 * ResourcePropertySource} 构造器，从资源描述派生）。
	 * <p>{@code PropertySource} 的名称有两方面用途：
	 * <ul>
	 * <li>诊断：在日志与调试中确定属性来源，例如 Spring Boot 应用中的
	 * {@code PropertySourceOrigin}。</li>
	 * <li>与 {@link org.springframework.core.env.MutablePropertySources MutablePropertySources}
	 * 的编程式交互：可按名称检索特定属性源（或判断是否已存在），也可相对已有属性源
	 * 添加新属性源（参见
	 * {@link org.springframework.core.env.MutablePropertySources#addBefore addBefore()}
	 * 与 {@link org.springframework.core.env.MutablePropertySources#addAfter addAfter()}）。</li>
	 * </ul>
	 * @see org.springframework.core.env.PropertySource#getName()
	 * @see org.springframework.core.io.Resource#getDescription()
	 */
	String name() default "";

	/**
	 * 指定要加载的属性文件资源位置。
	 * <p>默认 {@link #factory() 工厂} 支持传统与基于 XML 的属性格式，例如
	 * {@code "classpath:/com/myco/app.properties"} 或 {@code "file:/path/to/file.xml"}。
	 * <p>自 Spring Framework 6.1 起，也支持资源位置通配符，例如
	 * {@code "classpath*:/config/*.properties"}。
	 * <p>{@code ${...}} 占位符将针对已向 {@code Environment} 注册的属性源解析。
	 * 示例见上文 {@linkplain PropertySource 类说明}。
	 * <p>每个位置将作为独立属性源按声明顺序（或使用通配符时按解析顺序）加入
	 * 封闭的 {@code Environment}。
	 */
	String[] value();

	/**
	 * 找不到 {@link #value 属性资源} 时是否忽略失败。
	 * <p>若属性文件完全可选，应设为 {@code true}。
	 * <p>默认为 {@code false}。
	 * @since 4.0
	 */
	boolean ignoreResourceNotFound() default false;

	/**
	 * 给定资源的特定字符编码，例如 "UTF-8"。
	 * @since 4.3
	 */
	String encoding() default "";

	/**
	 * 指定自定义 {@link PropertySourceFactory}（若有）。
	 * <p>默认使用支持 {@code *.properties} 与 {@code *.xml} 格式的标准资源文件工厂，
	 * 用于 {@link java.util.Properties}。
	 * @since 4.3
	 * @see org.springframework.core.io.support.DefaultPropertySourceFactory
	 * @see org.springframework.core.io.support.ResourcePropertySource
	 */
	Class<? extends PropertySourceFactory> factory() default PropertySourceFactory.class;

}
