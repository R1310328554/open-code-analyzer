package org.keycloak.jose.jwk;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.crypto.Algorithm;

/**
 * AKP 公钥编解码工具：为 X.509 DER 公钥添加/移除算法相关前缀，以适配 JWK {@code pub} 字段格式。
 */
public class AKPUtils {

    // 新增算法前缀时可参考 AKPJWKTest 生成
    /** 各 ML-DSA 算法对应的 X.509 SubjectPublicKeyInfo 固定前缀。 */
    static final Map<String, byte[]> PREFIXES = new HashMap<>();
    static {
        PREFIXES.put(Algorithm.ML_DSA_44, new byte[] { 48, -126, 5, 50, 48, 11, 6, 9, 96, -122, 72, 1, 101, 3, 4, 3, 17, 3, -126, 5, 33, 0, });
        PREFIXES.put(Algorithm.ML_DSA_65, new byte[] { 48, -126, 7, -78, 48, 11, 6, 9, 96, -122, 72, 1, 101, 3, 4, 3, 18, 3, -126, 7, -95, 0, });
        PREFIXES.put(Algorithm.ML_DSA_87, new byte[] { 48, -126, 10, 50, 48, 11, 6, 9, 96, -122, 72, 1, 101, 3, 4, 3, 19, 3, -126, 10, 33, 0, });
    }

    /**
     * 将 JWK {@code pub} 字段解码为 {@link PublicKey}（拼接前缀后按 X.509 解析）。
     *
     * @param publicKey Base64URL 编码的公钥材料
     * @param algorithm JWA 算法名（如 ML-DSA-44）
     */
    public static PublicKey fromEncodedPub(String publicKey, String algorithm) {
        try {
            byte[] prefix = PREFIXES.get(algorithm);
            byte[] keyWithPadding = combine(prefix, Base64.getUrlDecoder().decode(publicKey));

            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyWithPadding);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 {@link PublicKey} 编码为 JWK {@code pub} 字段（去除 X.509 前缀后 Base64URL）。
     *
     * @param publicKey 公钥
     * @param algorithm JWA 算法名
     */
    public static String toEncodedPub(PublicKey publicKey, String algorithm) {
        byte[] prefix = PREFIXES.get(algorithm);
        byte[] keyOutWithoutPadding = removePadding(publicKey.getEncoded(), prefix.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyOutWithoutPadding);
    }

    private static byte[] combine(byte[] first, byte[] second) {
        byte[] c = new byte[first.length + second.length];
        System.arraycopy(first, 0, c, 0, first.length);
        System.arraycopy(second, 0, c, first.length, second.length);
        return c;
    }

    private static byte[] removePadding(byte[] bytes, int length) {
        byte[] b = new byte[bytes.length - length];
        System.arraycopy(bytes, length, b, 0, bytes.length - length);
        return b;
    }

}
