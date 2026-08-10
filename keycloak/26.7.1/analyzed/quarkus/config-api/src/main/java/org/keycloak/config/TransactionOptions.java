package org.keycloak.config;

import static org.keycloak.config.WildcardOptionsUtil.getWildcardNamedKey;

/**
 * 全局与按数据源 XA 事务、超时相关配置选项。
 */
public class TransactionOptions {

    /** 数据库迁移/导入/导出事务的默认超时。 */
    public static final String MIGRATION_TRANSACTION_TIMEOUT = "30m";

    /** 配置选项：transaction-xa-enabled-<datasource>，是否为指定数据源启用 XA。 */
    public static final Option<Boolean> TRANSACTION_XA_ENABLED_DATASOURCE = new OptionBuilder<>("transaction-xa-enabled-<datasource>", Boolean.class)
            .category(OptionCategory.TRANSACTION)
            .description("If set to true, XA for <datasource> datasource will be used.")
            .buildTime(true)
            .defaultValue(Boolean.TRUE)
            .build();

    /** 配置选项：transaction-xa-enabled，是否默认启用 XA 数据源。 */
    public static final Option<Boolean> TRANSACTION_XA_ENABLED = new OptionBuilder<>("transaction-xa-enabled", Boolean.class)
            .category(OptionCategory.TRANSACTION)
            .description("If set to true, XA datasources will be used.")
            .buildTime(true)
            .defaultValue(Boolean.FALSE)
            .wildcardKey(TRANSACTION_XA_ENABLED_DATASOURCE.getKey())
            .build();

    /** 配置选项：transaction-default-timeout，默认事务超时。 */
    public static final Option<String> TRANSACTION_DEFAULT_TIMEOUT = new OptionBuilder<>("transaction-default-timeout", String.class)
            .category(OptionCategory.TRANSACTION)
            .description("The default transaction timeout. " + OptionsUtil.DURATION_DESCRIPTION)
            .buildTime(false)
            .defaultValue("5m")
            .build();

    /** 配置选项：transaction-setup-timeout，迁移/导入/导出事务超时。 */
    public static final Option<String> TRANSACTION_SETUP_TIMEOUT = new OptionBuilder<>("transaction-setup-timeout", String.class)
            .category(OptionCategory.TRANSACTION)
            .description("The transaction timeout for database migration/import/export transactions. " + OptionsUtil.DURATION_DESCRIPTION)
            .buildTime(false)
            .defaultValue(MIGRATION_TRANSACTION_TIMEOUT)
            .build();

    /**
     * 根据命名数据源属性解析对应的 XA 启用配置键。
     *
     * @param namedProperty 数据源名称，{@code "<default>"} 表示默认数据源
     * @return 完整的配置键名
     */
    public static String getNamedTxXADatasource(String namedProperty) {
        if ("<default>".equals(namedProperty)) {
            return TRANSACTION_XA_ENABLED.getKey();
        }
        return getWildcardNamedKey(TRANSACTION_XA_ENABLED_DATASOURCE.getKey(), namedProperty);
    }
}
