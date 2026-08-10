package org.keycloak.models.credential.dto;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * 密码凭据密钥数据 DTO：存储哈希值、盐值及算法额外参数（JSON 序列化）。
 */
public class PasswordSecretData {

    /** 日志记录器。 */
    public static final Logger logger = Logger.getLogger(PasswordSecretData.class);

    private final String value;
    private final byte[] salt;

    private MultivaluedHashMap<String, String> additionalParameters;

    /**
     * 支持自定义密钥数据的构造器（如多盐值等）。
     * Creator with the option to provide customized secret data (multiple salt values, chiefly)
     * @param value hash value
     * @param salt salt value
     * @param additionalParameters additional data required by the algorithm
     * @throws IOException invalid base64 in salt value
     */
    @JsonCreator
    public PasswordSecretData(@JsonProperty("value") String value, @JsonProperty("salt") String salt, @JsonProperty("algorithmData") Map<String, List<String>> additionalParameters) throws IOException {
        this.additionalParameters = additionalParameters != null ? new MultivaluedHashMap<>( additionalParameters) : null;

        if (salt == null || "__SALT__".equals(salt)) {
            this.value = value;
            this.salt = null;
        }
        else {
            this.value = value;
            this.salt = Base64.getMimeDecoder().decode(salt);
        }
    }

    /**
     * 默认构造器（仅包含哈希值与单一盐值）。
     * Default creator (Secret consists only of a value and a single salt)
     * @param value hash value
     * @param salt salt
     */
    public PasswordSecretData(String value, byte[] salt) {
        this.value = value;
        this.salt = salt;
        this.additionalParameters = null;
    }

    /** @return 编码后的密码哈希值 */
    public String getValue() {
        return value;
    }

    /** @return 盐值字节数组 */
    public byte[] getSalt() {
        return salt;
    }

    /** @return 算法额外参数映射 */
    public MultivaluedHashMap<String, String> getAdditionalParameters() {
        if (additionalParameters == null) {
            additionalParameters = new MultivaluedHashMap<>();
        }
        return additionalParameters;
    }
}
