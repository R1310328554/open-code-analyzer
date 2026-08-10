package org.keycloak.authentication;

/**
 * 标记可注册特定类型用户凭证的 Required Action 实现。
 * <p>继承 {@link CredentialAction}，语义上强调「注册/绑定」而非仅校验。</p>
 *
 * Marking implementation of the action, which is able to register credential of the particular type
 */
public interface CredentialRegistrator extends CredentialAction {
}
