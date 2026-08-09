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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;

/**
 * 定义向 {@link ClassLoader} 添加一个或多个
 * {@link ClassFileTransformer ClassFileTransformer} 的契约。
 *
 * <p>实现类可对当前上下文 {@code ClassLoader} 操作，
 * 或暴露其自身的可织入 {@code ClassLoader}。
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @since 2.0
 * @see java.lang.instrument.ClassFileTransformer
 */
public interface LoadTimeWeaver {

	/**
	 * 添加由本 {@code LoadTimeWeaver} 应用的 {@code ClassFileTransformer}。
	 * @param transformer 要添加的 {@code ClassFileTransformer}
	 */
	void addTransformer(ClassFileTransformer transformer);

	/**
	 * 返回支持通过用户定义的
	 * {@link ClassFileTransformer ClassFileTransformer}
	 * 进行 AspectJ 风格加载时织入的 {@code ClassLoader}。
	 * <p>可以是当前 {@code ClassLoader}，也可以是由本
	 * {@link LoadTimeWeaver} 实例创建的 {@code ClassLoader}。
	 * @return 将根据已注册转换器暴露织入后类的 {@code ClassLoader}
	 */
	ClassLoader getInstrumentableClassLoader();

	/**
	 * 返回一次性 {@code ClassLoader}，用于加载并检查类而不影响父 {@code ClassLoader}。
	 * <p>不应返回与 {@link #getInstrumentableClassLoader()} 调用结果相同的
	 * {@link ClassLoader} 实例。
	 * @return 临时一次性 {@code ClassLoader}；每次调用应返回新实例，且不携带既有状态
	 */
	ClassLoader getThrowawayClassLoader();

}
