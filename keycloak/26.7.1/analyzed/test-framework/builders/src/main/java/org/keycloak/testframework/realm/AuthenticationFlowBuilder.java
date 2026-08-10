package org.keycloak.testframework.realm;

import org.keycloak.representations.idm.AuthenticationFlowRepresentation;

/** {@link AuthenticationFlowRepresentation} 的流式构建器。 */
public class AuthenticationFlowBuilder extends Builder<AuthenticationFlowRepresentation> {

    private AuthenticationFlowBuilder(AuthenticationFlowRepresentation rep) {
        super(rep);
    }

    /** 创建空认证流构建器。 */
    public static AuthenticationFlowBuilder create() {
        return new AuthenticationFlowBuilder(new AuthenticationFlowRepresentation());
    }

    /** 一次性设置别名、描述、Provider id、顶级标志与内置标志。 */
    public static AuthenticationFlowBuilder create(String alias, String description, String providerId, boolean topLevel, boolean builtIn) {
        return create().alias(alias).description(description).providerId(providerId).topLevel(topLevel).builtIn(builtIn);
    }

    /** 基于已有表示包装为构建器。 */
    public static AuthenticationFlowBuilder update(AuthenticationFlowRepresentation rep) {
        return new AuthenticationFlowBuilder(rep);
    }

    /** @param alias 流别名（唯一标识） */
    public AuthenticationFlowBuilder alias(String alias) {
        rep.setAlias(alias);
        return this;
    }

    /** @param description 流描述 */
    public AuthenticationFlowBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /** @param providerId 流 Provider id（如 basic-flow） */
    public AuthenticationFlowBuilder providerId(String providerId) {
        rep.setProviderId(providerId);
        return this;
    }

    /** @param enabled 是否为顶级认证流 */
    public AuthenticationFlowBuilder topLevel(boolean enabled) {
        rep.setTopLevel(enabled);
        return this;
    }

    /** @param enabled 是否为内置流 */
    public AuthenticationFlowBuilder builtIn(boolean enabled) {
        rep.setBuiltIn(enabled);
        return this;
    }

    /** 追加认证执行项（导出表示）。 */
    public AuthenticationFlowBuilder authenticationExecutions(AuthenticationExecutionExportBuilder... authenticationExecutions) {
        rep.setAuthenticationExecutions(combine(rep.getAuthenticationExecutions(), authenticationExecutions));
        return this;
    }

}
