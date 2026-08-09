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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;

/**
 * AOP Alliance {@link MethodInterceptor} 实现，
 * 委托 JSR-303 提供者对带注解方法执行方法级校验。
 *
 * <p>适用方法的参数和/或返回值（后者在方法级指定，通常为内联注解）
 * 带有 {@link jakarta.validation.Constraint} 注解。
 *
 * <p>For example: {@code public @NotNull Object myValidMethod(@NotNull String arg1, @Max(10) int arg2)}
 *
 * <p>校验出错时，拦截器可抛出 {@link ConstraintViolationException}，
 * 或将违例适配为 {@link MethodValidationResult} 并抛出 {@link MethodValidationException}。
 *
 * <p>可通过 Spring 的 {@link Validated} 注解在包含目标类的类型级指定校验分组，
 * 应用于该类的所有 public 服务方法。默认情况下，JSR-303 仅针对默认分组校验。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see MethodValidationPostProcessor
 * @see jakarta.validation.executable.ExecutableValidator
 */
public class MethodValidationInterceptor implements MethodInterceptor {

	private static final boolean REACTOR_PRESENT = ClassUtils.isPresent(
			"reactor.core.publisher.Mono", MethodValidationInterceptor.class.getClassLoader());


	private final MethodValidationAdapter validationAdapter;

	private final boolean adaptViolations;


	/**
	 * 使用底层默认 JSR-303 校验器创建新的 MethodValidationInterceptor。
	 */
	public MethodValidationInterceptor() {
		this(new MethodValidationAdapter(), false);
	}

	/**
	 * 使用给定 JSR-303 ValidatorFactory 创建新的 MethodValidationInterceptor。
	 * @param validatorFactory 要使用的 JSR-303 ValidatorFactory
	 */
	public MethodValidationInterceptor(ValidatorFactory validatorFactory) {
		this(new MethodValidationAdapter(validatorFactory), false);
	}

	/**
	 * 使用给定 JSR-303 Validator 创建新的 MethodValidationInterceptor。
	 * @param validator 要使用的 JSR-303 Validator
	 */
	public MethodValidationInterceptor(Validator validator) {
		this(new MethodValidationAdapter(validator), false);
	}

	/**
	 * 为所提供的（可能延迟初始化的）Validator 创建新的 MethodValidationInterceptor。
	 * @param validator 要使用的 Validator 的 Supplier
	 * @since 6.0
	 */
	public MethodValidationInterceptor(Supplier<Validator> validator) {
		this(validator, false);
	}

	/**
	 * Create a new MethodValidationInterceptor for the supplied
	 * (potentially lazily initialized) Validator.
	 * @param validator a Supplier for the Validator to use
	 * @param adaptViolations 是否适配 {@link ConstraintViolation}；
	 * 若为 {@code true} 则抛出 {@link MethodValidationException}，
	 * 若为 {@code false} 则抛出 {@link ConstraintViolationException}
	 * @since 6.1
	 */
	public MethodValidationInterceptor(Supplier<Validator> validator, boolean adaptViolations) {
		this(new MethodValidationAdapter(validator), adaptViolations);
	}

	private MethodValidationInterceptor(MethodValidationAdapter validationAdapter, boolean adaptViolations) {
		this.validationAdapter = validationAdapter;
		this.adaptViolations = adaptViolations;
	}


	@Override
	public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
		// Avoid Validator invocation on FactoryBean.getObjectType/isSingleton
		if (isFactoryBeanMetadataMethod(invocation.getMethod())) {
			return invocation.proceed();
		}

		Object target = getTarget(invocation);
		Method method = invocation.getMethod();
		@Nullable Object[] arguments = invocation.getArguments();
		Class<?>[] groups = determineValidationGroups(invocation);

		if (REACTOR_PRESENT) {
			arguments = ReactorValidationHelper.insertAsyncValidation(
					this.validationAdapter.getSpringValidatorAdapter(), this.adaptViolations,
					target, method, arguments);
		}

		Set<ConstraintViolation<Object>> violations;

		if (this.adaptViolations) {
			this.validationAdapter.applyArgumentValidation(target, method, null, arguments, groups);
		}
		else {
			violations = this.validationAdapter.invokeValidatorForArguments(target, method, arguments, groups);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		}

		Object returnValue = invocation.proceed();

		if (this.adaptViolations) {
			this.validationAdapter.applyReturnValueValidation(target, method, null, returnValue, groups);
		}
		else {
			violations = this.validationAdapter.invokeValidatorForReturnValue(target, method, returnValue, groups);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		}

