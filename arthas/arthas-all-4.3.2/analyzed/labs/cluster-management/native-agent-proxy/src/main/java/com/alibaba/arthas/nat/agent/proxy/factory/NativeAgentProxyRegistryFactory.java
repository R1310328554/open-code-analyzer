package com.alibaba.arthas.nat.agent.proxy.factory;

import com.alibaba.arthas.nat.agent.proxy.registry.NativeAgentProxyRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Native Agent Proxy 注册中心工厂，通过 SPI 配置加载 {@link NativeAgentProxyRegistry} 各实现并缓存。
 *
 * @description: NativeAgentDiscoveryFactory
 * @author：flzjkl
 * @date: 2024-09-15 16:22
 */
public class NativeAgentProxyRegistryFactory {

    /** SPI 配置文件路径，键为注册类型名，值为实现类全限定名 */
    private static final String FILE_PATH = "META-INF/arthas/com.alibaba.arthas.native.agent.proxy.NativeAgentProxyRegistryFactory";
    /** 注册类型名 → 注册实现实例 */
    private static Map<String, NativeAgentProxyRegistry> nativeAgentProxyRegistryMap = new ConcurrentHashMap<>();

    private static volatile NativeAgentProxyRegistryFactory nativeAgentProxyRegistryFactory;

    private NativeAgentProxyRegistryFactory() {
        Map<String, String> registrationConfigMap = readConfigInfo(FILE_PATH);
        loadNativeAgentProxyRegistry2Map(registrationConfigMap);
    }

    /**
     * 获取工厂单例（双重检查锁）。
     */
    public static NativeAgentProxyRegistryFactory getNativeAgentProxyRegistryFactory() {
        if (nativeAgentProxyRegistryFactory == null) {
            synchronized (NativeAgentProxyRegistryFactory.class) {
                if (nativeAgentProxyRegistryFactory == null) {
                    nativeAgentProxyRegistryFactory = new NativeAgentProxyRegistryFactory();
                }
            }
        }
        return nativeAgentProxyRegistryFactory;
    }

    /**
     * 反射实例化 SPI 配置中的各 {@link NativeAgentProxyRegistry} 实现并写入缓存。
     */
    private void loadNativeAgentProxyRegistry2Map(Map<String, String> registrationConfigMap) {
        for (Map.Entry<String, String> entry : registrationConfigMap.entrySet()) {
            String name = entry.getKey();
            String classPath = entry.getValue();

            try {
                Class<?> clazz = Class.forName(classPath);
                Constructor<?> constructor = clazz.getConstructor();
                NativeAgentProxyRegistry instance = (NativeAgentProxyRegistry) constructor.newInstance();
                nativeAgentProxyRegistryMap.put(name, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 读取 classpath 下 SPI 配置文件，解析 key=value 行。
     *
     * @param filePath 资源路径
     * @return 配置键值对
     */
    public Map<String, String> readConfigInfo (String filePath) {
        Map<String, String> nativeAgentDiscoveryConfigMap = new ConcurrentHashMap<>();
        ClassLoader classLoader = NativeAgentProxyRegistryFactory.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        nativeAgentDiscoveryConfigMap.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nativeAgentDiscoveryConfigMap;
    }

    /**
     * 按注册类型名获取对应的 {@link NativeAgentProxyRegistry} 实现。
     *
     * @param name 注册类型（如 etcd、zookeeper）
     */
    public NativeAgentProxyRegistry getNativeAgentProxyRegistry(String name) {
        return nativeAgentProxyRegistryMap.get(name);
    }
}
