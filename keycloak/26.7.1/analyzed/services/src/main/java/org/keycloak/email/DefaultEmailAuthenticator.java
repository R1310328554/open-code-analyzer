package org.keycloak.email;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

import org.keycloak.models.KeycloakSession;

/**
 * 无认证 SMTP 连接策略：当 realm 未启用 {@code auth} 时直接调用 {@link Transport#connect()}。
 */
public class DefaultEmailAuthenticator implements EmailAuthenticator {

    @Override
    /** 建立无需用户名/密码的 SMTP 连接。 */
    public void connect(KeycloakSession session, Map<String, String> config, Transport transport) throws EmailException {
        try {
            transport.connect();
        } catch (MessagingException e) {
            throw new EmailException("Non authenticated connect failed", e);
        }
    }
}
