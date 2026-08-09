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

package org.springframework.beans;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.ResourceEditor;

/**
 * {@link RuntimeHintsRegistrar} 实现：为 {@link BeanUtils#findEditorByConvention(Class)}
 * 中常用的按约定查找 PropertyEditor 场景注册运行时提示（AOT / 原生镜像反射提示）。
 *
 * @author Sebastien Deleuze
 * @since 6.0.10
 */
class BeanUtilsRuntimeHints implements RuntimeHintsRegistrar {

	/**
	 * 注册反射提示：声明式构造器可被调用，以便按约定实例化常见 PropertyEditor。
	 * <p>始终注册 {@link ResourceEditor}；若类路径上存在
	 * {@code org.springframework.http.MediaTypeEditor}，则一并注册。
	 * @param hints 待填充的 {@link RuntimeHints}
	 * @param classLoader 用于探测可选类型是否存在的类加载器，可为 {@code null}
	 */
	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		ReflectionHints reflectionHints = hints.reflection();
		// ResourceEditor 随 core 提供，始终需要可反射调用其声明的构造器
		reflectionHints.registerType(ResourceEditor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
		// MediaTypeEditor 位于 spring-web，类路径上有才注册
		reflectionHints.registerTypeIfPresent(classLoader, "org.springframework.http.MediaTypeEditor",
				MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
	}

}
