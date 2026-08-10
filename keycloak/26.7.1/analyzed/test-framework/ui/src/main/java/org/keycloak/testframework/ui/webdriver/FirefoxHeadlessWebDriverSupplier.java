package org.keycloak.testframework.ui.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Firefox 无头模式 WebDriver 供应器，别名为 {@code firefox-headless}。
 * <p>
 * 通过 {@link DriverUtils#createFirefoxDriver(boolean)} 创建不显示 UI 的 Firefox 实例。
 * </p>
 */
public class FirefoxHeadlessWebDriverSupplier extends AbstractWebDriverSupplier {

    /** {@inheritDoc} 返回供应器别名 {@code firefox-headless}。 */
    @Override
    public String getAlias() {
        return "firefox-headless";
    }

    /** {@inheritDoc} 创建无头 Firefox WebDriver。 */
    @Override
    public WebDriver getWebDriver() {
        return DriverUtils.createFirefoxDriver(true);
    }
}
