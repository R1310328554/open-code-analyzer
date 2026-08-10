package org.keycloak.testframework.https;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在测试类字段上注入 {@link ManagedCertificates} 实例。
 * <p>
 * 通过 {@link #config()} 指定声明式证书配置类。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectCertificates {

    /**
     * 证书配置实现类。
     *
     * @return {@link CertificatesConfig} 子类
     */
    Class<? extends CertificatesConfig> config() default DefaultCertificatesConfig.class;
}
