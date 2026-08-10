/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.keycloak.common.util;

import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * 跨 JDK 版本委托 {@link ObjectInputStream} 反序列化过滤器的工具类。
 *
 * <p>Java 6–8 使用 {@code sun.misc.ObjectInputFilter}，Java 9+ 使用 {@code java.io.ObjectInputFilter}，
 * 通过反射适配不同 API，并提供 {@link FilterPatternBuilder} 构建白名单模式。</p>
 */
public class DelegatingSerializationFilter {
    private static final Logger LOG = Logger.getLogger(DelegatingSerializationFilter.class.getName());

    private static final SerializationFilterAdapter serializationFilterAdapter = isJava6To8() ? createOnJava6To8Adapter() : createOnJavaAfter8Adapter();

    /** 判断当前 JVM 是否为 Java 6/7/8。 */
    private static boolean isJava6To8() {
        List<String> olderVersions = Arrays.asList("1.6", "1.7", "1.8");
        return olderVersions.contains(System.getProperty("java.specification.version"));
    }

    private DelegatingSerializationFilter() {
    }

    /** 创建 {@link FilterPatternBuilder} 以构建反序列化白名单。 */
    public static DelegatingSerializationFilter.FilterPatternBuilder builder() {
        return new DelegatingSerializationFilter.FilterPatternBuilder();
    }

    /** 若流尚未设置过滤器，则应用给定模式。 */
    private void setFilter(ObjectInputStream ois, String filterPattern) {
        LOG.debug("Using: " + serializationFilterAdapter.getClass().getSimpleName());

        if (serializationFilterAdapter.getObjectInputFilter(ois) == null) {
            serializationFilterAdapter.setObjectInputFilter(ois, filterPattern);
        }
    }

    /** 适配不同 JDK 的 ObjectInputFilter API。 */
    interface SerializationFilterAdapter {

        Object getObjectInputFilter(ObjectInputStream ois);

        void setObjectInputFilter(ObjectInputStream ois, String filterPattern);
    }

