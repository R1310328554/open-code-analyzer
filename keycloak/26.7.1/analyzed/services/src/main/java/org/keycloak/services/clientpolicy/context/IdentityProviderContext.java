package org.keycloak.services.clientpolicy.context;

import org.keycloak.services.clientpolicy.ClientPolicyContext;

/**
 * 携带身份提供者信息的 {@link org.keycloak.services.clientpolicy.ClientPolicyContext} 扩展接口。
 * <p>供条件与执行器在策略评估时读取目标 IdP 别名。</p>
 */
public interface IdentityProviderContext extends ClientPolicyContext {

    /** @return 目标身份提供者别名 */
    String getIdentityProviderAlias();
}
