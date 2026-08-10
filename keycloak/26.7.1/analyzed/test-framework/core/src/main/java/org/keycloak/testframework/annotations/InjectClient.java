package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.realm.ClientConfig;
import org.keycloak.testframework.realm.DefaultClientConfig;

/**
 * 注入 {@link org.keycloak.testframework.realm.ManagedClient}，用于在 realm 内创建客户端。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectClient {

    /**
     * 指定客户端的自定义配置类。
     */
    Class<? extends ClientConfig> config() default DefaultClientConfig.class;

    /**
     * 控制该资源的生命周期。
     */
    LifeCycle lifecycle() default LifeCycle.CLASS;

    /**
     * 若测试需要多个实例，必须设置 ref 引用标识。
     */
    String ref() default "";

    /**
     * 指定要绑定的非默认 realm 的 ref。
     */
    String realmRef() default "";

}
