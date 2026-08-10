package org.keycloak.config;

/**
 * Keycloak 配置选项在帮助文档与 CLI 中的分组分类。
 */
public enum OptionCategory {
    /** 缓存相关选项。 */
    CACHE("Cache", 10, ConfigSupportLevel.SUPPORTED),
    /** 配置系统相关选项。 */
    CONFIG("Config", 15, ConfigSupportLevel.SUPPORTED),
    /** 主数据库相关选项。 */
    DATABASE("Database", 20, ConfigSupportLevel.SUPPORTED),
    /** 附加数据源相关选项。 */
    DATABASE_DATASOURCES("Database - additional datasources", 21, ConfigSupportLevel.SUPPORTED),
    /** 事务相关选项。 */
    TRANSACTION("Transaction",30, ConfigSupportLevel.SUPPORTED),
    /** 功能特性（Profile）相关选项。 */
    FEATURE("Feature", 40, ConfigSupportLevel.SUPPORTED),
    /** 主机名 v2 相关选项。 */
    HOSTNAME_V2("Hostname v2", 50, ConfigSupportLevel.SUPPORTED),
    /** 主机名 v1 相关选项（已弃用）。 */
    HOSTNAME_V1("Hostname v1", 51, ConfigSupportLevel.DEPRECATED),
    /** HTTP(S) 相关选项。 */
    HTTP("HTTP(S)", 60, ConfigSupportLevel.SUPPORTED),
    /** HTTP 访问日志相关选项。 */
    HTTP_ACCESS_LOG("HTTP Access log", 61, ConfigSupportLevel.SUPPORTED),
    /** 健康检查相关选项。 */
    HEALTH("Health", 70, ConfigSupportLevel.SUPPORTED),
    /** 管理接口相关选项。 */
    MANAGEMENT("Management", 75, ConfigSupportLevel.SUPPORTED),
    /** 指标相关选项。 */
    METRICS("Metrics", 80, ConfigSupportLevel.SUPPORTED),
    /** 反向代理相关选项。 */
    PROXY("Proxy", 90, ConfigSupportLevel.SUPPORTED),
    /** 密钥库（Vault）相关选项。 */
    VAULT("Vault", 100, ConfigSupportLevel.SUPPORTED),
    /** 日志相关选项。 */
    LOGGING("Logging", 110, ConfigSupportLevel.SUPPORTED),
    /** OpenTelemetry 遥测相关选项。 */
    TELEMETRY("Telemetry (OpenTelemetry)", 112, ConfigSupportLevel.SUPPORTED),
    /** 分布式追踪相关选项。 */
    TRACING("Tracing", 113, ConfigSupportLevel.SUPPORTED),
    /** 事件相关选项。 */
    EVENTS("Events", 114, ConfigSupportLevel.SUPPORTED),
    /** 信任库相关选项。 */
    TRUSTSTORE("Truststore", 115, ConfigSupportLevel.SUPPORTED),
    /** 安全相关选项。 */
    SECURITY("Security", 120, ConfigSupportLevel.SUPPORTED),
    /** 导出相关选项。 */
    EXPORT("Export", 130, ConfigSupportLevel.SUPPORTED),
    /** 导入相关选项。 */
    IMPORT("Import", 140, ConfigSupportLevel.SUPPORTED),
    /** OpenAPI 相关选项。 */
    OPENAPI("OpenAPI configuration", 150, ConfigSupportLevel.SUPPORTED),
    /** 服务器行为相关选项。 */
    SERVER("Server configuration", 160, ConfigSupportLevel.SUPPORTED),
    /** 引导管理员相关选项。 */
    BOOTSTRAP_ADMIN("Bootstrap Admin", 998, ConfigSupportLevel.SUPPORTED),
    /** 通用/未分类选项。 */
    GENERAL("General", 999, ConfigSupportLevel.SUPPORTED);

    /** 在帮助文档中显示的分组标题。 */
    private final String heading;
    // 序号较小的分类在文档中排在前面
    /** 分组显示顺序（数值越小越靠前）。 */
    private final int order;
    /** 该分类的支持级别。 */
    private final ConfigSupportLevel supportLevel;

    OptionCategory(String heading, int order, ConfigSupportLevel supportLevel) {
        this.order = order;
        this.supportLevel = supportLevel;
        this.heading = getHeadingBySupportLevel(heading);
    }

    /** @return 分组标题（可能含 Experimental/Preview/Deprecated 后缀） */
    public String getHeading() {
        return this.heading;
    }

    /** @return 显示顺序 */
    public int getOrder() {
        return this.order;
    }

    /** @return 支持级别 */
    public ConfigSupportLevel getSupportLevel() {
        return this.supportLevel;
    }

    /** 按支持级别为标题追加后缀。 */
    private String getHeadingBySupportLevel(String heading) {
        return switch (supportLevel) {
            case EXPERIMENTAL -> heading + " (Experimental)";
            case PREVIEW -> heading + " (Preview)";
            case DEPRECATED -> heading + " (Deprecated)";
            default -> heading;
        };
    }

    /** 根据分组标题查找对应的 {@link OptionCategory}。 */
    public static OptionCategory fromHeading(String heading) {
        for (OptionCategory category : OptionCategory.values()) {
            if (category.getHeading().equals(heading)) {
                return category;
            }
        }
        throw new RuntimeException("Could not find category with heading " + heading);
    }
}
