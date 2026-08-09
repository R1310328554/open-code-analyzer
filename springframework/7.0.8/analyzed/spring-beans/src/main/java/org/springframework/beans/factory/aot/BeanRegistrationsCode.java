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

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.javapoet.ClassName;

/**
 * 用于配置将生成的 Bean 注册代码的接口。
 *
 * @author Phillip Webb
 * @since 6.0
 */
public interface BeanRegistrationsCode {

	/**
	 * 返回用于注册的类名。
	 * @return 生成类的名称
	 */
	ClassName getClassName();

	/**
	 * 返回注册代码所使用的 {@link GeneratedMethods}。
	 * @return 方法生成器
	 */
	GeneratedMethods getMethods();

}
