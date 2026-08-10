package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.oauth.DefaultOAuthIdentityProviderConfig;
import org.keycloak.testframework.oauth.OAuthIdentityProviderConfig;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.OAuthIdentityProvider}，用于模拟外部 OAuth/OIDC 身份提供者。
 * <p>
 * 适用于身份联合、代理登录等需要可控 IdP 响应的集成测试场景。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectOAuthIdentityProvider {

    /** 控制模拟 IdP 的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;

    /** 身份提供者配置类，默认 {@link DefaultOAuthIdentityProviderConfig}。 */
    Class<? extends OAuthIdentityProviderConfig> config() default DefaultOAuthIdentityProviderConfig.class;

}
