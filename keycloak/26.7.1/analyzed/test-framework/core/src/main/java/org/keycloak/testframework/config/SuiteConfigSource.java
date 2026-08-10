package org.keycloak.testframework.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * 内存中的套件级配置源，优先级 270，供测试套件在运行时动态注入键值对。
 */
public class SuiteConfigSource implements ConfigSource {

    private static final Map<String, String> SUITE_CONFIG = new HashMap<>();

    /** 设置或覆盖套件级配置项。 */
    public static void set(String key, String value) {
        SUITE_CONFIG.put(key, value);
    }

    /** 清空所有套件级配置。 */
    public static void clear() {
        SUITE_CONFIG.clear();
    }

    /** @return 当前已注册的配置键集合 */
    @Override
    public Set<String> getPropertyNames() {
        return SUITE_CONFIG.keySet();
    }

    /** @param s 配置键 @return 对应值，不存在时返回 null */
    @Override
    public String getValue(String s) {
        return SUITE_CONFIG.get(s);
    }

    /** @return 配置源名称 */
    @Override
    public String getName() {
        return "SuiteConfigSource";
    }

    /** @return 配置源优先级（270） */
    @Override
    public int getOrdinal() {
        return 270;
    }
}
