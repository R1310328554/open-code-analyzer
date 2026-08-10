package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.oauth.DefaultOAuthClientConfiguration;
import org.keycloak.testframework.realm.ClientConfig;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.OAuthClient}，用于向 Keycloak 发送 OAuth/OIDC 请求。
 * <p>
 * 可通过 {@link org.keycloak.testframework.realm.ClientConfig} 定制客户端属性，并关联 realm、WebDriver 与测试应用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectOAuthClient {

    /** 客户端配置类，默认 {@link DefaultOAuthClientConfiguration}。 */
    Class<? extends ClientConfig> config() default DefaultOAuthClientConfiguration.class;

    /** 控制 OAuth 客户端实例的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.CLASS;

    /** 同一测试中需要多个客户端实例时用于区分的引用名。 */
    String ref() default "";

    /** 关联的 Realm 引用名，对应 {@link org.keycloak.testframework.annotations.InjectRealm#ref()}。 */
    String realmRef() default "";

    /** 关联的 WebDriver 引用名，对应 {@link org.keycloak.testframework.annotations.InjectWebDriver#ref()}。 */
    String webDriverRef() default "";

    /** 是否在客户端上配置 Keycloak 管理回调 URL（{@link TestApp#getAdminUri()}）。 */
    boolean kcAdmin() default false;

}
