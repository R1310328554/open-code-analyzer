package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.KeycloakIntegrationTestExtension;
import org.keycloak.testframework.server.DefaultKeycloakServerConfig;
import org.keycloak.testframework.server.KeycloakServerConfig;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 在测试类上启用 Keycloak 集成测试框架，注册 {@link KeycloakIntegrationTestExtension}。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith({KeycloakIntegrationTestExtension.class})
public @interface KeycloakIntegrationTest {

    /** 指定 Keycloak 服务器的自定义 {@link KeycloakServerConfig} 实现类。 */
    Class<? extends KeycloakServerConfig> config() default DefaultKeycloakServerConfig.class;

}
