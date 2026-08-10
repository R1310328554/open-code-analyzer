package org.keycloak.testframework.injection;

import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.config.SuiteConfigSource;
import org.keycloak.testframework.server.KeycloakServerConfig;

/**
 * 测试套件级别的配置入口与生命周期辅助。
 * <p>
 * 通过 {@link SuiteConfigSource} 注册供应器/服务器配置，并在套件结束时重置 {@link Config} 与 {@link Extensions}。
 */
public class SuiteSupport {

    /** 当前活跃的套件配置构建器。 */
    private static SuiteConfig suiteConfig = new SuiteConfig();

    /** @return 启动或复用当前 {@link SuiteConfig} */
    public static SuiteConfig startSuite() {
        if (suiteConfig == null) {
            suiteConfig = new SuiteConfig();
        }
        return suiteConfig;
    }

    /** 套件结束：清除配置源、重新初始化 {@link Config} 并重置扩展单例。 */
    public static void stopSuite() {
        SuiteConfigSource.clear();
        Config.initConfig();
        Extensions.reset();
        suiteConfig = null;
    }

    /** 链式注册套件级 {@code kc.test.*} 配置的构建器。 */
    public static class SuiteConfig {

        /** 注册 Keycloak 服务器配置类（{@code kc.test.server.config}）。 */
        public SuiteConfig registerServerConfig(Class<? extends KeycloakServerConfig> serverConfig) {
            registerSupplierConfig("server", serverConfig);
            return this;
        }

        /** 为指定值类型注册供应器配置类的全限定名。 */
        public SuiteConfig registerSupplierConfig(String supplierValueType, Class<?> supplierConfig) {
            SuiteConfigSource.set("kc.test." + supplierValueType + ".config", supplierConfig.getName());
            return this;
        }

        /** 为供应器注册单个配置键值对。 */
        public SuiteConfig registerSupplierConfig(String supplierValueType, String supplierConfigKey, String supplierConfigValue) {
            SuiteConfigSource.set("kc.test." + supplierValueType + "." + supplierConfigKey, supplierConfigValue);
            return this;
        }

        /** 为值类型别名选择默认供应器实现。 */
        public SuiteConfig supplier(String name, String supplier) {
            SuiteConfigSource.set("kc.test." + name, supplier);
            return this;
        }

        /** 设置值类型的 include 供应器列表（逗号分隔）。 */
        public SuiteConfig includedSuppliers(String name, String... suppliers) {
            SuiteConfigSource.set("kc.test." + name + ".suppliers.included", String.join(",", suppliers));
            return this;
        }

        /** 设置值类型的 exclude 供应器列表（逗号分隔）。 */
        public SuiteConfig excludedSuppliers(String name, String... suppliers) {
            SuiteConfigSource.set("kc.test." + name + ".suppliers.excluded", String.join(",", suppliers));
            return this;
        }

    }

}
