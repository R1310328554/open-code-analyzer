package org.keycloak.infinispan.module.configuration.global;

import org.keycloak.models.KeycloakSessionFactory;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;

/**
 * {@link KeycloakConfiguration} 的构建器，用于在 Infinispan 全局配置阶段注入 Keycloak 依赖。
 */
public class KeycloakConfigurationBuilder implements Builder<KeycloakConfiguration> {

    /** 可变的属性集合，最终经 {@link AttributeSet#protect()} 固化为不可变配置。 */
    private final AttributeSet attributes;

    /** 从全局配置构建器上下文初始化属性集（未使用 GlobalConfigurationBuilder 参数）。 */
    public KeycloakConfigurationBuilder(GlobalConfigurationBuilder unused) {
        attributes = KeycloakConfiguration.attributeSet();
    }

    /** {@inheritDoc} 生成受保护（不可变）的 {@link KeycloakConfiguration} 实例。 */
    @Override
    public KeycloakConfiguration create() {
        return new KeycloakConfiguration(attributes.protect());
    }

    /** {@inheritDoc} 从模板配置合并或覆盖当前属性。 */
    @Override
    public Builder<?> read(KeycloakConfiguration template, Combine combine) {
        attributes.read(template.attributes(), combine);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AttributeSet attributes() {
        return attributes;
    }

    /** {@inheritDoc} Keycloak 模块配置无额外校验规则。 */
    @Override
    public void validate() {

    }

    /**
     * 设置要注入 Infinispan 组件工厂的 Keycloak 会话工厂。
     *
     * @param keycloakSessionFactory Keycloak 会话工厂实例
     * @return 当前 builder，支持链式调用
     */
    public KeycloakConfigurationBuilder setKeycloakSessionFactory(KeycloakSessionFactory keycloakSessionFactory) {
        attributes.attribute(KeycloakConfiguration.KEYCLOAK_SESSION_FACTORY).set(keycloakSessionFactory);
        return this;
    }

}
