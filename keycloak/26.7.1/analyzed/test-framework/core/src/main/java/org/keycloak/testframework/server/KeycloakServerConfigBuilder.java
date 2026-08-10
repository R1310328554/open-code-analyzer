package org.keycloak.testframework.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.Profile;
import org.keycloak.testframework.infinispan.CacheType;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * 构建托管 Keycloak 服务器启动所需的 CLI 参数、特性开关与 provider 依赖。
 * <p>
 * 通过 {@link #startDev()} 等工厂方法创建实例，并以链式 API 累积配置。
 */
public class KeycloakServerConfigBuilder {

    /** SPI 选项在 CLI 中的键名格式：{@code spi-<spi>--<provider>--<key>}。 */
    private static final String SPI_OPTION = "spi-%s--%s--%s";

    private final String command;
    private final Map<String, String> options = new HashMap<>();
    private final Set<String> features = new HashSet<>();
    private final Set<String> featuresDisabled = new HashSet<>();
    private final LogBuilder log = new LogBuilder();
    private final Set<KeycloakDependency> dependencies = new HashSet<>();
    private CacheType cacheType = CacheType.LOCAL;
    private boolean externalInfinispan = false;
    private String shutdownDelay = "0s";
    private String shutdownTimeout = "1s";

    /** 使用指定 Quarkus 子命令（如 {@code start-dev}）初始化构建器。 */
    private KeycloakServerConfigBuilder(String command) {
        this.command = command;
    }

    /**
     * 创建以 {@code start-dev} 模式启动的开发服务器配置构建器。
     *
     * @return 开发模式配置构建器
     */
    public static KeycloakServerConfigBuilder startDev() {
        return new KeycloakServerConfigBuilder("start-dev");
    }

    /**
     * 设置引导初始管理员配置时使用的客户端 ID 与密钥。
     *
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder bootstrapAdminClient(String clientId, String clientSecret) {
        return option("bootstrap-admin-client-id", clientId)
                .option("bootstrap-admin-client-secret", clientSecret);
    }

    /**
     * 设置引导初始管理员配置时使用的用户名与密码。
     *
     * @param username 用户名
     * @param password 密码
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder bootstrapAdminUser(String username, String password) {
        return option("bootstrap-admin-username", username)
                .option("bootstrap-admin-password", password);
    }

    /**
     * 配置使用本地缓存还是集群缓存；本地缓存可缩短测试启动时间。
     *
     * @param cacheType 缓存类型
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder cache(CacheType cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    /**
     * 是否连接外部托管的 Infinispan 服务器；启用时自动切换为集群缓存模式。
     *
     * @param enabled 是否启用外部 Infinispan
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder externalInfinispanEnabled(boolean enabled) {
        if (enabled) {
            this.externalInfinispan = true;
            cache(CacheType.ISPN);
        } else {
            this.externalInfinispan = false;
            cache(CacheType.LOCAL);
        }
        return this;
    }

    /** @return 是否已启用外部 Infinispan 连接 */
    public boolean isExternalInfinispanEnabled() {
        return this.externalInfinispan;
    }

    /**
     * 设置优雅关闭前的等待时长。
     *
     * @param shutdownDelay 关闭延迟（如 {@code 0s}）
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder shutdownDelay(String shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
        return this;
    }

    /**
     * 设置关闭流程的超时时长。
     *
     * @param shutdownTimeout 关闭超时（如 {@code 1s}）
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder shutdownTimeout(String shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    /**
     * 获取日志配置子构建器。
     *
     * @return 日志构建器
     */
    public LogBuilder log() {
        return log;
    }

    /**
     * 启用指定特性，通常用于打开默认关闭的预览或实验性功能。
     *
     * @param features 要启用的特性
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder features(Profile.Feature... features) {
        this.features.addAll(toFeatureStrings(features));
        return this;
    }

    /**
     * 禁用指定特性，通常用于关闭默认开启但测试不需要的功能。
     *
     * @param features 要禁用的特性
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder featuresDisabled(Profile.Feature... features) {
        this.featuresDisabled.addAll(Arrays.stream(features)
                .map(Profile.Feature::getUnversionedKey)
                .collect(Collectors.toSet()));
        return this;
    }

    /**
     * 批量设置 CLI 选项。
     *
     * @param options 键值对形式的选项
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder options(Map<String, String> options) {
        this.options.putAll(options);
        return this;
    }

    /**
     * 设置单个 CLI 选项。
     *
     * @param key 选项键
     * @param value 选项值
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder option(String key, String value) {
        options.put(key, value);
        return this;
    }

    /**
     * 设置 SPI provider 的配置项。
     *
     * @param spi SPI 名称
     * @param provider provider 名称
     * @param key 配置项键
     * @param value 配置项值
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder spiOption(String spi, String provider, String key, String value) {
        options.put(String.format(SPI_OPTION, spi, provider, key), value);
        return this;
    }

    /**
     * 通过 Maven 坐标声明要部署到服务器的 provider 依赖；版本由项目 POM 解析。
     *
     * @param groupId Maven groupId
     * @param artifactId Maven artifactId
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder dependency(String groupId, String artifactId) {
        return dependency(groupId, artifactId, false, false);
    }

    /**
     * 声明 provider 依赖，并指定是否支持热部署。
     *
     * @param groupId Maven groupId
     * @param artifactId Maven artifactId
     * @param hotDeployable 是否允许从编译输出目录热部署
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder dependency(String groupId, String artifactId, boolean hotDeployable) {
        return dependency(groupId, artifactId, hotDeployable, false);
    }

    /** 内部方法：注册 provider 依赖，可选热部署或当前模块。 */
    private KeycloakServerConfigBuilder dependency(String groupId, String artifactId, boolean hotDeployable, boolean dependencyCurrentProject) {
        dependencies.add(
                new KeycloakDependency.Builder()
                        .setGroupId(groupId)
                        .setArtifactId(artifactId)
                        .hotDeployable(hotDeployable)
                        .dependencyCurrentProject(dependencyCurrentProject)
                        .build()
        );
        return this;
    }

    /**
     * 将当前 Maven 模块的编译输出作为 provider 部署到服务器。
     *
     * @return 当前构建器
     */
    public KeycloakServerConfigBuilder dependencyCurrentProject() {
        return dependency("", "", false, true);
    }

    /** 日志相关 CLI 选项的链式子构建器。 */
    public class LogBuilder {

        private Boolean color;
        private String format;
        private String rootLevel;
        private final Map<String, String> categoryLevels = new HashMap<>();
        private final Map<String, String> handlerLevels = new HashMap<>();
        private final Set<String> handlers = new HashSet<>();
        private String syslogEndpoint;

        /**
         * 指定启用的日志 handler（如 CONSOLE、FILE）。
         *
         * @param handlers 要启用的 handler
         * @return 当前日志构建器
         */
        public LogBuilder handlers(LogHandlers... handlers) {
            this.handlers.addAll(Arrays.stream(handlers).map(l -> l.name().toLowerCase()).collect(Collectors.toSet()));
            return this;
        }

        /**
         * 为指定 handler 设置日志级别。
         *
         * @param handler 目标 handler
         * @param logLevel 日志级别
         * @return 当前日志构建器
         */
        public LogBuilder handlerLevel(LogHandlers handler, String logLevel) {
            handlerLevels.put(handler.name().toLowerCase(), logLevel);
            return this;
        }

        /**
         * 为指定日志分类设置级别。
         *
         * @param category 日志分类名
         * @param logLevel 日志级别
         * @return 当前日志构建器
         */
        public LogBuilder categoryLevel(String category, String logLevel) {
            categoryLevels.put(category, logLevel);
            return this;
        }

        /**
         * 设置 syslog handler 的上报端点。
         *
         * @param syslogEndpoint syslog 端点地址
         * @return 当前日志构建器
         */
        public LogBuilder syslogEndpoint(String syslogEndpoint) {
            this.syslogEndpoint = syslogEndpoint;
            return this;
        }

        /**
         * 从 MicroProfile 配置（环境变量或测试配置源）导入日志默认值。
         *
         * @param config SmallRye 配置实例
         * @return 当前日志构建器
         */
        public LogBuilder fromConfig(SmallRyeConfig config) {
            List<ConfigSource> sources = new LinkedList<>();
            for (ConfigSource source : config.getConfigSources()) {
                if (source.getName().startsWith("EnvConfigSource") || source.getName().equals("KeycloakTestConfig")) {
                    sources.add(source);
                }
            }

            for (ConfigSource source : sources) {
                for (String p : source.getPropertyNames()) {
                    if (p.equals("kc.test.log.console.format") && format == null) {
                        format = source.getValue(p);
                    }
                    if (p.equals("kc.test.console.color") && color == null) {
                        color = Boolean.parseBoolean(source.getValue(p));
                    } else if (p.equals("kc.test.log.level") && rootLevel == null) {
                        rootLevel = source.getValue(p);
                    } else if (p.startsWith("kc.test.log.category.")) {
                        String category = p.split("\"")[1];
                        String level = source.getValue(p);

                        if (!categoryLevels.containsKey(category)) {
                            categoryLevels.put(category, level);
                        }
                    }
                }
            }
            return this;
        }

        /** 将累积的日志选项写入父构建器的 CLI 参数。 */
        private void build() {
            if (!handlers.isEmpty()) {
                option("log", String.join(",", handlers));
            }

            if (!handlerLevels.isEmpty()) {
                handlerLevels.forEach((key, value) -> option("log-" + key + "-level", value));
            }

            if (syslogEndpoint != null) {
                option("log-syslog-endpoint", syslogEndpoint);
            }

            if (format != null) {
                option("log-console-format", format);
            }

            if (rootLevel != null) {
                option("log-level", rootLevel);
            }

            for (Map.Entry<String, String> e : categoryLevels.entrySet()) {
                option("log-level-" + e.getKey(), e.getValue());
            }

            if (color != null) {
                option("log-console-color", color.toString());
            }
        }
    }

    /**
     * 将当前配置转换为 Keycloak 启动命令行参数列表。
     *
     * @return 含子命令与 {@code --key=value} 选项的参数列表
     */
    List<String> toArgs() {
        // 缓存配置：可选 local 或 ispn
        option("cache", cacheType.name().toLowerCase());

        // 关闭选项：默认值针对测试速度优化
        option("shutdown-delay", shutdownDelay);
        option("shutdown-timeout", shutdownTimeout);

        log.build();

        List<String> args = new LinkedList<>();
        args.add(command);
        for (Map.Entry<String, String> e : options.entrySet()) {
            args.add("--" + e.getKey() + "=" + e.getValue());
        }
        if (!features.isEmpty()) {
            args.add("--features=" + String.join(",", features));
        }
        if (!featuresDisabled.isEmpty()) {
            args.add("--features-disabled=" + String.join(",", featuresDisabled));
        }

        return args;
    }

    /** @return 已声明的 provider 依赖集合 */
    Set<KeycloakDependency> toDependencies() {
        return dependencies;
    }

    /** 将 {@link Profile.Feature} 枚举转换为 CLI 特性名字符串集合。 */
    private Set<String> toFeatureStrings(Profile.Feature... features) {
        return Arrays.stream(features).map(f -> {
            if (f.getVersion() > 1 || Profile.getFeatureVersions(f.getKey()).size() > 1) {
                return f.getVersionedKey();
            }
            return f.getUnversionedKey();
        }).collect(Collectors.toSet());
    }

    /** Keycloak 支持的日志输出 handler 类型。 */
    public enum LogHandlers {
        CONSOLE,
        FILE,
        SYSLOG
    }

}
