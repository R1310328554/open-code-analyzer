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

package org.springframework.transaction.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;

/**
 * 根据 {@link TransactionPhase} 调用的 {@link EventListener}。
 * 这是 {@link TransactionalApplicationListener} 的基于注解的等价物。
 *
 * <p>若事件未在活动事务内发布，除非显式设置 {@link #fallbackExecution} 标志，
 * 否则事件将被丢弃。若有事务运行，则按 {@code TransactionPhase} 处理事件。
 *
 * <p>在注解方法上添加 {@link org.springframework.core.annotation.Order @Order}
 * 可让该监听器在事务完成前后运行的其他监听器中优先执行。
 *
 * <p>自 6.1 起，事务事件监听器可与由
 * {@link org.springframework.transaction.PlatformTransactionManager} 管理的线程绑定事务
 * 以及由 {@link org.springframework.transaction.ReactiveTransactionManager} 管理的响应式事务配合工作。
 * 对于前者，监听器保证能看到当前线程绑定的事务。
 * 由于后者使用 Reactor 上下文而非线程局部变量，
 * 事务上下文需作为事件源包含在发布的事件实例中：
 * 参见 {@link org.springframework.transaction.reactive.TransactionalEventPublisher}。
 *
 * <p><strong>警告：</strong>若 {@code TransactionPhase} 设为
 * {@link TransactionPhase#AFTER_COMMIT AFTER_COMMIT}（默认）、
 * {@link TransactionPhase#AFTER_ROLLBACK AFTER_ROLLBACK} 或
 * {@link TransactionPhase#AFTER_COMPLETION AFTER_COMPLETION}，
 * 事务已提交或回滚，但事务资源可能仍活跃且可访问。
 * 因此，此阶段触发的任何数据访问代码仍将 "参与" 原始事务，
 * 但变更不会提交到事务资源。详见
 * {@link org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)
 * TransactionSynchronization.afterCompletion(int)}。
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @author Oliver Drotbohm
 * @since 4.2
 * @see TransactionalApplicationListener
 * @see TransactionalApplicationListenerMethodAdapter
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener
public @interface TransactionalEventListener {

	/**
	 * 绑定事件处理的事务阶段。
	 * <p>默认阶段为 {@link TransactionPhase#AFTER_COMMIT}。
	 * <p>若无事务进行中，除非显式启用 {@link #fallbackExecution}，
	 * 否则事件完全不会被处理。
	 */
	TransactionPhase phase() default TransactionPhase.AFTER_COMMIT;

	/**
	 * {@link #classes} 的别名。
	 */
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] value() default {};

	/**
	 * 此监听器处理的事件类。
	 * <p>若此属性以单个值指定，注解方法可选择接受单个参数。
	 * 但若以多个值指定，注解方法<em>不得</em>声明任何参数。
	 */
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] classes() default {};

	/**
	 * 用于使事件处理条件化的 Spring 表达式语言（SpEL）属性。
	 * <p>默认为 {@code ""}，表示始终处理事件。
	 * @see EventListener#condition
	 */
	@AliasFor(annotation = EventListener.class, attribute = "condition")
	String condition() default "";

	/**
	 * 若无事务运行，是否应处理事件。
	 * @see EventListener#defaultExecution()
	 */
	@AliasFor(annotation = EventListener.class, attribute = "defaultExecution")
	boolean fallbackExecution() default false;

	/**
	 * 监听器的可选标识符，默认为声明方法的全限定签名
	 * （例如 "mypackage.MyClass.myMethod()"）。
	 * @since 5.3
	 * @see EventListener#id
	 * @see TransactionalApplicationListener#getListenerId()
	 */
	@AliasFor(annotation = EventListener.class, attribute = "id")
	String id() default "";

}
