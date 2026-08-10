package org.keycloak.services.ui.extend;

import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;

/**
 * {@link UiPageProvider} 的组件工厂接口。
 * <p>基于 {@link ComponentModel} 创建页面提供者实例。</p>
 */
public interface UiPageProviderFactory<T> extends ComponentFactory<T, UiPageProvider> {
    /** 默认实现返回 {@code null}，子类应覆盖以创建具体实例。 */
    default T create(KeycloakSession session, ComponentModel model) {
        return null;
    }
}
