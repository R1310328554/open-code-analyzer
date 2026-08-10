package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

/**
 * Keycloak UI 测试页面对象基类。
 * <p>
 * 持有 {@link ManagedWebDriver} 并提供 {@link #getExpectedPageId()} 与 {@link #assertCurrent()} 等通用能力。
 */
public abstract class AbstractPage {

    /** 当前页面对象绑定的托管 WebDriver。 */
    protected final ManagedWebDriver driver;

    /**
     * 使用指定 WebDriver 构造页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public AbstractPage(ManagedWebDriver driver) {
        this.driver = driver;
    }

    /**
     * 返回 Keycloak 登录主题中标识当前页面的 {@code kc-page-id} 值。
     *
     * @return 期望的页面标识字符串
     */
    public abstract String getExpectedPageId();

    /** 阻塞等待直至浏览器当前页面与 {@link #getExpectedPageId()} 匹配。 */
    public void assertCurrent() {
        driver.waiting().waitForPage(this);
    }
}
