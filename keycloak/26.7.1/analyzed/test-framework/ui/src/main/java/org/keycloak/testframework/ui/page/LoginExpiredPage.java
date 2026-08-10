package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 登录会话过期页面对象。
 * <p>
 * 当认证流程超时后，用户可选择重新开始或继续当前流程。
 */
public class LoginExpiredPage extends AbstractLoginPage {

    /**
     * 使用指定 WebDriver 构造登录过期页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginExpiredPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** 重新开始登录流程的链接。 */
    @FindBy(id = "loginRestartLink")
    private WebElement loginRestartLink;

    /** 在过期提示后继续当前登录流程的链接。 */
    @FindBy(id = "loginContinueLink")
    private WebElement loginContinueLink;


    /** 点击“重新开始登录”链接。 */
    public void clickLoginRestartLink() {
        loginRestartLink.click();
    }

    /** 点击“继续登录”链接。 */
    public void clickLoginContinueLink() {
        loginContinueLink.click();
    }

    /** {@inheritDoc} 登录过期页面标识为 {@code login-login-page-expired}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-page-expired";
    }
}
