package org.keycloak.theme;

import org.keycloak.models.ThemeManager;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link ThemeManager} 的 SPI 工厂接口。
 * <p>负责创建主题管理器并支持清除主题缓存。</p>
 */
public interface ThemeManagerFactory extends ProviderFactory<ThemeManager> {
  /** 清除主题缓存。 */
  void clearCache();
}
