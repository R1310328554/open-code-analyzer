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

package org.springframework.boot.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.ContextResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 可用于获取支持 {@code spring.factories} 中注册的额外 {@link ProtocolResolver ProtocolResolvers} 的
 * {@link ResourceLoader ResourceLoaders} 的类。
 * <p>
 * 未委托给现有 resource loader 时，无限定符的 plain path 将解析为文件系统资源，
 * 这与 {@code DefaultResourceLoader} 将无限定 path 解析为 classpath 资源不同。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @author Phillip Webb
 * @since 3.3.0
 */
public class ApplicationResourceLoader extends DefaultResourceLoader {

	@Override
	protected Resource getResourceByPath(String path) {
		return new ApplicationResource(path);
	}

	/**
	 * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link ResourceLoader}。factories 文件在调用时使用默认 class loader 解析；
	 * 资源在解析时使用当时的默认 class loader。
	 *
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.0
	 */
	public static ResourceLoader get() {
		return get((ClassLoader) null);
	}

	/**
	 * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link ResourceLoader}。factories 文件与资源均使用指定 class loader 解析。
	 *
	 * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.0
	 */
	public static ResourceLoader get(@Nullable ClassLoader classLoader) {
		return get(classLoader, SpringFactoriesLoader.forDefaultResourceLocation(classLoader));
	}

	/**
	 * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link ResourceLoader}。
	 *
	 * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader
	 * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link SpringFactoriesLoader}
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.0
	 */
	public static ResourceLoader get(@Nullable ClassLoader classLoader, SpringFactoriesLoader springFactoriesLoader) {
		return get(classLoader, springFactoriesLoader, null);
	}

	/**
	 * 返回支持 {@code spring.factories} 中额外 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link ResourceLoader}。
	 *
	 * @param classLoader 使用的 class loader，{@code null} 表示默认 class loader
	 * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link SpringFactoriesLoader}
	 * @param workingDirectory 工作目录
	 * @return {@link ResourceLoader} 实例
	 * @since 3.5.0
	 */
	public static ResourceLoader get(@Nullable ClassLoader classLoader, SpringFactoriesLoader springFactoriesLoader,
			@Nullable Path workingDirectory) {
		return get(ApplicationFileSystemResourceLoader.get(classLoader, workingDirectory), springFactoriesLoader);
	}

	/**
	 * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外
	 * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。
	 * factories 文件在调用时使用默认 class loader 解析。
	 *
	 * @param resourceLoader 委托 resource loader
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.0
	 */
	public static ResourceLoader get(ResourceLoader resourceLoader) {
		return get(resourceLoader, false);
	}

	/**
	 * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外
	 * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。
	 * factories 文件在调用时使用默认 class loader 解析。
	 *
	 * @param resourceLoader 委托 resource loader
	 * @param preferFileResolution 当合适的 {@link FilePathResolver} 支持该资源时是否优先基于文件的解析
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.1
	 */
	public static ResourceLoader get(ResourceLoader resourceLoader, boolean preferFileResolution) {
		Assert.notNull(resourceLoader, "'resourceLoader' must not be null");
		return get(resourceLoader, SpringFactoriesLoader.forDefaultResourceLocation(resourceLoader.getClassLoader()),
				preferFileResolution);
	}

	/**
	 * 返回委托给给定 resource loader 并支持 {@code spring.factories} 中额外
	 * {@link ProtocolResolver ProtocolResolvers} 的 {@link ResourceLoader}。
	 *
	 * @param resourceLoader 委托 resource loader
	 * @param springFactoriesLoader 用于加载 {@link ProtocolResolver ProtocolResolvers} 的
	 * {@link SpringFactoriesLoader}
	 * @return {@link ResourceLoader} 实例
	 * @since 3.4.0
	 */
	public static ResourceLoader get(ResourceLoader resourceLoader, SpringFactoriesLoader springFactoriesLoader) {
		return get(resourceLoader, springFactoriesLoader, false);
	}

	private static ResourceLoader get(ResourceLoader resourceLoader, SpringFactoriesLoader springFactoriesLoader,
			boolean preferFileResolution) {
		Assert.notNull(resourceLoader, "'resourceLoader' must not be null");
		Assert.notNull(springFactoriesLoader, "'springFactoriesLoader' must not be null");
		List<ProtocolResolver> protocolResolvers = springFactoriesLoader.load(ProtocolResolver.class);
		List<FilePathResolver> filePathResolvers = (preferFileResolution)
				? springFactoriesLoader.load(FilePathResolver.class) : Collections.emptyList();
		return new ProtocolResolvingResourceLoader(resourceLoader, protocolResolvers, filePathResolvers);
	}

