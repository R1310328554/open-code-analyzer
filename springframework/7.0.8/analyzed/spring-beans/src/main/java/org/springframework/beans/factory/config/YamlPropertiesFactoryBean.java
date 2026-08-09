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

import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.CollectionFactory;

/**
 * 从 YAML 源读取并创建 {@link java.util.Properties} 的工厂，
 * 暴露扁平化的 String 属性值结构。
 *
 * <p>YAML 是易读的配置格式，具有有用的层次结构特性，大致是 JSON 的超集，
 * 具备许多类似功能。
 *
 * <p><b>注意：所有暴露的值均为 {@code String} 类型</b>，以便通过常见的
 * {@link Properties#getProperty} 方法访问（例如通过
 * {@link PropertyResourceConfigurer#setProperties(Properties)} 进行配置属性解析）。
 * 若不需要此行为，请使用 {@link YamlMapFactoryBean}。
 *
 * <p>本工厂创建的 Properties 对层次对象使用嵌套路径。例如以下 YAML：
 *
 * <pre class="code">
 * environments:
 *   dev:
 *     url: https://dev.bar.com
 *     name: Developer Setup
 *   prod:
 *     url: https://foo.bar.com
 *     name: My Cool App
 * </pre>
 *
 * 转换为以下 properties：
 *
 * <pre class="code">
 * environments.dev.url=https://dev.bar.com
 * environments.dev.name=Developer Setup
 * environments.prod.url=https://foo.bar.com
 * environments.prod.name=My Cool App
 * </pre>
 *
 * 列表以带 <code>[]</code> 下标的属性键拆分，例如以下 YAML：
 *
 * <pre class="code">
 * servers:
 * - dev.bar.com
 * - foo.bar.com
 * </pre>
 *
 * 变为如下 properties：
 *
 * <pre class="code">
 * servers[0]=dev.bar.com
 * servers[1]=foo.bar.com
 * </pre>
 *
 * <p>需要 SnakeYAML 2.0 或更高版本。
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.1
 */
public class YamlPropertiesFactoryBean extends YamlProcessor implements FactoryBean<Properties>, InitializingBean {

	/** 是否以单例模式创建。 */
	private boolean singleton = true;

	/** 单例模式下的 Properties 缓存。 */
	private @Nullable Properties properties;


	/**
	 * 设置是否创建单例，否则每次请求创建新对象。默认为 {@code true}（单例）。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	@Override
	public void afterPropertiesSet() {
		if (isSingleton()) {
			this.properties = createProperties();
		}
	}

	@Override
	public @Nullable Properties getObject() {
		return (this.properties != null ? this.properties : createProperties());
	}

	@Override
	public Class<?> getObjectType() {
		return Properties.class;
	}


	/**
	 * 子类可覆盖的模板方法，用于构造本工厂返回的对象。默认实现返回
	 * 包含所有资源内容的 properties。
	 * <p>单例模式下在首次调用 {@link #getObject()} 时懒加载调用；
	 * 否则在每次 {@link #getObject()} 时调用。
	 * @return 本工厂返回的对象
	 * @see #process(MatchCallback)
	 */
	protected Properties createProperties() {
		Properties result = CollectionFactory.createStringAdaptingProperties();
		process((properties, map) -> result.putAll(properties));
		return result;
	}

}
