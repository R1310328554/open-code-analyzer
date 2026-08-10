package org.keycloak.testframework.ui.webdriver;

/**
 * UI 测试支持的浏览器类型枚举。
 * <p>
 * 由 {@link ManagedWebDriver#getBrowserType()} 根据底层 {@link org.openqa.selenium.WebDriver} 实现类推断。
 * </p>
 */
public enum BrowserType {

    /** Google Chrome 浏览器。 */
    CHROME,
    /** Mozilla Firefox 浏览器。 */
    FIREFOX,
    /** HtmlUnit 无头浏览器（纯 Java 实现，无需外部驱动）。 */
    HTML_UNIT

}
