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

package org.springframework.boot.ssl.pem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * 可提供 {@link X509Certificate 证书} 与 {@link PrivateKey 私钥} 的 PEM 编码内容。
 *
 * @author Scott Frederick
 * @author Phillip Webb
 * @since 3.2.0
 */
public final class PemContent {

	private static final Pattern PEM_HEADER = Pattern.compile("-+BEGIN\\s+[^-]*-+", Pattern.CASE_INSENSITIVE);

	private static final Pattern PEM_FOOTER = Pattern.compile("-+END\\s+[^-]*-+", Pattern.CASE_INSENSITIVE);

	private final String text;

	private PemContent(String text) {
		this.text = text.lines().map(String::trim).collect(Collectors.joining("\n"));
	}

	/**
	 * 解析并返回 PEM 内容中的全部 {@link X509Certificate 证书}。
	 * 多数 PEM 文件包含单个证书或证书链。
	 * @return the certificates 证书列表
	 * @throws IllegalStateException if no certificates could be loaded 无法加载证书时抛出
	 */
	public List<X509Certificate> getCertificates() {
		return PemCertificateParser.parse(this.text);
	}

	/**
	 * 解析并返回 PEM 内容中的 {@link PrivateKey 私钥}。
	 * @return the private keys 私钥
	 * @throws IllegalStateException if no private key could be loaded 无法加载私钥时抛出
	 */
	public @Nullable PrivateKey getPrivateKey() {
		return getPrivateKey(null);
	}

	/**
	 * 解析并返回 PEM 内容中的 {@link PrivateKey 私钥}；无私钥时返回 {@code null}。
	 * @param password the password to decrypt the private keys or {@code null} 解密私钥的密码或 {@code null}
	 * @return the private keys 私钥
	 */
	public @Nullable PrivateKey getPrivateKey(@Nullable String password) {
		return PemPrivateKeyParser.parse(this.text, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return Objects.equals(this.text, ((PemContent) obj).text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.text);
	}

	@Override
	public String toString() {
		return this.text;
	}

	/**
	 * 从给定内容加载 {@link PemContent}（可为 PEM 文本本身或资源引用）。
	 * @param content the content to load 要加载的内容
	 * @param resourceLoader the resource loader used to load content 用于加载内容的资源加载器
	 * @return a new {@link PemContent} instance or {@code null} 新的 {@link PemContent} 实例或 {@code null}
	 * @throws IOException on IO error I/O 错误时抛出
	 */
	static @Nullable PemContent load(@Nullable String content, ResourceLoader resourceLoader) throws IOException {
		if (!StringUtils.hasLength(content)) {
			return null;
		}
		if (isPresentInText(content)) {
			return new PemContent(content);
		}
		try (InputStream in = resourceLoader.getResource(content).getInputStream()) {
			return load(in);
		}
		catch (IOException | UncheckedIOException ex) {
			throw new IOException("Error reading certificate or key from file '%s'".formatted(content), ex);
		}
	}

	/**
	 * 从给定 {@link Path} 加载 {@link PemContent}。
	 * @param path a path to load the content from 内容路径
	 * @return the loaded PEM content 加载的 PEM 内容
	 * @throws IOException on IO error I/O 错误时抛出
	 */
	public static PemContent load(Path path) throws IOException {
		Assert.notNull(path, "'path' must not be null");
		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			return load(in);
		}
	}

	/**
	 * 从给定 {@link InputStream} 加载 {@link PemContent}。
	 * @param in an input stream to load the content from 输入流
	 * @return the loaded PEM content 加载的 PEM 内容
	 * @throws IOException on IO error I/O 错误时抛出
	 */
	public static PemContent load(InputStream in) throws IOException {
		return of(StreamUtils.copyToString(in, StandardCharsets.UTF_8));
	}

	/**
	 * 返回包含给定文本的新 {@link PemContent} 实例。
	 * @param text the text containing PEM encoded content 含 PEM 编码内容的文本
	 * @return a new {@link PemContent} instance 新的 {@link PemContent} 实例
	 */
	@Contract("!null -> !null")
	public static @Nullable PemContent of(@Nullable String text) {
		return (text != null) ? new PemContent(text) : null;
	}

	/**
	 * 判断给定文本是否包含 PEM 内容。
	 * @param text the text to check 待检查文本
	 * @return if the text includes PEM encoded content 包含 PEM 编码内容时返回 {@code true}
	 */
	public static boolean isPresentInText(@Nullable String text) {
		return text != null && PEM_HEADER.matcher(text).find() && PEM_FOOTER.matcher(text).find();
	}

}