	/**
	 * 用于加载 {@link ApplicationResource} 的内部 {@link ResourceLoader}。
	 */
	private static final class ApplicationFileSystemResourceLoader extends DefaultResourceLoader {

		private static final ResourceLoader shared = new ApplicationFileSystemResourceLoader(null, null);

		private final @Nullable Path workingDirectory;

		private ApplicationFileSystemResourceLoader(@Nullable ClassLoader classLoader,
				@Nullable Path workingDirectory) {
			super(classLoader);
			this.workingDirectory = workingDirectory;
		}

		@Override
		public Resource getResource(String location) {
			Resource resource = super.getResource(location);
			if (this.workingDirectory == null) {
				return resource;
			}
			if (!resource.isFile()) {
				return resource;
			}
			return resolveFile(resource, this.workingDirectory);
		}

		private Resource resolveFile(Resource resource, Path workingDirectory) {
			try {
				File file = resource.getFile();
				return new ApplicationResource(workingDirectory.resolve(file.toPath()));
			}
			catch (FileNotFoundException ex) {
				return resource;
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}

		@Override
		protected Resource getResourceByPath(String path) {
			return new ApplicationResource(path);
		}

		static ResourceLoader get(@Nullable ClassLoader classLoader, @Nullable Path workingDirectory) {
			if (classLoader == null && workingDirectory != null) {
				throw new IllegalArgumentException(
						"It's not possible to use null as 'classLoader' but specify a 'workingDirectory'");
			}
			return (classLoader != null) ? new ApplicationFileSystemResourceLoader(classLoader, workingDirectory)
					: ApplicationFileSystemResourceLoader.shared;
		}

	}

	/**
	 * 在 {@code spring.factories} 中注册、由 {@link ApplicationResourceLoader} 使用的策略接口，
	 * 当已加载资源也可表示为 {@link FileSystemResource} 时确定其文件路径。
	 *
	 * @author Phillip Webb
	 * @since 3.4.5
	 */
	public interface FilePathResolver {

		/**
		 * 若给定资源也可表示为 {@link FileSystemResource}，返回其 {@code path}。
		 *
		 * @param location 创建资源时使用的 location
		 * @param resource 待检查的资源
		 * @return 资源的文件路径；若无法表示为 {@link FileSystemResource} 则返回 {@code null}
		 */
		@Nullable String resolveFilePath(String location, Resource resource);

	}

	/**
	 * 应用 {@link Resource}。
	 */
	private static final class ApplicationResource extends FileSystemResource implements ContextResource {

		ApplicationResource(String path) {
			super(path);
		}

		ApplicationResource(Path path) {
			super(path);
		}

		@Override
		public String getPathWithinContext() {
			return getPath();
		}

	}

	/**
	 * 为额外 {@link ProtocolResolver ProtocolResolvers} 提供支持的 {@link ResourceLoader} 装饰器。
	 */
	private static class ProtocolResolvingResourceLoader implements ResourceLoader {

		private final ResourceLoader resourceLoader;

		private final List<ProtocolResolver> protocolResolvers;

		private final List<FilePathResolver> filePathResolvers;

		ProtocolResolvingResourceLoader(ResourceLoader resourceLoader, List<ProtocolResolver> protocolResolvers,
				List<FilePathResolver> filePathResolvers) {
			this.resourceLoader = resourceLoader;
			this.protocolResolvers = protocolResolvers;
			this.filePathResolvers = filePathResolvers;
		}

		@Override
		public @Nullable ClassLoader getClassLoader() {
			return this.resourceLoader.getClassLoader();
		}

		@Override
		public Resource getResource(String location) {
			if (StringUtils.hasLength(location)) {
				for (ProtocolResolver protocolResolver : this.protocolResolvers) {
					Resource resource = protocolResolver.resolve(location, this);
					if (resource != null) {
						return resource;
					}
				}
			}
			Resource resource = this.resourceLoader.getResource(location);
			String filePath = getFilePath(location, resource);
			return (filePath != null) ? new ApplicationResource(filePath) : resource;
		}

		private @Nullable String getFilePath(String location, Resource resource) {
			for (FilePathResolver filePathResolver : this.filePathResolvers) {
				String filePath = filePathResolver.resolveFilePath(location, resource);
				if (filePath != null) {
					return filePath;
				}
			}
			return null;
		}

	}

}