    private static SerializationFilterAdapter createOnJava6To8Adapter() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> objectInputFilterClass = cl.loadClass("sun.misc.ObjectInputFilter");
            Class<?> objectInputFilterConfigClass = cl.loadClass("sun.misc.ObjectInputFilter$Config");
            Method getObjectInputFilter = objectInputFilterConfigClass.getDeclaredMethod("getObjectInputFilter", ObjectInputStream.class);
            Method setObjectInputFilter = objectInputFilterConfigClass.getDeclaredMethod("setObjectInputFilter", ObjectInputStream.class, objectInputFilterClass);
            Method createFilter = objectInputFilterConfigClass.getDeclaredMethod("createFilter", String.class);
            LOG.info("Using OnJava6To8 serialization filter adapter");
            return new OnJava6To8(getObjectInputFilter, setObjectInputFilter, createFilter);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // This can happen for older JDK updates.
            LOG.warn("Could not configure SerializationFilterAdapter. For better security, it is highly recommended to upgrade to newer JDK version update!");
            LOG.warn("For the Java 7, the recommended update is at least 131 (1.7.0_131 or newer). For the Java 8, the recommended update is at least 121 (1.8.0_121 or newer).");
            LOG.warn("Error details", e);
            return new EmptyFilterAdapter();
        }
    }

    private static SerializationFilterAdapter createOnJavaAfter8Adapter() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> objectInputFilterClass = cl.loadClass("java.io.ObjectInputFilter");
            Class<?> objectInputFilterConfigClass = cl.loadClass("java.io.ObjectInputFilter$Config");
            Class<?> objectInputStreamClass = cl.loadClass("java.io.ObjectInputStream");
            Method getObjectInputFilter = objectInputStreamClass.getDeclaredMethod("getObjectInputFilter");
            Method setObjectInputFilter = objectInputStreamClass.getDeclaredMethod("setObjectInputFilter", objectInputFilterClass);
            Method createFilter = objectInputFilterConfigClass.getDeclaredMethod("createFilter", String.class);
            LOG.info("Using OnJavaAfter8 serialization filter adapter");
            return new OnJavaAfter8(getObjectInputFilter, setObjectInputFilter, createFilter);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // This can happen for older JDK updates.
            LOG.warn("Could not configure SerializationFilterAdapter. For better security, it is highly recommended to upgrade to newer JDK version update!");
            LOG.warn("Error details", e);
            return new EmptyFilterAdapter();
        }
    }

    // 若代码库长期停留在 Java 8，可直接使用 Java 8 类而无需反射
    static class OnJava6To8 implements SerializationFilterAdapter {

        private final Method getObjectInputFilterMethod;
        private final Method setObjectInputFilterMethod;
        private final Method createFilterMethod;

        private OnJava6To8(Method getObjectInputFilterMethod, Method setObjectInputFilterMethod, Method createFilterMethod) {
            this.getObjectInputFilterMethod = getObjectInputFilterMethod;
            this.setObjectInputFilterMethod = setObjectInputFilterMethod;
            this.createFilterMethod = createFilterMethod;
        }

        public Object getObjectInputFilter(ObjectInputStream ois) {
            try {
                return getObjectInputFilterMethod.invoke(null, ois);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Could not read ObjectFilter from ObjectInputStream: " + e.getMessage());
                return null;
            }
        }

        public void setObjectInputFilter(ObjectInputStream ois, String filterPattern) {
            try {
                Object objectFilter = createFilterMethod.invoke(null, filterPattern);
                setObjectInputFilterMethod.invoke(null, ois, objectFilter);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Could not set ObjectFilter: " + e.getMessage());
            }
        }
    }


    /** 无法配置过滤器时的空实现。 */
    static class EmptyFilterAdapter implements SerializationFilterAdapter {

        @Override
        public Object getObjectInputFilter(ObjectInputStream ois) {
            return null;
        }

        @Override
        public void setObjectInputFilter(ObjectInputStream ois, String filterPattern) {

        }

    }


    // 若迁移至 Java 9+，可直接使用新 API 并保留反射版以兼容旧 JDK
    static class OnJavaAfter8 implements SerializationFilterAdapter {

        private final Method getObjectInputFilterMethod;
        private final Method setObjectInputFilterMethod;
        private final Method createFilterMethod;

        private OnJavaAfter8(Method getObjectInputFilterMethod, Method setObjectInputFilterMethod, Method createFilterMethod) {
            this.getObjectInputFilterMethod = getObjectInputFilterMethod;
            this.setObjectInputFilterMethod = setObjectInputFilterMethod;
            this.createFilterMethod = createFilterMethod;
        }

        public Object getObjectInputFilter(ObjectInputStream ois) {
            try {
                return getObjectInputFilterMethod.invoke(ois);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Could not read ObjectFilter from ObjectInputStream: " + e.getMessage());
                return null;
            }
        }

        public void setObjectInputFilter(ObjectInputStream ois, String filterPattern) {
            try {
                Object objectFilter = createFilterMethod.invoke(ois, filterPattern);
                setObjectInputFilterMethod.invoke(ois, objectFilter);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Could not set ObjectFilter: " + e.getMessage());
            }
        }
    }


    /** 构建 ObjectInputFilter 白名单模式的流式 API。 */
    public static class FilterPatternBuilder {

        private Set<Class> classes = new HashSet<>();
        private Set<String> patterns = new HashSet<>();

        public FilterPatternBuilder() {
            // 默认允许 java.util 包（含基础集合类）
            addAllowedPattern("java.util.*");
        }

        /**
         * 当无法使用 {@link #addAllowedClass(Class)} 时添加包或类模式，
         * 例如类为 private、编译期不可见，或需允许整个包如 {@code java.util.*}。
         *
         * @param pattern
         * @return
         */
        public FilterPatternBuilder addAllowedPattern(String pattern) {
            this.patterns.add(pattern);
            return this;
        }

        /** 允许反序列化指定类。 */
        public FilterPatternBuilder addAllowedClass(Class javaClass) {
            this.classes.add(javaClass);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            for (Class javaClass : classes) {
                builder.append(javaClass.getName()).append(";");
            }
            for (String pattern : patterns) {
                builder.append(pattern).append(";");
            }

            builder.append("!*");

            return builder.toString();
        }

        /** 将当前模式应用到 {@link ObjectInputStream}。 */
        public void setFilter(ObjectInputStream ois) {
            DelegatingSerializationFilter filter = new DelegatingSerializationFilter();
            String filterPattern = this.toString();
            filter.setFilter(ois, filterPattern);
        }
    }
}
