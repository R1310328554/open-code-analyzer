package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.realm.DefaultRealmConfig;
import org.keycloak.testframework.realm.RealmConfig;

/**
 * 注入 {@link org.keycloak.testframework.realm.ManagedRealm}，用于在测试中创建或绑定 realm。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectRealm {

    /**
     * 指定 realm 的自定义配置类。
     */
    Class<? extends RealmConfig> config() default DefaultRealmConfig.class;

    /**
     * 从 classpath 上的 JSON 文件加载自定义配置。
     */
    String fromJson() default "";

    /**
     * 控制该资源的生命周期。
     */
    LifeCycle lifecycle() default LifeCycle.CLASS;

    /**
     * 若测试需要多个实例，必须设置 ref 引用标识。
     */
    String ref() default "";

    /**
     * 绑定到已有 realm 而非新建；绑定时将忽略 config，且不会自动删除该 realm。
     *
     * @return 要绑定的已有 realm 名称
     */
    String attachTo() default "";

}
