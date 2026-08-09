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
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 提供应用主目录（home directory）访问。
 * <p>
 * 尝试为 JAR 文件、解压归档及直接运行的应用选择合理的主目录。
 *
 * @author Phillip Webb
 * @author Raja Kolli
 * @since 2.0.0
 */
public class ApplicationHome {

	private final @Nullable File source;

	private final File dir;

	/**
	 * 创建新的 {@link ApplicationHome} 实例。
	 */
	public ApplicationHome() {
		this(null);
	}

	/**
	 * 为指定源类创建新的 {@link ApplicationHome} 实例。
	 * @param sourceClass the source class or {@code null} 源类或 {@code null}
	 */
	public ApplicationHome(@Nullable Class<?> sourceClass) {
		this.source = findSource((sourceClass != null) ? sourceClass : getStartClass());
		this.dir = findHomeDir(this.source);
	}

	private @Nullable Class<?> getStartClass() {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			return getStartClass(classLoader.getResources("META-INF/MANIFEST.MF"));
		}
		catch (Exception ex) {
			return null;
		}
	}

	private @Nullable Class<?> getStartClass(Enumeration<URL> manifestResources) {
		while (manifestResources.hasMoreElements()) {
			try (InputStream inputStream = manifestResources.nextElement().openStream()) {
				Manifest manifest = new Manifest(inputStream);
				String startClass = manifest.getMainAttributes().getValue("Start-Class");
				if (startClass != null) {
					return ClassUtils.forName(startClass, getClass().getClassLoader());
				}
			}
			catch (Exception ex) {
				// 忽略
			}
		}
		return null;
	}

	private @Nullable File findSource(@Nullable Class<?> sourceClass) {
		try {
			ProtectionDomain domain = (sourceClass != null) ? sourceClass.getProtectionDomain() : null;
			CodeSource codeSource = (domain != null) ? domain.getCodeSource() : null;
			URL location = (codeSource != null) ? codeSource.getLocation() : null;
			File source = (location != null) ? findSource(location) : null;
			if (source != null && source.exists() && !isUnitTest()) {
				return source.getAbsoluteFile();
			}
		}
		catch (Exception ex) {
			// Ignore
		}
		return null;
	}

	private boolean isUnitTest() {
		try {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (int i = stackTrace.length - 1; i >= 0; i--) {
				if (stackTrace[i].getClassName().startsWith("org.junit.")) {
					return true;
				}
			}
		}
		catch (Exception ex) {
			// Ignore
		}
		return false;
	}

	private File findSource(URL location) throws IOException, URISyntaxException {
		URLConnection connection = location.openConnection();
		if (connection instanceof JarURLConnection jarURLConnection) {
			return getRootJarFile(jarURLConnection.getJarFile());
		}
		return new File(location.toURI());
	}

	private File getRootJarFile(JarFile jarFile) {
		String name = jarFile.getName();
		int separator = name.indexOf("!/");
		if (separator > 0) {
			name = name.substring(0, separator);
		}
		return new File(name);
	}

	private File findHomeDir(@Nullable File source) {
		File homeDir = source;
		homeDir = (homeDir != null) ? homeDir : findDefaultHomeDir();
		if (homeDir.isFile()) {
			homeDir = homeDir.getParentFile();
		}
		homeDir = homeDir.exists() ? homeDir : new File(".");
		return homeDir.getAbsoluteFile();
	}

	private File findDefaultHomeDir() {
		String userDir = System.getProperty("user.dir");
		return new File(StringUtils.hasLength(userDir) ? userDir : ".");
	}

	/**
	 * 返回用于定位主目录的底层源，通常为 JAR 文件或目录。
	 * 无法确定源时返回 {@code null}。
	 * @return the underlying source or {@code null} 底层源或 {@code null}
	 */
	public @Nullable File getSource() {
		return this.source;
	}

	/**
	 * 返回应用主目录。
	 * @return the home directory (never {@code null}) 主目录（永不为 {@code null}）
	 */
	public File getDir() {
		return this.dir;
	}

	@Override
	public String toString() {
		return getDir().toString();
	}

}
