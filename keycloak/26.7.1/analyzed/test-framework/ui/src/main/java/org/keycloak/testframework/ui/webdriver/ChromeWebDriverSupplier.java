package org.keycloak.testframework.ui.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Chrome 有界面模式 WebDriver 供应器，别名为 {@code chrome}。
 * <p>
 * 通过 {@link DriverUtils#createChromeDriver(boolean)} 创建可见 UI 的 Chrome 实例。
 * </p>
 */
public class ChromeWebDriverSupplier extends AbstractWebDriverSupplier {

    /** {@inheritDoc} 返回供应器别名 {@code chrome}。 */
    @Override
    public String getAlias() {
        return "chrome";
    }

    /** {@inheritDoc} 创建有界面 Chrome WebDriver。 */
    @Override
    public WebDriver getWebDriver() {
        return DriverUtils.createChromeDriver(false);
    }
}
