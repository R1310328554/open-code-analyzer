package org.keycloak.services.ui.extend;

import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.Provider;

/**
 * 声明式 UI 标签页提供者：扩展管理控制台中的自定义标签页。
 * <p>同时继承 {@link Provider} 与 {@link ConfiguredProvider}，支持组件化配置。</p>
 */
public interface UiTabProvider extends Provider, ConfiguredProvider {

}
