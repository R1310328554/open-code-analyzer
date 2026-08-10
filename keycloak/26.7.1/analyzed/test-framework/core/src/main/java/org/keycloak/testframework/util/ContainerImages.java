package org.keycloak.testframework.util;

import java.io.IOException;
import java.util.Properties;

/**
 * 从 classpath 上的 {@code containers.properties} 读取 Testcontainers 镜像名。
 */
public class ContainerImages {

    /**
     * 返回指定逻辑容器名对应的 Docker 镜像引用。
     *
     * @param containerName 属性前缀（不含 {@code .container} 后缀）
     * @return 完整镜像名
     */
    public static String getContainerImageName(String containerName) {
        return loadProperties().getProperty(containerName + ".container");
    }

    /** 懒加载并缓存 {@code containers.properties}。 */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ContainerImages.class.getResourceAsStream("containers.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
