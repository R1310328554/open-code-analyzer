package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 向配置类注入依赖；例如当 {@link org.keycloak.testframework.realm.ClientConfig}
 * 需要访问 {@link org.keycloak.testframework.realm.ManagedRealm} 以设置正确配置时使用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectDependency {

}
