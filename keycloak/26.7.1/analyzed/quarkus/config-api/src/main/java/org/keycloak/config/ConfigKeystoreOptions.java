package org.keycloak.config;


/**
 * 配置密钥库（KeyStore）相关选项，用于从 KeyStore 加载配置源。
 */
public class ConfigKeystoreOptions {

    /** 配置选项：config keystore */
    public static final Option<String> CONFIG_KEYSTORE = new OptionBuilder<>("config-keystore", String.class)
            .category(OptionCategory.CONFIG)
            .description("Specifies a path to the KeyStore Configuration Source.")
            .build();

    /** 配置选项：config keystore password */
    public static final Option<String> CONFIG_KEYSTORE_PASSWORD = new OptionBuilder<>("config-keystore-password", String.class)
            .category(OptionCategory.CONFIG)
            .description("Specifies a password to the KeyStore Configuration Source.")
            .build();

    /** 配置选项：config keystore type */
    public static final Option<String> CONFIG_KEYSTORE_TYPE = new OptionBuilder<>("config-keystore-type", String.class)
            .category(OptionCategory.CONFIG)
            .description("Specifies a type of the KeyStore Configuration Source.")
            .defaultValue("PKCS12")
            .build();

}
