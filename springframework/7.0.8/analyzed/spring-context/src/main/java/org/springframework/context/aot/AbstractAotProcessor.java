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
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.FileSystemGeneratedFiles;
import org.springframework.aot.generate.GeneratedFiles.Kind;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.nativex.FileNativeConfigurationWriter;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

/**
 * 基于文件系统的提前（AOT）处理抽象基类。
 *
 * <p>具体实现应重写 {@link #doProcess()}，启动对目标（通常是应用）的优化。
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 6.0
 * @param <T> 处理结果的类型
 * @see FileSystemGeneratedFiles
 * @see FileNativeConfigurationWriter
 * @see org.springframework.context.aot.ContextAotProcessor
 * @see org.springframework.test.context.aot.TestAotProcessor
 */
public abstract class AbstractAotProcessor<T> {

	/**
	 * 处理器运行时可用的系统属性名称。
	 * @since 6.2
	 * @see #doProcess()
	 */
	public static final String AOT_PROCESSING = "spring.aot.processing";

	/** AOT 处理器的通用设置。 */
	private final Settings settings;


	/**
	 * 使用提供的 {@linkplain Settings 设置} 创建新的处理器实例。
	 * @see Settings#builder()
	 */
	protected AbstractAotProcessor(Settings settings) {
		this.settings = settings;
	}


	/**
	 * 获取本 AOT 处理器的 {@linkplain Settings 设置}。
	 */
	protected Settings getSettings() {
		return this.settings;
	}

	/**
	 * 运行 AOT 处理。
	 * @return 处理结果
	 */
	public final T process() {
		try {
			System.setProperty(AOT_PROCESSING, "true");
			return doProcess();
		}
		finally {
			System.clearProperty(AOT_PROCESSING);
		}
	}

	protected abstract T doProcess();

	/**
	 * 删除源码、资源与类输出目录。
	 */
	protected void deleteExistingOutput() {
		deleteExistingOutput(getSettings().getSourceOutput(),
				getSettings().getResourceOutput(), getSettings().getClassOutput());
	}

	private void deleteExistingOutput(Path... paths) {
		for (Path path : paths) {
			try {
				FileSystemUtils.deleteRecursively(path);
			}
			catch (IOException ex) {
				throw new UncheckedIOException("Failed to delete existing output in '" + path + "'", ex);
			}
		}
	}

	protected FileSystemGeneratedFiles createFileSystemGeneratedFiles() {
		return new FileSystemGeneratedFiles(this::getRoot);
	}

	private Path getRoot(Kind kind) {
		return switch (kind) {
			case SOURCE -> getSettings().getSourceOutput();
			case RESOURCE -> getSettings().getResourceOutput();
			case CLASS -> getSettings().getClassOutput();
		};
	}

	protected void writeHints(RuntimeHints hints) {
		FileNativeConfigurationWriter writer = new FileNativeConfigurationWriter(
				getSettings().getResourceOutput(), getSettings().getGroupId(), getSettings().getArtifactId());
		writer.write(hints);
	}


	/**
	 * AOT 处理器的通用设置。
	 */
	public static final class Settings {

		/** 生成源码的输出目录。 */
		private final Path sourceOutput;

		/** 生成资源的输出目录。 */
		private final Path resourceOutput;

		/** 生成类的输出目录。 */
		private final Path classOutput;

		/** 应用的 group ID。 */
		private final String groupId;

		/** 应用的 artifact ID。 */
		private final String artifactId;

		private Settings(Path sourceOutput, Path resourceOutput, Path classOutput, String groupId, String artifactId) {
			this.sourceOutput = sourceOutput;
			this.resourceOutput = resourceOutput;
			this.classOutput = classOutput;
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		/**
		 * 创建 {@link Settings} 的 {@link Builder}。
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * 获取生成源码的输出目录。
		 */
		public Path getSourceOutput() {
			return this.sourceOutput;
		}

		/**
		 * 获取生成资源的输出目录。
		 */
		public Path getResourceOutput() {
			return this.resourceOutput;
		}

		/**
		 * 获取生成类的输出目录。
		 */
		public Path getClassOutput() {
			return this.classOutput;
		}

		/**
		 * 获取应用的 group ID。
		 */
		public String getGroupId() {
			return this.groupId;
		}

		/**
		 * 获取应用的 artifact ID。
		 */
		public String getArtifactId() {
			return this.artifactId;
		}


		/**
		 * {@link Settings} 的流式构建器 API。
		 */
		public static final class Builder {

			private @Nullable Path sourceOutput;

			private @Nullable Path resourceOutput;

			private @Nullable Path classOutput;

			private @Nullable String groupId;

			private @Nullable String artifactId;

			private Builder() {
				// internal constructor
			}

			/**
			 * 设置生成源码的输出目录。
			 * @param sourceOutput 生成源码的位置
			 * @return 本构建器，支持链式调用
			 */
			public Builder sourceOutput(Path sourceOutput) {
				this.sourceOutput = sourceOutput;
				return this;
			}

			/**
			 * 设置生成资源的输出目录。
			 * @param resourceOutput 生成资源的位置
			 * @return 本构建器，支持链式调用
			 */
			public Builder resourceOutput(Path resourceOutput) {
				this.resourceOutput = resourceOutput;
				return this;
			}

			/**
			 * 设置生成类的输出目录。
			 * @param classOutput 生成类的位置
			 * @return 本构建器，支持链式调用
			 */
			public Builder classOutput(Path classOutput) {
				this.classOutput = classOutput;
				return this;
			}

			/**
			 * 设置应用的 group ID。
			 * @param groupId 应用的 group ID，用于定位 {@code native-image.properties}
			 * @return 本构建器，支持链式调用
			 */
			public Builder groupId(String groupId) {
				Assert.hasText(groupId, "'groupId' must not be empty");
				this.groupId = groupId;
				return this;
			}

			/**
			 * 设置应用的 artifact ID。
			 * @param artifactId 应用的 artifact ID，用于定位 {@code native-image.properties}
			 * @return 本构建器，支持链式调用
			 */
			public Builder artifactId(String artifactId) {
				Assert.hasText(artifactId, "'artifactId' must not be empty");
				this.artifactId = artifactId;
				return this;
			}

			/**
			 * 构建本 {@code Builder} 中配置的 {@link Settings}。
			 */
			public Settings build() {
				Assert.notNull(this.sourceOutput, "'sourceOutput' must not be null");
				Assert.notNull(this.resourceOutput, "'resourceOutput' must not be null");
				Assert.notNull(this.classOutput, "'classOutput' must not be null");
				Assert.notNull(this.groupId, "'groupId' must not be null");
				Assert.notNull(this.artifactId, "'artifactId' must not be null");
				return new Settings(this.sourceOutput, this.resourceOutput, this.classOutput,
						this.groupId, this.artifactId);
			}
		}
	}

}
