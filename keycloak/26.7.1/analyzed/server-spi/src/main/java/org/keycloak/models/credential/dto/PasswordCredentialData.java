package org.keycloak.models.credential.dto;

import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 密码凭据公开数据 DTO：哈希迭代次数、算法 ID 及可选算法调参（JSON 序列化）。
 */
public class PasswordCredentialData {
    private final int hashIterations;
    private final String algorithm;

    private MultivaluedHashMap<String, String> additionalParameters;

    /**
     * 标准算法构造器（除哈希迭代外无额外调参）。
     * Creator for standard algorithms (no algorithm tuning beyond hash iterations)
     * @param hashIterations iterations
     * @param algorithm algorithm id
     */
    public PasswordCredentialData(int hashIterations, String algorithm) {
        this(hashIterations, algorithm, null);
    }

    /**
     * 自定义算法构造器（含额外调参，如 Bcrypt 内存参数）。
     * Creator for custom algorithms (algorithm with tuning parameters beyond simple has iterations)
     * @param hashIterations iterations
     * @param algorithm algorithm id
     * @param additionalParameters additional tuning parameters
     */
    @JsonCreator
    public PasswordCredentialData(@JsonProperty("hashIterations") int hashIterations, @JsonProperty("algorithm") String algorithm, @JsonProperty("algorithmData") Map<String, List<String>> additionalParameters) {
        this.hashIterations = hashIterations;
        this.algorithm = algorithm;
        this.additionalParameters = additionalParameters != null ?  new MultivaluedHashMap<>(additionalParameters) : null;
    }



    /** @return 哈希迭代次数 */
    public int getHashIterations() {
        return hashIterations;
    }

    /** @return 密码哈希算法 ID */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 返回算法特定设置映射（如 Bcrypt 内存参数）；应不可变使用。
     * Returns a map of algorithm-specific settings. These settings may include additional
     * parameters such as Bcrypt memory-tuning parameters. It should be used immutably.
     * @return algorithm data
     */
    public MultivaluedHashMap<String, String> getAdditionalParameters() {
        if (additionalParameters == null) {
            additionalParameters = new MultivaluedHashMap<>();
        }
        return additionalParameters;
    }
}
