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

package org.springframework.boot.context.properties;

import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindHandler;

/**
 * 允许对 {@link ConfigurationPropertiesBindingPostProcessor} 使用的 {@link BindHandler}
 * 应用额外功能。
 *
 * @author Phillip Webb
 * @since 2.1.0
 * @see AbstractBindHandler
 */
@FunctionalInterface
public interface ConfigurationPropertiesBindHandlerAdvisor {

	/**
	 * 对源绑定处理器应用额外功能。
	 *
	 * @param bindHandler 源绑定处理器
	 * @return 委托源处理器并提供额外功能的替换绑定处理器
	 */
	BindHandler apply(BindHandler bindHandler);

}
