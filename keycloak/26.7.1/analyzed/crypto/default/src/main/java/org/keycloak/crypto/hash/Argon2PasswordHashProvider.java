package org.keycloak.crypto.hash;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.Salt;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.dto.PasswordCredentialData;
import org.keycloak.models.credential.dto.PasswordSecretData;
import org.keycloak.tracing.TracingProviderUtil;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.jboss.logging.Logger;

import static org.keycloak.crypto.hash.Argon2PasswordHashProviderFactory.MEMORY_KEY;
import static org.keycloak.crypto.hash.Argon2PasswordHashProviderFactory.PARALLELISM_KEY;
import static org.keycloak.crypto.hash.Argon2PasswordHashProviderFactory.TYPE_KEY;
import static org.keycloak.crypto.hash.Argon2PasswordHashProviderFactory.VERSION_KEY;

/**
 * Argon2 密码哈希 {@link PasswordHashProvider} 实现，使用 BouncyCastle 生成与校验凭据。
 */
public class Argon2PasswordHashProvider implements PasswordHashProvider {

    private static final Logger logger = Logger.getLogger(Argon2PasswordHashProvider.class);
    /** Argon2 版本（如 1.3）。 */
    private final String version;
    /** Argon2 变体（id/d/i）。 */
    private final String type;
    /** 哈希输出长度（字节）。 */
    private final int hashLength;
    /** 内存参数（KB）。 */
    private final int memory;
    /** 迭代次数。 */
    private final int iterations;
    /** 并行度。 */
    private final int parallelism;
    /** CPU 核心信号量，限制并发哈希任务数。 */
    private final Semaphore cpuCoreSemaphore;

    /**
     * @param version Argon2 版本
     * @param type 变体类型
     * @param hashLength 输出哈希长度
     * @param memory 内存（KB）
     * @param iterations 迭代次数
     * @param parallelism 并行度
     * @param cpuCoreSemaphore 并发控制信号量
     */
    public Argon2PasswordHashProvider(String version, String type, int hashLength, int memory, int iterations, int parallelism, Semaphore cpuCoreSemaphore) {
        this.version = version;
        this.type = type;
        this.hashLength = hashLength;
        this.memory = memory;
        this.iterations = iterations;
        this.parallelism = parallelism;
        this.cpuCoreSemaphore = cpuCoreSemaphore;
    }

