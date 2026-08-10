/*
 * Copyright 2002-2020 the original author or authors.
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

import com.alibaba.nacos.common.packagescan.util.ResourceUtils;
import com.alibaba.nacos.common.packagescan.util.AbstractAssert;
import com.alibaba.nacos.common.utils.ClassUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copy from https://github.com/spring-projects/spring-framework.git, with less modifications
 * {@link ResourceLoader} 默认实现：按 URL / classpath: / 相对路径解析资源，可独立使用。
 * Default implementation of the {@link ResourceLoader} interface.
 * Can also be used standalone.
 *
 * <p>Will return a {@link UrlResource} if the location value is a URL,
 * and a {@link ClassPathResource} if it is a non-URL path or a
 * "classpath:" pseudo-URL.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 */
public class DefaultResourceLoader implements ResourceLoader {

    /** 显式指定的类加载器；为 null 时使用 {@link ClassUtils#getDefaultClassLoader()} */
    private ClassLoader classLoader;

    /** 已注册的自定义协议解析器，优先于标准解析规则 */
    private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

    /** 按值类型分组的资源解析缓存（如 ASM MetadataReader） */
    private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);

    /**
     * Create a new DefaultResourceLoader.
     *
     * <p>ClassLoader access will happen using the thread context class loader
     * at the time of actual resource access (since 5.3). For more control, pass
     * a specific ClassLoader to {@link #DefaultResourceLoader(ClassLoader)}.
     *
     * @see Thread#getContextClassLoader()
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public DefaultResourceLoader() {
    }

    /**
     * Create a new DefaultResourceLoader.
     *
     * @param classLoader the ClassLoader to load class path resources with, or {@code null}
     *                    for using the thread context class loader at the time of actual resource access
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Specify the ClassLoader to load class path resources with, or {@code null}
     * for using the thread context class loader at the time of actual resource access.
     *
     * <p>The default is that ClassLoader access will happen using the thread context
     * class loader at the time of actual resource access (since 5.3).
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Return the ClassLoader to load class path resources with.
     *
     * <p>Will get passed to ClassPathResource's constructor for all
     * ClassPathResource objects created by this resource loader.
     *
     * @see ClassPathResource
      * <p>默认资源加载器；详见类级说明。</p>
     */
    @Override

    public ClassLoader getClassLoader() {
        return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
    }

    /**
     * Register the given resolver with this resource loader, allowing for
     * additional protocols to be handled.
     *
     * <p>Any such resolver will be invoked ahead of this loader's standard
     * resolution rules. It may therefore also override any default rules.
     *
     * @see #getProtocolResolvers()
     * @since 4.3
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public void addProtocolResolver(ProtocolResolver resolver) {
        AbstractAssert.notNull(resolver, "ProtocolResolver must not be null");
        this.protocolResolvers.add(resolver);
    }

    /**
     * Return the collection of currently registered protocol resolvers,
     * allowing for introspection as well as modification.
     *
     * @since 4.3
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public Collection<ProtocolResolver> getProtocolResolvers() {
        return this.protocolResolvers;
    }

    /**
     * Obtain a cache for the given value type, keyed by {@link Resource}.
     *
     * @param valueType the value type, e.g. an ASM {@code MetadataReader}
     * @return the cache {@link Map}, shared at the {@code ResourceLoader} level
     * @since 5.0
      * <p>默认资源加载器；详见类级说明。</p>
     */
    @SuppressWarnings("unchecked")
    public <T> Map<Resource, T> getResourceCache(Class<T> valueType) {
        return (Map<Resource, T>) this.resourceCaches.computeIfAbsent(valueType, key -> new ConcurrentHashMap<>());
    }

    /**
     * Clear all resource caches in this resource loader.
     *
     * @see #getResourceCache
     * @since 5.0
      * <p>默认资源加载器；详见类级说明。</p>
     */
    public void clearResourceCaches() {
        this.resourceCaches.clear();
    }

    @Override
    public Resource getResource(String location) {
        AbstractAssert.notNull(location, "Location must not be null");

        // 先尝试 SPI 协议解析器
        for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
            Resource resource = protocolResolver.resolve(location, this);
            if (resource != null) {
                return resource;
            }
        }

        if (location.startsWith("/")) {
            return getResourceByPath(location);
        } else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
        } else {
            try {
                // 尝试按 URL 解析；失败则退化为类路径相对路径
                // Try to parse the location as a URL...
                URL url = new URL(location);
                return (ResourceUtils.isFileUrl(url) ? new FileUrlResource(url) : new UrlResource(url));
            } catch (MalformedURLException ex) {
                // 非 URL 格式：按类路径资源路径处理
                // No URL -> resolve as resource path.
                return getResourceByPath(location);
            }
        }
    }

    /**
     * Return a Resource handle for the resource at the given path.
     *
     * <p>The default implementation supports class path locations. This should
     * be appropriate for standalone implementations but can be overridden,
     * e.g. for implementations targeted at a Servlet container.
     *
     * @param path the path to the resource
     * @return the corresponding Resource handle
     * @see ClassPathResource
      * <p>默认资源加载器；详见类级说明。</p>
     */
    protected Resource getResourceByPath(String path) {
        return new ClassPathContextResource(path, getClassLoader());
    }

    /**
     * ClassPathResource that explicitly expresses a context-relative path
     * through implementing the ContextResource interface.
      * <p>默认资源加载器；详见类级说明。</p>
     */
    /** 实现 {@link ContextResource} 的类路径资源，表达上下文相对路径 */
    protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {

        public ClassPathContextResource(String path, ClassLoader classLoader) {
            super(path, classLoader);
        }

        @Override
        public String getPathWithinContext() {
            return getPath();
        }

        @Override
        public Resource createRelative(String relativePath) {
            String pathToUse = StringUtils.applyRelativePath(getPath(), relativePath);
            return new ClassPathContextResource(pathToUse, getClassLoader());
        }
    }

}
