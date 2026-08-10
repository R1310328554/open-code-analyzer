package org.keycloak.protocol.oidc.refresh;

import org.keycloak.provider.ProviderFactory;

/**
 * 刷新令牌提供者工厂 SPI：创建 {@link RefreshTokenProvider} 实例。
 */
public interface RefreshTokenProviderFactory extends ProviderFactory<RefreshTokenProvider> {
}
