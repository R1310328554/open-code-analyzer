package org.keycloak.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.oauth.OIDCClientRepresentationBuilder;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.oauth.CimdProvider}，用于提供 CIMD 客户端元数据端点。
 *
 * @author rmartinc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectCimdProvider {
    /** 控制 CIMD 模拟端点的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;

    /** 指定构建 CIMD 元数据的 {@link OIDCClientRepresentationBuilder} 实现类。 */
    Class<? extends OIDCClientRepresentationBuilder> config() default OIDCClientRepresentationBuilder.class;
}
