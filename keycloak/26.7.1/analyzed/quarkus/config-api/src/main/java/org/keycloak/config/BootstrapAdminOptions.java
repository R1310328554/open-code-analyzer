package org.keycloak.config;

/**
 * 首次创建 master 领域时使用的临时 bootstrap 管理员配置项。
 *
 * <p>仅在 master realm 初次创建时生效，用于提供可登录的管理员账号或服务账号。
 */
public class BootstrapAdminOptions {
    
    /** 默认临时管理员用户名。 */
    public static final String DEFAULT_TEMP_ADMIN_USERNAME = "temp-admin";
    /** 默认临时管理员服务账号 client id（与用户名相同）。 */
    public static final String DEFAULT_TEMP_ADMIN_SERVICE = DEFAULT_TEMP_ADMIN_USERNAME;
    /** 默认临时管理员账号过期时间（分钟）。 */
    public static final int DEFAULT_TEMP_ADMIN_EXPIRATION = 120;
    private static final String USED_ONLY_WHEN = " Used only when the master realm is created.";
    private static final String NON_CLI = " Use a non-CLI configuration option for this option if possible.";

    /** 临时 bootstrap 管理员密码（仅 master realm 创建时使用，建议非 CLI 配置）。 */
    public static final Option<String> PASSWORD = new OptionBuilder<>("bootstrap-admin-password", String.class)
            .category(OptionCategory.BOOTSTRAP_ADMIN)
            .description("Temporary bootstrap admin password." + USED_ONLY_WHEN + NON_CLI)
            .build();

    /** 临时 bootstrap 管理员用户名。 */
    public static final Option<String> USERNAME = new OptionBuilder<>("bootstrap-admin-username", String.class)
            .category(OptionCategory.BOOTSTRAP_ADMIN)
            .description("Temporary bootstrap admin username." + USED_ONLY_WHEN)
            .defaultValue(DEFAULT_TEMP_ADMIN_USERNAME)
            .build();

    /** 临时 bootstrap 管理员账号过期时间（分钟，隐藏选项）。 */
    public static final Option<Integer> EXPIRATION = new OptionBuilder<>("bootstrap-admin-expiration", Integer.class)
            .category(OptionCategory.BOOTSTRAP_ADMIN)
            .description("Time in minutes for the bootstrap admin user to expire." + USED_ONLY_WHEN)
            .hidden()
            .build();

    /** 临时 bootstrap 服务账号的 client id。 */
    public static final Option<String> CLIENT_ID = new OptionBuilder<>("bootstrap-admin-client-id", String.class)
            .category(OptionCategory.BOOTSTRAP_ADMIN)
            .description("Client id for the temporary bootstrap admin service account." + USED_ONLY_WHEN)
            .defaultValue(DEFAULT_TEMP_ADMIN_SERVICE)
            .build();

    /** 临时 bootstrap 服务账号的 client secret（仅 master realm 创建时使用）。 */
    public static final Option<String> CLIENT_SECRET = new OptionBuilder<>("bootstrap-admin-client-secret", String.class)
            .category(OptionCategory.BOOTSTRAP_ADMIN)
            .description("Client secret for the temporary bootstrap admin service account." + USED_ONLY_WHEN + NON_CLI)
            .build();

}
