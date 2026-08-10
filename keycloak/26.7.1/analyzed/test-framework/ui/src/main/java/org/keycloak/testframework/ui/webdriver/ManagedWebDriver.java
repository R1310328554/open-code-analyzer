package org.keycloak.testframework.ui.webdriver;

import java.net.URL;

import org.keycloak.testframework.injection.ManagedTestResource;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * 托管 WebDriver 资源，封装底层 Selenium 驱动及各类 UI 测试辅助工具。
 * <p>
 * 由 {@link AbstractWebDriverSupplier} 子类创建并注入测试用例；测试结束时会自动关闭多余标签页。
 * </p>
 */
public class ManagedWebDriver extends ManagedTestResource {

    private WebDriver driver;

    private AssertionUtils assertionUtils = new AssertionUtils(this);
    private CookieUtils cookieUtils = new CookieUtils(this);
    private PageUtils pageUtils = new PageUtils(this);
    private NavigateUtils  navigateUtils = new NavigateUtils(this);
    private WaitUtils waitUtils = new WaitUtils(this);
    private final BrowserTabUtils tabUtils;

    /**
     * 使用给定底层 WebDriver 构造托管实例，并初始化各工具类。
     *
     * @param driver Selenium WebDriver 实例
     */
    public ManagedWebDriver(WebDriver driver) {
        this.driver = driver;
        this.tabUtils = new BrowserTabUtils(this);
    }

    /** @return 底层 Selenium WebDriver */
    public WebDriver driver() {
        return driver;
    }

    /**
     * 根据底层驱动实现类推断浏览器类型。
     *
     * @return 对应的 {@link BrowserType}
     * @throws RuntimeException 无法识别的驱动类型
     */
    public BrowserType getBrowserType() {
        if (driver instanceof HtmlUnitDriver) {
            return BrowserType.HTML_UNIT;
        } else if (driver instanceof ChromeDriver) {
            return BrowserType.CHROME;
        } else if (driver instanceof FirefoxDriver) {
            return BrowserType.FIREFOX;
        }
        throw new RuntimeException("Unknown browser type: " + driver.getClass());
    }

    /** @return 当前页面 URL */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * 按定位器查找单个页面元素。
     *
     * @param by Selenium 定位器
     * @return 匹配的 {@link WebElement}
     */
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    /**
     * 导航至指定 URL 字符串。
     *
     * @param url 目标地址
     */
    public void open(String url) {
        driver.navigate().to(url);
    }

    /**
     * 导航至指定 {@link URL}。
     *
     * @param url 目标地址
     */
    public void open(URL url) {
        driver.navigate().to(url);
    }

    /** @return 页面断言工具 */
    public AssertionUtils assertions() {
        return assertionUtils;
    }

    /** @return Cookie 操作工具 */
    public CookieUtils cookies() {
        return cookieUtils;
    }

    /** @return 页面对象与页面信息工具 */
    public PageUtils page() {
        return pageUtils;
    }

    /** @return 浏览器导航工具 */
    public NavigateUtils navigate() {
        return navigateUtils;
    }

    /** @return 标签页管理工具 */
    public BrowserTabUtils tabs() {
        return tabUtils;
    }

    /** @return 显式等待工具 */
    public WaitUtils waiting() {
        return waitUtils;
    }

    /** {@inheritDoc} 清理时关闭除原始标签页外的所有标签页。 */
    @Override
    public void runCleanup() {
        tabUtils.closeTabs();
    }
}
