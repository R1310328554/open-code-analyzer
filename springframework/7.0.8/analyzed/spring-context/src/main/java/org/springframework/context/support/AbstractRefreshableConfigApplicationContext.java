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

package org.springframework.context.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link AbstractRefreshableApplicationContext} 子类，增加对指定配置位置的通用处理。
 * 作为基于 XML 的应用上下文实现（如 {@link ClassPathXmlApplicationContext}、
 * {@link FileSystemXmlApplicationContext}）以及
 * {@link org.springframework.web.context.support.XmlWebApplicationContext} 的基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see #setConfigLocation
 * @see #setConfigLocations
 * @see #getDefaultConfigLocations
 */
public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {

	/** 配置位置数组。 */
	private String @Nullable [] configLocations;

	/** 是否已通过 setId 显式设置 ID。 */
	private boolean setIdCalled = false;


	/**
	 * 创建无父上下文的 AbstractRefreshableConfigApplicationContext。
	 */
	public AbstractRefreshableConfigApplicationContext() {
	}

	/**
	 * 使用给定父上下文创建新的 AbstractRefreshableConfigApplicationContext。
	 * @param parent 父上下文
	 */
	public AbstractRefreshableConfigApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * 以 init-param 风格设置本应用上下文的配置位置，
	 * 即用逗号、分号或空白分隔的多个位置。
	 * <p>若未设置，实现可按需使用默认值。
	 */
	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
	}

	/**
	 * 设置本应用上下文的配置位置。
	 * <p>若未设置，实现可按需使用默认值。
	 */
	public void setConfigLocations(String @Nullable ... locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		}
		else {
			this.configLocations = null;
		}
	}

	/**
	 * 返回资源位置数组，指向构建本上下文所用的 XML Bean 定义文件。
	 * 也可包含位置模式，将通过 ResourcePatternResolver 解析。
	 * <p>默认实现返回 {@code null}。子类可覆盖以提供要加载 Bean 定义的资源位置集合。
	 * @return 资源位置数组，或 {@code null} 表示无
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected String @Nullable [] getConfigLocations() {
		return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
	}

	/**
	 * 在未显式指定配置位置时返回默认配置位置。
	 * <p>默认实现返回 {@code null}，要求显式提供配置位置。
	 * @return 默认配置位置数组（若有）
	 * @see #setConfigLocations
	 */
	protected String @Nullable [] getDefaultConfigLocations() {
		return null;
	}

	/**
	 * 解析给定路径，必要时将占位符替换为对应的环境属性值。应用于配置位置。
	 * @param path 原始文件路径
	 * @return 解析后的文件路径
	 * @see org.springframework.core.env.Environment#resolveRequiredPlaceholders(String)
	 */
	protected String resolvePath(String path) {
		return getEnvironment().resolveRequiredPlaceholders(path);
	}


	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}

	/**
	 * 默认将本上下文 ID 设为 Bean 名称，适用于上下文实例本身定义为 Bean 的情况。
	 */
	@Override
	public void setBeanName(String name) {
		if (!this.setIdCalled) {
			super.setId(name);
			setDisplayName("ApplicationContext '" + name + "'");
		}
	}

	/**
	 * 若具体上下文的构造函数中尚未刷新，则触发 {@link #refresh()}。
	 */
	@Override
	public void afterPropertiesSet() {
		if (!isActive()) {
			refresh();
		}
	}

}
