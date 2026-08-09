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

import java.util.Map;

import javax.management.MBeanServer;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 注册 {@link AnnotationMBeanExporter} Bean 的 {@code @Configuration} 类。
 *
 * <p>使用 {@link EnableMBeanExport} 注解时会自动导入本配置类。
 * 完整用法参见该注解的 JavaDoc。
 *
 * @author Phillip Webb
 * @author Chris Beams
 * @since 3.2
 * @see EnableMBeanExport
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class MBeanExportConfiguration implements ImportAware, EnvironmentAware, BeanFactoryAware {

	/** MBean 导出器 Bean 名称。 */
	private static final String MBEAN_EXPORTER_BEAN_NAME = "mbeanExporter";

	/** {@code @EnableMBeanExport} 注解属性。 */
	private @Nullable AnnotationAttributes enableMBeanExport;

	/** 用于解析占位符的环境。 */
	private @Nullable Environment environment;

	/** 用于按名称查找 {@link MBeanServer} 的 Bean 工厂。 */
	private @Nullable BeanFactory beanFactory;


	/** 从导入类读取 {@code @EnableMBeanExport} 元数据。 */
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		Map<String, @Nullable Object> map = importMetadata.getAnnotationAttributes(EnableMBeanExport.class.getName());
		this.enableMBeanExport = AnnotationAttributes.fromMap(map);
		if (this.enableMBeanExport == null) {
			throw new IllegalArgumentException(
					"@EnableMBeanExport is not present on importing class " + importMetadata.getClassName());
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/**
	 * 创建并配置 {@link AnnotationMBeanExporter} Bean。
	 */
	@Bean(name = MBEAN_EXPORTER_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public AnnotationMBeanExporter mbeanExporter() {
		AnnotationMBeanExporter exporter = new AnnotationMBeanExporter();
		Assert.state(this.enableMBeanExport != null, "No EnableMBeanExport annotation found");
		setupDomain(exporter, this.enableMBeanExport);
		setupServer(exporter, this.enableMBeanExport);
		setupRegistrationPolicy(exporter, this.enableMBeanExport);
		return exporter;
	}

	/** 配置默认 JMX 域，并解析其中的占位符。 */
	private void setupDomain(AnnotationMBeanExporter exporter, AnnotationAttributes enableMBeanExport) {
		String defaultDomain = enableMBeanExport.getString("defaultDomain");
		if (StringUtils.hasLength(defaultDomain) && this.environment != null) {
			defaultDomain = this.environment.resolvePlaceholders(defaultDomain);
		}
		if (StringUtils.hasText(defaultDomain)) {
			exporter.setDefaultDomain(defaultDomain);
		}
	}

	/** 配置目标 {@link MBeanServer}，按 Bean 名称从工厂解析。 */
	private void setupServer(AnnotationMBeanExporter exporter, AnnotationAttributes enableMBeanExport) {
		String server = enableMBeanExport.getString("server");
		if (StringUtils.hasLength(server) && this.environment != null) {
			server = this.environment.resolvePlaceholders(server);
		}
		if (StringUtils.hasText(server)) {
			Assert.state(this.beanFactory != null, "No BeanFactory set");
			exporter.setServer(this.beanFactory.getBean(server, MBeanServer.class));
		}
	}

	/** 配置 MBean 注册冲突时的处理策略。 */
	private void setupRegistrationPolicy(AnnotationMBeanExporter exporter, AnnotationAttributes enableMBeanExport) {
		RegistrationPolicy registrationPolicy = enableMBeanExport.getEnum("registration");
		exporter.setRegistrationPolicy(registrationPolicy);
	}

}
