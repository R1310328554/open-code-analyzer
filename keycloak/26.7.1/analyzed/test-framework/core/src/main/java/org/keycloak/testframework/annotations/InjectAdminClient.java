package org.keycloak.testframework.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 {@link org.keycloak.admin.client.Keycloak} 实例，用于访问 Keycloak 管理 REST API。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectAdminClient {

    /**
     * 若测试需要多个实例，必须设置 ref 引用标识。
     */
    String ref() default "";

    /**
     * 指定要绑定的非默认 realm 的 ref。
     */
    String realmRef() default "";

    /**
     * <code>BOOTSTRAP</code> 绑定 master realm 与全局测试客户端；<code>MANAGED_REALM</code>
     * 绑定受管 realm，并使用指定的 client 或 user 认证。使用 <code>MANAGED_REALM</code> 时，
     * 须在 {@link org.keycloak.testframework.realm.ManagedRealm} 实例中通过 {@link org.keycloak.testframework.realm.RealmConfig} 实现配置 client 或 user。
     */
    Mode mode() default Mode.BOOTSTRAP;

    /**
     * 以该客户端身份认证。
     * 客户端须在 {@link org.keycloak.testframework.realm.ManagedRealm} 实例中配置，不支持 {@link org.keycloak.testframework.realm.ManagedClient}。
     */
    String client() default "";

    /**
     * 以该用户身份认证。
     * 用户须在 {@link org.keycloak.testframework.realm.ManagedRealm} 实例中配置，不支持 {@link org.keycloak.testframework.realm.ManagedUser}。
     */
    String user() default "";

    /** 注入模式：引导或受管 realm。 */
    enum Mode {

        /** 引导模式：绑定 master realm 与全局测试客户端。 */
        BOOTSTRAP,
        /** 受管 realm 模式：绑定指定 realm 并使用配置的 client 或 user。 */
        MANAGED_REALM

    }

}
