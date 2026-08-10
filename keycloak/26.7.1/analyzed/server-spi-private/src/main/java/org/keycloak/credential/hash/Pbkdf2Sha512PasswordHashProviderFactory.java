package org.keycloak.credential.hash;

import org.keycloak.models.KeycloakSession;

/**
 * PBKDF2-HMAC-SHA512 密码哈希工厂（推荐变体之一）。
 *
 * @author @author <a href="mailto:abkaplan07@gmail.com">Adam Kaplan</a>
 */
public class Pbkdf2Sha512PasswordHashProviderFactory extends AbstractPbkdf2PasswordHashProviderFactory implements PasswordHashProviderFactory {

    public static final String ID = "pbkdf2-sha512";

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";

    /** OWASP 推荐的 PBKDF2-HMAC-SHA512 迭代次数（210,000）。 */

    public static final int DEFAULT_ITERATIONS = 210_000;

    /** 创建默认 512 位派生密钥的 SHA512 变体提供者。 */
    @Override
    public PasswordHashProvider create(KeycloakSession session) {
        return new Pbkdf2PasswordHashProvider(ID, PBKDF2_ALGORITHM, DEFAULT_ITERATIONS, getMaxPaddingLength());
    }

    /** @return 提供者 ID：{@code pbkdf2-sha512} */
    @Override
    public String getId() {
        return ID;
    }

    /** 最高优先级（200），作为首选 PBKDF2 变体之一。 */
    @Override
    public int order() {
        return 200;
    }
}
