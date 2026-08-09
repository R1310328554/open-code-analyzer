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

package org.springframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用 Spring 基于注解的缓存管理能力，类似于 Spring {@code <cache:*>} XML 命名空间中的支持。
 * 与 @{@link org.springframework.context.annotation.Configuration Configuration} 类一起使用，如下所示：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * class AppConfig {
 *
 *     &#064;Bean
 *     MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 * }</pre>
 *
 * <p>作为参考，上述示例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;cache:annotation-driven/&gt;
 *
 *     &lt;bean id="myService" class="com.foo.MyService"/&gt;
 *
 *     &lt;bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager"&gt;
 *         &lt;property name="caches"&gt;
 *             &lt;set&gt;
 *                 &lt;bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean"&gt;
 *                     &lt;property name="name" value="default"/&gt;
 *                 &lt;/bean&gt;
 *             &lt;/set&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * 在上述两种场景中，{@code @EnableCaching} 和 {@code <cache:annotation-driven/>} 负责注册
 * 支撑基于注解缓存管理所需的 Spring 组件，例如
 * {@link org.springframework.cache.interceptor.CacheInterceptor CacheInterceptor}，
 * 以及在调用 {@link org.springframework.cache.annotation.Cacheable @Cacheable} 方法时
 * 将拦截器织入调用栈的基于代理或 AspectJ 的通知。
 *
 * <p>若 JSR-107 API 和 Spring 的 JCache 实现存在，还将注册管理标准缓存注解所需的组件。
 * 这创建了在调用带有 {@code CacheResult}、{@code CachePut}、{@code CacheRemove} 或
 * {@code CacheRemoveAll} 注解的方法时，将拦截器织入调用栈的基于代理或 AspectJ 的通知。
 *
 * <p><strong>必须注册 {@link org.springframework.cache.CacheManager CacheManager} 类型的 Bean</strong>，
 * 因为框架无法使用合理的默认值作为约定。而 {@code <cache:annotation-driven>} 元素假定
 * <em>名为</em> "cacheManager" 的 Bean，{@code @EnableCaching} 则<em>按类型</em>搜索缓存管理器 Bean。
 * 因此，缓存管理器 Bean 方法的命名并不重要。
 *
 * <p>对于希望在 {@code @EnableCaching} 与要使用的确切缓存管理器 Bean 之间建立更直接关系的情况，
 * 可实现 {@link CachingConfigurer} 回调接口。注意下面带 {@code @Override} 注解的方法：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * class AppConfig implements CachingConfigurer {
 *
 *     &#064;Bean
 *     MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     &#064;Override
 *     CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 *
 *     &#064;Override
 *     KeyGenerator keyGenerator() {
 *         // configure and return an implementation of Spring's KeyGenerator SPI
 *         return new MyKeyGenerator();
 *     }
 * }</pre>
 *
 * 这种方式可能仅因更明确而可取，或在同一容器中存在两个 {@code CacheManager} Bean
 * 需要区分时可能是必要的。
 *
 * <p>另注意上述示例中的 {@code keyGenerator} 方法。这允许按 Spring 的
 * {@link org.springframework.cache.interceptor.KeyGenerator KeyGenerator} SPI
 * 自定义缓存键生成策略。通常 {@code @EnableCaching} 会为此配置 Spring 的
 * {@link org.springframework.cache.interceptor.SimpleKeyGenerator SimpleKeyGenerator}，
 * 但实现 {@code CachingConfigurer} 时可指定自定义键生成器。
 *
 * <p>{@link CachingConfigurer} 还提供其他自定义选项：更多细节参见 {@link CachingConfigurer} 的 javadoc。
 *
 * <p>{@link #mode} 属性控制如何应用缓存通知：若模式为 {@link AdviceMode#PROXY}（默认），
 * 则其他属性控制代理行为。请注意，代理模式仅允许通过代理拦截调用；
 * 同一类内的本地调用无法被拦截，本地调用中带缓存注解的方法将被忽略，
 * 因为 Spring 拦截器在此运行时场景下甚至不会启动。
 *
 * <p>注意，若 {@linkplain #mode} 设为 {@link AdviceMode#ASPECTJ}，则 {@link #proxyTargetClass}
 * 属性的值将被忽略。另请注意，此情况下 classpath 上必须存在 {@code spring-aspects} 模块 JAR，
 * 并通过编译时织入或加载时织入将切面应用于受影响的类。此场景不涉及代理；本地调用也会被拦截。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see CachingConfigurer
 * @see CachingConfigurationSelector
 * @see ProxyCachingConfiguration
 * @see org.springframework.cache.aspectj.AspectJCachingConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CachingConfigurationSelector.class)
public @interface EnableCaching {

	/**
	 * 指示是否创建基于子类的（CGLIB）代理，而非标准 Java 接口代理。默认为 {@code false}。<strong>
	 * 仅当 {@link #mode()} 设为 {@link AdviceMode#PROXY} 时适用</strong>。
	 * <p>注意，将此属性设为 {@code true} 将影响<em>所有</em>需要代理的 Spring 管理 Bean，
	 * 而不仅是带 {@code @Cacheable} 的 Bean。例如，同时带 Spring {@code @Transactional}
	 * 注解的其他 Bean 也将同时升级为子类代理。除非明确期望某种代理类型而非另一种
	 * （例如在测试中），实践中通常无负面影响。
	 * <p>通常建议依赖全局默认代理配置，对特定 Bean 的代理需求通过受影响 Bean 类上的
	 * {@link org.springframework.context.annotation.Proxyable} 注解表达。
	 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 指示如何应用缓存通知。
	 * <p><b>默认为 {@link AdviceMode#PROXY}。</b>
	 * 请注意，代理模式仅允许通过代理拦截调用。同一类内的本地调用无法被拦截；
	 * 本地调用中带缓存注解的方法将被忽略，因为 Spring 拦截器在此运行时场景下甚至不会启动。
	 * 若要更高级的拦截模式，可考虑切换为 {@link AdviceMode#ASPECTJ}。
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 指示在特定连接点应用多个通知时缓存 advisor 的执行顺序。
	 * <p>默认为 {@link Ordered#LOWEST_PRECEDENCE}。
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

}
