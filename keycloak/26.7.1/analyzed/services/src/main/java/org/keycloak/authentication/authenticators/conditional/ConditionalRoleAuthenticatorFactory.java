package org.keycloak.authentication.authenticators.conditional;

import java.util.Arrays;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用户角色条件认证器工厂：注册 {@link ConditionalRoleAuthenticator}，可配置目标角色及是否取反。
 */
public class ConditionalRoleAuthenticatorFactory implements ConditionalAuthenticatorFactory {
    /** Provider ID：conditional-user-role。 */
    public static final String PROVIDER_ID = "conditional-user-role";

    /** 配置键：待校验的用户角色。 */
    public static final String CONDITIONAL_USER_ROLE = "condUserRole";
    /** 配置键：是否对校验结果取反。 */
    public static final String CONF_NEGATE = "negate";

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
        return "Condition - user role";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static final Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
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
    /** @return 帮助说明：仅当用户拥有指定角色时执行流程 */
    public String getHelpText() {
        return "Flow is executed only if user has the given role.";
    }

    @Override
    /** @return 角色选择与取反开关配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty role = new ProviderConfigProperty();
        role.setType(ProviderConfigProperty.ROLE_TYPE);
        role.setName(CONDITIONAL_USER_ROLE);
        role.setLabel("User role");
        role.setHelpText("Role the user should have to execute this flow. Click 'Select Role' button to browse roles, or just type it in the textbox. To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");

        ProviderConfigProperty negateOutput = new ProviderConfigProperty();
        negateOutput.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        negateOutput.setName(CONF_NEGATE);
        negateOutput.setLabel("Negate output");
        negateOutput.setHelpText("Apply a NOT to the check result. When this is true, then the condition will evaluate to true just if user does NOT have the specified role. When this is false, the condition will evaluate to true just if user has the specified role");

        return Arrays.asList(role, negateOutput);
    }

    @Override
    /** @return {@link ConditionalRoleAuthenticator} 单例 */
    public ConditionalAuthenticator getSingleton() {
        return ConditionalRoleAuthenticator.SINGLETON;
    }
}
