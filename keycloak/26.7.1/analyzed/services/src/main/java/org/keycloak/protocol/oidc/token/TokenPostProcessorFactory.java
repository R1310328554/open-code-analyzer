package org.keycloak.protocol.oidc.token;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * 令牌后处理器工厂。
 * <p>创建 {@link TokenPostProcessor} 实例；生命周期方法均为默认空实现。</p>
 */
public interface TokenPostProcessorFactory extends ProviderFactory<TokenPostProcessor> {

    /** 初始化（默认无操作） @param config 配置作用域 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 工厂初始化后回调（默认无操作） @param factory 会话工厂 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭资源（默认无操作） */
    @Override
    default void close() {
    }
}
