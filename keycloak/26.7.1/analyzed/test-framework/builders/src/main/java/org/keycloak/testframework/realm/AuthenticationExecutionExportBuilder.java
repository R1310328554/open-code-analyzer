package org.keycloak.testframework.realm;

import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;

/** {@link AuthenticationExecutionExportRepresentation} 的流式构建器，用于领域导入/导出场景。 */
public class AuthenticationExecutionExportBuilder extends Builder<AuthenticationExecutionExportRepresentation> {

    private AuthenticationExecutionExportBuilder(AuthenticationExecutionExportRepresentation rep) {
        super(rep);
    }

    /** 创建空的导出执行构建器。 */
    public static AuthenticationExecutionExportBuilder create() {
        return new AuthenticationExecutionExportBuilder(new AuthenticationExecutionExportRepresentation());
    }

    /** 快捷创建单步认证器执行项。 */
    public static AuthenticationExecutionExportBuilder authenticator(String authenticator, String requirement, Integer priority, boolean  userSetupAllowed) {
        return create().authenticatorFlow(false).authenticator(authenticator).requirement(requirement).priority(priority);
    }

    /** 快捷创建嵌套流别名执行项。 */
    public static AuthenticationExecutionExportBuilder alias(String flowAlias, String requirement, Integer priority, boolean  userSetupAllowed) {
        return create().authenticatorFlow(true).flowAlias(flowAlias).requirement(requirement).priority(priority);
    }

    /** 基于已有表示包装为构建器。 */
    public static AuthenticationExecutionExportBuilder update(AuthenticationExecutionExportRepresentation rep) {
        return new AuthenticationExecutionExportBuilder(rep);
    }

    /** @param authenticator 认证器 Provider id */
    public AuthenticationExecutionExportBuilder authenticator(String authenticator) {
        rep.setAuthenticator(authenticator);
        return this;
    }

    /** @param flowAlias 嵌套认证流别名 */
    public AuthenticationExecutionExportBuilder flowAlias(String flowAlias) {
        rep.setFlowAlias(flowAlias);
        return this;
    }

    /** @param requirement 执行要求 */
    public AuthenticationExecutionExportBuilder requirement(String requirement) {
        rep.setRequirement(requirement);
        return this;
    }

    /** @param priority 执行优先级 */
    public AuthenticationExecutionExportBuilder priority(Integer priority) {
        rep.setPriority(priority);
        return this;
    }

    /** @param authenticatorFlow 是否为嵌套流 */
    public AuthenticationExecutionExportBuilder authenticatorFlow(boolean authenticatorFlow) {
        rep.setAuthenticatorFlow(authenticatorFlow);
        return this;
    }

    /** @param userSetupAllowed 是否允许用户自行配置（如 OTP 注册） */
    public AuthenticationExecutionExportBuilder userSetupAllowed(boolean userSetupAllowed) {
        rep.setUserSetupAllowed(userSetupAllowed);
        return this;
    }

}
