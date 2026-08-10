package org.keycloak.models.credential.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OTP 密钥数据 DTO：存储编码后的 OTP 共享密钥（JSON 序列化）。
 */
public class OTPSecretData {
    private final String value;

    /** Jackson 反序列化构造器。
     * @param value 编码后的 OTP 密钥 */
    @JsonCreator
    public OTPSecretData(@JsonProperty("value") String value) {
        this.value = value;
    }

    /** @return 编码后的 OTP 密钥 */
    public String getValue() {
        return value;
    }
}
