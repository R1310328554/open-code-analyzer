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

import java.util.Iterator;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;

/**
 * 简单的 {@link BeanPostProcessor}，检查 Spring 托管 Bean 中的 JSR-303 约束注解，
 * 在调用 Bean 的 init 方法（若有）之前，若存在约束违规则抛出初始化异常。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class BeanValidationPostProcessor implements BeanPostProcessor, InitializingBean {

	private @Nullable Validator validator;

	private boolean afterInitialization = false;


	/**
	 * 设置用于校验 Bean 的 JSR-303 Validator 委托对象。
	 * <p>默认为默认 ValidatorFactory 的默认 Validator。
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * 设置用于校验 Bean 的 JSR-303 ValidatorFactory 委托对象，
	 * 使用其默认 Validator。
	 * <p>默认为默认 ValidatorFactory 的默认 Validator。
	 * @see jakarta.validation.ValidatorFactory#getValidator()
	 */
	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.validator = validatorFactory.getValidator();
	}

	/**
	 * 选择是在 Bean 初始化之后（即 init 方法之后）而非之前（默认）执行校验。
	 * <p>默认为 "false"（初始化之前）。若希望 init 方法有机会
	 * 在校验前填充受约束字段，可设为 "true"（初始化之后）。
	 */
	public void setAfterInitialization(boolean afterInitialization) {
		this.afterInitialization = afterInitialization;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.validator == null) {
			this.validator = Validation.buildDefaultValidatorFactory().getValidator();
		}
	}


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!this.afterInitialization) {
			doValidate(bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (this.afterInitialization) {
			doValidate(bean);
		}
		return bean;
	}


	/**
	 * 执行给定 Bean 的校验。
	 * @param bean 要校验的 Bean 实例
	 * @see jakarta.validation.Validator#validate
	 */
	protected void doValidate(Object bean) {
		Assert.state(this.validator != null, "No Validator set");
		Object objectToValidate = AopProxyUtils.getSingletonTarget(bean);
		if (objectToValidate == null) {
			objectToValidate = bean;
		}
		Set<ConstraintViolation<Object>> result = this.validator.validate(objectToValidate);

		if (!result.isEmpty()) {
			StringBuilder sb = new StringBuilder("Bean state is invalid: ");
			for (Iterator<ConstraintViolation<Object>> it = result.iterator(); it.hasNext();) {
				ConstraintViolation<Object> violation = it.next();
				sb.append(violation.getPropertyPath()).append(" - ").append(violation.getMessage());
				if (it.hasNext()) {
					sb.append("; ");
				}
			}
			throw new BeanInitializationException(sb.toString());
		}
	}

}
