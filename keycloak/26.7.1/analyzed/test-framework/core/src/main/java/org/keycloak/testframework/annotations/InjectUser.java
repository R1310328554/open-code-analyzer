package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.realm.DefaultUserConfig;
import org.keycloak.testframework.realm.UserConfig;

/**
 * 在测试类字段上注入由测试框架托管的 {@link org.keycloak.testframework.realm.ManagedUser} 实例。
 * 可通过 {@link org.keycloak.testframework.realm.UserConfig} 子类定制用户属性。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectUser {

    /** @return 用户配置类，默认 {@link DefaultUserConfig} */
    Class<? extends UserConfig> config() default DefaultUserConfig.class;

    /** 控制托管用户的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.CLASS;

    /** 同一测试中需要多个用户实例时必须设置 ref 以区分。 */
    String ref() default "";

    /** 关联的 Realm 引用名，对应 {@link InjectRealm#ref()}。 */
    String realmRef() default "";
}
