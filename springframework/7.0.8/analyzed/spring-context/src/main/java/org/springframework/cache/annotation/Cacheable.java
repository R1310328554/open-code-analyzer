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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;

/**
 * 指示调用方法（或类中所有方法）的结果可被缓存的注解。
 *
 * <p>每次调用被通知方法时，将应用缓存行为，检查该方法是否已为给定参数调用过。
 * 合理的默认实现仅使用方法参数计算键，但可通过 {@link #key} 属性提供 SpEL 表达式，
 * 或使用自定义 {@link org.springframework.cache.interceptor.KeyGenerator} 实现
 * 替换默认生成器（参见 {@link #keyGenerator}）。
 *
 * <p>若缓存中未找到计算键对应的值，将调用目标方法并将返回值存入关联缓存。
 * 注意 {@link java.util.Optional} 返回类型会自动解包。
 * 若 {@code Optional} 值 {@linkplain java.util.Optional#isPresent() 存在}，
 * 将存入关联缓存；若不存在，则在关联缓存中存储 {@code null}。
 *
 * <p>可作为<em>元注解</em>使用，以创建带属性覆盖的自定义<em>组合注解</em>。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see CacheConfig
 * @see CachePut
 * @see CacheEvict
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Cacheable {

	/**
	 * {@link #cacheNames} 的别名。
	 * <p>适用于无需其他属性时，例如：{@code @Cacheable("books")}。
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * 存储方法调用结果的缓存名称。
	 * <p>名称可用于确定目标缓存，通过配置的 {@link #cacheResolver()} 解析，
	 * 通常委托给 {@link org.springframework.cache.CacheManager#getCache}。
	 * <p>通常仅为单个缓存名称。若指定多个名称，将按定义顺序查询缓存命中，
	 * 且所有缓存都将收到相同新缓存值的 put/evict 请求。
	 * <p>注意，异步/响应式缓存访问可能不会完全查询所有指定缓存，取决于目标缓存。
	 * 对于迟确定的缓存未命中（例如 Redis），将不再查询后续缓存。
	 * 因此，在异步缓存模式设置中指定多个缓存名称仅对早确定的缓存未命中有意义（例如 Caffeine）。
	 * @since 4.2
	 * @see #value
	 * @see CacheConfig#cacheNames
	 */
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * 用于动态计算键的 Spring 表达式语言（SpEL）表达式。
	 * <p>默认为 {@code ""}，表示所有方法参数均视为键，除非配置了自定义 {@link #keyGenerator}。
	 * <p>SpEL 表达式针对提供以下元数据的专用上下文求值：
	 * <ul>
	 * <li>{@code #root.method}、{@code #root.target} 和 {@code #root.caches} 分别引用
	 * {@link java.lang.reflect.Method method}、目标对象和受影响的缓存</li>
	 * <li>方法名（{@code #root.methodName}）和目标类（{@code #root.targetClass}）的快捷方式也可用</li>
	 * <li>方法参数可通过索引访问。例如第二个参数可通过 {@code #root.args[1]}、{@code #p1}
	 * 或 {@code #a1} 访问。若有信息可用，也可按名称访问参数</li>
	 * </ul>
	 */
	String key() default "";

	/**
	 * 要使用的自定义 {@link org.springframework.cache.interceptor.KeyGenerator} 的 Bean 名称。
	 * <p>与 {@link #key} 属性互斥。
	 * @see CacheConfig#keyGenerator
	 */
	String keyGenerator() default "";

	/**
	 * 用于创建默认 {@link org.springframework.cache.interceptor.CacheResolver} 的
	 * 自定义 {@link org.springframework.cache.CacheManager} 的 Bean 名称（若尚未设置）。
	 * <p>与 {@link #cacheResolver} 属性互斥。
	 * @see org.springframework.cache.interceptor.SimpleCacheResolver
	 * @see CacheConfig#cacheManager
	 */
	String cacheManager() default "";

	/**
	 * 要使用的自定义 {@link org.springframework.cache.interceptor.CacheResolver} 的 Bean 名称。
	 * @see CacheConfig#cacheResolver
	 */
	String cacheResolver() default "";

	/**
	 * 用于使方法缓存条件化的 Spring 表达式语言（SpEL）表达式。
	 * 若条件求值为 {@code true} 则缓存结果。
	 * <p>默认为 {@code ""}，表示始终缓存方法结果。
	 * <p>SpEL 表达式针对提供以下元数据的专用上下文求值：
	 * <ul>
	 * <li>{@code #root.method}、{@code #root.target} 和 {@code #root.caches} 分别引用
	 * {@link java.lang.reflect.Method method}、目标对象和受影响的缓存</li>
	 * <li>方法名（{@code #root.methodName}）和目标类（{@code #root.targetClass}）的快捷方式也可用</li>
	 * <li>方法参数可通过索引访问。例如第二个参数可通过 {@code #root.args[1]}、{@code #p1}
	 * 或 {@code #a1} 访问。若有信息可用，也可按名称访问参数</li>
	 * </ul>
	 */
	String condition() default "";

	/**
	 * 用于否决方法缓存的 Spring 表达式语言（SpEL）表达式。
	 * 若条件求值为 {@code true} 则否决缓存结果。
	 * <p>与 {@link #condition} 不同，此表达式在方法调用后求值，因此可引用 {@code result}。
	 * <p>默认为 {@code ""}，表示从不否决缓存。
	 * <p>SpEL 表达式针对提供以下元数据的专用上下文求值：
	 * <ul>
	 * <li>{@code #result} 引用方法调用结果。对于 {@code Optional} 等支持的包装类型，
	 * {@code #result} 引用实际对象而非包装器</li>
	 * <li>{@code #root.method}、{@code #root.target} 和 {@code #root.caches} 分别引用
	 * {@link java.lang.reflect.Method method}、目标对象和受影响的缓存</li>
	 * <li>方法名（{@code #root.methodName}）和目标类（{@code #root.targetClass}）的快捷方式也可用</li>
	 * <li>方法参数可通过索引访问。例如第二个参数可通过 {@code #root.args[1]}、{@code #p1}
	 * 或 {@code #a1} 访问。若有信息可用，也可按名称访问参数</li>
	 * </ul>
	 * @since 3.2
	 */
	String unless() default "";

	/**
	 * 若多个线程尝试为同一键加载值，则同步底层方法的调用。同步带来若干限制：
	 * <ol>
	 * <li>不支持 {@link #unless()}</li>
	 * <li>只能指定一个缓存</li>
	 * <li>不能组合其他缓存相关操作</li>
	 * </ol>
	 * 这实质上是提示，所选缓存提供者可能不会以同步方式实际支持。
	 * 有关实际语义的更多细节，请查阅提供者文档。
	 * <p>注意，`sync=true` 会导致对缓存提供者的组合回调操作。若此组合操作在初始缓存访问时失败，
	 * 将不再尝试单独的 put 操作。而对于默认 `sync=false` 设置，存在独立的 get 和 put 步骤：
	 * 若 get 步骤失败但在 {@code CacheErrorHandler} 设置中被抑制错误，
	 * 调用底层方法后仍会尝试 put。
	 * @since 4.3
	 * @see org.springframework.cache.Cache#get(Object, Callable)
	 * @see org.springframework.cache.Cache#get(Object)
	 * @see org.springframework.cache.Cache#put(Object, Object)
	 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCacheGetError
	 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCachePutError
	 */
	boolean sync() default false;

}
