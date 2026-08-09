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

package org.springframework.context.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.annotation.AliasFor;

/**
 * 将方法标记为应用事件监听器的注解。
 *
 * <p>若标注方法仅支持单一事件类型，可声明一个反映待监听事件类型的参数。
 * 若标注方法支持多种事件类型，可通过 {@code classes} 属性引用一种或多种支持的事件类型。
 * 详见 {@link #classes} 的 Javadoc。
 *
 * <p>事件可以是 {@link ApplicationEvent} 实例，也可以是任意对象。
 *
 * <p>{@code @EventListener} 注解的处理由内部 {@link EventListenerMethodProcessor} Bean 执行，
 * 使用 Java 配置时会自动注册；使用 XML 配置时需通过 {@code <context:annotation-config/>}
 * 或 {@code <context:component-scan/>} 元素手动启用。
 *
 * <p>标注方法的返回类型可以不是 {@code void}。此时，方法调用的结果将作为新事件发布。
 * 若返回类型为数组或集合，则每个元素作为独立的新事件发布。
 *
 * <p>本注解可作为<em>元注解</em>，用于创建自定义<em>组合注解</em>。
 *
 * <h3>异常处理</h3>
 * <p>事件监听器可声明抛出任意异常类型，但监听器抛出的受检异常会被包装为
 * {@link java.lang.reflect.UndeclaredThrowableException UndeclaredThrowableException}，
 * 因为事件发布器只能处理运行时异常。
 *
 * <h3>异步监听器</h3>
 * <p>若希望特定监听器异步处理事件，可使用 Spring 的
 * {@link org.springframework.scheduling.annotation.Async @Async} 支持，
 * 但使用异步事件时需注意以下限制。
 *
 * <ul>
 * <li>若异步事件监听器抛出异常，异常不会传播给调用方。详见
 * {@link org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
 * AsyncUncaughtExceptionHandler}。</li>
 * <li>异步事件监听器方法不能通过返回值发布后续事件。若需将处理结果作为事件发布，
 * 请注入 {@link org.springframework.context.ApplicationEventPublisher ApplicationEventPublisher}
 * 手动发布。</li>
 * </ul>
 *
 * <h3>监听器排序</h3>
 * <p>也可定义某类事件的监听器调用顺序。为此，在本事件监听器注解旁添加 Spring 通用的
 * {@link org.springframework.core.annotation.Order @Order} 注解即可。
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 4.2
 * @see EventListenerMethodProcessor
 * @see org.springframework.transaction.event.TransactionalEventListener
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface EventListener {

	/**
	 * {@link #classes} 的别名。
	 */
	@AliasFor("classes")
	Class<?>[] value() default {};

	/**
	 * 本监听器处理的事件类型。
	 * <p>标注方法可（可选）接受给定事件类型的一个参数，
	 * 或接受所有给定事件类型的公共基类/接口参数。
	 */
	@AliasFor("value")
	Class<?>[] classes() default {};

	/**
	 * 用于使事件处理具备条件的 Spring 表达式语言（SpEL）表达式。
	 * <p>当表达式求值为布尔 {@code true}，或以下字符串之一时，将处理该事件：
	 * {@code "true"}、{@code "on"}、{@code "yes"} 或 {@code "1"}。
	 * <p>默认表达式为 {@code ""}，表示始终处理该事件。
	 * <p>SpEL 表达式将在专用上下文中求值，提供以下元数据：
	 * <ul>
	 * <li>{@code #root.event} 或 {@code event}：引用 {@link ApplicationEvent}</li>
	 * <li>{@code #root.args} 或 {@code args}：引用方法参数数组</li>
	 * <li>方法参数可按索引访问。例如，第一个参数可通过
	 * {@code #root.args[0]}、{@code args[0]}、{@code #a0} 或 {@code #p0} 访问。</li>
	 * <li>若编译字节码中可用参数名，可按名称（前缀为井号）访问方法参数。</li>
	 * </ul>
	 */
	String condition() default "";

	/**
	 * 事件是否默认处理，无需特殊前置条件（如活动事务）。
	 * 在此声明以便在 {@code TransactionalEventListener} 等组合注解中覆盖。
	 * @since 6.2
	 */
	boolean defaultExecution() default true;

	/**
	 * 监听器的可选标识符，默认为声明方法的完全限定签名
	 * （例如 "mypackage.MyClass.myMethod()"）。
	 * @since 5.3.5
	 * @see SmartApplicationListener#getListenerId()
	 * @see ApplicationEventMulticaster#removeApplicationListeners(Predicate)
	 */
	String id() default "";

}
