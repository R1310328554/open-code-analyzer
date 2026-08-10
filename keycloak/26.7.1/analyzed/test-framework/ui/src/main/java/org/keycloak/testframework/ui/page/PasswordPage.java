package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 仅含密码字段的登录页面对象，对应 {@code login-login-password} 模板。
 * <p>
 * 常见于分步登录流程中用户名已确定、仅需输入密码的场景。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PasswordPage extends AbstractLoginPage {

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "input-error-password")
    private WebElement passwordError;

    @FindBy(name = "login")
    private WebElement submitButton;

    @FindBy(css = "div[class^='pf-v5-c-alert'], div[class^='alert-error']")
    private WebElement loginErrorMessage;

    @FindBy(linkText = "Forgot Password?")
    private WebElement resetPasswordLink;

    @FindBy(id = "try-another-way")
    private WebElement tryAnotherWayLink;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public PasswordPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 清空并填写密码输入框。
     *
     * @param password 待输入的密码
     */
    public void fillPassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    /** 点击登录按钮提交密码。 */
    public void submit() {
        submitButton.click();
    }

    /** @return 密码输入框当前值 */
    public String getPassword() {
        return passwordInput.getAttribute("value");
    }

    /**
     * 读取密码字段校验错误文本。
     *
     * @return 错误提示；元素不存在时返回 {@code null}
     */
    public String getPasswordError() {
        try {
            return passwordError.getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * 读取页面级登录错误提示。
     *
     * @return 错误消息；元素不存在时返回 {@code null}
     */
    public String getError() {
        try {
            return loginErrorMessage.getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /** 点击“尝试其他方式”链接以切换认证方法。 */
    public void clickTryAnotherWayLink() {
        tryAnotherWayLink.click();
    }

    /** {@inheritDoc} 返回 {@code login-login-password}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-password";
    }
}
