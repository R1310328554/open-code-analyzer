package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 {@link org.keycloak.testframework.events.Events}，用于轮询 Keycloak 登录事件。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectEvents {

    /**
     * 若测试需要多个实例，必须设置 ref 引用标识。
     */
    String ref() default "";

    /**
     * 指定要绑定的非默认 realm 的 ref。
     */
    String realmRef() default "";

}
