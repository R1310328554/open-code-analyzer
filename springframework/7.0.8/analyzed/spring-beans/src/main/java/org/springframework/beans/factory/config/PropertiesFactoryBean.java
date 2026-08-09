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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;

/**
 * 将 classpath 上的 properties 文件以 {@link Properties} 实例形式暴露到 Bean 工厂。
 * 可通过 bean 引用为任意 Properties 类型属性注入值。
 *
 * <p>支持从 properties 文件加载，和/或在本 FactoryBean 上设置本地属性。
 * 创建的 Properties 会合并加载值与本地值。若既未设置 location 也未设置本地属性，
 * 初始化时将抛出异常。
 *
 * <p>可创建单例，或在每次请求时创建新对象。默认为单例。
 *
 * @author Juergen Hoeller
 * @see #setLocation
 * @see #setProperties
 * @see #setLocalOverride
 * @see java.util.Properties
 */
public class PropertiesFactoryBean extends PropertiesLoaderSupport
		implements FactoryBean<Properties>, InitializingBean {

	/** 是否以单例模式创建共享的 Properties 实例。 */
	private boolean singleton = true;

	/** 单例模式下的 Properties 实例缓存。 */
	private @Nullable Properties singletonInstance;


	/**
	 * 设置是否创建共享的单例 Properties，或在每次请求时创建新实例。
	 * <p>默认为 {@code true}（共享单例）。
	 */
	public final void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public final boolean isSingleton() {
		return this.singleton;
	}


	@Override
	public final void afterPropertiesSet() throws IOException {
		if (this.singleton) {
			this.singletonInstance = createProperties();
		}
	}

	@Override
	public final @Nullable Properties getObject() throws IOException {
		if (this.singleton) {
			return this.singletonInstance;
		}
		else {
			return createProperties();
		}
	}

	@Override
	public Class<Properties> getObjectType() {
		return Properties.class;
	}


	/**
	 * 子类可覆盖的模板方法，用于构造本工厂返回的对象。默认实现返回合并后的 Properties。
	 * <p>单例模式下在 FactoryBean 初始化时调用；否则在每次 {@link #getObject()} 时调用。
	 * @return 本工厂返回的对象
	 * @throws IOException properties 加载过程中发生异常时
	 * @see #mergeProperties()
	 */
	protected Properties createProperties() throws IOException {
		return mergeProperties();
	}

}
