package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 仅含用户名字段的登录页面对象，对应 Keycloak {@code login-login-username} 模板。
 * <p>
 * 支持“记住我”、社交 IdP 按钮以及用户名校验错误信息的读取与交互。
 */
public class LoginUsernamePage extends AbstractLoginPage {

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(css = "[type=submit]")
    private WebElement submitButton;

    @FindBy(id = "input-error-username")
    private WebElement userNameInputError;

    @FindBy(id = "rememberMe")
    private WebElement rememberMe;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginUsernamePage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 清空并填写用户名输入框。
     *
     * @param username 待输入的用户名
     */
    public void fillLoginWithUsernameOnly(String username) {
        usernameInput.clear();
        usernameInput.sendKeys(username);
    }

    /** @return 用户名输入框当前值 */
    public String getUsername() {
        return usernameInput.getAttribute("value");
    }

    /** @return 用户名输入框的 {@code autocomplete} DOM 属性 */
    public String getUsernameAutocomplete() {
        return usernameInput.getDomAttribute("autocomplete");
    }

    /**
     * 读取用户名字段校验错误文本。
     *
     * @return 错误提示；元素不存在时返回 {@code null}
     */
    public String getUsernameInputError() {
        try {
            return userNameInputError.getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /** 点击提交按钮继续认证流程。 */
    public void submit() {
        submitButton.click();
    }

    /** {@inheritDoc} 返回 {@code login-login-username}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-username";
    }

    /**
     * 设置“记住我”复选框状态。
     *
     * @param value 期望选中时为 {@code true}
     */
    public void rememberMe(boolean value) {
        boolean selected = isRememberMe();
        if ((value && !selected) || !value && selected) {
            rememberMe.click();
        }
    }

    /** @return “记住我”复选框是否已选中 */
    public boolean isRememberMe() {
        return rememberMe.isSelected();
    }

    /**
     * 判断指定别名的社交登录按钮是否存在于 DOM 中。
     *
     * @param alias IdP 别名，对应元素 id {@code social-<alias>}
     * @return 存在返回 {@code true}
     */
    public boolean isSocialButtonPresent(String alias) {
        String id = "social-" + alias;
        return !driver.driver().findElements(By.id(id)).isEmpty();
    }

    /**
     * 点击指定别名的社交登录按钮。
     *
     * @param alias IdP 别名
     */
    public void clickSocial(String alias) {
        WebElement socialButton = findSocialButton(alias);
        socialButton.click();
    }

    /**
     * 查找指定别名的社交登录按钮元素。
     *
     * @param alias IdP 别名
     * @return 对应的 {@link WebElement}
     */
    public WebElement findSocialButton(String alias) {
        String id = "social-" + alias;
        return driver.findElement(By.id(id));
    }
}
