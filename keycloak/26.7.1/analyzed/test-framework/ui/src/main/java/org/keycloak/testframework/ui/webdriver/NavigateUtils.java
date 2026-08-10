package org.keycloak.testframework.ui.webdriver;

import org.keycloak.testframework.ui.page.AbstractPage;

/**
 * 浏览器导航辅助工具，由 {@link ManagedWebDriver#navigate()} 持有。
 * <p>
 * 封装刷新、后退等导航操作，并处理 Chrome 后退后页面 ID 不一致时的补偿刷新。
 * </p>
 */
public class NavigateUtils {

    private final ManagedWebDriver driver;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param driver 关联的托管 WebDriver
     */
    NavigateUtils(ManagedWebDriver driver) {
        this.driver = driver;
    }

    /** 刷新当前页面。 */
    public void refresh() {
        driver.driver().navigate().refresh();
    }

    /**
     * 执行浏览器后退，并在 Chrome 下必要时刷新以恢复预期页面，最后断言当前页。
     * <p>
     * Chrome 后退后有时不会正确更新 {@code data-page-id}，需额外刷新以对齐页面对象状态。
     * </p>
     *
     * @param expectedPage 后退后期望到达的页面对象
     */
    public void backWithRefresh(AbstractPage expectedPage) {
        driver.driver().navigate().back();

        String currentPageId = driver.page().getCurrentPageId();
        if (!expectedPage.getExpectedPageId().equals(currentPageId) && driver.getBrowserType().equals(BrowserType.CHROME)) {
            driver.driver().navigate().refresh();
        }

        expectedPage.assertCurrent();
    }

}
