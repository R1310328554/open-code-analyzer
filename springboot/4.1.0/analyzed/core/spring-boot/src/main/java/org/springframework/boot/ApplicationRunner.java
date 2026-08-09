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

package org.springframework.boot;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 标识 Bean 在 {@link SpringApplication} 中应被<em>执行</em>的接口。
 * <p>
 * 同一应用上下文中可定义多个 {@link ApplicationRunner} Bean，
 * 可通过 {@link Ordered} 接口或 {@link Order @Order} 注解排序。
 *
 * @author Phillip Webb
 * @since 1.3.0
 * @see CommandLineRunner
 */
@FunctionalInterface
public interface ApplicationRunner extends Runner {

	/**
	 * 执行 Bean 的回调方法。
	 *
	 * @param args 传入的应用参数
	 * @throws Exception 发生错误时
	 */
	void run(ApplicationArguments args) throws Exception;

}
