package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.CibaProvider}，用于模拟 CIBA 认证通道与推送通知端点。
 *
 * @author rmartinc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectCibaProvider {
    /** 控制 CIBA 模拟端点的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;
}
