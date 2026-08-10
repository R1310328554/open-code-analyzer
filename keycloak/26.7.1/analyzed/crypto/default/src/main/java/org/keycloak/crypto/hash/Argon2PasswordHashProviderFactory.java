package org.keycloak.crypto.hash;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.credential.hash.PasswordHashProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * Argon2 密码哈希提供器工厂，负责创建 {@link Argon2PasswordHashProvider} 并暴露可配置参数。
 * <p>
 * 在 FIPS 模式下不可用（{@link #isSupported} 返回 false）。
 */
public class Argon2PasswordHashProviderFactory implements PasswordHashProviderFactory, EnvironmentDependentProviderFactory {

    public static final String ID = "argon2";
    public static final String TYPE_KEY = "type";
    public static final String VERSION_KEY = "version";
    public static final String HASH_LENGTH_KEY = "hashLength";
    public static final String MEMORY_KEY = "memory";
    public static final String ITERATIONS_KEY = "iterations";
    public static final String PARALLELISM_KEY = "parallelism";
    public static final String CPU_CORES_KEY = "cpuCores";

    /**
     * Argon2 哈希为 CPU 密集型，并发哈希数不应超过机器核心数；否则在容器 CPU 限额下会触发节流并影响 Keycloak 其他任务。
     */
    private Semaphore cpuCoreSemaphore;

    private String version;
    private String type;
    private int hashLength;
    private int memory;
    private int iterations;
    private int parallelism;

    /** {@inheritDoc} 使用工厂配置创建 Argon2 密码哈希提供器。 */
    @Override
    public PasswordHashProvider create(KeycloakSession session) {
        return new Argon2PasswordHashProvider(version, type, hashLength, memory, iterations, parallelism, cpuCoreSemaphore);
    }

    /** {@inheritDoc} 从配置读取 Argon2 版本、类型、内存、迭代等参数并初始化 CPU 核心信号量。 */
    @Override
    public void init(Config.Scope config) {
        version = config.get(VERSION_KEY, Argon2Parameters.DEFAULT_VERSION);
        type = config.get(TYPE_KEY, Argon2Parameters.DEFAULT_TYPE);
        hashLength = config.getInt(HASH_LENGTH_KEY, Argon2Parameters.DEFAULT_HASH_LENGTH);
        memory = config.getInt(MEMORY_KEY, Argon2Parameters.DEFAULT_MEMORY);
        iterations = config.getInt(ITERATIONS_KEY, Argon2Parameters.DEFAULT_ITERATIONS);
        parallelism = config.getInt(PARALLELISM_KEY, Argon2Parameters.DEFAULT_PARALLELISM);
        cpuCoreSemaphore = new Semaphore(config.getInt(CPU_CORES_KEY, Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回提供器标识 {@value #ID}。 */
    @Override
    public String getId() {
        return ID;
    }

    /** {@inheritDoc} 返回 Argon2 相关配置项元数据。 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(VERSION_KEY)
                .type("string")
                .helpText("Version")
                .options(new LinkedList<>(Argon2Parameters.listVersions()))
                .defaultValue(Argon2Parameters.DEFAULT_VERSION)
                .add();

        builder.property()
                .name(TYPE_KEY)
                .type("string")
                .helpText("Type")
                .options(new LinkedList<>(Argon2Parameters.listTypes()))
                .defaultValue(Argon2Parameters.DEFAULT_TYPE)
                .add();

        builder.property()
                .name(HASH_LENGTH_KEY)
                .type("int")
                .helpText("Hash length")
                .defaultValue(Argon2Parameters.DEFAULT_HASH_LENGTH)
                .add();

        builder.property()
                .name(MEMORY_KEY)
                .type("int")
                .helpText("Memory size (KB)")
                .defaultValue(Argon2Parameters.DEFAULT_MEMORY)
                .add();

        builder.property()
                .name(ITERATIONS_KEY)
                .type("int")
                .helpText("Iterations")
                .defaultValue(Argon2Parameters.DEFAULT_ITERATIONS)
                .add();

        builder.property()
                .name(PARALLELISM_KEY)
                .type("int")
                .helpText("Parallelism")
                .defaultValue(Argon2Parameters.DEFAULT_PARALLELISM)
                .add();

        builder.property()
                .name(CPU_CORES_KEY)
                .type("int")
                .helpText("Maximum parallel CPU cores to use for hashing")
                .add();

        return builder.build();
    }

    /** {@inheritDoc} FIPS 特性启用时不支持 Argon2。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return !Profile.isFeatureEnabled(Profile.Feature.FIPS);
    }

    /** {@inheritDoc} 提供器排序优先级。 */
    @Override
    public int order() {
        return 300;
    }
}
