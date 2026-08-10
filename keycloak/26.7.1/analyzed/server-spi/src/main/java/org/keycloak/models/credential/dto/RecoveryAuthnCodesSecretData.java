package org.keycloak.models.credential.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 恢复认证码密钥数据 DTO：存储已哈希的恢复码列表（JSON 序列化）。
 */
public class RecoveryAuthnCodesSecretData {

    private final List<RecoveryAuthnCodeRepresentation> codes;

    /** Jackson 反序列化构造器。
     * @param codes 恢复码表示列表 */
    @JsonCreator
    public RecoveryAuthnCodesSecretData(@JsonProperty("codes") List<RecoveryAuthnCodeRepresentation> codes) {
        this.codes = codes;
    }

    /** @return 恢复码列表 */
    public List<RecoveryAuthnCodeRepresentation> getCodes() {
        return this.codes;
    }

    /** 消费并移除列表中第一个（下一个）恢复码。 */
    public void removeNextBackupCode() {
        this.codes.remove(0);
    }

}
