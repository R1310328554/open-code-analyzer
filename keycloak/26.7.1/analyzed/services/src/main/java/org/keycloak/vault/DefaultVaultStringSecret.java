package org.keycloak.vault;

import java.util.Optional;

/**
 * 基于 {@link String} 的默认 {@link VaultStringSecret} 实现。
 * <p>关闭时将内部字符串引用置空，便于 GC 回收。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class DefaultVaultStringSecret implements VaultStringSecret {

    /** 空密钥单例（Optional.empty）。 */
    private static final VaultStringSecret EMPTY_VAULT_SECRET = new VaultStringSecret() {
        @Override
        public Optional<String> get() {
            return Optional.empty();
        }

        @Override
        public void close() {
        }
    };

    /**
     * 从 Optional {@link String} 创建 {@link VaultStringSecret}。
     * @param secret 密钥字符串
     * @return 非空时返回包装实例，否则返回空单例
     */
    public static VaultStringSecret forString(Optional<String> secret) {
        if (secret == null || ! secret.isPresent()) {
            return EMPTY_VAULT_SECRET;
        }
        return new DefaultVaultStringSecret(secret.get());
    }

    /** 底层密钥字符串。 */
    private String secret;

    /** 私有构造，通过 {@link #forString(Optional)} 创建。 */
    private DefaultVaultStringSecret(final String secret) {
        this.secret = secret;
    }

    /** @return 密钥字符串的 Optional 包装 */
    @Override
    public Optional<String> get() {
        return Optional.of(this.secret);
    }

    /** 清空内部字符串引用，释放密钥。 */
    @Override
    public void close() {
        this.secret = null;
    }
}
