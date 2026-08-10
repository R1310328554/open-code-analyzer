package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 密码重置请求页面对象。
 * <p>
 * 用户提交用户名或邮箱以触发重置密码邮件或后续流程。
 */
public class LoginPasswordResetPage extends AbstractLoginPage {

    /** 用户名或邮箱输入框。 */
    @FindBy(id = "username")
    private WebElement usernameInput;

    /** 提交重置请求的按钮。 */
    @FindBy(css = "[type=\"submit\"]")
    private WebElement submitButton;

    /** 密码重置表单元素。 */
    @FindBy(id = "kc-reset-password-form")
    private WebElement formResetPassword;

    /** 返回登录页的链接。 */
    @FindBy(partialLinkText = "Back to Login")
    private WebElement backToLogin;

    /**
     * 使用指定 WebDriver 构造密码重置页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginPasswordResetPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 填写用户名并提交密码重置请求。
     *
     * @param username 用户名或邮箱
     */
    public void changePassword(String username) {
        usernameInput.clear();
        usernameInput.sendKeys(username);

        submitButton.click();
    }

    /** 点击返回登录页链接。 */
    public void backToLogin() {
        backToLogin.click();
    }

    /**
     * 读取重置表单的提交目标 URL。
     *
     * @return 表单 {@code action} 属性值
     */
    public String getFormUrl() {
        return formResetPassword.getAttribute("action");
    }

    /** {@inheritDoc} 密码重置页面标识为 {@code login-login-reset-password}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-reset-password";
    }

}
