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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 从 YAML 源读取并创建 {@code Map} 的工厂，保留 YAML 声明的值类型与结构。
 *
 * <p>YAML 是易读的配置格式，具有有用的层次结构特性，大致是 JSON 的超集，
 * 具备许多类似功能。
 *
 * <p>若提供多个资源，后续资源会按层次覆盖先前的条目：任意深度上具有相同
 * {@code Map} 类型嵌套键的条目会被合并。例如：
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: two
 * three: four
 * </pre>
 *
 * 加上（列表中靠后的）
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * five: six
 * </pre>
 *
 * 等效输入为：
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * three: four
 * five: six
 * </pre>
 *
 * 注意：第一个文档中 "foo" 的值不会被第二个文档的值简单替换，而是嵌套值被合并。
 *
 * <p>需要 SnakeYAML 2.0 或更高版本。
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @since 4.1
 */
public class YamlMapFactoryBean extends YamlProcessor implements FactoryBean<Map<String, Object>>, InitializingBean {

	/** 是否以单例模式创建。 */
	private boolean singleton = true;

	/** 单例模式下的 Map 缓存。 */
	private @Nullable Map<String, Object> map;


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
			this.map = createMap();
		}
	}

	@Override
	public @Nullable Map<String, Object> getObject() {
		return (this.map != null ? this.map : createMap());
	}

	@Override
	public Class<?> getObjectType() {
		return Map.class;
	}


	/**
	 * 子类可覆盖的模板方法，用于构造本工厂返回的对象。
	 * <p>单例模式下在首次调用 {@link #getObject()} 时懒加载调用；
	 * 否则在每次 {@link #getObject()} 时调用。
	 * <p>默认实现返回合并后的 {@code Map} 实例。
	 * @return 本工厂返回的对象
	 * @see #process(MatchCallback)
	 */
	protected Map<String, Object> createMap() {
		Map<String, Object> result = new LinkedHashMap<>();
		process((properties, map) -> merge(result, map));
		return result;
	}

	/**
	 * 将源 map 层次合并到输出 map 中。
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void merge(Map<String, Object> output, Map<String, Object> map) {
		map.forEach((key, value) -> {
			Object existing = output.get(key);
			// 双方均为 Map 时递归合并嵌套结构
			if (value instanceof Map valueMap && existing instanceof Map existingMap) {
				Map<String, Object> result = new LinkedHashMap<>(existingMap);
				merge(result, valueMap);
				output.put(key, result);
			}
			else {
				output.put(key, value);
			}
		});
	}

}
