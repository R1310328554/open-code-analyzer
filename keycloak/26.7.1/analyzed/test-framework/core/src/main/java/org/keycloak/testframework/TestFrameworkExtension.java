package org.keycloak.testframework;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.keycloak.testframework.injection.Supplier;

/**
 * 测试框架扩展 SPI，用于向框架注册额外的 {@link Supplier} 与值类型别名。
 */
public interface TestFrameworkExtension {

    /**
     * 扩展提供的 Supplier 列表。
     * @return supplier 列表
     */
    List<Supplier<?, ?>> suppliers();

    /**
     * 运行测试时始终创建的值类型；扩展通常无需覆写。
     * @return 始终请求的值类型列表
     */
    default List<Class<?>> alwaysEnabledValueTypes() {
        return Collections.emptyList();
    }

    /**
     * 值类型别名映射。默认使用 {@code getSimpleName}；可实现本方法自定义，
     * 例如核心扩展将 {@code KeycloakServer} 别名为 {@code server}。
     * @return 键为值类型、值为别名的映射
     */
    default Map<Class<?>, String> valueTypeAliases() {
        return Collections.emptyMap();
    }

}
