package org.keycloak.testframework.ui.webdriver;

import java.util.Set;

import org.keycloak.cookie.CookieType;

import org.openqa.selenium.Cookie;

/**
 * 浏览器 Cookie 操作工具，由 {@link ManagedWebDriver#cookies()} 持有。
 * <p>
 * 封装 Selenium Cookie API，便于 UI 测试中读写与清理 Cookie。
 * </p>
 */
public class CookieUtils {

    private final ManagedWebDriver managed;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param managed 关联的托管 WebDriver
     */
    CookieUtils(ManagedWebDriver managed) {
        this.managed = managed;
    }

    /**
     * 向当前页面域添加 Cookie。
     *
     * @param cookie 要添加的 Selenium Cookie
     */
    public void add(Cookie cookie) {
        managed.driver().manage().addCookie(cookie);
    }

    /**
     * 按 Keycloak {@link CookieType} 获取命名 Cookie。
     *
     * @param cookieType Keycloak Cookie 类型
     * @return 匹配的 Cookie，不存在时返回 {@code null}
     */
    public Cookie get(CookieType cookieType) {
        return managed.driver().manage().getCookieNamed(cookieType.getName());
    }

    /** @return 当前页面域下的全部 Cookie 集合 */
    public Set<Cookie> getAll() {
        return managed.driver().manage().getCookies();
    }

    /**
     * 按名称获取 Cookie。
     *
     * @param name Cookie 名称
     * @return 匹配的 Cookie，不存在时返回 {@code null}
     */
    public Cookie get(String name) {
        return managed.driver().manage().getCookieNamed(name);
    }

    /** 删除当前页面域下的全部 Cookie。 */
    public void deleteAll() {
        managed.driver().manage().deleteAllCookies();
    }

}
