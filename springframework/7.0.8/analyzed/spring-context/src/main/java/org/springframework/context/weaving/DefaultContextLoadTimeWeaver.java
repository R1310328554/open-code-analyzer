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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver;
import org.springframework.instrument.classloading.glassfish.GlassFishLoadTimeWeaver;
import org.springframework.instrument.classloading.jboss.JBossLoadTimeWeaver;
import org.springframework.instrument.classloading.tomcat.TomcatLoadTimeWeaver;
import org.springframework.util.Assert;

/**
 * 应用上下文中使用的默认 {@link LoadTimeWeaver} Bean，
 * 封装自动检测到的内部 {@code LoadTimeWeaver} 实现。
 *
 * <p>通常以默认 Bean 名 "{@code loadTimeWeaver}" 注册；
 * 最便捷的方式是使用 Spring 的 {@code <context:load-time-weaver>} XML 标签
 * 或在 {@code @Configuration} 类上使用 {@code @EnableLoadTimeWeaving}。
 *
 * <p>本类在运行时检测环境，自动选择最合适的织入器实现，包括
 * {@link InstrumentationSavingAgent Spring VM 代理} 以及
 * Spring {@link ReflectiveLoadTimeWeaver} 所支持的各类 {@link ClassLoader}。
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Costin Leau
 * @since 2.5
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public class DefaultContextLoadTimeWeaver implements LoadTimeWeaver, BeanClassLoaderAware, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** 根据运行环境选定的底层织入器。 */
	private @Nullable LoadTimeWeaver loadTimeWeaver;


	public DefaultContextLoadTimeWeaver() {
	}

	public DefaultContextLoadTimeWeaver(ClassLoader beanClassLoader) {
		setBeanClassLoader(beanClassLoader);
	}


	/**
	 * 根据类加载器类型自动选择织入器实现：
	 * 优先尝试应用服务器专用实现，其次 JVM Instrumentation，最后反射式织入器。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		LoadTimeWeaver serverSpecificLoadTimeWeaver = createServerSpecificLoadTimeWeaver(classLoader);
		if (serverSpecificLoadTimeWeaver != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Determined server-specific load-time weaver: " +
						serverSpecificLoadTimeWeaver.getClass().getName());
			}
			this.loadTimeWeaver = serverSpecificLoadTimeWeaver;
		}
		else if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
			logger.debug("Found Spring's JVM agent for instrumentation");
			this.loadTimeWeaver = new InstrumentationLoadTimeWeaver(classLoader);
		}
		else {
			try {
				this.loadTimeWeaver = new ReflectiveLoadTimeWeaver(classLoader);
				if (logger.isDebugEnabled()) {
					logger.debug("Using reflective load-time weaver for class loader: " +
							this.loadTimeWeaver.getInstrumentableClassLoader().getClass().getName());
				}
			}
			catch (IllegalStateException ex) {
				throw new IllegalStateException(ex.getMessage() + " Specify a custom LoadTimeWeaver or start your " +
						"Java virtual machine with Spring's agent: -javaagent:spring-instrument-{version}.jar");
			}
		}
	}

	/**
	 * 按类加载器类型尝试创建 Tomcat / GlassFish / JBoss 专用织入器。
	 * <p>本方法永不抛出异常，以便在服务器专用织入器不可用时继续尝试其他方式。
	 * 仅凭 ClassLoader 名称判断织入器可能因其他不匹配而合法失败，因此需要这种容错逻辑。
	 */
	protected @Nullable LoadTimeWeaver createServerSpecificLoadTimeWeaver(ClassLoader classLoader) {
		String name = classLoader.getClass().getName();
		try {
			if (name.startsWith("org.apache.catalina")) {
				return new TomcatLoadTimeWeaver(classLoader);
			}
			else if (name.startsWith("org.glassfish")) {
				return new GlassFishLoadTimeWeaver(classLoader);
			}
			else if (name.startsWith("org.jboss.modules")) {
				return new JBossLoadTimeWeaver(classLoader);
			}
		}
		catch (Exception ex) {
			if (logger.isInfoEnabled()) {
				logger.info("Could not obtain server-specific LoadTimeWeaver: " + ex.getMessage());
			}
		}
		return null;
	}

	/** 销毁时移除已注册的类文件转换器。 */
	@Override
	public void destroy() {
		if (this.loadTimeWeaver instanceof InstrumentationLoadTimeWeaver iltw) {
			if (logger.isDebugEnabled()) {
				logger.debug("Removing all registered transformers for class loader: " +
						this.loadTimeWeaver.getInstrumentableClassLoader().getClass().getName());
			}
			iltw.removeTransformers();
		}
	}


	/** 向底层织入器注册类文件转换器。 */
	@Override
	public void addTransformer(ClassFileTransformer transformer) {
		Assert.state(this.loadTimeWeaver != null, "Not initialized");
		this.loadTimeWeaver.addTransformer(transformer);
	}

	/** 返回可被织入的类加载器。 */
	@Override
	public ClassLoader getInstrumentableClassLoader() {
		Assert.state(this.loadTimeWeaver != null, "Not initialized");
		return this.loadTimeWeaver.getInstrumentableClassLoader();
	}

	/** 返回用于临时类加载的一次性类加载器。 */
	@Override
	public ClassLoader getThrowawayClassLoader() {
		Assert.state(this.loadTimeWeaver != null, "Not initialized");
		return this.loadTimeWeaver.getThrowawayClassLoader();
	}

}
