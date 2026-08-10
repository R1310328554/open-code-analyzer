package org.keycloak.testframework.server;

import java.lang.annotation.Annotation;

import org.keycloak.testframework.injection.InstanceContext;

/**
 * 在服务器启动前拦截并修改 {@link KeycloakServerConfigBuilder} 的扩展点。
 * <p>
 * 由 {@link org.keycloak.testframework.injection.Supplier} 实现，以便依赖实例影响托管服务器配置。
 *
 * @param <T> 被注入的值类型
 * @param <S> 触发注入的注解类型
 */
public interface KeycloakServerConfigInterceptor<T, S extends Annotation> {

    /**
     * 基于已部署实例上下文调整服务器配置。
     *
     * @param serverConfig 当前服务器配置构建器
     * @param instanceContext 触发拦截的实例上下文
     * @return 修改后的配置构建器
     */
    KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<T, S> instanceContext);

}
