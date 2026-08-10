package org.keycloak.common.crypto;

/**
 * JWE/JWA 算法名、安全提供方 ID 与椭圆曲线标识等密码学常量。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CryptoConstants {

    // JWE 密钥包装算法
    public static final String A128KW = "A128KW";
    public static final String RSA1_5 = "RSA1_5";
    public static final String RSA_OAEP = "RSA-OAEP";
    public static final String RSA_OAEP_256 = "RSA-OAEP-256";
    public static final String ECDH_ES = "ECDH-ES";
    public static final String ECDH_ES_A128KW = "ECDH-ES+A128KW";
    public static final String ECDH_ES_A192KW = "ECDH-ES+A192KW";
    public static final String ECDH_ES_A256KW = "ECDH-ES+A256KW";

    // OCSP 提供方常量（已注释）
    // public static final String OCSP = "OCSP";

    /** 非 FIPS 环境下 BouncyCastle 的 {@link java.security.Provider} 名称。 */
    public static final String BC_PROVIDER_ID = "BC";

    /** FIPS 140-2 模式下 BouncyCastle FIPS 提供方名称。 */
    public static final String BCFIPS_PROVIDER_ID = "BCFIPS";

    /** NIST P-256 曲线名（secp256r1）。 */
    public static final String EC_KEY_SECP256R1 = "secp256r1";
    /** NIST P-384 曲线名（secp384r1）。 */
    public static final String EC_KEY_SECP384R1 = "secp384r1";
    /** NIST P-521 曲线名（secp521r1）。 */
    public static final String EC_KEY_SECP521R1 = "secp521r1";

}
