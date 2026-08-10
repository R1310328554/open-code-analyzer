package org.keycloak.email;

import java.util.Map;

import jakarta.mail.Transport;

import org.keycloak.models.KeycloakSession;

/**
 * SMTP 传输层认证策略 SPI。
 * <p>由 {@link DefaultEmailSenderProvider} 根据 realm SMTP 配置选择具体实现并调用 {@link #connect}。</p>
 */
public interface EmailAuthenticator {

    /** 按配置对 {@link Transport} 执行 SMTP 认证或匿名连接。 */
    void connect(KeycloakSession session, Map<String, String> config, Transport transport) throws EmailException;

    /** SMTP 认证方式枚举，与 realm 配置 {@code authType} 对应。 */
    enum AuthenticatorType {
        /** 不启用 SMTP 认证。 */
        NONE,
        /** 用户名/密码（Basic）认证。 */
        BASIC,
        /** OAuth2 XOAUTH2 令牌认证。 */
        TOKEN
    }
}
