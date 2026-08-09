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

package org.springframework.validation.beanvalidation;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;

/**
 * 便捷的 {@link BeanPostProcessor} 实现，委托 JSR-303 提供者
 * 对带注解方法执行方法级校验。
 *
 * <p>适用方法的参数和/或返回值（后者在方法级指定，通常为内联注解）
 * 带有 JSR-303 约束注解，例如：
 *
 * <pre class="code">
 * public @NotNull Object myValidMethod(@NotNull String arg1, @Max(10) int arg2)
 * </pre>
 *
 * <p>校验出错时，拦截器可抛出 {@link ConstraintViolationException}，
 * 或将违例适配为 {@link MethodValidationResult} 并抛出 {@link MethodValidationException}。
 *
 * <p>含此类注解方法的目标类需在类型级标注 Spring 的 {@link Validated} 注解，
 * 以便搜索内联约束注解。也可通过 {@code @Validated} 指定校验分组。
 * 默认情况下，JSR-303 仅针对默认分组校验。
 *
 * @author Juergen Hoeller
 * @since 3.1
 * @see MethodValidationInterceptor
 * @see jakarta.validation.executable.ExecutableValidator
 */
@SuppressWarnings("serial")
public class MethodValidationPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor
		implements InitializingBean {

	private Class<? extends Annotation> validatedAnnotationType = Validated.class;

	private Supplier<Validator> validator = SingletonSupplier.of(() ->
			Validation.buildDefaultValidatorFactory().getValidator());

	private boolean adaptConstraintViolations;


	/**
	 * 设置 "validated" 注解类型。
	 * 默认 validated 注解类型为 {@link Validated} 注解。
	 * <p>此 setter 属性使开发者可提供自己的（非 Spring 专用）注解类型，
	 * 表示类应进行方法校验意义上的校验。
	 * @param validatedAnnotationType 所需的注解类型
	 */
	public void setValidatedAnnotationType(Class<? extends Annotation> validatedAnnotationType) {
		Assert.notNull(validatedAnnotationType, "'validatedAnnotationType' must not be null");
		this.validatedAnnotationType = validatedAnnotationType;
	}

	/**
	 * 设置用于校验方法的 JSR-303 ValidatorFactory 委托对象，
	 * 使用其默认 Validator。
	 * <p>默认为默认 ValidatorFactory 的默认 Validator。
	 * @see jakarta.validation.ValidatorFactory#getValidator()
	 */
	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validator = SingletonSupplier.of(validatorFactory::getValidator);
	}

	/**
	 * 设置用于校验方法的 JSR-303 Validator 委托对象。
	 * <p>默认为默认 ValidatorFactory 的默认 Validator。
	 */
	public void setValidator(Validator validator) {
		this.validator = () -> validator;
	}

	/**
	 * 设置用于校验方法的延迟初始化 Validator 委托对象。
	 * @since 6.0
	 * @see #setValidator
	 */
	public void setValidatorProvider(ObjectProvider<Validator> validatorProvider) {
		this.validator = validatorProvider::getObject;
	}

	/**
	 * 是否将 {@link ConstraintViolation} 适配为 {@link MethodValidationResult}。
	 * <p>默认为 {@code false}，此时违例时抛出
	 * {@link jakarta.validation.ConstraintViolationException}。
	 * 设为 {@code true} 时，改为抛出带方法校验结果的 {@link MethodValidationException}。
	 * @since 6.1
	 */
	public void setAdaptConstraintViolations(boolean adaptViolations) {
		this.adaptConstraintViolations = adaptViolations;
	}


	@Override
	public void afterPropertiesSet() {
		Pointcut pointcut = new AnnotationMatchingPointcut(this.validatedAnnotationType, true);
		this.advisor = new DefaultPointcutAdvisor(pointcut, createMethodValidationAdvice(this.validator));
	}

	/**
	 * 创建用于方法校验的 AOP 通知，与指定 "validated" 注解的切点配合应用。
	 * @param validator 要使用的 Validator 的 Supplier
	 * @return 要使用的拦截器（通常但不一定是 {@link MethodValidationInterceptor} 或其子类）
	 * @since 6.0
	 */
	protected Advice createMethodValidationAdvice(Supplier<Validator> validator) {
		return new MethodValidationInterceptor(validator, this.adaptConstraintViolations);
	}

}
