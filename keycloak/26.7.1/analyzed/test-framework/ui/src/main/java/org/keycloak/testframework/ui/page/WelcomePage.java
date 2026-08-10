package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Keycloak 欢迎/初始管理员注册页面对象，对应 {@code welcome} 模板。
 * <p>
 * 用于首次启动时创建初始管理员账户或进入管理控制台。
 */
public class WelcomePage extends AbstractPage {

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(id = "firstName")
    private WebElement firstNameInput;

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "password-confirmation")
    private WebElement passwordConfirmationInput;

    @FindBy(xpath = "//button")
    private WebElement submitButton;

    @FindBy(css = ".pf-v5-c-alert")
    private WebElement pageAlert;

    @FindBy(css = ".pf-v5-c-title")
    private WebElement welcomeMessage;

    @FindBy(css = ".pf-v5-c-login__main-header-desc")
    private WebElement welcomeDescription;

    @FindBy(css = ".pf-v5-c-button")
    private WebElement openAdminConsoleLink;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public WelcomePage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 填写用户名与密码（不含姓名与邮箱）。
     *
     * @param username 用户名
     * @param password 密码
     */
    public void fillRegistration(String username, String password) {
        fillRegistration(username, null, null, null, password);
    }

    /**
     * 填写完整注册表单字段。
     *
     * @param username 用户名
     * @param firstName 名（可为 {@code null} 跳过）
     * @param lastName 姓（可为 {@code null} 跳过）
     * @param email 邮箱（可为 {@code null} 跳过）
     * @param password 密码（同时填入确认密码字段）
     */
    public void fillRegistration(String username, String firstName, String lastName, String email, String password) {
        usernameInput.sendKeys(username);
        if (firstName != null) {
            firstNameInput.sendKeys(firstName);
        }
        if (lastName != null) {
            lastNameInput.sendKeys(lastName);
        }
        if (email != null) {
            emailInput.sendKeys(email);
        }
        passwordInput.sendKeys(password);
        passwordConfirmationInput.sendKeys(password);
    }

    /** 点击提交按钮完成注册。 */
    public void submit() {
        submitButton.click();
    }

    /** 点击打开管理控制台链接。 */
    public void clickOpenAdminConsole() {
        openAdminConsoleLink.click();
    }

    /** @return 欢迎标题文本 */
    public String getWelcomeMessage() {
        return welcomeMessage.getText();
    }

    /** @return 欢迎页描述文本 */
    public String getWelcomeDescription() {
        return welcomeDescription.getText();
    }

    /** @return 页面告警提示文本 */
    public String getPageAlert() {
        return pageAlert.getText();
    }

    /** {@inheritDoc} 返回 {@code welcome}。 */
    @Override
    public String getExpectedPageId() {
        return "welcome";
    }
}
