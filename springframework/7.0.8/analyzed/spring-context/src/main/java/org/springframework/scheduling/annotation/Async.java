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

import org.springframework.aot.hint.annotation.Reflective;

/**
 * 标记方法为<i>异步</i>执行候选的注解。
 *
 * <p>也可用于类型级别，此时该类型的所有方法均视为异步。
 * 但 {@code @Async} 不支持
 * {@link org.springframework.context.annotation.Configuration @Configuration} 类中声明的方法。
 *
 * <p>目标方法签名支持任意参数类型，
 * 但返回类型须为 {@code void} 或 {@link java.util.concurrent.Future}。
 * 后者可声明更具体的 {@link java.util.concurrent.CompletableFuture}，
 * 以便与异步任务 richer 交互并立即组合后续处理步骤。
 *
 * <p>代理返回的 {@code Future} 句柄为真正的异步 {@code (Completable)Future}，
 * 可用于跟踪异步方法执行结果。由于目标方法须实现相同签名，
 * 它须返回在执行线程中计算后传递值的临时 {@code Future} 句柄：
 * 通常通过 {@link java.util.concurrent.CompletableFuture#completedFuture(Object)}。
 * 提供的值将在运行时通过真正的异步 {@code Future} 句柄暴露给调用方。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see AnnotationAsyncExecutionInterceptor
 * @see AsyncAnnotationAdvisor
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface Async {

	/**
	 * 指定异步操作的限定符值。
	 * <p>可用于确定执行异步操作时使用的目标执行器，
	 * 匹配特定 {@link java.util.concurrent.Executor Executor} 或
	 * {@link org.springframework.core.task.TaskExecutor TaskExecutor}
	 * Bean 定义的限定符值（或 Bean 名称）。
	 * <p>在类级 {@code @Async} 中指定时，表示该类内所有方法均使用该执行器。
	 * 方法级 {@code Async#value} 始终覆盖类级配置的限定符值。
	 * <p>若限定符值为 SpEL 表达式（如 {@code "#{environment['myExecutor']}"}）
	 * 或属性占位符（如 {@code "${my.app.myExecutor}"}），将动态解析。
	 * @since 3.1.2
	 */
	String value() default "";

}
