package org.keycloak.testframework.conditions;

import java.lang.annotation.Annotation;

import org.keycloak.testframework.server.KeycloakServer;

/** {@link DisabledForServers} 的执行条件实现，针对 {@link org.keycloak.testframework.server.KeycloakServer}。 */
class DisabledForServersCondition extends AbstractDisabledForSupplierCondition {

    /** @return {@link org.keycloak.testframework.server.KeycloakServer} 类型 */
    @Override
    Class<?> valueType() {
        return KeycloakServer.class;
    }

    /** @return {@link DisabledForServers} 注解类型 */
    Class<? extends Annotation> annotation() {
        return DisabledForServers.class;
    }

}
