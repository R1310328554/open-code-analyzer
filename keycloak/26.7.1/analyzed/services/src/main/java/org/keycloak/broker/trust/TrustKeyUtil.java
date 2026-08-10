package org.keycloak.broker.trust;

import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.broker.provider.TrustMaterialRequest;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.util.Strings;

/**
 * 信任材料密钥工具：按 {@link TrustMaterialRequest} 的 kid、算法与用途
 * 过滤 JWK 流，仅保留可用于签名校验的密钥。
 */
public class TrustKeyUtil {

    /** 工具类，禁止实例化。 */
    private TrustKeyUtil() {
    }

    /** 过滤 null、kid/算法不匹配及非 SIG 用途的 JWK。 */
    public static Stream<JWK> filterKeys(Stream<JWK> keys, TrustMaterialRequest request) {
        return keys
                .filter(Objects::nonNull)
                .filter(key -> Strings.isEmpty(request.getKid()) || Objects.equals(request.getKid(), key.getKeyId()))
                .filter(key -> Strings.isEmpty(request.getAlgorithm()) || Strings.isEmpty(key.getAlgorithm())
                        || Objects.equals(request.getAlgorithm(), key.getAlgorithm()))
                .filter(key -> Strings.isEmpty(key.getPublicKeyUse()) || Objects.equals(JWK.Use.SIG.asString(), key.getPublicKeyUse()));
    }
}
