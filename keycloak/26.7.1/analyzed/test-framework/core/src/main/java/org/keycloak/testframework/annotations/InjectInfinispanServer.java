package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 注入 {@link org.keycloak.testframework.infinispan.InfinispanServer}，启动外部 Infinispan 服务器。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InjectInfinispanServer {

    /**
     * 控制该资源的生命周期。
     */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;
}
