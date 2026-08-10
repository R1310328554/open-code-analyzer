package org.keycloak.credential;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * 恢复认证码凭证 {@link RecoveryAuthnCodesCredentialProvider} 的 SPI 工厂。
 * <p>仅在 {@link Profile.Feature#RECOVERY_CODES} 特性启用时注册。</p>
 */
public class RecoveryAuthnCodesCredentialProviderFactory
        implements CredentialProviderFactory<RecoveryAuthnCodesCredentialProvider>, EnvironmentDependentProviderFactory {

    /** SPI 工厂标识：{@code keycloak-recovery-authn-codes}。 */
    public static final String PROVIDER_ID = "keycloak-recovery-authn-codes";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @param session 当前会话 @return 恢复码凭证提供者实例 */
    public RecoveryAuthnCodesCredentialProvider create(KeycloakSession session) {
        return new RecoveryAuthnCodesCredentialProvider(session);
    }

    @Override
    /** @return 是否启用 RECOVERY_CODES 特性 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RECOVERY_CODES);
    }
}
