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

package org.springframework.context.annotation;

/**
 * 用于决定应用基于 JDK/CGLIB 代理的增强，还是基于 AspectJ 织入的增强。
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.scheduling.annotation.AsyncConfigurationSelector#selectImports
 * @see org.springframework.scheduling.annotation.EnableAsync#mode()
 */
public enum AdviceMode {

	/**
	 * 基于 JDK/CGLIB 代理的增强。
	 */
	PROXY,

	/**
	 * 基于 AspectJ 织入的增强。
	 */
	ASPECTJ

}
