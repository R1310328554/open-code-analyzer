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

package org.springframework.context.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.context.weaving.AspectJWeavingEnabler;
import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;

/**
 * 注册 {@link LoadTimeWeaver} Bean 的 {@code @Configuration} 类。
 *
 * <p>使用 {@link EnableLoadTimeWeaving} 注解时会自动导入本配置类。
 * 完整用法参见 {@code @EnableLoadTimeWeaving} 的 JavaDoc。
 *
 * @author Chris Beams
 * @since 3.1
 * @see LoadTimeWeavingConfigurer
 * @see ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class LoadTimeWeavingConfiguration implements ImportAware, BeanClassLoaderAware {

	/** {@code @EnableLoadTimeWeaving} 注解属性。 */
	private @Nullable AnnotationAttributes enableLTW;

	/** 可选的自定义织入器配置器。 */
	private @Nullable LoadTimeWeavingConfigurer ltwConfigurer;

	/** 当前 Bean 工厂使用的类加载器。 */
	private @Nullable ClassLoader beanClassLoader;


	/**
	 * 从导入本配置的类上读取 {@code @EnableLoadTimeWeaving} 元数据。
	 */
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		this.enableLTW = AnnotationConfigUtils.attributesFor(importMetadata, EnableLoadTimeWeaving.class);
		if (this.enableLTW == null) {
			throw new IllegalArgumentException(
					"@EnableLoadTimeWeaving is not present on importing class " + importMetadata.getClassName());
		}
	}

	/**
	 * 注入可选的 {@link LoadTimeWeavingConfigurer}，用于自定义 {@link LoadTimeWeaver}。
	 */
	@Autowired(required = false)
	public void setLoadTimeWeavingConfigurer(LoadTimeWeavingConfigurer ltwConfigurer) {
		this.ltwConfigurer = ltwConfigurer;
	}

	/** 设置 Bean 类加载器。 */
	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * 创建并配置 {@link LoadTimeWeaver} Bean。
	 * <p>优先使用 {@link LoadTimeWeavingConfigurer} 提供的实例，否则回退到
	 * {@link DefaultContextLoadTimeWeaver}，并按注解属性决定是否启用 AspectJ 织入。
	 */
	@Bean(name = ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public LoadTimeWeaver loadTimeWeaver() {
		Assert.state(this.beanClassLoader != null, "No ClassLoader set");
		LoadTimeWeaver loadTimeWeaver = null;

		if (this.ltwConfigurer != null) {
			// 用户提供了自定义 LoadTimeWeaver 实例
			loadTimeWeaver = this.ltwConfigurer.getLoadTimeWeaver();
		}

		if (loadTimeWeaver == null) {
			// 未提供自定义织入器，使用默认实现
			loadTimeWeaver = new DefaultContextLoadTimeWeaver(this.beanClassLoader);
		}

		if (this.enableLTW != null) {
			AspectJWeaving aspectJWeaving = this.enableLTW.getEnum("aspectjWeaving");
			switch (aspectJWeaving) {
				case DISABLED -> {
					// AspectJ 织入已禁用，不做处理
				}
				case AUTODETECT -> {
					if (this.beanClassLoader.getResource(AspectJWeavingEnabler.ASPECTJ_AOP_XML_RESOURCE) == null) {
						// classpath 上无 aop.xml，视为禁用
						break;
					}
					// classpath 存在 aop.xml，启用 AspectJ 织入
					AspectJWeavingEnabler.enableAspectJWeaving(loadTimeWeaver, this.beanClassLoader);
				}
				case ENABLED -> {
					AspectJWeavingEnabler.enableAspectJWeaving(loadTimeWeaver, this.beanClassLoader);
				}
			}
		}

		return loadTimeWeaver;
	}

}
