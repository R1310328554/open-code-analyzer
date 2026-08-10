package org.keycloak.testframework.injection;

/**
 * 测试框架注入注解中常用属性名的常量定义。
 * <p>
 * 供反射与代理逻辑统一引用，避免硬编码字符串。
 */
public interface AnnotationFields {

    /** {@code config} 属性名。 */
    String CONFIG = "config";
    /** {@code lifecycle} 属性名。 */
    String LIFECYCLE = "lifecycle";
    /** {@code ref} 属性名，用于区分同类型多实例。 */
    String REF = "ref";

    /** {@code realmRef} 属性名，引用托管 realm 实例。 */
    String REALM_REF = "realmRef";

}
