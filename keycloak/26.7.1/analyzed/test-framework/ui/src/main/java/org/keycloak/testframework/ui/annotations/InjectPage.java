package org.keycloak.testframework.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.ui.page.AbstractPage} 实现，用于与 Keycloak 服务器发布的 HTML 页面交互。
 * <p>
 * 页面对象封装 Selenium 元素定位与常见操作，比直接操作 WebDriver 更便于编写 UI 测试。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectPage {

    /**
     * 同一测试中需要多个页面对象实例时用于区分的引用名。
     *
     * @return 页面对象引用标识，默认为空字符串
     */
    String ref() default "";

    /**
     * 关联的 {@link InjectWebDriver} 引用名，用于绑定页面对象所使用的 WebDriver 实例。
     *
     * @return WebDriver 引用标识，默认为空字符串
     */
    String webDriverRef() default "";

}
