package org.keycloak.email;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

import org.keycloak.models.KeycloakSession;
import org.keycloak.vault.VaultStringSecret;

/**
 * 基于用户名/密码的 SMTP 认证实现。
 * <p>优先从 {@link org.keycloak.vault.Vault} 读取密码密钥，否则使用明文配置值。</p>
 */
public class PasswordAuthEmailAuthenticator implements EmailAuthenticator {

    @Override
    /** 使用 {@code user} 与 vault/配置密码连接 SMTP 服务器。 */
    public void connect(KeycloakSession session, Map<String, String> config, Transport transport) throws EmailException {
        try (VaultStringSecret vaultStringSecret = session.vault().getStringSecret(config.get("password"))) {
            transport.connect(config.get("user"), vaultStringSecret.get().orElse(config.get("password")));
        } catch (MessagingException e) {
            throw new EmailException("Password based SMTP connect failed", e);
        }
    }

}
