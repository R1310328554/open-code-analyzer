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
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.javapoet.ClassName;
import org.springframework.util.function.ThrowingBiFunction;

/**
 * 用于配置将生成的、执行单个 Bean 注册代码的接口。
 *
 * @author Phillip Webb
 * @since 6.0
 * @see BeanRegistrationCodeFragments
 */
public interface BeanRegistrationCode {

	/**
	 * 返回用于注册的类名。
	 * @return 类名
	 */
	ClassName getClassName();

	/**
	 * 返回注册代码所使用的 {@link GeneratedMethods}。
	 * @return 生成的方法集合
	 */
	GeneratedMethods getMethods();

	/**
	 * 向注册代码添加实例后处理器方法调用。
	 * @param methodReference 待调用的后处理方法引用。
	 * 所引用方法的函数签名须与 {@link InstanceSupplier#andThen} 兼容。
	 * @see InstanceSupplier#andThen(ThrowingBiFunction)
	 */
	void addInstancePostProcessor(MethodReference methodReference);

}