		return returnValue;
	}

	private static Object getTarget(MethodInvocation invocation) {
		Object target = invocation.getThis();
		if (target == null && invocation instanceof ProxyMethodInvocation methodInvocation) {
			// Allow validation for AOP proxy without a target
			target = methodInvocation.getProxy();
		}
		Assert.state(target != null, "Target must not be null");
		return target;
	}

	private boolean isFactoryBeanMetadataMethod(Method method) {
		Class<?> clazz = method.getDeclaringClass();

		// Call from interface-based proxy handle, allowing for an efficient check?
		if (clazz.isInterface()) {
			return ((clazz == FactoryBean.class || clazz == SmartFactoryBean.class) &&
					!method.getName().equals("getObject"));
		}

		// Call from CGLIB proxy handle, potentially implementing a FactoryBean method?
		Class<?> factoryBeanType = null;
		if (SmartFactoryBean.class.isAssignableFrom(clazz)) {
			factoryBeanType = SmartFactoryBean.class;
		}
		else if (FactoryBean.class.isAssignableFrom(clazz)) {
			factoryBeanType = FactoryBean.class;
		}
		return (factoryBeanType != null && !method.getName().equals("getObject") &&
				ClassUtils.hasMethod(factoryBeanType, method));
	}

	/**
	 * 确定给定方法调用要针对的校验分组。
	 * <p>默认为 {@link Validated} 注解在方法上、
	 * 方法所属目标类上指定的校验分组，
	 * 或对于无目标对象的 AOP 代理（行为全在 advisor 中）还检查被代理接口。
	 * @param invocation 当前 MethodInvocation
	 * @return 适用的校验分组，以 Class 数组形式
	 */
	protected Class<?>[] determineValidationGroups(MethodInvocation invocation) {
		Object target = getTarget(invocation);
		return ValidationAnnotationUtils.determineValidationGroups(target, invocation.getMethod());
	}


	/**
	 * 辅助类，为响应式参数装饰异步校验。
	 */
	private static final class ReactorValidationHelper {

		private static final ReactiveAdapterRegistry reactiveAdapterRegistry =
				ReactiveAdapterRegistry.getSharedInstance();


		static @Nullable Object[] insertAsyncValidation(
				Supplier<SpringValidatorAdapter> validatorAdapterSupplier, boolean adaptViolations,
				Object target, Method method, @Nullable Object[] arguments) {

			for (int i = 0; i < method.getParameterCount(); i++) {
				if (arguments[i] == null) {
					continue;
				}
				Class<?> parameterType = method.getParameterTypes()[i];
				ReactiveAdapter reactiveAdapter = reactiveAdapterRegistry.getAdapter(parameterType);
				if (reactiveAdapter == null || reactiveAdapter.isNoValue()) {
					continue;
				}
				Class<?>[] groups = determineValidationGroups(method.getParameters()[i]);
				if (groups == null) {
					continue;
				}
				SpringValidatorAdapter validatorAdapter = validatorAdapterSupplier.get();
				MethodParameter param = new MethodParameter(method, i);
				arguments[i] = (reactiveAdapter.isMultiValue() ?
						Flux.from(reactiveAdapter.toPublisher(arguments[i])).doOnNext(value ->
								validate(validatorAdapter, adaptViolations, target, method, param, value, groups)) :
						Mono.from(reactiveAdapter.toPublisher(arguments[i])).doOnNext(value ->
								validate(validatorAdapter, adaptViolations, target, method, param, value, groups)));
			}
			return arguments;
		}

		private static Class<?> @Nullable [] determineValidationGroups(Parameter parameter) {
			Validated validated = AnnotationUtils.findAnnotation(parameter, Validated.class);
			if (validated != null) {
				return validated.value();
			}
			Valid valid = AnnotationUtils.findAnnotation(parameter, Valid.class);
			if (valid != null) {
				return new Class<?>[0];
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private static <T> void validate(
				SpringValidatorAdapter validatorAdapter, boolean adaptViolations,
				Object target, Method method, MethodParameter parameter, Object argument, Class<?>[] groups) {

			if (adaptViolations) {
				Errors errors = new BeanPropertyBindingResult(argument, argument.getClass().getSimpleName());
				validatorAdapter.validate(argument, errors);
				if (errors.hasErrors()) {
					ParameterErrors paramErrors = new ParameterErrors(parameter, argument, errors, null, null, null);
					List<ParameterValidationResult> results = Collections.singletonList(paramErrors);
					throw new MethodValidationException(MethodValidationResult.create(target, method, results));
				}
			}
			else {
				Set<ConstraintViolation<T>> violations = validatorAdapter.validate((T) argument, groups);
				if (!violations.isEmpty()) {
					throw new ConstraintViolationException(violations);
				}
			}
		}
	}

}
