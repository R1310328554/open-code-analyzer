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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * 用于确定在判定未绑定元素时是否应包含某 {@link ConfigurationPropertySource} 的函数。
 * 若底层 {@link PropertySource} 为 systemEnvironment 或 systemProperties 属性源，
 * 则不会将其计入未绑定元素失败。
 *
 * @author Madhura Bhave
 * @since 2.0.0
 */
public class UnboundElementsSourceFilter implements Function<ConfigurationPropertySource, Boolean> {

	private static final Set<String> BENIGN_PROPERTY_SOURCE_NAMES = Collections
		.unmodifiableSet(new HashSet<>(Arrays.asList(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
				StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)));

	@Override
	public Boolean apply(ConfigurationPropertySource configurationPropertySource) {
		Object underlyingSource = configurationPropertySource.getUnderlyingSource();
		if (underlyingSource instanceof PropertySource<?> propertySource) {
			String name = propertySource.getName();
			return !BENIGN_PROPERTY_SOURCE_NAMES.contains(name);
		}
		return true;
	}

}
