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

package org.springframework.context.aot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 专用于使用提前生成的构件初始化 {@link ConfigurableApplicationContext} 的
 * {@link ApplicationContextInitializer}。
 * <p>
 * 实例通常通过 {@link #forInitializerClasses(String...)} 创建，
 * 传入代码生成的初始化器类名。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 6.0
 * @param <C> 应用上下文类型
 */
@FunctionalInterface
public interface AotApplicationContextInitializer<C extends ConfigurableApplicationContext>
		extends ApplicationContextInitializer<C> {

	/**
	 * 工厂方法：创建委托给给定类名集合所加载初始化器的新
	 * {@link AotApplicationContextInitializer} 实例。
	 * @param <C> 应用上下文类型
	 * @param initializerClassNames 要加载的初始化器类名
	 * @return 新的 {@link AotApplicationContextInitializer} 实例
	 */
	static <C extends ConfigurableApplicationContext> AotApplicationContextInitializer<C> forInitializerClasses(
			String... initializerClassNames) {

		Assert.noNullElements(initializerClassNames, "'initializerClassNames' must not contain null elements");
		return applicationContext -> initialize(applicationContext, initializerClassNames);
	}

	private static <C extends ConfigurableApplicationContext> void initialize(
			C applicationContext, String... initializerClassNames) {

		Log logger = LogFactory.getLog(AotApplicationContextInitializer.class);
		ClassLoader classLoader = applicationContext.getClassLoader();
		logger.debug("Initializing ApplicationContext with AOT");
		for (String initializerClassName : initializerClassNames) {
			logger.trace(LogMessage.format("Applying %s", initializerClassName));
			instantiateInitializer(initializerClassName, classLoader).initialize(applicationContext);
		}
	}

	@SuppressWarnings("unchecked")
	static <C extends ConfigurableApplicationContext> ApplicationContextInitializer<C> instantiateInitializer(
			String initializerClassName, @Nullable ClassLoader classLoader) {
		try {
			Class<?> initializerClass = ClassUtils.resolveClassName(initializerClassName, classLoader);
			Assert.isAssignable(ApplicationContextInitializer.class, initializerClass);
			return (ApplicationContextInitializer<C>) BeanUtils.instantiateClass(initializerClass);
		}
		catch (BeanInstantiationException ex) {
			throw new IllegalArgumentException(
					"Failed to instantiate ApplicationContextInitializer: " + initializerClassName, ex);
		}
	}

}
