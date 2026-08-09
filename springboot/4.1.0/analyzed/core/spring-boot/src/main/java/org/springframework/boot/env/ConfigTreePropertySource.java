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

package org.springframework.boot.env;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.OriginProvider;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.boot.origin.TextResourceOrigin.Location;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * 由目录树支持的 {@link PropertySource}，每个值对应一个文件。
 * 该 {@link PropertySource} 会递归扫描给定源目录，并为每个文件暴露一个属性；
 * 属性名为文件名，属性值为文件内容。
 * <p>
 * 目录仅在源首次创建时扫描，不会监控更新，因此不应添加或删除文件。
 * 但若属性源以 {@link Option#ALWAYS_READ} 选项创建，文件内容仍可更新。
 * 嵌套目录会包含在源中，但路径分隔符使用 {@code '.'} 而非 {@code '/'}。
 * <p>
 * 属性值以 {@link Value} 实例返回，可视为 {@link InputStreamSource} 或 {@link CharSequence}。
 * 若与配置了 {@link ApplicationConversionService} 的 {@link Environment} 一起使用，
 * 属性值可转换为 {@code String} 或 {@code byte[]}。
 * <p>
 * 此属性源通常用于读取 Kubernetes {@code configMap} 卷挂载。
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class ConfigTreePropertySource extends EnumerablePropertySource<Path>
		implements PropertySourceInfo, OriginLookup<String> {

	private static final int MAX_DEPTH = 100;

	private final Map<String, PropertyFile> propertyFiles;

	private final String[] names;

	private final Set<Option> options;

	/**
	 * 创建新的 {@link ConfigTreePropertySource} 实例。
	 *
	 * @param name 属性源名称
	 * @param sourceDirectory 底层源目录
	 */
	public ConfigTreePropertySource(String name, Path sourceDirectory) {
		this(name, sourceDirectory, EnumSet.noneOf(Option.class));
	}

	/**
	 * 创建新的 {@link ConfigTreePropertySource} 实例。
	 *
	 * @param name 属性源名称
	 * @param sourceDirectory 底层源目录
	 * @param options 属性源选项
	 */
	public ConfigTreePropertySource(String name, Path sourceDirectory, Option... options) {
		this(name, sourceDirectory, EnumSet.copyOf(Arrays.asList(options)));
	}

	private ConfigTreePropertySource(String name, Path sourceDirectory, Set<Option> options) {
		super(name, sourceDirectory);
		Assert.isTrue(Files.exists(sourceDirectory),
				() -> "'sourceDirectory' [%s] must exist".formatted(sourceDirectory));
		Assert.isTrue(Files.isDirectory(sourceDirectory),
				() -> "'sourceDirectory' [%s] must be a directory".formatted(sourceDirectory));
		this.propertyFiles = PropertyFile.findAll(sourceDirectory, options);
		this.options = options;
		this.names = StringUtils.toStringArray(this.propertyFiles.keySet());
	}

	@Override
	public String[] getPropertyNames() {
		return this.names.clone();
	}

	@Override
	public @Nullable Value getProperty(String name) {
		PropertyFile propertyFile = this.propertyFiles.get(name);
		return (propertyFile != null) ? propertyFile.getContent() : null;
	}

	@Override
	public @Nullable Origin getOrigin(String name) {
		PropertyFile propertyFile = this.propertyFiles.get(name);
		return (propertyFile != null) ? propertyFile.getOrigin() : null;
	}

	@Override
	public boolean isImmutable() {
		return !this.options.contains(Option.ALWAYS_READ);
	}

	/**
	 * 属性源选项。
	 */
	public enum Option {

		/**
		 * 访问属性值时始终读取文件内容。
		 * 未设置此选项时，属性源会在首次读取时缓存值。
		 */
		ALWAYS_READ,

		/**
		 * 将文件与目录名转为小写。
		 */
		USE_LOWERCASE_NAMES,

		/**
		 * 自动尝试去除末尾换行符。
		 */
		AUTO_TRIM_TRAILING_NEW_LINE

	}

	/**
	 * 属性源返回的值，暴露属性文件内容。
	 * 值可视为 {@link CharSequence} 或 {@link InputStreamSource}。
	 */
	public interface Value extends CharSequence, InputStreamSource {

	}

	/**
	 * 创建源时发现的单个属性文件。
	 */
	private static final class PropertyFile {

		private static final Location START_OF_FILE = new Location(0, 0);

		private final Path path;

		private final FileSystemResource resource;

		private final Origin origin;

		private final @Nullable PropertyFileContent cachedContent;

		private final boolean autoTrimTrailingNewLine;

		private PropertyFile(Path path, Set<Option> options) {
			this.path = path;
			this.resource = new FileSystemResource(path);
			this.origin = new TextResourceOrigin(this.resource, START_OF_FILE);
			this.autoTrimTrailingNewLine = options.contains(Option.AUTO_TRIM_TRAILING_NEW_LINE);
			this.cachedContent = options.contains(Option.ALWAYS_READ) ? null
					: new PropertyFileContent(path, this.resource, this.origin, true, this.autoTrimTrailingNewLine);
		}

		PropertyFileContent getContent() {
			if (this.cachedContent != null) {
				return this.cachedContent;
			}
			return new PropertyFileContent(this.path, this.resource, this.origin, false, this.autoTrimTrailingNewLine);
		}

		Origin getOrigin() {
			return this.origin;
		}

		static Map<String, PropertyFile> findAll(Path sourceDirectory, Set<Option> options) {
			try {
				Map<String, PropertyFile> propertyFiles = new TreeMap<>();
				try (Stream<Path> pathStream = Files.find(sourceDirectory, MAX_DEPTH, PropertyFile::isPropertyFile,
						FileVisitOption.FOLLOW_LINKS)) {
					pathStream.forEach((path) -> {
						String name = getName(sourceDirectory.relativize(path));
						if (StringUtils.hasText(name)) {
							if (options.contains(Option.USE_LOWERCASE_NAMES)) {
								name = name.toLowerCase(Locale.getDefault());
							}
							propertyFiles.put(name, new PropertyFile(path, options));
						}
					});
				}
				return Collections.unmodifiableMap(propertyFiles);
			}
			catch (IOException ex) {
				throw new IllegalStateException("Unable to find files in '" + sourceDirectory + "'", ex);
			}
		}

		private static boolean isPropertyFile(Path path, BasicFileAttributes attributes) {
			return !hasHiddenPathElement(path) && (attributes.isRegularFile() || attributes.isSymbolicLink());
		}

		private static boolean hasHiddenPathElement(Path path) {
			for (Path element : path) {
				if (element.toString().startsWith("..")) {
					return true;
				}
			}
			return false;
		}

		private static String getName(Path relativePath) {
			int nameCount = relativePath.getNameCount();
			if (nameCount == 1) {
				return relativePath.toString();
			}
			StringBuilder name = new StringBuilder();
			for (int i = 0; i < nameCount; i++) {
				name.append((i != 0) ? "." : "");
				name.append(relativePath.getName(i));
			}
			return name.toString();
		}

	}

	/**
	 * 已发现属性文件的内容。
	 */
	private static final class PropertyFileContent implements Value, OriginProvider {

		private final Path path;

		private final Lock resourceLock = new ReentrantLock();

		private final Resource resource;

		private final Origin origin;

		private final boolean cacheContent;

		private final boolean autoTrimTrailingNewLine;

		private volatile byte @Nullable [] content;

		private PropertyFileContent(Path path, Resource resource, Origin origin, boolean cacheContent,
				boolean autoTrimTrailingNewLine) {
			this.path = path;
			this.resource = resource;
			this.origin = origin;
			this.cacheContent = cacheContent;
			this.autoTrimTrailingNewLine = autoTrimTrailingNewLine;
		}

		@Override
		public Origin getOrigin() {
			return this.origin;
		}

		@Override
		public int length() {
			return toString().length();
		}

		@Override
		public char charAt(int index) {
			return toString().charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return toString().subSequence(start, end);
		}

		@Override
		public String toString() {
			String string = new String(getBytes());
			if (this.autoTrimTrailingNewLine) {
				string = autoTrimTrailingNewLine(string);
			}
			return string;
		}

		private String autoTrimTrailingNewLine(String string) {
			if (!string.endsWith("\n")) {
				return string;
			}
			int numberOfLines = 0;
			for (int i = 0; i < string.length(); i++) {
				char ch = string.charAt(i);
				if (ch == '\n') {
					numberOfLines++;
				}
			}
			if (numberOfLines > 1) {
				return string;
			}
			return (string.endsWith("\r\n")) ? string.substring(0, string.length() - 2)
					: string.substring(0, string.length() - 1);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!this.cacheContent) {
				assertStillExists();
				return this.resource.getInputStream();
			}
			return new ByteArrayInputStream(getBytes());
		}

		private byte[] getBytes() {
			try {
				if (!this.cacheContent) {
					assertStillExists();
					return FileCopyUtils.copyToByteArray(this.resource.getInputStream());
				}
				byte[] content = this.content;
				if (content == null) {
					assertStillExists();
					this.resourceLock.lock();
					try {
						content = this.content;
						if (content == null) {
							content = FileCopyUtils.copyToByteArray(this.resource.getInputStream());
							this.content = content;
						}
					}
					finally {
						this.resourceLock.unlock();
					}
				}
				return content;
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private void assertStillExists() {
			Assert.state(Files.exists(this.path), () -> "The property file '" + this.path + "' no longer exists");
		}

	}

}
