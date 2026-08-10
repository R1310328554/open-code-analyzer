package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.SectorIdentifierRedirectUrisProvider}，
 * 用于暴露 OIDC sector identifier 重定向 URI 文档。
 *
 * @author rmartinc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectSectorIdentifierRedirectUrisProvider {
    /** 控制 sector identifier 端点的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;

    /** sector identifier 文档中包含的重定向 URI 列表。 */
    String[] value();
}
