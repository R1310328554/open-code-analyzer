package org.keycloak.models.credential.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 恢复认证码凭据公开数据 DTO：哈希参数、总数与剩余数量（JSON 序列化）。
 */
public class RecoveryAuthnCodesCredentialData {

    private final Integer hashIterations;
    private final String algorithm;

    private int totalCodes;
    private int remainingCodes;

    /** Jackson 反序列化构造器。 */
    @JsonCreator
    public RecoveryAuthnCodesCredentialData(@JsonProperty("hashIterations") Integer hashIterations,
            @JsonProperty("algorithm") String algorithm, @JsonProperty("remaining") int remainingCodes,
                                            @JsonProperty("total") int totalCodes) {
        this.hashIterations = hashIterations;
        this.algorithm = algorithm;
        this.remainingCodes = remainingCodes;
        this.totalCodes = totalCodes;
    }

    /** @return 哈希迭代次数 */
    public Integer getHashIterations() {
        return hashIterations;
    }

    /** @return 哈希算法 ID */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @return 剩余恢复码数量 */
    public int getRemainingCodes() {
        return remainingCodes;
    }

    /** @param remainingCodes 剩余恢复码数量 */
    public void setRemainingCodes(int remainingCodes) {
        this.remainingCodes = remainingCodes;
    }

    /** @return 恢复码总数 */
    public int getTotalCodes() {
        return totalCodes;
    }

    /** @param totalCodes 恢复码总数 */
    public void setTotalCodes(int totalCodes) {
        this.totalCodes = totalCodes;
    }


}
