package com.alibaba.arthas.nat.agent.management.web.factory;

import com.alibaba.arthas.nat.agent.management.web.discovery.NativeAgentProxyDiscovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Native Agent Proxy 服务发现工厂，通过 SPI 配置加载 etcd/ZooKeeper 等实现并缓存单例。
 *
 * @description: NativeAgentProxyDiscoveryFactory
 * @author：flzjkl
 * @date: 2024-10-20 20:37
 */
public class NativeAgentProxyDiscoveryFactory {

    /** SPI 配置文件路径，格式为 registrationType=实现类全名 */
    private static final String FILE_PATH = "META-INF/arthas/com.alibaba.arthas.native.agent.management.web.NativeAgentProxyDiscoveryFactory";
    /** 注册类型名称到发现实现实例的映射 */
    private static Map<String, NativeAgentProxyDiscovery> nativeAgentProxyDiscoveryMap = new ConcurrentHashMap<>();

    private static volatile NativeAgentProxyDiscoveryFactory nativeAgentProxyDiscoveryFactory;

    private NativeAgentProxyDiscoveryFactory() {
        Map<String, String> registrationConfigMap = readConfigInfo(FILE_PATH);
        loadNativeAgentDiscovery2Map(registrationConfigMap);
    }

    /** 双重检查锁获取工厂单例 */
    public static NativeAgentProxyDiscoveryFactory getNativeAgentProxyDiscoveryFactory() {
        if (nativeAgentProxyDiscoveryFactory == null) {
            synchronized (NativeAgentProxyDiscoveryFactory.class) {
                if (nativeAgentProxyDiscoveryFactory == null) {
                    nativeAgentProxyDiscoveryFactory = new NativeAgentProxyDiscoveryFactory();
                }
            }
        }
        return nativeAgentProxyDiscoveryFactory;
    }

    /** 反射实例化 SPI 配置中的各发现实现并注册到 map */
    private void loadNativeAgentDiscovery2Map(Map<String, String> registrationConfigMap) {
        for (Map.Entry<String, String> entry : registrationConfigMap.entrySet()) {
            String name = entry.getKey();
            String classPath = entry.getValue();

            try {
                Class<?> clazz = Class.forName(classPath);
                Constructor<?> constructor = clazz.getConstructor();
                NativeAgentProxyDiscovery instance = (NativeAgentProxyDiscovery) constructor.newInstance();
                nativeAgentProxyDiscoveryMap.put(name, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 从 classpath 读取 key=value 格式的 SPI 配置。
     *
     * @param filePath 配置文件在 classpath 中的路径
     */
    public Map<String, String> readConfigInfo (String filePath) {
        Map<String, String> nativeAgentDiscoveryConfigMap = new ConcurrentHashMap<>();
        ClassLoader classLoader = NativeAgentProxyDiscoveryFactory.class.getClassLoader();

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
     * 按注册类型名称获取对应的发现实现。
     *
     * @param name 注册类型，如 etcd、zookeeper
     */
    public NativeAgentProxyDiscovery getNativeAgentProxyDiscovery(String name) {
        return nativeAgentProxyDiscoveryMap.get(name);
    }
}
