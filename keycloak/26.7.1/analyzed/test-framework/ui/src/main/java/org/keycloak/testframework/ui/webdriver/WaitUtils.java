package org.keycloak.testframework.ui.webdriver;

import java.time.Duration;
import java.util.function.Function;

import org.keycloak.OAuth2Constants;
import org.keycloak.testframework.ui.page.AbstractPage;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 显式等待工具，由 {@link ManagedWebDriver#waiting()} 持有。
 * <p>
 * 封装 {@link WebDriverWait}，用于等待页面加载、OAuth 回调及自定义条件满足。
 * </p>
 */
public class WaitUtils {

    private final ManagedWebDriver managed;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param managed 关联的托管 WebDriver
     */
    WaitUtils(ManagedWebDriver managed) {
        this.managed = managed;
    }

    /**
     * 等待指定页面对象对应的 {@code data-page-id} 出现在当前文档中。
     *
     * @param page 期望加载的页面对象
     * @return 当前 {@code WaitUtils} 实例，支持链式调用
     */
    public WaitUtils waitForPage(AbstractPage page) {
        String expectedPageId = page.getExpectedPageId();
        try {
            createDefaultWait().ignoring(StaleElementReferenceException.class).until(d -> expectedPageId.equals(managed.page().getCurrentPageId()));
        } catch (TimeoutException e) {
            Assertions.fail("Expected page '" + expectedPageId + "' to be loaded, but currently on page '" + managed.page().getCurrentPageId() + "' after timeout");
        }
        return this;
    }

    /**
     * 等待 OAuth 授权回调 URL 中出现 {@code code=} 或 {@code error=} 参数。
     *
     * @return 当前 {@code WaitUtils} 实例，支持链式调用
     */
    public WaitUtils waitForOAuthCallback() {
        waitForOAuthCallback(webdriver1 -> webdriver1.getCurrentUrl().contains(OAuth2Constants.CODE + "=") || webdriver1.getCurrentUrl().contains(OAuth2Constants.ERROR + "="));
        return this;
    }

    /**
     * 使用自定义条件等待 OAuth 回调完成。
     *
     * @param oAuthResponseIsPresent 判断 OAuth 响应是否已出现的条件函数
     * @return 当前 {@code WaitUtils} 实例，支持链式调用
     */
    public WaitUtils waitForOAuthCallback(Function<? super WebDriver, Boolean> oAuthResponseIsPresent) {
        try {
            createDefaultWait().until(oAuthResponseIsPresent);
        } catch (TimeoutException e) {
            Assertions.fail("Expected OAuth callback, but URL was '" + managed.getCurrentUrl() + "' after timeout");
        }
        return this;
    }

    /**
     * 等待浏览器文档标题与期望值一致。
     *
     * @param title 期望的页面标题
     * @return 当前 {@code WaitUtils} 实例，支持链式调用
     */
    public WaitUtils waitForTitle(String title) {
        createDefaultWait().until(d -> d.getTitle().equals(title));
        return this;
    }

    /**
     * 等待自定义条件成立并返回其结果。
     *
     * @param isTrue 等待条件函数
     * @param <V> 条件返回值类型
     * @return 条件满足时的返回值
     */
    public <V> V until(Function<WebDriver, V> isTrue) {
        return createDefaultWait().until(isTrue);
    }

    /** 创建默认 5 秒超时、50 毫秒轮询间隔的 {@link WebDriverWait}。 */
    private WebDriverWait createDefaultWait() {
        return new WebDriverWait(managed.driver(), Duration.ofSeconds(5), Duration.ofMillis(50));
    }

}
