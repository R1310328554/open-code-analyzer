package org.keycloak.testframework.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.ui.webdriver.ManagedWebDriver}，以便直接操作浏览器驱动。
 * <p>
 * 在可行时更推荐使用 {@link InjectPage} 页面对象，而非直接访问 WebDriver。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectWebDriver {

    /**
     * 同一测试中需要多个 WebDriver 实例时用于区分的引用名。
     *
     * @return WebDriver 引用标识，默认为空字符串
     */
    String ref() default "";

    /**
     * 控制 WebDriver 资源的生命周期范围。
     *
     * @return 生命周期枚举，默认为 {@link LifeCycle#GLOBAL}
     */
    LifeCycle lifecycle() default LifeCycle.GLOBAL;
}
