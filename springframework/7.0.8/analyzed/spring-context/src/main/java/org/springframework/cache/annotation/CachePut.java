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

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;

/**
 * 指示方法（或类上所有方法）触发 {@link org.springframework.cache.Cache#put(Object, Object) 缓存写入} 操作的注解。
 *
 * <p>与 {@link Cacheable @Cacheable} 注解不同，此注解不会导致被通知方法被跳过。
 * 它始终会调用方法，并在 {@link #condition()} 和 {@link #unless()} 表达式匹配时
 * 将结果存入关联缓存。注意 Java 8 的 {@code Optional} 返回类型会自动处理，
 * 若存在则将其内容存入缓存。
 *
 * <p>可作为<em>元注解</em>使用，以创建带属性覆盖的自定义<em>组合注解</em>。
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see CacheConfig
 * @see Cacheable
 * @see CacheEvict
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CachePut {

	/**
	 * {@link #cacheNames} 的别名。
	 * <p>适用于无需其他属性时，例如：{@code @CachePut("books")}。
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * 用于缓存写入操作的缓存名称。
	 * <p>名称可用于确定目标缓存，匹配特定 Bean 定义的限定符值或 Bean 名称。
	 * @since 4.2
	 * @see #value
	 * @see CacheConfig#cacheNames
	 */
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * 用于动态计算键的 Spring 表达式语言（SpEL）表达式。
	 * <p>默认为 {@code ""}，表示所有方法参数均视为键，除非设置了自定义 {@link #keyGenerator}。
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
	 * 用于使缓存写入操作条件化的 Spring 表达式语言（SpEL）表达式。
	 * 若条件求值为 {@code true} 则更新缓存。
	 * <p>由于写入操作的性质，此表达式在方法调用后求值，因此可引用 {@code result}。
	 * <p>默认为 {@code ""}，表示始终缓存方法结果。
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
	 */
	String condition() default "";

	/**
	 * 用于否决缓存写入操作的 Spring 表达式语言（SpEL）表达式。
	 * 若条件求值为 {@code true} 则否决更新缓存。
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

}
