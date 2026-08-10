package org.keycloak.testframework.ui.webdriver;

import java.lang.reflect.Constructor;

import org.keycloak.testframework.ui.page.AbstractPage;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

/**
 * 页面对象与页面信息工具，由 {@link ManagedWebDriver#page()} 持有。
 * <p>
 * 负责实例化 {@link AbstractPage} 子类、读取页面标识与标题等元数据。
 * </p>
 */
public class PageUtils {

    private final ManagedWebDriver managed;

    /**
     * 包内可见构造器，由 {@link ManagedWebDriver} 创建实例。
     *
     * @param managed 关联的托管 WebDriver
     */
    PageUtils(ManagedWebDriver managed) {
        this.managed = managed;
    }

    /**
     * 通过反射创建页面对象并用 {@link PageFactory} 初始化元素绑定。
     *
     * @param valueType 页面对象类型，须含 {@code (ManagedWebDriver)} 构造器
     * @param <S> 页面对象泛型
     * @return 已绑定 WebDriver 的页面实例
     */
    public <S extends AbstractPage> S createPage(Class<S> valueType) {
        try {
            Constructor<S> constructor = valueType.getDeclaredConstructor(ManagedWebDriver.class);
            S page = constructor.newInstance(managed);
            PageFactory.initElements(managed.driver(), page);
            return page;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取当前页面 {@code body} 元素的 {@code data-page-id} 属性。
     *
     * @return 页面标识字符串
     */
    public String getCurrentPageId() {
        return managed.findElement(By.xpath("//body")).getAttribute("data-page-id");
    }

    /** @return 浏览器文档标题 */
    public String getTitle() {
        return managed.driver().getTitle();
    }

    /** @return 当前页面 HTML 源码 */
    public String getPageSource() {
        return managed.driver().getPageSource();
    }

}
