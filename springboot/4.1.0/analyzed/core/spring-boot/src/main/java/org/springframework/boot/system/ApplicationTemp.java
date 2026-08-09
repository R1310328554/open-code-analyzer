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

package org.springframework.boot.system;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NativeDetector;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 提供应用专属临时目录的访问。
 * 不同 Spring Boot 应用通常获得不同位置；仅重启同一应用时位置保持不变。
 * 目录名由应用来源、工作目录、类路径等信息的 SHA-1 哈希决定。
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class ApplicationTemp {

	private static final FileAttribute<?>[] NO_FILE_ATTRIBUTES = {};

	private static final EnumSet<PosixFilePermission> DIRECTORY_PERMISSIONS = EnumSet.of(PosixFilePermission.OWNER_READ,
			PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);

	private final @Nullable Class<?> sourceClass;

	private final Lock pathLock = new ReentrantLock();

	private final @Nullable UserPrincipal directoryOwner;

	private volatile @Nullable Path path;

	/**
	 * 创建新的 {@link ApplicationTemp} 实例。
	 */
	public ApplicationTemp() {
		this(null);
	}

	/**
	 * 为指定源类创建新的 {@link ApplicationTemp} 实例。
	 *
	 * @param sourceClass the source class or {@code null} 源类或 {@code null}
	 */
	public ApplicationTemp(@Nullable Class<?> sourceClass) {
		this.sourceClass = sourceClass;
		this.directoryOwner = directoryOwner();
	}

	private static @Nullable UserPrincipal directoryOwner() {
		try {
			Path tempFile = Files.createTempFile("application-temp", "-owner");
			UserPrincipal owner = Files.getOwner(tempFile);
			Files.delete(tempFile);
			return owner;
		}
		catch (UnsupportedOperationException ex) {
			return null;
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	public String toString() {
		return getDir().getAbsolutePath();
	}

	/**
	 * 返回用于应用专属临时文件的目录。
	 *
	 * @return the application temp directory 应用临时目录
	 */
	public File getDir() {
		return getPath().toFile();
	}

	/**
	 * 返回应用临时目录下的子目录。
	 *
	 * @param subDir the subdirectory name 子目录名称
	 * @return a subdirectory 子目录
	 */
	public File getDir(String subDir) {
		return createDirectory(getPath().resolve(subDir)).toFile();
	}

	private Path getPath() {
		if (this.path == null) {
			this.pathLock.lock();
			try {
				if (this.path == null) {
					String hash = HexFormat.of().withUpperCase().formatHex(generateHash(this.sourceClass));
					this.path = createDirectory(getTempDirectory().resolve(hash));
				}
			}
			finally {
				this.pathLock.unlock();
			}
		}
		Path path = this.path;
		Assert.state(path != null, "'path' must not be null");
		return path;
	}

	private Path createDirectory(Path path) {
		try {
			FileSystem fileSystem = path.getFileSystem();
			if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				Files.createDirectory(path, asFileAttributes(fileSystem, DIRECTORY_PERMISSIONS));
			}
			else {
				if (supportsPosixView(fileSystem)) {
					PosixFileAttributes attributes = Files.readAttributes(path, PosixFileAttributes.class,
							LinkOption.NOFOLLOW_LINKS);
					Assert.state(attributes.isDirectory(),
							() -> "'" + path + "' already exists but it is not a directory");
					Assert.state(DIRECTORY_PERMISSIONS.equals(attributes.permissions()), () -> "Existing directory '"
							+ path + "' does not have the permissions " + DIRECTORY_PERMISSIONS);
					assertDirectoryOwnership(attributes.owner(), path);
				}
				else {
					Assert.state(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS),
							() -> "'" + path + "' already exists but it is not a directory");
					try {
						assertDirectoryOwnership(Files.getOwner(path, LinkOption.NOFOLLOW_LINKS), path);
					}
					catch (UnsupportedOperationException ex) {
						// 不支持所有权检查，继续。
					}
				}
			}
			return path;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to create application temp directory " + path, ex);
		}
	}

	private void assertDirectoryOwnership(UserPrincipal owner, Path path) {
		if (this.directoryOwner != null) {
			Assert.state(this.directoryOwner.equals(owner),
					() -> "Existing directory '" + path + "' is not owned by " + this.directoryOwner.getName());
		}
	}

	private FileAttribute<?>[] asFileAttributes(FileSystem fileSystem, EnumSet<PosixFilePermission> ownerReadWrite) {
		if (!supportsPosixView(fileSystem)) {
			return NO_FILE_ATTRIBUTES;
		}
		return new FileAttribute<?>[] { PosixFilePermissions.asFileAttribute(ownerReadWrite) };
	}

	private boolean supportsPosixView(FileSystem fileSystem) {
		return fileSystem.supportedFileAttributeViews().contains("posix");
	}

	private Path getTempDirectory() {
		String property = System.getProperty("java.io.tmpdir");
		Assert.state(StringUtils.hasLength(property), "No 'java.io.tmpdir' property set");
		Path tempDirectory = Paths.get(property);
		Assert.state(Files.exists(tempDirectory), () -> "Temp directory '" + tempDirectory + "' does not exist");
		Assert.state(Files.isDirectory(tempDirectory),
				() -> "Temp location '" + tempDirectory + "' is not a directory");
		return tempDirectory;
	}

	private byte[] generateHash(@Nullable Class<?> sourceClass) {
		ApplicationHome home = new ApplicationHome(sourceClass);
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			update(digest, home.getSource());
			update(digest, home.getDir());
			update(digest, System.getProperty("user.dir"));
			if (!NativeDetector.inNativeImage()) {
				update(digest, System.getProperty("java.home"));
			}
			update(digest, System.getProperty("java.class.path"));
			update(digest, System.getProperty("sun.java.command"));
			update(digest, System.getProperty("sun.boot.class.path"));
			return digest.digest();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void update(MessageDigest digest, @Nullable Object source) {
		if (source != null) {
			digest.update(getUpdateSourceBytes(source));
		}
	}

	private byte[] getUpdateSourceBytes(Object source) {
		if (source instanceof File file) {
			return getUpdateSourceBytes(file.getAbsolutePath());
		}
		return source.toString().getBytes();
	}

}
