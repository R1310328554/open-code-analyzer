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

import org.jspecify.annotations.Nullable;

import org.springframework.core.OverridingClassLoader;

/**
 * 可在不将类引入父加载器的情况下加载类的 {@code ClassLoader}。
 * 用于满足 JPA「临时类加载器」要求，但并非 JPA 专用。
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class SimpleThrowawayClassLoader extends OverridingClassLoader {

	static {
		ClassLoader.registerAsParallelCapable();
	}


	/**
	 * 为给定 {@code ClassLoader} 创建新的 SimpleThrowawayClassLoader。
	 * @param parent 要为其构建一次性 ClassLoader 的 ClassLoader
	 */
	public SimpleThrowawayClassLoader(@Nullable ClassLoader parent) {
		super(parent);
	}

}
