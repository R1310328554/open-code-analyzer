package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记测试类字段，由框架注入 {@link org.keycloak.testframework.clustering.LoadBalancer} 实例。
 * 仅在与 {@link org.keycloak.testframework.server.ClusteredKeycloakServer} 联用时有效。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectLoadBalancer {
}
