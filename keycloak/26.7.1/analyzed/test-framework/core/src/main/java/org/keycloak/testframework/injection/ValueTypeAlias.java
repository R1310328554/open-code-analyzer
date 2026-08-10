package org.keycloak.testframework.injection;

import java.util.HashMap;
import java.util.Map;

/**
 * 值类型 Class 到配置别名（{@code kc.test.*} 键前缀）的映射。
 * <p>
 * 由 {@link Extensions} 从各 {@link org.keycloak.testframework.TestFrameworkExtension} 合并加载。
 */
public class ValueTypeAlias {

    /** 值类型到别名字符串的内部映射。 */
    private final Map<Class<?>, String> aliases = new HashMap<>();

    /** 批量合并扩展声明的别名条目。 */
    public void addAll(Map<Class<?>, String> aliases) {
        this.aliases.putAll(aliases);
    }

    /**
     * 获取值类型的配置别名。
     *
     * @param clazz 值类型
     * @return 已注册别名，未注册时返回 {@link Class#getSimpleName()}
     */
    public String getAlias(Class<?> clazz) {
        String alias = aliases.get(clazz);
        if (alias == null) {
            alias = clazz.getSimpleName();
        }
        return alias;
    }

}
