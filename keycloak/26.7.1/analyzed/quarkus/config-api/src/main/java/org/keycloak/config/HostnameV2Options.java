package org.keycloak.config;


/**
 * 主机名 v2 配置选项，控制对外 URL、管理控制台地址与回通道解析。
 */
public class HostnameV2Options {

    /** 服务器对外暴露地址 */
    public static final Option<String> HOSTNAME = new OptionBuilder<>("hostname", String.class)
            .category(OptionCategory.HOSTNAME_V2)
            .description("Address at which is the server exposed. Can be a full URL, or just a hostname. When only hostname is provided, scheme, port and context path are resolved from the request.")
            .build();

    /** 管理控制台访问地址 */
    public static final Option<String> HOSTNAME_ADMIN = new OptionBuilder<>("hostname-admin", String.class)
            .category(OptionCategory.HOSTNAME_V2)
            .description("Address for accessing the administration console. Use this option if you are exposing the administration console using a reverse proxy on a different address than specified in the 'hostname' option.")
            .build();

    /** 是否动态解析回通道 URL */
    public static final Option<Boolean> HOSTNAME_BACKCHANNEL_DYNAMIC = new OptionBuilder<>("hostname-backchannel-dynamic", Boolean.class)
            .category(OptionCategory.HOSTNAME_V2)
            .description("Enables dynamic resolving of backchannel URLs, including hostname, scheme, port and context path. Set to true if your application accesses Keycloak via a private network. If set to true, 'hostname' option needs to be specified as a full URL.")
            .defaultValue(Boolean.FALSE)
            .build();

    /** 是否严格校验 Host 头 */
    public static final Option<Boolean> HOSTNAME_STRICT = new OptionBuilder<>("hostname-strict", Boolean.class)
            .category(OptionCategory.HOSTNAME_V2)
            .description("Disables dynamically resolving the hostname from request headers. Should always be set to true in production, unless your reverse proxy overwrites the Host header. If enabled, the 'hostname' option needs to be specified.")
            .defaultValue(Boolean.TRUE)
            .build();

    /** 是否启用主机名调试页 */
    public static final Option<Boolean> HOSTNAME_DEBUG = new OptionBuilder<>("hostname-debug", Boolean.class)
            .category(OptionCategory.HOSTNAME_V2)
            .description("Toggles the hostname debug page that is accessible at /realms/master/hostname-debug.")
            .defaultValue(Boolean.FALSE)
            .build();

}
