package org.keycloak.testframework.remote.runonserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link RunOnServerClient}，用于在 Keycloak 服务器进程内执行代码。
 * <p>
 * 所需类会在必要时序列化并发送至服务端，以便远程加载与执行。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectRunOnServer {

    /** 控制 {@link RunOnServerClient} 实例的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.CLASS;

    /** 同一测试中需要多个客户端实例时用于区分的引用名。 */
    String ref() default "";

    /** 关联的 Realm 引用名，用于确定 RunOnServer 请求的目标 realm URL。 */
    String realmRef() default "";

    /** 允许通过 {@link TestClassServer} 下载的额外包名前缀。 */
    String[] permittedPackages() default "";

}
