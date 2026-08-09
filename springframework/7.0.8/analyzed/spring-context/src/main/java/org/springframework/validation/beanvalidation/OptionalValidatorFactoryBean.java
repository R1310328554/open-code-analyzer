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

import jakarta.validation.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link LocalValidatorFactoryBean} 子类，在无 Bean Validation 提供者可用时
 * 将 {@link org.springframework.validation.Validator} 调用变为空操作。
 *
 * <p>当存在 {@code jakarta.validation} API 但未显式配置 Validator 时，
 * 这是 Spring MVC 配置命名空间实际使用的类。
 *
 * @author Juergen Hoeller
 * @since 4.0.1
 */
public class OptionalValidatorFactoryBean extends LocalValidatorFactoryBean {

	@Override
	public void afterPropertiesSet() {
		try {
			super.afterPropertiesSet();
		}
		catch (ValidationException ex) {
			Log logger = LogFactory.getLog(getClass());
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to set up a Bean Validation provider", ex);
			}
			else if (logger.isInfoEnabled()) {
				logger.info("Failed to set up a Bean Validation provider: " + ex);
			}
		}
	}

}
