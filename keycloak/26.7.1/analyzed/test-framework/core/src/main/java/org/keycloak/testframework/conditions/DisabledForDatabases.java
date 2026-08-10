package org.keycloak.testframework.conditions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 在指定数据库 Supplier 别名下跳过测试。
 * {@code value} 为 {@link org.keycloak.testframework.database.TestDatabase} 的 Supplier 别名列表。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith({DisabledForDatabasesCondition.class})
public @interface DisabledForDatabases {

    /** @return 需跳过的数据库 Supplier 别名 */
    String[] value() default "";

}
