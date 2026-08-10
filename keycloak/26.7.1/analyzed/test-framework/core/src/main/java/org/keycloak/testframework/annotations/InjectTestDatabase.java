package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.database.DatabaseConfig;
import org.keycloak.testframework.database.DefaultDatabaseConfig;
import org.keycloak.testframework.injection.LifeCycle;

/**
 * 注入测试用数据库资源，生命周期与配置由注解属性控制。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectTestDatabase {

    /**
     * 控制该资源的生命周期。
     */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;

    /** 指定数据库配置类。 */
    Class<? extends DatabaseConfig> config() default DefaultDatabaseConfig.class;
}
