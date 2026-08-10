package org.keycloak.testframework.ui.page;

import java.util.Optional;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Keycloak 标准用户名/密码登录页面对象。
 * <p>
 * 封装凭据输入、社交 IdP 按钮、记住我及注册/重置密码链接等交互。
 */
public class LoginPage extends AbstractLoginPage {

    /** 用户名输入框。 */
    @FindBy(id = "username")
    private WebElement usernameInput;

    /** 密码输入框。 */
    @FindBy(id = "password")
    private WebElement passwordInput;

    /** 提交登录表单的按钮。 */
    @FindBy(css = "[type=submit]")
    private WebElement submitButton;

    /** “记住我”复选框。 */
    @FindBy(id = "rememberMe")
    private WebElement rememberMe;

    /** 跳转注册页面的链接。 */
    @FindBy(linkText = "Register")
    private WebElement registerLink;

    /** 跳转密码重置页面的链接。 */
    @FindBy(linkText = "Forgot Password?")
    private WebElement resetPasswordLink;

    /** 登录成功提示消息元素。 */
    @FindBy(className = "pf-m-success")
    private WebElement loginSuccessMessage;

    /** 用户名字段的校验错误提示。 */
    @FindBy(id = "input-error-username")
    private WebElement userNameInputError;

    /** 密码字段的校验错误提示。 */
    @FindBy(id = "input-error-password")
    private WebElement passwordInputError;

    /**
     * 使用指定 WebDriver 构造登录页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 填写用户名与密码字段。
     *
     * @param username 用户名
     * @param password 密码
     */
    public void fillLogin(String username, String password) {
        usernameInput.clear();
        usernameInput.sendKeys(username);
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    /**
     * 仅填写密码字段（用于仅需密码的重新认证场景）。
     *
     * @param password 密码
     */
    public void fillPassword(String password) {
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    /** 点击提交按钮发送登录表单。 */
    public void submit() {
        submitButton.click();
    }

    /**
     * 点击指定别名的社交身份提供者登录按钮。
     *
     * @param alias IdP 别名，对应按钮 id 后缀
     */
    public void clickSocial(String alias) {
        WebElement socialButton = findSocialButton(alias);
        socialButton.click();
    }

    /**
     * 定位指定别名的社交 IdP 按钮元素。
     *
     * @param alias IdP 别名
     * @return 对应的 {@link WebElement}
     */
    public WebElement findSocialButton(String alias) {
        String id = "social-" + alias;
        return driver.findElement(By.id(id));
    }

    /**
     * 判断指定别名的社交 IdP 按钮是否存在于 DOM 中。
     *
     * @param alias IdP 别名
     * @return 存在返回 {@code true}
     */
    public boolean isSocialButtonPresent(String alias) {
        String id = "social-" + alias;
        return !driver.driver().findElements(By.id(id)).isEmpty();
    }

    /**
     * 设置“记住我”复选框的选中状态。
     *
     * @param value {@code true} 表示勾选
     */
    public void rememberMe(boolean value) {
        boolean selected = isRememberMe();
        if ((value && !selected) || !value && selected) {
            rememberMe.click();
        }
    }

    /**
     * 读取“记住我”复选框当前是否已选中。
     *
     * @return 已选中返回 {@code true}
     */
    public boolean isRememberMe() {
        return rememberMe.isSelected();
    }

    /** 点击注册链接。 */
    public void clickRegister() {
        registerLink.click();
    }

    /** 点击“忘记密码”链接。 */
    public void resetPassword() {
        resetPasswordLink.click();
    }

    /**
     * 读取登录成功提示文本。
     *
     * @return 成功消息；元素不存在时返回 {@code null}
     */
    public String getSuccessMessage() {
        return loginSuccessMessage != null ? loginSuccessMessage.getText() : null;
    }

    /** {@inheritDoc} 标准登录页面标识为 {@code login-login}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login";
    }

    /**
     * 读取用户名输入框当前值。
     *
     * @return 用户名文本
     */
    public String getUsername() {
        return usernameInput.getAttribute("value");
    }

    /**
     * 读取用户名输入框的 {@code autocomplete} DOM 属性。
     *
     * @return autocomplete 属性值
     */
    public String getUsernameAutocomplete() {
        return usernameInput.getDomAttribute("autocomplete");
    }

    /** 清空用户名输入框内容。 */
    public void clearUsernameInput() {
        usernameInput.clear();
    }

    /**
     * 读取用户名字段的校验错误文本。
     *
     * @return 错误消息；无错误时返回 {@code null}
     */
    public String getUsernameInputError() {
        try {
            return userNameInputError.getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * 读取密码字段的校验错误文本。
     *
     * @return 包含错误内容的 {@link Optional}，无错误时为空
     */
    public Optional<String> getPasswordInputError() {
        try {
            return Optional.of(passwordInputError.getText());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    /**
     * 判断“记住我”复选框是否在页面上可见。
     *
     * @return 可见返回 {@code true}
     */
    public boolean isRememberMeCheckboxPresent() {
        try {
            return rememberMe.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
