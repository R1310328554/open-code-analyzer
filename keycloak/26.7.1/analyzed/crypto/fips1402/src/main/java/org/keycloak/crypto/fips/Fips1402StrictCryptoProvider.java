package org.keycloak.crypto.fips;

import org.bouncycastle.crypto.CryptoServicesRegistrar;

/**
 * 强制 BouncyCastle 默认运行于 FIPS 批准模式的 {@link FIPS1402Provider} 子类。
 * <p>
 * 通过系统属性 {@code org.bouncycastle.fips.approved_only} 设置全局模式，
 * 避免 {@link CryptoServicesRegistrar#setApprovedOnlyMode(boolean)} 的每线程限制。
 */
public class Fips1402StrictCryptoProvider extends FIPS1402Provider {

    static {
        System.setProperty("org.bouncycastle.fips.approved_only", Boolean.TRUE.toString());
    }

    /** {@inheritDoc} 批准模式下仅支持 2048/3072/4096 位 RSA。 */
    @Override
    public String[] getSupportedRsaKeySizes() {
        // BCFIPS 批准模式不支持 1024 位 RSA
        return new String[] {"2048", "3072", "4096"};
    }
}
