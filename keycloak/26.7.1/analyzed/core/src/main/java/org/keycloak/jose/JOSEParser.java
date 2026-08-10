package org.keycloak.jose;

import java.io.IOException;

import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * JOSE 令牌解析器：根据头部 {@code enc} 字段区分 JWS 与 JWE 并构造对应对象。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class JOSEParser {

    /**
     * 解析编码后的 {@code jwt}，根据 JOSE 头部配置返回 {@link JWSInput} 或 {@link JWE}。
     *
     * @param jwt 编码后的 JWT 字符串
     * @return 解析后的 {@link JOSE} 实例
     */
    public static JOSE parse(String jwt) {
        String[] parts = jwt.split("\\.");

        if (parts.length == 0) {
            throw new RuntimeException("Could not infer header from JWT");
        }

        JsonNode header;

        try {
            header = JsonSerialization.readValue(Base64Url.decode(parts[0]), JsonNode.class);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to parse JWT header", cause);
        }

        // 头部含 enc 字段则为 JWE，否则按 JWS 处理
        if (header.has("enc")) {
            return new JWE(jwt);
        }

        try {
            return new JWSInput(jwt);
        } catch (JWSInputException cause) {
            throw new RuntimeException("Failed to build JWS", cause);
        }
    }
}
