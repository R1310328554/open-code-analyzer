package org.keycloak.common.crypto;

/**
 * FIPS 140-2 运行模式枚举。
 *
 * <p>每种模式映射到 classpath 上应加载的 {@link CryptoProvider} 实现类全名。</p>
 */
public enum FipsMode {
    /** 非严格 FIPS 模式。 */
    NON_STRICT("org.keycloak.crypto.fips.FIPS1402Provider"),
    /** 严格 FIPS 140-2 模式。 */
    STRICT("org.keycloak.crypto.fips.Fips1402StrictCryptoProvider"),
    /** 禁用 FIPS，使用默认（非 FIPS）提供方。 */
    DISABLED("org.keycloak.crypto.def.DefaultCryptoProvider");

    /** 对应的 CryptoProvider 实现类名。 */
    private final String providerClassName;
    /** CLI/配置选项中的 kebab-case 名称。 */
    private final String optionName;

    FipsMode(String providerClassName) {
        this.providerClassName = providerClassName;
        this.optionName = name().toLowerCase().replace('_', '-');
    }

    /** @return 当前模式是否启用 FIPS（非 {@link #DISABLED}） */
    public boolean isFipsEnabled() {
        return this.equals(NON_STRICT) || this.equals(STRICT);
    }

    /** @return 应实例化的 CryptoProvider 类全名 */
    public String getProviderClassName() {
        return providerClassName;
    }

    /** 将配置选项字符串（如 {@code non-strict}）解析为枚举常量。 */
    public static FipsMode valueOfOption(String name) {
        return valueOf(name.toUpperCase().replace('-', '_'));
    }

    /** @return 配置选项使用的 kebab-case 名称 */
    @Override
    public String toString() {
        return optionName;
    }
}
