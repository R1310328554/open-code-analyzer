package org.keycloak.testframework.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 注入 {@link org.keycloak.testframework.admin.AdminClientFactory}，用于创建
 * {@link org.keycloak.admin.client.Keycloak} 实例。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectAdminClientFactory {

    /**
     * 若测试需要多个实例，必须设置 ref 引用标识。
     */
    String ref() default "";

    /**
     * 控制该资源的生命周期。
     */
    LifeCycle lifecycle() default LifeCycle.CLASS;
}
