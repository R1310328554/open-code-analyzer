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

package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.support.RegisteredBean;

/**
 * 用于排除 {@link RegisteredBean} 的 AOT 处理与注册的过滤器。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
@FunctionalInterface
public interface BeanRegistrationExcludeFilter {

	/**
	 * 判断已注册的 Bean 是否应从 AOT 处理与注册中排除。
	 * @param registeredBean 已注册的 Bean
	 * @return 是否应排除
	 */
	boolean isExcludedFromAotProcessing(RegisteredBean registeredBean);

}
