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

package org.springframework.context.aot;

import java.util.function.Consumer;

import org.springframework.aot.generate.GeneratedFiles;
import org.springframework.aot.generate.GeneratedFiles.Kind;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint.Builder;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.support.ClassHintUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.io.ByteArrayResource;

/**
 * 处理 CGLIB 类：将其加入 {@link GenerationContext}，
 * 并注册实例化所需的运行时提示。
 *
 * @author Stephane Nicoll
 * @since 6.0
 * @see ReflectUtils#setGeneratedClassHandler
 * @see ReflectUtils#setLoadedClassHandler
 * @see ClassHintUtils#registerProxyIfNecessary
 */
class CglibClassHandler {

	private static final Consumer<Builder> instantiateCglibProxy = hint ->
			hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

	/** AOT 运行时反射提示。 */
	private final RuntimeHints runtimeHints;

	/** 生成文件写入目标。 */
	private final GeneratedFiles generatedFiles;


	CglibClassHandler(GenerationContext generationContext) {
		this.runtimeHints = generationContext.getRuntimeHints();
		this.generatedFiles = generationContext.getGeneratedFiles();
	}


	/**
	 * 处理指定的 CGLIB 生成类。
	 * @param cglibClassName 生成类的名称
	 * @param content 生成类的字节码
	 */
	public void handleGeneratedClass(String cglibClassName, byte[] content) {
		registerHints(TypeReference.of(cglibClassName));
		String path = cglibClassName.replace(".", "/") + ".class";
		this.generatedFiles.addFile(Kind.CLASS, path, new ByteArrayResource(content));
	}

	/**
	 * 处理已加载的 CGLIB 类。
	 * @param cglibClass 已加载的 CGLIB 类
	 */
	public void handleLoadedClass(Class<?> cglibClass) {
		registerHints(TypeReference.of(cglibClass));
	}

	private void registerHints(TypeReference cglibTypeReference) {
		this.runtimeHints.reflection().registerType(cglibTypeReference, instantiateCglibProxy);
	}

}
