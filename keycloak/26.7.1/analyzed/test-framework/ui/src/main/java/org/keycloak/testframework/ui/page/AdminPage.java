package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

/**
 * Keycloak 管理控制台页面对象。
 * <p>
 * 对应 {@code kc-page-id="admin"} 的管理 UI 页面。
 */
public class AdminPage extends AbstractPage {

    /**
     * 使用指定 WebDriver 构造管理控制台页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public AdminPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** {@inheritDoc} 管理控制台页面标识为 {@code admin}。 */
    @Override
    public String getExpectedPageId() {
        return "admin";
    }

}
