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

import java.util.function.Supplier;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.javapoet.ClassName;

/**
 * 处理 {@link ApplicationContext} 及其 {@link BeanFactory}，生成表示 Bean 工厂状态的代码，
 * 以及在受限运行时环境中所需的提示信息。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 6.0
 */
public class ApplicationContextAotGenerator {

	/**
	 * 使用指定的 {@link GenerationContext} 对 {@link GenericApplicationContext} 进行提前处理。
	 * <p>返回用于恢复应用上下文优化状态的 {@link ApplicationContextInitializer} 的 {@link ClassName}。
	 * @param applicationContext 待处理的未刷新应用上下文
	 * @param generationContext 使用的生成上下文
	 * @return {@code ApplicationContextInitializer} 入口点的 {@code ClassName}
	 */
	public ClassName processAheadOfTime(GenericApplicationContext applicationContext,
			GenerationContext generationContext) {

		return withCglibClassHandler(new CglibClassHandler(generationContext), () -> {
			applicationContext.refreshForAotProcessing(generationContext.getRuntimeHints());
			ApplicationContextInitializationCodeGenerator codeGenerator =
					new ApplicationContextInitializationCodeGenerator(applicationContext, generationContext);
			DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();
			new BeanFactoryInitializationAotContributions(beanFactory).applyTo(generationContext, codeGenerator);
			return codeGenerator.getClassName();
		});
	}

	/**
	 * 在 CGLIB 类处理器上下文中执行任务，确保生成的代理类被正确捕获。
	 */
	private <T> T withCglibClassHandler(CglibClassHandler cglibClassHandler, Supplier<T> task) {
		try {
			ReflectUtils.setLoadedClassHandler(cglibClassHandler::handleLoadedClass);
			ReflectUtils.setGeneratedClassHandler(cglibClassHandler::handleGeneratedClass);
			return task.get();
		}
		finally {
			ReflectUtils.setLoadedClassHandler(null);
			ReflectUtils.setGeneratedClassHandler(null);
		}
	}

}
