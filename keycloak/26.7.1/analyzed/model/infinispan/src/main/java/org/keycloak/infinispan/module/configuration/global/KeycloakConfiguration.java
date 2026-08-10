package org.keycloak.infinispan.module.configuration.global;

import org.keycloak.models.KeycloakSessionFactory;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;

/**
 * Keycloak 全局 Infinispan 模块配置：将 {@link KeycloakSessionFactory} 注入 Infinispan 组件工厂。
 */
@BuiltBy(KeycloakConfigurationBuilder.class)
public class KeycloakConfiguration {

    /** 全局不可变属性：供 Infinispan 工厂访问 Keycloak 会话工厂。 */
    static final AttributeDefinition<KeycloakSessionFactory> KEYCLOAK_SESSION_FACTORY = AttributeDefinition.builder("keycloak-session-factory", null, KeycloakSessionFactory.class)
            .global(true)
            .autoPersist(false)
            .immutable()
            .build();

    /** 受保护的属性集合快照。 */
    private final AttributeSet attributes;

    /** 构建本配置类所需的属性定义集合。 */
    static AttributeSet attributeSet() {
        return new AttributeSet(KeycloakConfiguration.class, KEYCLOAK_SESSION_FACTORY);
    }

    /** 包内构造：由 {@link KeycloakConfigurationBuilder} 创建受保护属性集。 */
    KeycloakConfiguration(AttributeSet attributes) {
        this.attributes = attributes;
    }

    /** 返回内部属性集合（供 Builder 读取/合并）。 */
    AttributeSet attributes() {
        return attributes;
    }

    /** 获取已注入的 Keycloak 会话工厂。 */
    public KeycloakSessionFactory keycloakSessionFactory() {
        return attributes.attribute(KEYCLOAK_SESSION_FACTORY).get();
    }

}
