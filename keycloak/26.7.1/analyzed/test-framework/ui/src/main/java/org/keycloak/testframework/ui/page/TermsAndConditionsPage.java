package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 条款与条件同意页面对象，对应 {@code login-terms} 模板。
 * <p>
 * 用户在继续认证前需接受或拒绝服务条款。
 */
public class TermsAndConditionsPage extends AbstractLoginPage {

    @FindBy(id = "kc-accept")
    private WebElement submitButton;

    @FindBy(id = "kc-decline")
    private WebElement cancelButton;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public TermsAndConditionsPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** 点击接受按钮同意条款。 */
    public void acceptTerms() {
        submitButton.click();
    }
    /** 点击拒绝按钮不同意条款。 */
    public void declineTerms() {
        cancelButton.click();
    }

    /** {@inheritDoc} 返回 {@code login-terms}。 */
    @Override
    public String getExpectedPageId() {
        return "login-terms";
    }
}
