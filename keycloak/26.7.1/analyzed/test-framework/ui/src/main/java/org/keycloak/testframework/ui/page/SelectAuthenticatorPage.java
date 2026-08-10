package org.keycloak.testframework.ui.page;

import java.util.List;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * 认证方式选择页面对象，对应 {@code login-select-authenticator} 模板。
 * <p>
 * 列出用户可选的认证机制（密码、OTP、WebAuthn 等），供测试点击切换。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:pzaoral@redhat.com">Peter Zaoral</a>
 */
public class SelectAuthenticatorPage extends AbstractLoginPage {

    // 对应 PasswordForm 认证器
    public static final String PASSWORD = "Password";

    // 对应 WebAuthn/Passkey 认证器
    public static final String SECURITY_KEY = "Passkey";

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public SelectAuthenticatorPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 点击选择指定的登录方式。
     *
     * @param loginMethodName 页面上展示的方式名称，例如 "Password" 或 "Authenticator Application"
     */
    public void selectLoginMethod(String loginMethodName) {
        getLoginMethodRowByName(loginMethodName).click();
    }

    /**
     * 读取指定登录方式对应的帮助说明文本。
     *
     * @param loginMethodName 页面上展示的方式名称
     * @return 帮助描述文本
     */
    public String getLoginMethodHelpText(String loginMethodName) {
        return getLoginMethodRowByName(loginMethodName).findElement(By.className("select-auth-box-desc")).getText();
    }


    private List<WebElement> getLoginMethodsRows() {
        return driver.driver().findElements(By.className("select-auth-box-parent"));
    }

    private String getLoginMethodNameFromRow(WebElement loginMethodRow) {
        return loginMethodRow.findElement(By.className("select-auth-box-headline")).getText();
    }

    private WebElement getLoginMethodRowByName(String loginMethodName) {
        return getLoginMethodsRows().stream()
                .filter(loginMethodRow -> loginMethodName.equals(getLoginMethodNameFromRow(loginMethodRow)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Login method '" + loginMethodName + "' not found in the available authentication mechanisms"));
    }

    /** {@inheritDoc} 返回 {@code login-select-authenticator}。 */
    @Override
    public String getExpectedPageId() {
        return "login-select-authenticator";
    }
}
