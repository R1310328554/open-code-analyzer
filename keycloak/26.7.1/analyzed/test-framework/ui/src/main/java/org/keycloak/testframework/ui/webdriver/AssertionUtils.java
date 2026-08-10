package org.keycloak.testframework.ui.webdriver;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

/**
 * Keycloak 登录页 UI 断言工具，由 {@link ManagedWebDriver#assertions()} 持有。
 * <p>
 * 封装常见页面元素校验，简化 UI 测试中的断言编写。
 */
public class AssertionUtils {

    private final ManagedWebDriver managed;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param managed 关联的托管 WebDriver
     */
    AssertionUtils(ManagedWebDriver managed) {
        this.managed = managed;
    }

    /**
     * 断言 {@code kc-page-title} 元素文本与期望值一致。
     *
     * @param title 期望的页面标题
     */
    public void assertTitle(String title) {
        String kcPageTitle = managed.findElement(By.id("kc-page-title")).getText();
        Assertions.assertEquals(title, kcPageTitle);
    }

}
