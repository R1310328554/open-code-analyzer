/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.utils;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * JDK Service Provider 机制加载实现类：从 META-INF/service/ 读取并实例化。
 */
public class ServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);
    /**
     * 加载本类的 ClassLoader 引用，只计算一次并缓存，避免重复获取。
     */
    private static ClassLoader thisClassLoader;
    
    /**
     * JDK1.3+ <a href= "http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider" > Service Provider
     * 规范</a> 中定义的资源路径前缀。
     */
    public static final String PREFIX = "META-INF/service/";
    
    static {
        thisClassLoader = getClassLoader(ServiceProvider.class);
    }
    
    /**
     * 返回能唯一标识指定对象（含其类）的字符串。
     * <p>
     * 格式为 "classname@hashcode"，与 {@link Object#toString()} 默认形式相同，
     * 但在目标类已重写 toString 时仍能稳定标识实例。
     *
     * @param o 可为 null
     * @return classname@hashcode，o 为 null 时返回 "null"
     */
    protected static String objectId(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }
    
    protected static ClassLoader getClassLoader(Class<?> clazz) {
        try {
            return clazz.getClassLoader();
        } catch (SecurityException e) {
            LOG.error("Unable to get classloader for class {} due to security restrictions , error info {}",
                clazz, e.getMessage());
            throw e;
        }
    }
    
    protected static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException ex) {
            /**
             * 当上下文 ClassLoader 不是调用类 ClassLoader 的祖先，或安全策略禁止访问时，
             * getContextClassLoader() 会抛出 SecurityException。
             */
        }
        return classLoader;
    }
    
    protected static InputStream getResourceAsStream(ClassLoader loader, String name) {
        if (loader != null) {
            return loader.getResourceAsStream(name);
        } else {
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }
    
    public static <T> List<T> load(Class<?> clazz) {
        String fullName = PREFIX + clazz.getName();
        return load(fullName, clazz);
    }
    
    public static <T> List<T> load(String name, Class<?> clazz) {
        LOG.info("Looking for a resource file of name [{}] ...", name);
        List<T> services = new ArrayList<>();
        InputStream is = getResourceAsStream(getContextClassLoader(), name);
        if (is == null) {
            LOG.warn("No resource file with name [{}] found.", name);
            return services;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String serviceName = reader.readLine();
            List<String> names = new ArrayList<>();
            while (serviceName != null && !"".equals(serviceName)) {
                LOG.info(
                    "Creating an instance as specified by file {} which was present in the path of the context classloader.",
                    name);
                if (!names.contains(serviceName)) {
                    names.add(serviceName);
                    services.add(initService(getContextClassLoader(), serviceName, clazz));
                }
                serviceName = reader.readLine();
            }
        } catch (Exception e) {
            LOG.error("Error occurred when looking for resource file " + name, e);
        }
        return services;
    }
    
    public static <T> T loadClass(Class<?> clazz) {
        String fullName = PREFIX + clazz.getName();
        return loadClass(fullName, clazz);
    }
    
    public static <T> T loadClass(String name, Class<?> clazz) {
        LOG.info("Looking for a resource file of name [{}] ...", name);
        T s = null;
        InputStream is = getResourceAsStream(getContextClassLoader(), name);
        if (is == null) {
            LOG.warn("No resource file with name [{}] found.", name);
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String serviceName = reader.readLine();
            if (serviceName != null && !"".equals(serviceName)) {
                s = initService(getContextClassLoader(), serviceName, clazz);
            } else {
                LOG.warn("ServiceName is empty!");
            }
        } catch (Exception e) {
            LOG.warn("Error occurred when looking for resource file " + name, e);
        }
        return s;
    }

    protected static <T> T initService(ClassLoader classLoader, String serviceName, Class<?> clazz) {
        Class<?> serviceClazz = null;
        try {
            if (classLoader != null) {
                try {
                    // 注意：此处需显式转型，并正确捕获/传播异常
                    serviceClazz = classLoader.loadClass(serviceName);
                    if (clazz.isAssignableFrom(serviceClazz)) {
                        LOG.info("Loaded class {} from classloader {}", serviceClazz.getName(),
                            objectId(classLoader));
                    } else {
                        // ClassLoader 树不一致：实现类并非由当前 loader 视角下的 clazz 子类
                        LOG.error(
                            "Class {} loaded from classloader {} does not extend {} as loaded by this classloader.",
                            serviceClazz.getName(),
                            objectId(serviceClazz.getClassLoader()), clazz.getName());
                    }
                    return (T) serviceClazz.getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException ex) {
                    if (classLoader == thisClassLoader) {
                        // 已无可选 loader，向上抛出
                        LOG.warn("Unable to locate any class {} via classloader {}", serviceName,
                            objectId(classLoader));
                        throw ex;
                    }
                    // 忽略，尝试其他 ClassLoader
                } catch (NoClassDefFoundError e) {
                    if (classLoader == thisClassLoader) {
                        // 已无可选 loader，向上抛出
                        LOG.warn(
                            "Class {} cannot be loaded via classloader {}.it depends on some other class that cannot be found.",
                            serviceClazz, objectId(classLoader));
                        throw e;
                    }
                    // 忽略，尝试其他 ClassLoader
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to init service.", e);
        }
        return (T) serviceClazz;
    }
}
