package org.keycloak.testframework.ui.page;


import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 邮箱验证页面对象，对应 {@code login-login-verify-email} 模板。
 * <p>
 * 提示用户查收验证邮件，并支持重发邮件或取消认证操作。
 */
public class VerifyEmailPage extends AbstractLoginPage {

    @FindBy(linkText = "Click here")
    private WebElement resendEmailLink;

    @FindBy(name = "cancel-aia")
    private WebElement cancelAIAButton;

    @FindBy(className = "kc-feedback-text")
    private WebElement feedbackText;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public VerifyEmailPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** 点击“点此重发”链接以重新发送验证邮件。 */
    public void clickResendEmail() {
        resendEmailLink.click();
    }

    /** @return 重发邮件链接的 {@code href} 属性 */
    public String getResendEmailLink() {
        return resendEmailLink.getAttribute("href");
    }

    /** @return 页面反馈提示文本 */
    public String getFeedbackText() {
        return feedbackText.getText();
    }

    /** 点击取消按钮中止认证操作。 */
    public void cancel() {
        cancelAIAButton.click();
    }

    /** {@inheritDoc} 返回 {@code login-login-verify-email}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-verify-email";
    }
}
