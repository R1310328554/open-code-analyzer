package org.keycloak.testframework.injection;

/**
 * 托管测试实例的生命周期范围。
 * <p>
 * 决定实例在 {@link Registry#afterEach}、{@link Registry#afterAll} 或 {@link Registry#close} 时何时销毁。
 */
public enum LifeCycle {

    /** 全局生命周期，仅在注册表 {@link Registry#close()} 时销毁。 */
    GLOBAL,
    /** 测试类级生命周期，在 {@link Registry#afterAll()} 销毁。 */
    CLASS,
    /** 测试方法级生命周期，在 {@link Registry#afterEach()} 销毁。 */
    METHOD

}
