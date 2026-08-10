package org.keycloak.testframework.ui.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * OAuth 授权同意页面对象，对应 {@code login-login-oauth-grant} 模板。
 * <p>
 * 展示客户端请求的 scope 列表，并提供接受或拒绝授权的操作。
 */
public class OAuthGrantPage extends AbstractLoginPage {

    // 内置客户端 scope 同意项的本地化展示文本
    public static final String PROFILE_CONSENT_TEXT = "User profile";
    public static final String EMAIL_CONSENT_TEXT = "Email address";
    public static final String ADDRESS_CONSENT_TEXT = "Address";
    public static final String PHONE_CONSENT_TEXT = "Phone number";
    public static final String OFFLINE_ACCESS_CONSENT_TEXT = "Offline Access";
    public static final String ROLES_CONSENT_TEXT = "User roles";

    @FindBy(css = "[name=\"accept\"]")
    private WebElement acceptButton;
    @FindBy(css = "[name=\"cancel\"]")
    private WebElement cancelButton;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public OAuthGrantPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** 点击“接受”按钮授予所请求的权限。 */
    public void accept(){
        acceptButton.click();
    }

    /** 点击“取消”按钮拒绝授权。 */
    public void cancel(){
        cancelButton.click();
    }

    /**
     * 读取页面上展示的全部 scope 同意项文本。
     *
     * @return 同意项描述列表，顺序与 DOM 一致
     */
    public List<String> getDisplayedGrants() {
        List<String> table = new ArrayList<>();
        WebElement divKcOauth = driver.findElement(By.id("kc-oauth"));
        for (WebElement li : divKcOauth.findElements(By.tagName("li"))) {
            WebElement span = li.findElement(By.tagName("span"));
            table.add(span.getText());
        }
        return table;
    }

    /**
     * 断言展示的 scope 与期望值完全一致（忽略顺序）。
     *
     * @param expectedGrants 期望出现的同意项文本
     */
    public void assertGrants(String... expectedGrants) {
        List<String> displayed = getDisplayedGrants();
        List<String> expected = Arrays.asList(expectedGrants);
        Assertions.assertTrue(displayed.containsAll(expected) && expected.containsAll(displayed),
                "Not matched grants. Displayed grants: " + displayed + ", expected grants: " + expected);
    }

    /** {@inheritDoc} 返回 {@code login-login-oauth-grant}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-oauth-grant";
    }

}
