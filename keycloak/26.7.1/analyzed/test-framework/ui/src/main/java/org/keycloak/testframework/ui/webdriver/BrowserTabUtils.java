package org.keycloak.testframework.ui.webdriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * 浏览器标签页管理工具，由 {@link ManagedWebDriver#tabs()} 持有。
 * <p>
 * 标签页从 0 开始编号（例如第一个标签页索引为 0）。
 * </p>
 * <p>
 * 注意：每个 {@link WebDriver} 实例应仅对应一个 {@code BrowserTabUtils}，以保证标签页顺序正确。
 * </p>
 *
 * @author <a href="mailto:mabartos@redhat.com">Martin Bartos</a>
 */
public class BrowserTabUtils {

    private final ManagedWebDriver managedDriver;
    private WebDriver driver;
    private JavascriptExecutor jsExecutor;
    /** 当前已知的窗口句柄列表，顺序与标签页索引一致。 */
    private List<String> tabs;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param managedDriver 关联的托管 WebDriver
     */
    BrowserTabUtils(ManagedWebDriver managedDriver) {
        this.managedDriver = managedDriver;
        driverValidation();
    }

    /** 初始化底层驱动引用并同步当前窗口句柄列表。 */
    private void driverValidation() {
        this.driver = managedDriver.driver();
        this.jsExecutor = (JavascriptExecutor) driver;
        tabs = new ArrayList<>(driver.getWindowHandles());
    }


    /** @return 当前活动标签页的窗口句柄 */
    public String getActualWindowHandle() {
        return driver.getWindowHandle();
    }

    /**
     * 切换到指定窗口句柄对应的标签页。
     *
     * @param windowHandle 目标窗口句柄
     */
    public void switchToTab(String windowHandle) {
        driver.switchTo().window(windowHandle);
    }

    /**
     * 按索引切换到指定标签页。
     *
     * @param index 标签页索引（从 0 开始）
     */
    public void switchToTab(int index) {
        assertValidIndex(index);
        switchToTab(tabs.get(index));
    }

    /**
     * 打开新标签页并导航至给定 URL，随后切换到该标签页。
     *
     * @param url 新标签页要加载的地址
     */
    public void newTab(String url) {
        jsExecutor.executeScript("window.open(arguments[0]);", url);

        final Set<String> handles = driver.getWindowHandles();
        final String tabHandle = handles.stream()
                .filter(tab -> !tabs.contains(tab))
                .findFirst()
                .orElse(null);

        if (handles.size() > tabs.size() + 1) {
            throw new RuntimeException("Too many window handles. You can only create a new one by this method.");
        }

        if (tabHandle == null) {
            throw new RuntimeException("Creating the new tab failed.");
        }

        tabs.add(tabHandle);
        switchToTab(tabHandle);
    }

    /**
     * 关闭指定索引的标签页，并切换到前一个标签页。
     * <p>
     * 不允许关闭原始标签页（索引 0）或仅剩一个标签页时的任意关闭。
     * </p>
     *
     * @param index 要关闭的标签页索引
     */
    public void closeTab(int index) {
        assertValidIndex(index);

        if (index == 0 || getCountOfTabs() == 1)
            throw new RuntimeException("You must not close the original tab.");

        switchToTab(index);
        driver.close();

        tabs.remove(index);
        switchToTab(index - 1);
    }

    /** @return 当前跟踪的标签页数量 */
    public int getCountOfTabs() {
        return tabs.size();
    }

    /**
     * 关闭除原始标签页（索引 0）外的所有标签页；原始标签页始终保持打开。
     */
    public void closeTabs() {
        for (int i = 1; i < getCountOfTabs(); i++) {
            closeTab(i);
        }
    }

    /** 判断索引是否在有效范围内。 */
    private boolean validIndex(int index) {
        return (index >= 0 && tabs != null && index < tabs.size());
    }

    /** 校验标签页索引，无效时抛出 {@link IndexOutOfBoundsException}。 */
    private void assertValidIndex(int index) {
        if (!validIndex(index))
            throw new IndexOutOfBoundsException("Invalid index of tab.");
    }

}
