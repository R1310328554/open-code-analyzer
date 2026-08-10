package org.keycloak.credential.hash;

import org.keycloak.models.KeycloakSession;

/**
 * PBKDF2-HMAC-SHA256 密码哈希工厂（推荐变体之一）。
 *
 * @author <a href"mailto:abkaplan07@gmail.com">Adam Kaplan</a>
 */
public class Pbkdf2Sha256PasswordHashProviderFactory extends AbstractPbkdf2PasswordHashProviderFactory implements PasswordHashProviderFactory {

    public static final String ID = "pbkdf2-sha256";

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    /** OWASP 推荐的 PBKDF2-HMAC-SHA256 迭代次数（600,000）。 */

    public static final int DEFAULT_ITERATIONS = 600_000;

    /** 创建 256 位派生密钥的 SHA256 变体提供者。 */
    @Override
    public PasswordHashProvider create(KeycloakSession session) {
        return new Pbkdf2PasswordHashProvider(ID, PBKDF2_ALGORITHM, DEFAULT_ITERATIONS, getMaxPaddingLength(), 256);
    }

    /** @return 提供者 ID：{@code pbkdf2-sha256} */
    @Override
    public String getId() {
        return ID;
    }

    /** 优先级 100，高于已弃用的 SHA1 变体。 */
    @Override
    public int order() {
        return 100;
    }
}
