package org.keycloak.testframework.remote.runonserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link TestClassServer}，用于在嵌入式 HTTP 服务器上暴露测试类字节码。
 * <p>
 * Keycloak 服务端通过该端点按需拉取测试类，以支持 {@link RunOnServerClient} 远程执行。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectTestClassServer {

    /** 控制 {@link TestClassServer} 实例的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;

}
