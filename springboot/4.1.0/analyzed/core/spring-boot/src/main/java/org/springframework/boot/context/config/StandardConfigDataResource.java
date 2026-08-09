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

package org.springframework.boot.context.config;

import java.io.File;
import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 由 {@link Resource} 支持的 {@link ConfigDataResource}。
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @since 2.4.0
 */
public class StandardConfigDataResource extends ConfigDataResource {

	private final StandardConfigDataReference reference;

	private final Resource resource;

	private final boolean emptyDirectory;

	/**
	 * 创建新的 {@link StandardConfigDataResource} 实例。
	 *
	 * @param reference 资源引用
	 * @param resource 底层资源
	 */
	StandardConfigDataResource(StandardConfigDataReference reference, Resource resource) {
		this(reference, resource, false);
	}

	/**
	 * 创建新的 {@link StandardConfigDataResource} 实例。
	 *
	 * @param reference 资源引用
	 * @param resource 底层资源
	 * @param emptyDirectory 资源是否为已知存在的空目录
	 */
	StandardConfigDataResource(StandardConfigDataReference reference, Resource resource, boolean emptyDirectory) {
		Assert.notNull(reference, "'reference' must not be null");
		Assert.notNull(resource, "'resource' must not be null");
		this.reference = reference;
		this.resource = resource;
		this.emptyDirectory = emptyDirectory;
	}

	StandardConfigDataReference getReference() {
		return this.reference;
	}

	/**
	 * 返回正在加载的底层 Spring {@link Resource}。
	 *
	 * @return 底层资源
	 * @since 2.4.2
	 */
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * 返回 profile；资源非 profile 特定时为 {@code null}。
	 *
	 * @return profile，或 {@code null}
	 * @since 2.4.6
	 */
	public @Nullable String getProfile() {
		return this.reference.getProfile();
	}

	boolean isEmptyDirectory() {
		return this.emptyDirectory;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		StandardConfigDataResource other = (StandardConfigDataResource) obj;
		return (this.emptyDirectory == other.emptyDirectory) && isSameUnderlyingResource(this.resource, other.resource);
	}

	private boolean isSameUnderlyingResource(Resource ours, Resource other) {
		return ours.equals(other) || isSameFile(getUnderlyingFile(ours), getUnderlyingFile(other));
	}

	private boolean isSameFile(@Nullable File ours, @Nullable File other) {
		return (ours != null) && ours.equals(other);
	}

	@Override
	public int hashCode() {
		File underlyingFile = getUnderlyingFile(this.resource);
		return (underlyingFile != null) ? underlyingFile.hashCode() : this.resource.hashCode();
	}

	@Override
	public String toString() {
		if (this.resource instanceof FileSystemResource || this.resource instanceof FileUrlResource) {
			try {
				return "file [" + this.resource.getFile() + "]";
			}
			catch (IOException ex) {
				// Ignore
			}
		}
		return this.resource.toString();
	}

	private @Nullable File getUnderlyingFile(Resource resource) {
		try {
			if (resource instanceof ClassPathResource || resource instanceof FileSystemResource
					|| resource instanceof FileUrlResource) {
				return resource.getFile().getAbsoluteFile();
			}
		}
		catch (IOException ex) {
			// Ignore
		}
		return null;
	}

}
