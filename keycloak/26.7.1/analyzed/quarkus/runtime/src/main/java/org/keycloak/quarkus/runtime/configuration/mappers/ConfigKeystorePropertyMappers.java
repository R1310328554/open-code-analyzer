package org.keycloak.quarkus.runtime.configuration.mappers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.keycloak.config.ConfigKeystoreOptions;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;

import io.smallrye.config.ConfigSourceInterceptorContext;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * 配置密钥库（Config Keystore）相关 {@link PropertyMapper} 分组：
 * 将 Keycloak 配置密钥库路径/密码/类型映射到 SmallRye Config Keystore 源。
 */
public final class ConfigKeystorePropertyMappers implements PropertyMapperGrouping {
    /** SmallRye 默认密钥库路径属性名。 */
    private static final String SMALLRYE_KEYSTORE_PATH = "smallrye.config.source.keystore.kc-default.path";
    /** SmallRye 默认密钥库密码属性名。 */
    private static final String SMALLRYE_KEYSTORE_PASSWORD = "smallrye.config.source.keystore.kc-default.password";

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(ConfigKeystoreOptions.CONFIG_KEYSTORE)
                        .to(SMALLRYE_KEYSTORE_PATH)
                        .transformer(ConfigKeystorePropertyMappers::validatePath)
                        .paramLabel("config-keystore")
                        .build(),
                fromOption(ConfigKeystoreOptions.CONFIG_KEYSTORE_PASSWORD)
                        .to(SMALLRYE_KEYSTORE_PASSWORD)
                        .transformer(ConfigKeystorePropertyMappers::validatePassword)
                        .paramLabel("config-keystore-password")
                        .isMasked(true)
                        .build(),
                fromOption(ConfigKeystoreOptions.CONFIG_KEYSTORE_TYPE)
                        .to("smallrye.config.source.keystore.kc-default.type")
                        .paramLabel("config-keystore-type")
                        .build()
        );
    }

    /**
     * 校验密钥库路径：必须与密码成对配置，且文件必须存在。
     *
     * @param option 用户配置的路径
     * @param context 配置拦截上下文
     * @return 规范化后的 file URI 字符串，未配置时返回 null
     */
    private static String validatePath(String option, ConfigSourceInterceptorContext context) {
        if (option == null) {
            return null;
        }
        boolean isPasswordDefined = context.proceed(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + ConfigKeystoreOptions.CONFIG_KEYSTORE_PASSWORD.getKey()) != null;

        if (!isPasswordDefined) {
            throw new IllegalArgumentException("config-keystore-password must be specified");
        }

        final Path realPath = Path.of(option).toAbsolutePath().normalize();
        if (!Files.exists(realPath)) {
            throw new IllegalArgumentException("config-keystore path does not exist: " + realPath);
        }

        return realPath.toUri().toString();
    }

    /**
     * 校验密钥库密码：必须与路径成对配置。
     *
     * @param option 用户配置的密码
     * @param context 配置拦截上下文
     * @return 原密码值，未配置时返回 null
     */
    private static String validatePassword(String option, ConfigSourceInterceptorContext context) {
        if (option == null) {
            return null;
        }
        boolean isPathDefined = context.proceed(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + ConfigKeystoreOptions.CONFIG_KEYSTORE.getKey()) != null;

        if (!isPathDefined) {
            throw new IllegalArgumentException("config-keystore must be specified");
        }

        return option;
    }

}
