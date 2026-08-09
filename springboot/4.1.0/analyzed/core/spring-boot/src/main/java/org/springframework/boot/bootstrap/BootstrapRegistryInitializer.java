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

package org.springframework.boot.bootstrap;

/**
 * 在 {@link BootstrapRegistry} 使用前对其进行初始化的回调接口。
 *
 * @author Phillip Webb
 * @since 4.0.0
 * @see BootstrapRegistry
 */
@FunctionalInterface
public interface BootstrapRegistryInitializer {

	/**
	 * 使用所需注册项初始化给定的 {@link BootstrapRegistry}。
	 * @param registry 要初始化的注册表
	 */
	void initialize(BootstrapRegistry registry);

}
