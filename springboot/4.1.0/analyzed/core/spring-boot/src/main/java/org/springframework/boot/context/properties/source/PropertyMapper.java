/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties.source;

import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * 用于在 {@link PropertySource} 与 {@link ConfigurationPropertySource} 之间提供映射的策略。
 * <p>
 * 应同时为 {@link ConfigurationPropertyName ConfigurationPropertyName} 类型
 * 与基于 {@code String} 的名称提供映射。这使 {@link SpringConfigurationPropertySource}
 * 可先尝试直接映射（即将 {@link ConfigurationPropertyName} 直接映射到 {@link PropertySource} 名称），
 * 再回退到 {@link EnumerablePropertySource 枚举}属性名、将其映射为 {@link ConfigurationPropertyName}
 * 并检查是否适用。详见 {@link SpringConfigurationPropertySource}。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @see SpringConfigurationPropertySource
 */
interface PropertyMapper {

	/**
	 * 默认的祖先关系检查。
	 */
	BiPredicate<ConfigurationPropertyName, ConfigurationPropertyName> DEFAULT_ANCESTOR_OF_CHECK = ConfigurationPropertyName::isAncestorOf;

	/**
	 * 从 {@link ConfigurationPropertySource} 的 {@link ConfigurationPropertyName} 提供映射。
	 *
	 * @param configurationPropertyName 待映射的名称
	 * @return 映射后的名称或空列表
	 */
	List<String> map(ConfigurationPropertyName configurationPropertyName);

	/**
	 * 从 {@link PropertySource} 属性名提供映射。
	 *
	 * @param propertySourceName 待映射的名称
	 * @return 映射后的配置属性名或 {@link ConfigurationPropertyName#EMPTY}
	 */
	ConfigurationPropertyName map(String propertySourceName);

	/**
	 * 返回可用于在考虑映射规则时检查一个名称是否为另一个名称祖先的 {@link BiPredicate}。
	 *
	 * @return 用于检查祖先关系的谓词
	 */
	default BiPredicate<ConfigurationPropertyName, ConfigurationPropertyName> getAncestorOfCheck() {
		return DEFAULT_ANCESTOR_OF_CHECK;
	}

}
