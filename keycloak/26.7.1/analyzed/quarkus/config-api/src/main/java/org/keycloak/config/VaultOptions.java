package org.keycloak.config;

import java.nio.file.Path;

/**
 * 密钥库（Vault）提供者及文件/Keystore 后端相关配置选项。
 */
public class VaultOptions {

    /** Vault 后端类型枚举。 */
    public enum VaultType {

        /** 基于文件目录的 Vault 提供者。 */
        file("file"),
        /** 基于 Keystore 文件的 Vault 提供者。 */
        keystore("keystore");

        /** Quarkus 扩展中的提供者标识。 */
        private final String provider;

        VaultType(String provider) {
            this.provider = provider;
        }

        /** @return 提供者标识字符串 */
        public String getProvider() {
            return provider;
        }
    }

    /** 配置选项：vault，启用的 Vault 提供者类型。 */
    public static final Option<VaultOptions.VaultType> VAULT = new OptionBuilder<>("vault", VaultType.class)
            .category(OptionCategory.VAULT)
            .description("Enables a vault provider.")
            .buildTime(true)
            .build();

    /** 配置选项：vault-dir，从目录读取密钥的文件 Vault 根目录。 */
    public static final Option<Path>  VAULT_DIR = new OptionBuilder<>("vault-dir", Path.class)
            .category(OptionCategory.VAULT)
            .description("If set, secrets can be obtained by reading the content of files within the given directory.")
            .build();

    /** 配置选项：vault-pass，Keystore Vault 的密码。 */
    public static final Option<String>  VAULT_PASS = new OptionBuilder<>("vault-pass", String.class)
            .category(OptionCategory.VAULT)
            .description("Password for the vault keystore.")
            .build();

    /** 配置选项：vault-file，Keystore 文件路径。 */
    public static final Option<Path>  VAULT_FILE = new OptionBuilder<>("vault-file", Path.class)
            .category(OptionCategory.VAULT)
            .description("Path to the keystore file.")
            .build();

    /** 配置选项：vault-type，Keystore 文件格式类型。 */
    public static final Option<String>  VAULT_TYPE = new OptionBuilder<>("vault-type", String.class)
            .category(OptionCategory.VAULT)
            .description("Specifies the type of the keystore file.")
            .defaultValue("PKCS12")
            .build();
}
