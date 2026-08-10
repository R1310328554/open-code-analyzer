package org.keycloak.authentication.authenticators.browser;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 恢复认证码表单认证器工厂，注册校验恢复认证码的浏览器表单认证器；仅在 RECOVERY_CODES 特性启用时可用。
 */
public class RecoveryAuthnCodesFormAuthenticatorFactory implements AuthenticatorFactory, EnvironmentDependentProviderFactory {

    /** 提供者 ID：auth-recovery-authn-code-form。 */
    public static final String PROVIDER_ID = "auth-recovery-authn-code-form";

    @Override
    /** @return 认证器提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Recovery Authentication Code Form";
    }

    @Override
    /** @return 恢复认证码凭证类型引用分类 */
    public String getReferenceCategory() {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }

    @Override
    /** @return 是否可配置（本认证器不可配置） */
    public boolean isConfigurable() {
        return false;
    }

    @Override
    /** @return 允许的执行要求选项 */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return ConfigurableAuthenticatorFactory.REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 是否允许用户自助配置恢复码 */
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    /** @return 认证器帮助说明文本 */
    public String getHelpText() {
        return "Validates a Recovery Authentication Code";
    }

    @Override
    /** @return 可配置属性列表（本认证器无配置项） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** @return 新建 {@link RecoveryAuthnCodesFormAuthenticator} 实例 */
    public Authenticator create(KeycloakSession keycloakSession) {
        return new RecoveryAuthnCodesFormAuthenticator(keycloakSession);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    /** @return 是否启用 RECOVERY_CODES 特性 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RECOVERY_CODES);
    }
}
