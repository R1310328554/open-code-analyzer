package org.keycloak.testframework.crypto;

import org.keycloak.common.crypto.FipsMode;

/**
 * 集成测试加密能力辅助类，根据 {@link FipsMode} 暴露 FIPS 状态、
 * 支持的密钥库类型与 RSA 密钥长度期望。
 */
public class CryptoHelper {

    private final FipsMode fips;

    /** @param fips 当前测试运行的 FIPS 模式 */
    public CryptoHelper(FipsMode fips) {
        this.fips = fips;
    }

    /** @return 绑定本 Helper 的 {@link CryptoKeyStore} 工具实例 */
    public CryptoKeyStore keystore() {
        return new CryptoKeyStore(this);
    }

    /** @return 是否为 STRICT 或 NON_STRICT FIPS 模式 */
    public boolean isFips() {
        return switch (fips) {
            case STRICT, NON_STRICT -> true;
            default -> false;
        };
    }

    /** @return 当前 FIPS 模式下预期支持的密钥库类型名称数组 */
    public String[] getExpectedSupportedKeyStoreTypes() {
        return switch (fips) {
            case NON_STRICT -> new String[] { "PKCS12", "BCFKS" };
            case STRICT -> new String[] { "BCFKS" };
            default -> new String[] { "BCFKS", "JKS", "PKCS12" };
        };
    }

    /** @return 当前 FIPS 模式下预期允许的 RSA 密钥长度（字符串形式） */
    public String[] getExpectedSupportedRsaKeySizes() {
        return switch (fips) {
            case STRICT -> new String[]{"2048", "3072", "4096"};
            default -> new String[]{"1024", "2048", "3072", "4096"};
        };
    }

}
