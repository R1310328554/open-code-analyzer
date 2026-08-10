package org.keycloak.testframework.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.keycloak.testframework.injection.ValueTypeAlias;

import io.quarkus.runtime.configuration.CharsetConverter;
import io.quarkus.runtime.configuration.InetSocketAddressConverter;
import io.quarkus.runtime.configuration.MemorySizeConverter;
import io.smallrye.config.EnvConfigSource;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.common.utils.ConfigSourceUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * Keycloak 测试框架的 MicroProfile 配置入口，基于 SmallRye Config 加载
 * 环境变量、{@code keycloak-test.properties}、{@code .env.test} 与套件级覆盖。
 */
public class Config {

    private static final SmallRyeConfig config = initConfig();
    private static ValueTypeAlias valueTypeAlias = new ValueTypeAlias();

    /** @param valueType 值类型 @return 当前选中的 Supplier 别名（{@code kc.test.<alias>}） */
    public static String getSelectedSupplier(Class<?> valueType) {
        return config.getOptionalValue("kc.test." + valueTypeAlias.getAlias(valueType), String.class).orElse(null);
    }

    /** @return 逗号分隔的 included Supplier 别名列表配置 */
    public static String getIncludedSuppliers(Class<?> valueType) {
        return config.getOptionalValue("kc.test." + valueTypeAlias.getAlias(valueType) + ".suppliers.included", String.class).orElse(null);
    }

    /** @return 逗号分隔的 excluded Supplier 别名列表配置 */
    public static String getExcludedSuppliers(Class<?> valueType) {
        return config.getOptionalValue("kc.test." + valueTypeAlias.getAlias(valueType) + ".suppliers.excluded", String.class).orElse(null);
    }

    /** @return Supplier 附加配置文件路径（{@code kc.test.<alias>.config}） */
    public static String getSupplierConfig(Class<?> valueType) {
        return config.getOptionalValue("kc.test." + valueTypeAlias.getAlias(valueType) + ".config", String.class).orElse(null);
    }

    /**
     * 读取某值类型下的命名配置项。
     * @param valueType 值类型
     * @param name 配置短名
     * @param defaultValue 未配置时的默认值
     * @param type 目标类型
     * @return 配置值，无值且无默认值时返回 null
     */
    public static <T> T getValueTypeConfig(Class<?> valueType, String name, String defaultValue, Class<T> type) {
        name = getValueTypeFQN(valueType, name);
        Optional<T> optionalValue = config.getOptionalValue(name, type);
        if (optionalValue.isPresent()) {
            return optionalValue.get();
        } else if (defaultValue != null && !defaultValue.equals(ConfigProperty.UNCONFIGURED_VALUE)) {
            return config.getConverter(type).orElseThrow(() -> new RuntimeException("Converter for " + type + " not found")).convert(defaultValue);
        } else {
            return null;
        }
    }

    /** @return {@code kc.test.<alias>.<name>} 形式的完整配置键 */
    public static String getValueTypeFQN(Class<?> valueType, String name) {
        return "kc.test." + valueTypeAlias.getAlias(valueType) + "." + name;
    }

    /** 读取全局配置项，不存在时返回 defaultValue。 */
    public static <T> T get(String name, T defaultValue, Class<T> clazz) {
        return config.getOptionalValue(name, clazz).orElse(defaultValue);
    }

    /** @return 底层 SmallRye 配置实例 */
    public static SmallRyeConfig getConfig() {
        return config;
    }

    /** @return Bootstrap Admin 客户端 ID */
    public static String getAdminClientId() {
        return "temp-admin";
    }

    /** @return Bootstrap Admin 客户端密钥 */
    public static String getAdminClientSecret() {
        return "mysecret";
    }

    /** @return master realm 管理员用户名 */
    public static String getAdminUsername() {
        return "admin";
    }

    /** @return master realm 管理员密码 */
    public static String getAdminPassword() {
        return "admin";
    }

    /** 构建 SmallRye 配置：默认源、拦截器、套件源与测试属性文件。 */
    public static SmallRyeConfig initConfig() {
        SmallRyeConfigBuilder configBuilder = new SmallRyeConfigBuilder()
                .addDefaultSources()
                .addDefaultInterceptors()
                .withConverters(new Converter[]{ new CharsetConverter(), new MemorySizeConverter(), new InetSocketAddressConverter() })
                .withInterceptors(new LogConfigInterceptor())
                .withSources(new SuiteConfigSource());

        ConfigSource testEnvConfigSource = initTestEnvConfigSource();
        if (testEnvConfigSource != null) {
            configBuilder.withSources(testEnvConfigSource);
        }

        ConfigSource testConfigSource = initTestConfigSource();
        if (testConfigSource != null) {
            configBuilder.withSources(testConfigSource);
        }

        return configBuilder.build();
    }

    /** 注册扩展提供的值类型别名解析器。 */
    public static void registerValueTypeAlias(ValueTypeAlias valueTypeAlias) {
        Config.valueTypeAlias = valueTypeAlias;
    }

    /** 自当前目录向上查找 {@code .env.test} 并作为 EnvConfigSource 加载。 */
    private static ConfigSource initTestEnvConfigSource() {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        while (Files.isDirectory(currentPath)) {
            Path envTestPath = currentPath.resolve(".env.test");
            if (Files.isRegularFile(envTestPath)) {
                try {
                    return new EnvConfigSource(ConfigSourceUtil.urlToMap(envTestPath.toUri().toURL()), 296);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            currentPath = currentPath.getParent();
            if (!Files.isRegularFile(currentPath.resolve("pom.xml"))) {
                break;
            }
        }
        return null;
    }

    /** 从系统属性/环境变量或 classpath 的 {@code keycloak-test.properties} 加载属性源。 */
    private static ConfigSource initTestConfigSource() {
        try {
            URL testConfig;
            String testConfigFile = System.getProperty("kc.test.config", System.getenv("KC_TEST_CONFIG"));
            if (testConfigFile != null) {
                testConfig = new File(testConfigFile).toURI().toURL();
            } else {
                testConfig = Thread.currentThread().getContextClassLoader().getResource("keycloak-test.properties");
            }
            return testConfig != null ? new PropertiesConfigSource(testConfig, 280) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
