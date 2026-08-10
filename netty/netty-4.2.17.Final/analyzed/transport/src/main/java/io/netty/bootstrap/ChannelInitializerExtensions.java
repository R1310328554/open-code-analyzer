/*
 * Copyright 2023 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.bootstrap;

import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 可配置的 {@link ChannelInitializerExtension} 加载门面：
 * 根据系统属性决定使用空实现、仅日志或 ServiceLoader 加载并缓存扩展。
 */
abstract class ChannelInitializerExtensions {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelInitializerExtensions.class);
    /** 单例实现，双重检查锁定初始化 */
    private static volatile ChannelInitializerExtensions implementation;

    private ChannelInitializerExtensions() {
    }

    /**
     * 获取扩展加载器。默认无操作；当 {@code io.netty.bootstrap.extensions} 为
     * {@code serviceload} 或 {@code log} 时使用 ServiceLoader 实现。
     */
    static ChannelInitializerExtensions getExtensions() {
        ChannelInitializerExtensions impl = implementation;
        if (impl == null) {
            synchronized (ChannelInitializerExtensions.class) {
                impl = implementation;
                if (impl != null) {
                    return impl;
                }
                String extensionProp = SystemPropertyUtil.get(ChannelInitializerExtension.EXTENSIONS_SYSTEM_PROPERTY);
                logger.debug("-Dio.netty.bootstrap.extensions: {}", extensionProp);
                if ("serviceload".equalsIgnoreCase(extensionProp)) {
                    impl = new ServiceLoadingExtensions(true);
                } else if ("log".equalsIgnoreCase(extensionProp)) {
                    impl = new ServiceLoadingExtensions(false);
                } else {
                    impl = new EmptyExtensions();
                }
                implementation = impl;
            }
        }
        return impl;
    }

    /**
     * @param cl 用于 ServiceLoader 的类加载器
     * @return 不可变的扩展集合
     */
    abstract Collection<ChannelInitializerExtension> extensions(ClassLoader cl);

    /** 未启用扩展时的空实现 */
    private static final class EmptyExtensions extends ChannelInitializerExtensions {
        @Override
        Collection<ChannelInitializerExtension> extensions(ClassLoader cl) {
            return Collections.emptyList();
        }
    }

    /** 通过 ServiceLoader 发现并（可选）缓存扩展 */
    private static final class ServiceLoadingExtensions extends ChannelInitializerExtensions {
        /** {@code true} 时缓存加载结果供 Bootstrap 使用；{@code log} 模式为 {@code false} */
        private final boolean loadAndCache;

        private WeakReference<ClassLoader> classLoader;
        private Collection<ChannelInitializerExtension> extensions;

        ServiceLoadingExtensions(boolean loadAndCache) {
            this.loadAndCache = loadAndCache;
        }

        @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
        @Override
        synchronized Collection<ChannelInitializerExtension> extensions(ClassLoader cl) {
            ClassLoader configured = classLoader == null ? null : classLoader.get();
            if (configured == null || configured != cl) {
                Collection<ChannelInitializerExtension> loaded = serviceLoadExtensions(loadAndCache, cl);
                classLoader = new WeakReference<ClassLoader>(cl);
                extensions = loadAndCache ? loaded : Collections.<ChannelInitializerExtension>emptyList();
            }
            return extensions;
        }

        /** ServiceLoader 扫描并按 {@link ChannelInitializerExtension#priority()} 排序 */
        private static Collection<ChannelInitializerExtension> serviceLoadExtensions(boolean load, ClassLoader cl) {
            List<ChannelInitializerExtension> extensions = new ArrayList<ChannelInitializerExtension>();

            ServiceLoader<ChannelInitializerExtension> loader = ServiceLoader.load(
                    ChannelInitializerExtension.class, cl);
            for (ChannelInitializerExtension extension : loader) {
                extensions.add(extension);
            }

            if (!extensions.isEmpty()) {
                Collections.sort(extensions, new Comparator<ChannelInitializerExtension>() {
                    @Override
                    public int compare(ChannelInitializerExtension a, ChannelInitializerExtension b) {
                        return Double.compare(a.priority(), b.priority());
                    }
                });
                logger.info("ServiceLoader {}(s) {}: {}", ChannelInitializerExtension.class.getSimpleName(),
                        load ? "registered" : "detected", extensions);
                return Collections.unmodifiableList(extensions);
            }
            logger.debug("ServiceLoader {}(s) {}: []", ChannelInitializerExtension.class.getSimpleName(),
                    load ? "registered" : "detected");
            return Collections.emptyList();
        }
    }
}
