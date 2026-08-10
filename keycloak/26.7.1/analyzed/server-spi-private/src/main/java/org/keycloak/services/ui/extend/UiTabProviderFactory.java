package org.keycloak.services.ui.extend;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * {@link UiTabProvider} 的组件工厂接口。
 * <p>提供标签页路径与参数元数据，基于 {@link ComponentModel} 创建实例。</p>
 */
public interface UiTabProviderFactory<T> extends ComponentFactory<T, UiTabProvider> {
    /** 默认实现返回 {@code null}，子类应覆盖以创建具体实例。 */
    default T create(KeycloakSession session, ComponentModel model) {
        return null;
    }

    /** 返回包含 {@code path} 与 {@code params} 的类型元数据。 */
    @Override
    default Map<String, Object> getTypeMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("path", getPath());
        metadata.put("params", new HashMap<>(getParams()));
        return metadata;
    }

    /** @return 标签页路由路径 */
    String getPath();

    /** @return 标签页路由参数字典 */
    Map<String, String> getParams();
}
