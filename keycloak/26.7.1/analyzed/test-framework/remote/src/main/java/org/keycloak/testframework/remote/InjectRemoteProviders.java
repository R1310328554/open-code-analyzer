package org.keycloak.testframework.remote;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 声明测试需要注入 {@link RemoteProviders} 标记类型的注解。
 * <p>
 * 由 {@link RemoteProvidersSupplier} 解析，用于挂载远程测试提供者依赖。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectRemoteProviders {

    /** @return 注入实例的生命周期，默认为 {@link LifeCycle#GLOBAL} */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;
}
