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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.FileSystemGeneratedFiles;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.javapoet.ClassName;
import org.springframework.util.CollectionUtils;

/**
 * 基于文件系统的提前（AOT）处理基类实现。
 *
 * <p>具体实现通常在构建工具中用于启动应用优化。
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 6.0
 * @see org.springframework.test.context.aot.TestAotProcessor
 */
public abstract class ContextAotProcessor extends AbstractAotProcessor<ClassName> {

	/** 应用入口类（通常含 {@code main()} 方法）。 */
	private final Class<?> applicationClass;


	/**
	 * 为指定应用入口点与通用设置创建新处理器。
	 * @param applicationClass 应用入口点（含 {@code main()} 方法的类）
	 * @param settings 要应用的设置
	 */
	protected ContextAotProcessor(Class<?> applicationClass, Settings settings) {
		super(settings);
		this.applicationClass = applicationClass;
	}


	/**
	 * 获取应用入口点（通常为含 {@code main()} 方法的类）。
	 */
	protected Class<?> getApplicationClass() {
		return this.applicationClass;
	}


	/**
	 * 先清空输出目录，再调用 {@link #performAotProcessing(GenericApplicationContext)} 执行处理。
	 * @return {@code ApplicationContextInitializer} 入口点的 {@code ClassName}
	 */
	@Override
	protected ClassName doProcess() {
		deleteExistingOutput();
		try (GenericApplicationContext applicationContext = prepareApplicationContext(getApplicationClass())) {
			return performAotProcessing(applicationContext);
		}
	}

	/**
	 * 为指定应用入口点准备 {@link GenericApplicationContext}，供 {@link ApplicationContextAotGenerator} 使用。
	 * @return 未刷新的 {@link GenericApplicationContext}
	 */
	protected abstract GenericApplicationContext prepareApplicationContext(Class<?> applicationClass);

	/**
	 * 对指定上下文执行提前处理。
	 * <p>代码、资源与生成类写入配置的输出目录；同时为应用及其入口点注册运行时提示。
	 * @param applicationContext 待处理的上下文
	 */
	protected ClassName performAotProcessing(GenericApplicationContext applicationContext) {
		FileSystemGeneratedFiles generatedFiles = createFileSystemGeneratedFiles();
		DefaultGenerationContext generationContext = new DefaultGenerationContext(
				createClassNameGenerator(), generatedFiles);
		ApplicationContextAotGenerator generator = new ApplicationContextAotGenerator();
		ClassName generatedInitializerClassName = generator.processAheadOfTime(applicationContext, generationContext);
		registerEntryPointHint(generationContext, generatedInitializerClassName);
		generationContext.writeGeneratedContent();
		writeHints(generationContext.getRuntimeHints());
		writeNativeImageProperties(getDefaultNativeImageArguments(getApplicationClass().getName()));
		return generatedInitializerClassName;
	}

	/**
	 * 自定义 {@link ClassNameGenerator} 的回调。
	 * <p>默认使用以配置的 {@linkplain #getApplicationClass() 应用入口点}
	 * 为默认目标的 {@link ClassNameGenerator}。
	 * @return 类名生成器
	 */
	protected ClassNameGenerator createClassNameGenerator() {
		return new ClassNameGenerator(ClassName.get(getApplicationClass()));
	}

	/**
	 * 返回要使用的 native image 参数。
	 * <p>默认添加主类及标准应用标志。
	 * <p>若返回空列表，则不贡献 {@code native-image.properties}。
	 * @param applicationClassName 应用入口点的全限定类名
	 * @return 要贡献的 native image 选项
	 */
	protected List<String> getDefaultNativeImageArguments(String applicationClassName) {
		List<String> args = new ArrayList<>();
		args.add("-H:Class=" + applicationClassName);
		args.add("--no-fallback");
		return args;
	}

	private void registerEntryPointHint(DefaultGenerationContext generationContext,
			ClassName generatedInitializerClassName) {

		TypeReference generatedType = TypeReference.of(generatedInitializerClassName.canonicalName());
		TypeReference applicationType = TypeReference.of(getApplicationClass());
		ReflectionHints reflection = generationContext.getRuntimeHints().reflection();
		reflection.registerType(applicationType);
		reflection.registerType(generatedType, typeHint -> typeHint.onReachableType(applicationType)
				.withConstructor(Collections.emptyList(), ExecutableMode.INVOKE));
	}

	private void writeNativeImageProperties(List<String> args) {
		if (CollectionUtils.isEmpty(args)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Args = ");
		sb.append(String.join(String.format(" \\%n"), args));
		Path file = getSettings().getResourceOutput().resolve("META-INF/native-image/" +
				getSettings().getGroupId() + "/" + getSettings().getArtifactId() + "/native-image.properties");
		try {
			if (!Files.exists(file)) {
				Files.createDirectories(file.getParent());
				Files.createFile(file);
			}
			Files.writeString(file, sb.toString());
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to write native-image.properties", ex);
		}
	}

}
