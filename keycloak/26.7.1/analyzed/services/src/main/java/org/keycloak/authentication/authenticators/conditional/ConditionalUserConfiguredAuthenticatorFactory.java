package org.keycloak.authentication.authenticators.conditional;

import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用户已配置条件认证器工厂：注册 {@link ConditionalUserConfiguredAuthenticator}，无额外配置项。
 */
public class ConditionalUserConfiguredAuthenticatorFactory implements ConditionalAuthenticatorFactory {
    /** Provider ID：conditional-user-configured。 */
    public static final String PROVIDER_ID = "conditional-user-configured";
    /** 内部配置键（当前未使用）。 */
    protected static final String CONDITIONAL_USER_ROLE = "condUserConfigured";

    @Override
    public void init(Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Condition - user configured";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static final Requirement[] REQUIREMENT_CHOICES = {
        AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    /** @return 帮助说明：仅当同级认证器已配置时执行当前流程 */
    public String getHelpText() {
        return "Executes the current flow only if authenticators are configured";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** @return {@link ConditionalUserConfiguredAuthenticator} 单例 */
    public ConditionalAuthenticator getSingleton() {
        return ConditionalUserConfiguredAuthenticator.SINGLETON;
    }
}
