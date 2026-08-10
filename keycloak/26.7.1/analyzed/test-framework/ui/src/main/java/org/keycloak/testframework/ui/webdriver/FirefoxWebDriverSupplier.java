package org.keycloak.testframework.ui.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Firefox 有界面模式 WebDriver 供应器，别名为 {@code firefox}。
 * <p>
 * 通过 {@link DriverUtils#createFirefoxDriver(boolean)} 创建可见 UI 的 Firefox 实例。
 * </p>
 */
public class FirefoxWebDriverSupplier extends AbstractWebDriverSupplier {

    /** {@inheritDoc} 返回供应器别名 {@code firefox}。 */
    @Override
    public String getAlias() {
        return "firefox";
    }

    /** {@inheritDoc} 创建有界面 Firefox WebDriver。 */
    @Override
    public WebDriver getWebDriver() {
        return DriverUtils.createFirefoxDriver(false);
    }
}
