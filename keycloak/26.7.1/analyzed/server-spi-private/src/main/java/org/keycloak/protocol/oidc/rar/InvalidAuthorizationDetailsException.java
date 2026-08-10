package org.keycloak.protocol.oidc.rar;

/**
 * 授权详情（{@code authorization_details}）无效时抛出的运行时异常。
 * <p>由 {@link AuthorizationDetailsProcessor} 在校验或处理阶段抛出。</p>
 */
public class InvalidAuthorizationDetailsException extends RuntimeException {

    /** @param message 错误描述 */
    public InvalidAuthorizationDetailsException(String message) {
        super(message);
    }
}
