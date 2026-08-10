package org.keycloak.models;

import java.io.IOException;
import java.util.Set;

import org.keycloak.provider.Provider;
import org.keycloak.theme.Theme;

/**
 * 主题管理器 Provider：按类型解析与缓存 UI 主题。
 */
public interface ThemeManager extends Provider {

    /**
     * 按主题选择器返回指定类型的主题。
     * Returns the theme for the specified type. The theme is determined by the theme selector.
     *
     * @param type
     * @return
     * @throws IOException
     */
    Theme getTheme(Theme.Type type) throws IOException;

    /**
     * 返回指定名称与类型的主题。
     * Returns the specified theme for the specified type.
     *
     * @param name
     * @param type
     * @return
     * @throws IOException
     */
    Theme getTheme(String name, Theme.Type type) throws IOException;

    /**
     * 返回指定类型的全部主题名称集合。
     * Returns a set of all theme names for the specified type.
     *
     * @param type
     * @return
     */
    Set<String> nameSet(Theme.Type type);

    /** @return 主题缓存是否启用 */
    boolean isCacheEnabled();

    /** 清空主题缓存。 */
    void clearCache();

}
