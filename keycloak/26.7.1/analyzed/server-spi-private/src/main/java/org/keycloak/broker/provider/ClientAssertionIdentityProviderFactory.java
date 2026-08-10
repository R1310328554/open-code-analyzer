package org.keycloak.broker.provider;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;

/**
 * 客户端断言身份提供者工厂辅助接口，定义断言类型支持与客户端/IdP 查找策略。
 */
public interface ClientAssertionIdentityProviderFactory {

    /** 返回断言解析策略；默认无策略。 */
    default ClientAssertionStrategy getClientAssertionStrategy() {
        return null;
    }

    /** 按断言类型查找 {@link ClientModel} 与 {@link IdentityProviderModel} 的策略。 */
    interface ClientAssertionStrategy {

        /** 是否支持给定断言类型（如 JWT bearer）。 */
        boolean isSupportedAssertionType(String assertionType);

        /** 从认证上下文中解析客户端与身份提供者配置。 */
        LookupResult lookup(ClientAuthenticationFlowContext context) throws Exception;

    }

    /** 客户端断言查找结果：匹配的客户端与 IdP 配置。 */
    record LookupResult(ClientModel clientModel, IdentityProviderModel identityProviderModel) {}

}
