package org.keycloak.authentication.otp;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link OTPApplicationProvider} 工厂接口。
 */
public interface OTPApplicationProviderFactory extends ProviderFactory<OTPApplicationProvider> {

    /** 启动时初始化，默认空实现。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 全部 Provider 注册完成后的回调，默认空实现。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

}
