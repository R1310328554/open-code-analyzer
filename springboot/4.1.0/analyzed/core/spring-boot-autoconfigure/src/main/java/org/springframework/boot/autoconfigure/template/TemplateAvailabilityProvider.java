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

package org.springframework.boot.autoconfigure.template;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * 指示特定模板引擎（如 FreeMarker 或 Thymeleaf）的视图模板是否可用。
 *
 * @author Andy Wilkinson
 * @since 1.1.0
 */
@FunctionalInterface
public interface TemplateAvailabilityProvider {

	/**
	 * 若给定 {@code view} 的模板可用则返回 {@code true}。
	 * @param view 视图名
	 * @param environment 环境
	 * @param classLoader 类加载器
	 * @param resourceLoader 资源加载器
	 * @return 模板是否可用
	 */
	boolean isTemplateAvailable(String view, Environment environment, ClassLoader classLoader,
			ResourceLoader resourceLoader);

}
