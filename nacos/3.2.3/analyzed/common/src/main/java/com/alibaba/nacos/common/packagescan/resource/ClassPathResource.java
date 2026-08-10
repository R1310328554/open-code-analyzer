/*
 * Copyright 2002-2021 the original author or authors.
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

package com.alibaba.nacos.common.packagescan.resource;

import com.alibaba.nacos.common.packagescan.util.AbstractObjectUtils;
import com.alibaba.nacos.common.packagescan.util.AbstractAssert;
import com.alibaba.nacos.common.utils.ClassUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * <p>classpath {@link Resource} 实现：通过 {@link ClassLoader} 或 {@link Class} 定位资源；文件系统上的 classpath 可解析为 {@link java.io.File}。</p>
 * {@link Resource} implementation for class path resources. Uses either a
 * given {@link ClassLoader} or a given {@link Class} for loading resources.
 *
 * <p>Supports resolution as {@code java.io.File} if the class path
 * resource resides in the file system, but not for resources in a JAR.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see ClassLoader#getResourceAsStream(String)
 * @see Class#getResourceAsStream(String)
 * @since 28.12.2003
 */
public class ClassPathResource extends AbstractFileResolvingResource {

    /** classpath 内资源路径（无前导斜杠） */
    private final String path;

    /** 用于加载资源的 ClassLoader，可为 null 时使用线程上下文类加载器 */
    private ClassLoader classLoader;

    /** 可选的锚定 Class，相对路径以此为包根解析 */
    private Class<?> clazz;

    /**
     * Create a new {@code ClassPathResource} for {@code ClassLoader} usage.
     * A leading slash will be removed, as the ClassLoader resource access
     * methods will not accept it.
     *
     * <p>The thread context class loader will be used for
     * loading the resource.
     *
     * @param path the absolute path within the class path
     * @see ClassLoader#getResourceAsStream(String)
     * @see ClassUtils#getDefaultClassLoader()
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    public ClassPathResource(String path) {
        this(path, (ClassLoader) null);
    }

    /**
     * Create a new {@code ClassPathResource} for {@code ClassLoader} usage.
     * A leading slash will be removed, as the ClassLoader resource access
     * methods will not accept it.
     *
     * @param path        the absolute path within the classpath
     * @param classLoader the class loader to load the resource with,
     *                    or {@code null} for the thread context class loader
     * @see ClassLoader#getResourceAsStream(String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        AbstractAssert.notNull(path, "Path must not be null");
        String pathToUse = StringUtils.cleanPath(path);
        // ClassLoader.getResource 不接受前导斜杠
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    /**
     * Create a new {@code ClassPathResource} for {@code Class} usage.
     * The path can be relative to the given class, or absolute within
     * the classpath via a leading slash.
     *
     * @param path  relative or absolute path within the class path
     * @param clazz the class to load resources with
     * @see Class#getResourceAsStream
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    public ClassPathResource(String path, Class<?> clazz) {
        AbstractAssert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    /**
     * Create a new {@code ClassPathResource} with optional {@code ClassLoader}
     * and {@code Class}. Only for internal usage.
     *
     * @param path        relative or absolute path within the classpath
     * @param classLoader the class loader to load the resource with, if any
     * @param clazz       the class to load resources with, if any
     * @deprecated as of 4.3.13, in favor of selective use of
     * {@link #ClassPathResource(String, ClassLoader)} vs {@link #ClassPathResource(String, Class)}
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Deprecated
    protected ClassPathResource(String path, ClassLoader classLoader, Class<?> clazz) {
        this.path = StringUtils.cleanPath(path);
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    /**
     * Return the path for this resource (as resource path within the class path).
     * <p>返回 classpath 内的规范化资源路径。</p>
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * Return the ClassLoader that this resource will be obtained from.
      * <p>classpath 资源实现；详见类级说明。</p>
     */

    public final ClassLoader getClassLoader() {
        return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
    }


    /**
     * This implementation checks for the resolution of a resource URL.
     *
     * @see ClassLoader#getResource(String)
     * @see Class#getResource(String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public boolean exists() {
        return (resolveUrl() != null);
    }

    /**
     * This implementation checks for the resolution of a resource URL upfront,
     * then proceeding with {@link AbstractFileResolvingResource}'s length check.
     *
     * @see ClassLoader#getResource(String)
     * @see Class#getResource(String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public boolean isReadable() {
        URL url = resolveUrl();
        return (url != null && checkReadable(url));
    }

    /**
     * Resolves a URL for the underlying class path resource.
     * <p>解析底层 classpath 资源的 URL，不存在时返回 null。</p>
     *

    protected URL resolveUrl() {
        try {
            if (this.clazz != null) {
                return this.clazz.getResource(this.path);
            } else if (this.classLoader != null) {
                return this.classLoader.getResource(this.path);
            } else {
                return ClassLoader.getSystemResource(this.path);
            }
        } catch (IllegalArgumentException ex) {
            // Should not happen according to the JDK's contract:
            // see https://github.com/openjdk/jdk/pull/2662
            return null;
        }
    }

    /**
     * This implementation opens an InputStream for the given class path resource.
     *
     * @see ClassLoader#getResourceAsStream(String)
     * @see Class#getResourceAsStream(String)
     */
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        } else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        // 三类加载方式均失败则抛出 FileNotFoundException
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    /**
     * This implementation returns a URL for the underlying class path resource,
     * if available.
     *
     * @see ClassLoader#getResource(String)
     * @see Class#getResource(String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public URL getUrl() throws IOException {
        URL url = resolveUrl();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    /**
     * This implementation creates a ClassPathResource, applying the given path
     * relative to the path of the underlying resource of this descriptor.
     *
     * @see StringUtils#applyRelativePath(String, String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return (this.clazz != null ? new ClassPathResource(pathToUse, this.clazz) :
                new ClassPathResource(pathToUse, this.classLoader));
    }

    /**
     * This implementation returns the name of the file that this class path
     * resource refers to.
     *
     * @see StringUtils#getFilename(String)
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override

    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }

    /**
     * This implementation returns a description that includes the class path location.
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = this.path;
        if (this.clazz != null && !pathToUse.startsWith("/")) {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }


    /**
     * This implementation compares the underlying class path locations.
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ClassPathResource)) {
            return false;
        }
        ClassPathResource otherRes = (ClassPathResource) other;
        return (this.path.equals(otherRes.path)
                && AbstractObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader)
                && AbstractObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
    }

    /**
     * This implementation returns the hash code of the underlying
     * class path location.
      * <p>classpath 资源实现；详见类级说明。</p>
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}
