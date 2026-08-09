/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.spi;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 轻量 SPI 加载器（1.8.1 起重构）。
 *
 * <p>SPI（Service Provider Interface）中 Service 为接口或抽象类，
 * Provider 为实现类，须有无参构造以便实例化。</p>
 *
 * <p>配置文件位于 {@code META-INF/services/<接口全名>}：
 * 每行一个实现类全名；空白行忽略；{@code #} 起为注释。</p>
 *
 * <p>{@code SpiLoader} 支持：</p>
 * <ul>
 * <li>加载全部 Provider（有序/无序）；</li>
 * <li>按 {@link Spi#order()} 取最高/最低优先级；</li>
 * <li>加载首个或默认实现；</li>
 * <li>按别名或类加载指定实例。</li>
 * </ul>
 *
 * @author Eric Zhao
 * @author cdfive
 * @since 1.4.0
 * @see com.alibaba.csp.sentinel.spi.Spi
 * @see java.util.ServiceLoader
 */
public final class SpiLoader<S> {

    // Provider 配置文件默认目录前缀
    private static final String SPI_FILE_PREFIX = "META-INF/services/";

    // SpiLoader 实例缓存，键为 Service 类名
    private static final ConcurrentHashMap<String, SpiLoader> SPI_LOADER_MAP = new ConcurrentHashMap<>();

    // 已加载的 Provider 类列表
    private final List<Class<? extends S>> classList = Collections.synchronizedList(new ArrayList<Class<? extends S>>());

    // 按 order 排序后的 Provider 类列表
    private final List<Class<? extends S>> sortedClassList = Collections.synchronizedList(new ArrayList<Class<? extends S>>());

    /**
     * Provider 类缓存：键为别名（{@link Spi#value()} 非空时）或类全名。
     */
    private final ConcurrentHashMap<String, Class<? extends S>> classMap = new ConcurrentHashMap<>();

    // Provider 单例实例缓存
    private final ConcurrentHashMap<String, S> singletonMap = new ConcurrentHashMap<>();

    // 是否已解析 SPI 配置文件
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    // 标记 {@link Spi#isDefault()} 的默认 Provider 类
    private Class<? extends S> defaultClass = null;

    // Service 接口或抽象类
    private Class<S> service;

    /**
     * 按 Service 类获取 SpiLoader，按类名缓存。
     *
     * @param service Service class
     * @param <T>     Service type
     * @return SpiLoader instance
     */
    public static <T> SpiLoader<T> of(Class<T> service) {
        AssertUtil.notNull(service, "SPI class cannot be null");
        AssertUtil.isTrue(service.isInterface() || Modifier.isAbstract(service.getModifiers()),
                "SPI class[" + service.getName() + "] must be interface or abstract class");

        String className = service.getName();
        SpiLoader<T> spiLoader = SPI_LOADER_MAP.get(className);
        if (spiLoader == null) {
            synchronized (SpiLoader.class) {
                spiLoader = SPI_LOADER_MAP.get(className);
                if (spiLoader == null) {
                    SPI_LOADER_MAP.putIfAbsent(className, new SpiLoader<>(service));
                    spiLoader = SPI_LOADER_MAP.get(className);
                }
            }
        }

        return spiLoader;
    }

    /**
     * 重置并清空全部 SpiLoader 缓存（包内可见，仅测试使用）。
     */
    synchronized static void resetAndClearAll() {
        Set<Map.Entry<String, SpiLoader>> entries = SPI_LOADER_MAP.entrySet();
        for (Map.Entry<String, SpiLoader> entry : entries) {
            SpiLoader spiLoader = entry.getValue();
            spiLoader.resetAndClear();
        }
        SPI_LOADER_MAP.clear();
    }

    // 私有构造
    private SpiLoader(Class<S> service) {
        this.service = service;
    }

    /**
     * 加载全部 Provider 实例（配置文件顺序）。
     *
     * @return Provider instances list
     */
    public List<S> loadInstanceList() {
        load();

        return createInstanceList(classList);
    }

    /**
     * 加载全部 Provider 实例，按 {@link Spi#order()} 排序。
     *
     * @return Sorted Provider instances list
     */
    public List<S> loadInstanceListSorted() {
        load();

        return createInstanceList(sortedClassList);
    }

    /**
     * 加载 order 最小（优先级最高）的 Provider。
     *
     * @return Provider instance of highest order priority
     */
    public S loadHighestPriorityInstance() {
        load();

        if (sortedClassList.size() == 0) {
            return null;
        }

        Class<? extends S> highestClass = sortedClassList.get(0);
        return createInstance(highestClass);
    }

    /**
     * 加载 order 最大（优先级最低）的 Provider。
     *
     * @return Provider instance of lowest order priority
     */
    public S loadLowestPriorityInstance() {
        load();

        if (sortedClassList.size() == 0) {
            return null;
        }

        Class<? extends S> lowestClass = sortedClassList.get(sortedClassList.size() - 1);
        return createInstance(lowestClass);
    }

    /**
     * 加载配置文件中首个 Provider。
     *
     * @return Provider instance of first-found specific
     */
    public S loadFirstInstance() {
        load();

        if (classList.size() == 0) {
            return null;
        }

        Class<? extends S> serviceClass = classList.get(0);
        S instance = createInstance(serviceClass);
        return instance;
    }

    /**
     * 加载首个非默认 Provider；若无则返回默认实现。
     *
     * @return Provider instance
     */
    public S loadFirstInstanceOrDefault() {
        load();

        for (Class<? extends S> clazz : classList) {
            if (defaultClass == null || clazz != defaultClass) {
                return createInstance(clazz);
            }
        }

        return loadDefaultInstance();
    }

    /**
     * 加载 {@code @Spi(isDefault=true)} 标记的默认 Provider。
     *
     * @return default Provider instance
     */
    public S loadDefaultInstance() {
        load();

        if (defaultClass == null) {
            return null;
        }

        return createInstance(defaultClass);
    }

    /**
     * 按实现类加载 Provider 实例。
     *
     * @param clazz class type
     * @return Provider instance
     */
    public S loadInstance(Class<? extends S> clazz) {
        AssertUtil.notNull(clazz, "SPI class cannot be null");

        if (clazz.equals(service)) {
            fail(clazz.getName() + " is not subtype of " + service.getName());
        }

        load();

        if (!classMap.containsValue(clazz)) {
            fail(clazz.getName() + " is not Provider class of " + service.getName() + ",check if it is in the SPI configuration file?");
        }

        return createInstance(clazz);
    }

    /**
     * 按 {@link Spi#value()} 别名加载 Provider。
     *
     * @param aliasName aliasName of Provider class
     * @return Provider instance
     */
    public S loadInstance(String aliasName) {
        AssertUtil.notEmpty(aliasName, "aliasName cannot be empty");

        load();

        Class<? extends S> clazz = classMap.get(aliasName);
        if (clazz == null) {
            fail("no Provider class's aliasName is " + aliasName);
        }

        return createInstance(clazz);
    }

    /**
     * 清空当前 SpiLoader 并从全局缓存移除
     */
    synchronized void resetAndClear() {
        SPI_LOADER_MAP.remove(service.getName());
        classList.clear();
        sortedClassList.clear();
        classMap.clear();
        singletonMap.clear();
        defaultClass = null;
        loaded.set(false);
    }

    /**
     * 从 SPI 配置文件加载 Provider 类
     */
    public void load() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        String fullFileName = SPI_FILE_PREFIX + service.getName();
        ClassLoader classLoader;
        if (SentinelConfig.shouldUseContextClassloader()) {
            classLoader = Thread.currentThread().getContextClassLoader();
        } else {
            classLoader = service.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Enumeration<URL> urls = null;
        try {
            urls = classLoader.getResources(fullFileName);
        } catch (IOException e) {
            fail("Error locating SPI configuration file,filename=" + fullFileName + ",classloader=" + classLoader, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            RecordLog.warn("No SPI configuration file,filename=" + fullFileName + ",classloader=" + classLoader);
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            InputStream in = null;
            BufferedReader br = null;
            try {
                in = url.openStream();
                br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtil.isBlank(line)) {
                        // 跳过空行
                        continue;
                    }

                    line = line.trim();
                    int commentIndex = line.indexOf("#");
                    if (commentIndex == 0) {
                        // 跳过注释行
                        continue;
                    }

                    if (commentIndex > 0) {
                        line = line.substring(0, commentIndex);
                    }
                    line = line.trim();

                    Class<S> clazz = null;
                    try {
                        clazz = (Class<S>) Class.forName(line, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        fail("class " + line + " not found", e);
                    }

                    if (classMap.containsValue(clazz)) {
                        RecordLog.warn("duplicate class found,className=" + clazz.getName() + ",SPI configuration file[" + url + "]");
                        continue;
                    }

                    if (!service.isAssignableFrom(clazz)) {
                        fail("class " + clazz.getName() + "is not subtype of " + service.getName() + ",SPI configuration file[" + url + "]");
                    }

                    classList.add(clazz);
                    Spi spi = clazz.getAnnotation(Spi.class);
                    String aliasName = spi == null || "".equals(spi.value()) ? clazz.getName() : spi.value();
                    if (classMap.containsKey(aliasName)) {
                        Class<? extends S> existClass = classMap.get(aliasName);
                        fail("Found repeat alias name for " + clazz.getName() + " and "
                                + existClass.getName() + ",SPI configuration file[" + url + "]");
                    }
                    classMap.put(aliasName, clazz);

                    if (spi != null && spi.isDefault()) {
                        if (defaultClass != null) {
                            fail("Found more than one default Provider,className=" + clazz.getName() + ",SPI configuration file[" + url + "]");
                        }
                        defaultClass = clazz;
                    }

                    RecordLog.info("[SpiLoader] Found SPI implementation for SPI {}, provider={}, aliasName={}"
                            + ", isSingleton={}, isDefault={}, order={}",
                        service.getName(), line, aliasName
                            , spi == null ? true : spi.isSingleton()
                            , spi == null ? false : spi.isDefault()
                            , spi == null ? 0 : spi.order());
                }
            } catch (IOException e) {
                fail("error reading SPI configuration file[" + url + "]", e);
            } finally {
                closeResources(in, br);
            }
        }

        sortedClassList.addAll(classList);
        Collections.sort(sortedClassList, new Comparator<Class<? extends S>>() {
            @Override
            public int compare(Class<? extends S> o1, Class<? extends S> o2) {
                Spi spi1 = o1.getAnnotation(Spi.class);
                int order1 = spi1 == null ? 0 : spi1.order();

                Spi spi2 = o2.getAnnotation(Spi.class);
                int order2 = spi2 == null ? 0 : spi2.order();

                return Integer.compare(order1, order2);
            }
        });
    }

    @Override
    public String toString() {
        return "com.alibaba.csp.sentinel.spi.SpiLoader[" + service.getName() + "]";
    }

    /**
     * 批量创建 Provider 实例。
     *
     * @param clazzList class types of Providers
     * @return Provider instance list
     */
    private List<S> createInstanceList(List<Class<? extends S>> clazzList) {
        if (clazzList == null || clazzList.size() == 0) {
            return Collections.emptyList();
        }

        List<S> instances = new ArrayList<>(clazzList.size());
        for (Class<? extends S> clazz : clazzList) {
            S instance = createInstance(clazz);
            instances.add(instance);
        }
        return instances;
    }

    /**
     * 创建 Provider 实例（根据 {@link Spi#isSingleton()} 决定是否单例）。
     *
     * @param clazz class type of Provider
     * @return Provider class
     */
    private S createInstance(Class<? extends S> clazz) {
        Spi spi = clazz.getAnnotation(Spi.class);
        boolean singleton = true;
        if (spi != null) {
            singleton = spi.isSingleton();
        }
        return createInstance(clazz, singleton);
    }

    /**
     * 创建 Provider 实例。
     *
     * @param clazz     class type of Provider
     * @param singleton if instance is singleton or prototype
     * @return Provider instance
     */
    private S createInstance(Class<? extends S> clazz, boolean singleton) {
        S instance = null;
        try {
            if (singleton) {
                instance = singletonMap.get(clazz.getName());
                if (instance == null) {
                    synchronized (this) {
                        instance = singletonMap.get(clazz.getName());
                        if (instance == null) {
                            instance = service.cast(clazz.newInstance());
                            singletonMap.put(clazz.getName(), instance);
                        }
                    }
                }
            } else {
                instance = service.cast(clazz.newInstance());
            }
        } catch (Throwable e) {
            fail(clazz.getName() + " could not be instantiated");
        }
        return instance;
    }

    /**
     * 关闭全部 {@link Closeable} 资源。
     *
     * @param closeables {@link Closeable} resources
     */
    private void closeResources(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        Exception firstException = null;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (firstException != null) {
            fail("error closing resources", firstException);
        }
    }

    /**
     * 抛出带消息的 {@link SpiLoaderException}。
     *
     * @param msg error message
     */
    private void fail(String msg) {
        RecordLog.error(msg);
        throw new SpiLoaderException("[" + service.getName() + "]" + msg);
    }

    /**
     * 抛出带消息与原因的 {@link SpiLoaderException}。
     *
     * @param msg error message
     */
    private void fail(String msg, Throwable e) {
        RecordLog.error(msg, e);
        throw new SpiLoaderException("[" + service.getName() + "]" + msg, e);
    }
}
