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

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;

/**
 * 描述单个方法或类上的事务属性。
 *
 * <p>在类级别声明此注解时，它作为默认值应用于
 * 声明类及其子类的所有方法。注意它不会应用于
 * 类层次结构中的祖先类；继承的方法需要
 * 在本地重新声明才能参与子类级别的注解。有关
 * 方法可见性约束的详情，请参阅参考手册的
 * <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html#transaction-declarative-annotations-method-visibility">Transaction Management</a>
 * 章节。
 *
 * <p>此注解通常与 Spring 的
 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}
 * 类直接可比，实际上 {@link AnnotationTransactionAttributeSource} 会
 * 直接将此注解的属性转换为 {@code RuleBasedTransactionAttribute} 中的属性，
 * 使 Spring 事务支持代码无需了解注解。
 *
 * <h3>属性语义</h3>
 *
 * <p>若此注解中未配置自定义回滚规则，事务将在
 * {@link RuntimeException} 和 {@link Error} 时回滚，但不会在受检异常时回滚。
 *
 * <p>回滚规则决定抛出给定异常时是否应回滚事务，
 * 规则基于类型或模式。可通过 {@link #rollbackFor}/{@link #noRollbackFor} 和
 * {@link #rollbackForClassName}/{@link #noRollbackForClassName} 配置自定义规则，
 * 分别允许以类型或模式指定规则。
 *
 * <p>当回滚规则以异常类型定义时（例如通过 {@link #rollbackFor}），
 * 该类型将用于匹配抛出的异常类型。具体而言，给定配置的异常类型
 * {@code C}，若 {@code T} 等于 {@code C} 或是 {@code C} 的子类，
 * 则类型为 {@code T} 的抛出异常将被视为与 {@code C} 匹配。
 * 这提供类型安全并避免使用模式时可能发生的意外匹配。
 * 例如，{@code jakarta.servlet.ServletException.class} 的值
 * 仅匹配 {@code jakarta.servlet.ServletException} 及其子类的抛出异常。
 *
 * <p>当回滚规则以异常模式定义时，模式可以是完全限定类名
 * 或异常类型（必须是 {@code Throwable} 的子类）完全限定类名的子串，
 * 目前不支持通配符。例如，{@code "jakarta.servlet.ServletException"}
 * 或 {@code "ServletException"} 将匹配 {@code jakarta.servlet.ServletException} 及其子类。
 *
 * <p><strong>警告：</strong>必须仔细考虑模式的特异性以及是否包含包信息（非强制）。
 * 例如，{@code "Exception"} 将匹配几乎所有内容并可能掩盖其他规则。
 * 若 {@code "Exception"} 意在定义所有受检异常的规则，
 * {@code "java.lang.Exception"} 才是正确的。对于更独特的异常名
 * 如 {@code "BaseBusinessException"}，通常无需为异常模式使用完全限定类名。
 * 此外，通过模式定义的回滚规则可能导致对名称相似的异常和嵌套类的意外匹配。
 * 这是因为当抛出异常的名称包含为回滚规则配置的异常模式时，
 * 该异常即被视为与给定基于模式的回滚规则匹配。
 * 例如，给定配置为匹配 {@code "com.example.CustomException"} 的规则，
 * 该规则将匹配名为 {@code com.example.CustomExceptionV2} 的异常
 * （与 {@code CustomException} 同包但带额外后缀）或
 * 名为 {@code com.example.CustomException$AnotherException} 的异常
 * （在 {@code CustomException} 中声明为嵌套类的异常）。
 *
 * <p>有关此注解其他属性语义的具体信息，
 * 请参阅 {@link org.springframework.transaction.TransactionDefinition}
 * 和 {@link org.springframework.transaction.interceptor.TransactionAttribute} 的 javadoc。
 *
 * <h3>事务管理</h3>
 *
 * <p>此注解通常与由 {@link org.springframework.transaction.PlatformTransactionManager}
 * 管理的线程绑定事务配合使用，将事务暴露给当前执行线程内的所有数据访问操作。
 * <b>注意：这不会传播到方法内新启动的线程。</b>
 *
 * <p>或者，此注解可标记由 {@link org.springframework.transaction.ReactiveTransactionManager}
 * 管理的响应式事务，后者使用 Reactor 上下文而非线程局部变量。
 * 因此，所有参与的数据访问操作需要在同一响应式管道中的相同 Reactor 上下文内执行。
 *
 * <p><b>注意：配置 {@code ReactiveTransactionManager} 时，
 * 所有事务标记的方法预期返回响应式管道。</b>
 * void 方法或常规返回类型需关联常规 {@code PlatformTransactionManager}，
 * 例如通过 {@link #transactionManager()}。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Mark Paluch
 * @since 1.2
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Transactional {

	/**
	 * {@link #transactionManager} 的别名。
	 * @see #transactionManager
	 */
	@AliasFor("transactionManager")
	String value() default "";

	/**
	 * 指定事务的<em>限定符</em>值。
	 * <p>可用于确定目标事务管理器，匹配特定
	 * {@link org.springframework.transaction.TransactionManager TransactionManager}
	 * Bean 定义的限定符值（或 Bean 名称）。
	 * <p>自 6.2 起，带有
	 * {@link org.springframework.beans.factory.annotation.Qualifier#value() 限定符值}
	 * 的类型级 Bean 限定符注解也会被考虑。若它与特定事务管理器的
	 * 限定符值（或 Bean 名称）匹配，该事务管理器将用于
	 * 此属性上未指定限定符的事务定义。
	 * 此类类型级限定符可声明在具体类上，
	 * 同样适用于基类的事务定义，
	 * 有效覆盖任何未限定基类方法的默认事务管理器选择。
	 * @since 4.2
	 * @see #value
	 * @see org.springframework.transaction.PlatformTransactionManager
	 * @see org.springframework.transaction.ReactiveTransactionManager
	 */
	@AliasFor("value")
	String transactionManager() default "";

	/**
	 * 定义零（0）个或多个事务标签。
	 * <p>标签可用于描述事务，并可由各个事务管理器评估。
	 * 标签可能仅作描述用途，或映射到预定义的事务管理器特定选项。
	 * <p>有关如何评估事务标签的详情，请参阅实际事务管理器实现的文档。
	 * @since 5.3
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#getLabels()
	 */
	String[] label() default {};

	/**
	 * 事务传播类型。
	 * <p>默认为 {@link Propagation#REQUIRED}。
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
	 */
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * 事务隔离级别。
	 * <p>默认为 {@link Isolation#DEFAULT}。
	 * <p>专用于 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。若希望参与具有不同隔离级别的现有事务时
	 * 拒绝隔离级别声明，可将事务管理器的 "validateExistingTransaction" 标志设为 "true"。
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
	 */
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * 本事务的超时（秒）。
	 * <p>默认为底层事务系统的默认超时。
	 * <p>专用于 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。
	 * @return 超时秒数
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * 本事务的超时（秒）。
	 * <p>默认为底层事务系统的默认超时。
	 * <p>专用于 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW}，
	 * 因为仅适用于新启动的事务。
	 * @return 以字符串表示的超时秒数，例如占位符
	 * @since 5.3
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	String timeoutString() default "";

	/**
	 * 若事务实际为只读，可设为 {@code true} 的布尔标志，
	 * 允许在运行时进行相应优化。
	 * <p>默认为 {@code false}。
	 * <p>这仅作为实际事务子系统的提示；<i>不必然</i>导致写访问尝试失败。
	 * 无法解释只读提示的事务管理器在请求只读事务时<i>不会</i>抛出异常，
	 * 而是静默忽略该提示。
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	boolean readOnly() default false;

	/**
	 * 定义零（0）个或多个异常 {@linkplain Class 类型}，
	 * 必须是 {@link Throwable} 的子类，指示哪些异常类型必须导致事务回滚。
	 * <p>默认情况下，事务在 {@link RuntimeException} 和 {@link Error} 时回滚，
	 * 但不在受检异常（业务异常）时回滚。详见
	 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)}。
	 * <p>这是构造回滚规则的首选方式（相对于 {@link #rollbackForClassName}），
	 * 以类型安全方式匹配异常类型及其子类。有关回滚规则语义的更多详情，
	 * 请参阅 {@linkplain Transactional 类级 javadoc}。
	 * @see #rollbackForClassName
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * 定义零（0）个或多个异常名称模式（异常必须是 {@link Throwable} 的子类），
	 * 指示哪些异常类型必须导致事务回滚。
	 * <p>有关回滚规则语义、模式及可能意外匹配警告的更多详情，
	 * 请参阅 {@linkplain Transactional 类级 javadoc}。
	 * @see #rollbackFor
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	String[] rollbackForClassName() default {};

	/**
	 * 定义零（0）或更多异常 {@link Class 类型}，
	 * 必须是 {@link Throwable} 的子类，指示哪些异常类型<b>不</b>应导致事务回滚。
	 * <p>这是构造回滚规则的首选方式（相对于 {@link #noRollbackForClassName}），
	 * 以类型安全方式匹配异常类型及其子类。有关回滚规则语义的更多详情，
	 * 请参阅 {@linkplain Transactional 类级 javadoc}。
	 * @see #noRollbackForClassName
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * 定义零（0）或更多异常名称模式（异常必须是 {@link Throwable} 的子类），
	 * 指示哪些异常类型<b>不</b>应导致事务回滚。
	 * <p>有关回滚规则语义、模式及可能意外匹配警告的更多详情，
	 * 请参阅 {@linkplain Transactional 类级 javadoc}。
	 * @see #noRollbackFor
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	String[] noRollbackForClassName() default {};

}
