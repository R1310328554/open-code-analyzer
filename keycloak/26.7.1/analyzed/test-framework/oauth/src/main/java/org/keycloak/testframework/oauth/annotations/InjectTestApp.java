package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.TestApp}，用于模拟 OAuth 客户端回调端点。
 * <p>
 * 通常与 {@link InjectOAuthClient} 配合，为 Keycloak 提供可访问的重定向与管理 URI。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectTestApp {

}
