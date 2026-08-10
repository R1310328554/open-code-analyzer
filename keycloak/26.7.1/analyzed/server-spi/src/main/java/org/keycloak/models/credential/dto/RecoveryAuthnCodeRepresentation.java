package org.keycloak.models.credential.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 单个恢复认证码表示：序号与 Base64 编码的哈希值（JSON 序列化，null 字段省略）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecoveryAuthnCodeRepresentation {

    private final int number;
    private final String encodedHashedValue;

    /** Jackson 反序列化构造器。
     * @param number 恢复码序号
     * @param encodedHashedValue Base64 编码的哈希值 */
    @JsonCreator
    public RecoveryAuthnCodeRepresentation(@JsonProperty("number") int number,
            @JsonProperty("encodedHashedValue") String encodedHashedValue) {
        this.number = number;
        this.encodedHashedValue = encodedHashedValue;
    }

    /** @return 恢复码序号 */
    public int getNumber() {
        return this.number;
    }

    /** @return Base64 编码的哈希值 */
    public String getEncodedHashedValue() {
        return this.encodedHashedValue;
    }

}