    /** {@inheritDoc} 校验存储凭据的 Argon2 参数是否与当前配置一致。 */
    @Override
    public boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential) {
        PasswordCredentialData data = credential.getPasswordCredentialData();

        return iterations == data.getHashIterations() &&
                checkCredData(TYPE_KEY, type, data) &&
                checkCredData(VERSION_KEY, version, data) &&
                checkCredData(Argon2PasswordHashProviderFactory.HASH_LENGTH_KEY, hashLength, data) &&
                checkCredData(MEMORY_KEY, memory, data) &&
                checkCredData(PARALLELISM_KEY, parallelism, data);
    }

    /**
     * 密码策略中的迭代次数目前被忽略，原因：1）默认 21 万次对 Argon2 过高；
     * 2）Argon2 应同时配置内存，而密码策略暂不支持内存参数。
     */
    @Override
    public PasswordCredentialModel encodedCredential(String rawPassword, int iterations) {
        if (iterations == -1) {
            iterations = this.iterations;
        } else if (iterations > 100) {
            logger.warn("Iterations for Argon should be less than 100, using default");
            iterations = this.iterations;
        }

        byte[] salt = Salt.generateSalt();
        String encoded = encode(rawPassword, salt, version, type, hashLength, parallelism, memory, iterations);

        Map<String, List<String>> additionalParameters = new HashMap<>();
        additionalParameters.put(VERSION_KEY, Collections.singletonList(version));
        additionalParameters.put(TYPE_KEY, Collections.singletonList(type));
        additionalParameters.put(Argon2PasswordHashProviderFactory.HASH_LENGTH_KEY, Collections.singletonList(Integer.toString(hashLength)));
        additionalParameters.put(MEMORY_KEY, Collections.singletonList(Integer.toString(memory)));
        additionalParameters.put(PARALLELISM_KEY, Collections.singletonList(Integer.toString(parallelism)));

        return PasswordCredentialModel.createFromValues(Argon2PasswordHashProviderFactory.ID, salt, iterations, additionalParameters, encoded);
    }

    /** {@inheritDoc} 重新计算 Argon2 哈希并与存储值比较。 */
    @Override
    public boolean verify(String rawPassword, PasswordCredentialModel credential) {
        PasswordCredentialData data = credential.getPasswordCredentialData();
        MultivaluedHashMap<String, String> parameters = data.getAdditionalParameters();
        PasswordSecretData secretData = credential.getPasswordSecretData();

        String version = parameters.getFirst(VERSION_KEY);
        String type = parameters.getFirst(TYPE_KEY);
        int hashLength = Integer.parseInt(parameters.getFirst(Argon2PasswordHashProviderFactory.HASH_LENGTH_KEY));
        int parallelism = Integer.parseInt(parameters.getFirst(PARALLELISM_KEY));
        int memory = Integer.parseInt(parameters.getFirst(MEMORY_KEY));
        int iterations = data.getHashIterations();

        String encoded = encode(rawPassword, secretData.getSalt(), version, type, hashLength, parallelism, memory, iterations);
        return encoded.equals(secretData.getValue());
    }

    /** {@inheritDoc} 返回 Argon2 强度描述字符串。 */
    @Override
    public String credentialHashingStrength(PasswordCredentialModel credential) {
        MultivaluedHashMap<String, String> parameters = credential.getPasswordCredentialData().getAdditionalParameters();
        return String.format("Argon2%s-%s[m=%s,t=%d,p=%s]", parameters.getFirst(TYPE_KEY), parameters.getFirst(VERSION_KEY), parameters.getFirst(MEMORY_KEY), credential.getPasswordCredentialData().getHashIterations(), parameters.getFirst(PARALLELISM_KEY));
    }

    /** 使用 Argon2 对明文密码编码为 Base64 哈希。 */
    private String encode(String rawPassword, byte[] salt, String version, String type, int hashLength, int parallelism, int memory, int iterations) {
        var tracing = TracingProviderUtil.getTracingProvider();
        return tracing.trace(Argon2PasswordHashProvider.class, "encode", span -> {
            try {
                cpuCoreSemaphore.acquire();
                try {
                    org.bouncycastle.crypto.params.Argon2Parameters parameters = new org.bouncycastle.crypto.params.Argon2Parameters.Builder(Argon2Parameters.getTypeValue(type))
                            .withVersion(Argon2Parameters.getVersionValue(version))
                            .withSalt(salt)
                            .withParallelism(parallelism)
                            .withMemoryAsKB(memory)
                            .withIterations(iterations).build();

                    Argon2BytesGenerator generator = new Argon2BytesGenerator();
                    generator.init(parameters);

                    byte[] result = new byte[hashLength];
                    generator.generateBytes(rawPassword.toCharArray(), result);
                    return Base64.getEncoder().encodeToString(result);
                } finally {
                    cpuCoreSemaphore.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    /** 比较凭据附加参数中的整型值。 */
    private boolean checkCredData(String key, int expectedValue, PasswordCredentialData data) {
        String s = data.getAdditionalParameters().getFirst(key);
        Integer v = s != null ? Integer.parseInt(s) : null;
        return v != null && expectedValue == v;
    }

    /** 比较凭据附加参数中的字符串值。 */
    private boolean checkCredData(String key, String expectedValue, PasswordCredentialData data) {
        String s = data.getAdditionalParameters().getFirst(key);
        return expectedValue.equals(s);
    }

    /** {@inheritDoc} 无资源需释放。 */
    @Override
    public void close() {
    }
}
