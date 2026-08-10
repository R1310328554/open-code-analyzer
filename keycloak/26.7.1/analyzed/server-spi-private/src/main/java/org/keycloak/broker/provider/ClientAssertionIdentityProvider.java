package org.keycloak.broker.provider;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;

/**
 * 支持客户端断言（client assertion）认证的身份提供者扩展接口。
 * <p>在 {@link ClientAuthenticationFlowContext} 中校验外部 IdP 签发的客户端断言。</p>
 */
public interface ClientAssertionIdentityProvider<C extends IdentityProviderModel> extends IdentityProvider<C> {

    /** 校验客户端断言是否有效并匹配本 IdP 配置。 */
    boolean verifyClientAssertion(ClientAuthenticationFlowContext context) throws Exception;

}
