package org.keycloak.testframework.ui.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Chrome 无头模式 WebDriver 供应器，别名为 {@code chrome-headless}。
 * <p>
 * 通过 {@link DriverUtils#createChromeDriver(boolean)} 创建不显示 UI 的 Chrome 实例。
 * </p>
 */
public class ChromeHeadlessWebDriverSupplier extends AbstractWebDriverSupplier {

    /** {@inheritDoc} 返回供应器别名 {@code chrome-headless}。 */
    @Override
    public String getAlias() {
        return "chrome-headless";
    }

    /** {@inheritDoc} 创建无头 Chrome WebDriver。 */
    @Override
    public WebDriver getWebDriver() {
        return DriverUtils.createChromeDriver(true);
    }
}
