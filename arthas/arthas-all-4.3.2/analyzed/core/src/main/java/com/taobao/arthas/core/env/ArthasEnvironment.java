package com.taobao.arthas.core.env;

import java.security.AccessControlException;
import java.util.Map;

/**
 * Arthas 运行时环境：聚合 JVM 系统属性、操作系统环境变量等属性源，
 * 并提供统一的 {@link PropertyResolver} 访问接口。
 * <p>
 * 启动时自动注册 {@link #SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME} 与
 * {@link #SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME} 两个内置源；
 * 可通过 {@link #addFirst}/{@link #addLast} 注入自定义配置（如 arthas.properties）。
 *
 * @author hengyunabc 2019-12-27
 */
public class ArthasEnvironment implements Environment {
    /** 操作系统环境变量属性源名称 */
    public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

    /** JVM 系统属性（-D）属性源名称 */
    public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";

    /** 可变的属性源链，优先级由 addFirst/addLast 决定 */
    private final MutablePropertySources propertySources = new MutablePropertySources();

    /** 基于属性源链的属性解析器，委托给 propertySources */
    private final ConfigurablePropertyResolver propertyResolver = new PropertySourcesPropertyResolver(
            this.propertySources);

    public ArthasEnvironment() {
        // 环境变量优先级低于系统属性（后 addLast 的源优先级更高，此处 env 先加、props 后加）
        propertySources.addLast(
                new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment()));
        propertySources
                .addLast(new PropertiesPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, getSystemProperties()));
    }

    /**
     * 将属性源插入链首（最高优先级）。
     */
    public void addFirst(PropertySource<?> propertySource) {
        this.propertySources.addFirst(propertySource);
    }

    /**
     * 将属性源追加到链尾（最低优先级）。
     */
    public void addLast(PropertySource<?> propertySource) {
        this.propertySources.addLast(propertySource);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> getSystemProperties() {
        try {
            return (Map) System.getProperties();
        } catch (AccessControlException ex) {
            // 安全沙箱下退化为只读视图
            return (Map) new ReadOnlySystemAttributesMap() {
                @Override
                protected String getSystemAttribute(String attributeName) {
                    try {
                        return System.getProperty(attributeName);
                    } catch (AccessControlException ex) {
                        return null;
                    }
                }
            };
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> getSystemEnvironment() {
        try {
            return (Map) System.getenv();
        } catch (AccessControlException ex) {
            // 安全沙箱下退化为只读视图
            return (Map) new ReadOnlySystemAttributesMap() {
                @Override
                protected String getSystemAttribute(String attributeName) {
                    try {
                        return System.getenv(attributeName);
                    } catch (AccessControlException ex) {
                        return null;
                    }
                }
            };
        }
    }

    // ---------------------------------------------------------------------
    // PropertyResolver 接口实现（委托给 propertyResolver）
    // ---------------------------------------------------------------------

    @Override
    public boolean containsProperty(String key) {
        return this.propertyResolver.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return this.propertyResolver.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.propertyResolver.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return this.propertyResolver.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return this.propertyResolver.getProperty(key, targetType, defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key, targetType);
    }

    @Override
    public String resolvePlaceholders(String text) {
        return this.propertyResolver.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return this.propertyResolver.resolveRequiredPlaceholders(text);
    }

}
