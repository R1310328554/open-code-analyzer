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

package org.springframework.context.weaving;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * 将 AspectJ 的 {@link org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter}
 * 注册到 Spring 应用上下文默认 {@link org.springframework.instrument.classloading.LoadTimeWeaver} 的后处理器。
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 */
public class AspectJWeavingEnabler
		implements BeanFactoryPostProcessor, BeanClassLoaderAware, LoadTimeWeaverAware, Ordered {

	/**
	 * {@code aop.xml} 资源位置。
	 */
	public static final String ASPECTJ_AOP_XML_RESOURCE = "META-INF/aop.xml";


	/** 当前 Bean 工厂使用的类加载器。 */
	private @Nullable ClassLoader beanClassLoader;

	/** 注入的加载时织入器。 */
	private @Nullable LoadTimeWeaver loadTimeWeaver;


	/** 设置 Bean 类加载器。 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	/** 注入应用上下文的默认 {@link LoadTimeWeaver}。 */
	@Override
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	/** 返回最高优先级，确保织入器尽早注册。 */
	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	/** 在 BeanFactory 后处理阶段启用 AspectJ 加载时织入。 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		enableAspectJWeaving(this.loadTimeWeaver, this.beanClassLoader);
	}


	/**
	 * 使用给定 {@link LoadTimeWeaver} 启用 AspectJ 织入。
	 * @param weaverToUse 要应用的 LoadTimeWeaver（为 {@code null} 时使用默认织入器）
	 * @param beanClassLoader 必要时用于创建默认织入器的类加载器
	 */
	public static void enableAspectJWeaving(
			@Nullable LoadTimeWeaver weaverToUse, @Nullable ClassLoader beanClassLoader) {

		if (weaverToUse == null) {
			if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
				weaverToUse = new InstrumentationLoadTimeWeaver(beanClassLoader);
			}
			else {
				throw new IllegalStateException("No LoadTimeWeaver available");
			}
		}
		// 注册 AspectJ 预处理器，并绕过 AspectJ 自身类以避免 LinkageError
		weaverToUse.addTransformer(
				new AspectJClassBypassingClassFileTransformer(new ClassPreProcessorAgentAdapter()));
	}


	/**
	 * 装饰 {@link ClassFileTransformer}，跳过 AspectJ 自身类的字节码处理，
	 * 以避免潜在的 {@link LinkageError}。
	 * @see org.springframework.context.annotation.LoadTimeWeavingConfiguration
	 */
	private static class AspectJClassBypassingClassFileTransformer implements ClassFileTransformer {

		/** 被装饰的底层转换器。 */
		private final ClassFileTransformer delegate;

		public AspectJClassBypassingClassFileTransformer(ClassFileTransformer delegate) {
			this.delegate = delegate;
		}

		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

			// AspectJ 内部类直接返回原始字节码，不交给织入器处理
			if (className.startsWith("org.aspectj") || className.startsWith("org/aspectj")) {
				return classfileBuffer;
			}
			return this.delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		}
	}

}
