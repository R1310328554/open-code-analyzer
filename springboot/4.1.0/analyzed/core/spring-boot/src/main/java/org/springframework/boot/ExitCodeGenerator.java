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

/**
 * 从运行中的命令行 {@link SpringApplication} 生成「退出码」的接口。
 * 可用于异常处理，也可直接用于 Bean。
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see SpringApplication#exit(org.springframework.context.ApplicationContext,
 * ExitCodeGenerator...)
 */
@FunctionalInterface
public interface ExitCodeGenerator {

	/**
	 * 返回应用应返回的退出码。
	 *
	 * @return 退出码
	 */
	int getExitCode();

}
