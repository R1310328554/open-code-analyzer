package org.keycloak.testframework.ui.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * HtmlUnit WebDriver 供应器，别名为 {@code htmlunit}。
 * <p>
 * 提供纯 Java 实现的轻量级浏览器，无需安装 Chrome/Firefox 驱动，适合快速 UI 测试。
 * </p>
 */
public class HtmlUnitWebDriverSupplier extends AbstractWebDriverSupplier {

    /** {@inheritDoc} 返回供应器别名 {@code htmlunit}。 */
    @Override
    public String getAlias() {
        return "htmlunit";
    }

    /** {@inheritDoc} 创建 HtmlUnit WebDriver。 */
    @Override
    public WebDriver getWebDriver() {
        return DriverUtils.createHtmlUnitDriver();
    }
}
