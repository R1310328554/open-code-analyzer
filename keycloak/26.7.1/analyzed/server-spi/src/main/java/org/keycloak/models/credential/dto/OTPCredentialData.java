package org.keycloak.models.credential.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OTP 凭据公开数据 DTO：子类型、位数、计数器、周期与算法等（JSON 序列化）。
 */
public class OTPCredentialData {
    private final String subType;
    private final int digits;
    private int counter;
    private final int period;
    private final String algorithm;

    private final String secretEncoding;

    /** Jackson 反序列化构造器。 */
    @JsonCreator
    public OTPCredentialData(@JsonProperty("subType") String subType,
                             @JsonProperty("digits") int digits,
                             @JsonProperty("counter") int counter,
                             @JsonProperty("period") int period,
                             @JsonProperty("algorithm") String algorithm,
                             @JsonProperty("secretEncoding") String secretEncoding) {
        this.subType = subType;
        this.digits = digits;
        this.counter = counter;
        this.period = period;
        this.algorithm = algorithm;
        this.secretEncoding = secretEncoding;
    }

    /** @return OTP 子类型（totp/hotp） */
    public String getSubType() {
        return subType;
    }

    /** @return OTP 位数 */
    public int getDigits() {
        return digits;
    }

    /** @return HOTP 计数器 */
    public int getCounter() {
        return counter;
    }

    /** @param counter HOTP 计数器 */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    /** @return TOTP 周期（秒） */
    public int getPeriod() {
        return period;
    }

    /** @return HMAC 算法 */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @return 密钥编码方式 */
    public String getSecretEncoding() {
        return secretEncoding;
    }
}
