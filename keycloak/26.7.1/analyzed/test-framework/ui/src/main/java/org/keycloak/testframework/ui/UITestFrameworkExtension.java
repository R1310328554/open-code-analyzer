package org.keycloak.testframework.ui;

import java.util.List;
import java.util.Map;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.ui.page.PageSupplier;
import org.keycloak.testframework.ui.webdriver.ChromeHeadlessWebDriverSupplier;
import org.keycloak.testframework.ui.webdriver.ChromeWebDriverSupplier;
import org.keycloak.testframework.ui.webdriver.FirefoxHeadlessWebDriverSupplier;
import org.keycloak.testframework.ui.webdriver.FirefoxWebDriverSupplier;
import org.keycloak.testframework.ui.webdriver.HtmlUnitWebDriverSupplier;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;


/**
 * UI 测试框架扩展，通过 SPI 注册 WebDriver 与页面对象相关 {@link Supplier}。
 * <p>
 * 支持 HtmlUnit、Chrome、Firefox 及其无头模式，并将 {@link ManagedWebDriver} 别名为 {@code browser}。
 */
public class UITestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 返回 UI 测试所需的 WebDriver 与 Page 供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new HtmlUnitWebDriverSupplier(),
                new ChromeHeadlessWebDriverSupplier(),
                new ChromeWebDriverSupplier(),
                new FirefoxHeadlessWebDriverSupplier(),
                new FirefoxWebDriverSupplier(),
                new PageSupplier()
        );
    }

    /** {@inheritDoc} 将 {@link ManagedWebDriver} 映射为配置别名 {@code browser}。 */
    @Override
    public Map<Class<?>, String> valueTypeAliases() {
        return Map.of(
                ManagedWebDriver.class, "browser"
        );
    }

}
