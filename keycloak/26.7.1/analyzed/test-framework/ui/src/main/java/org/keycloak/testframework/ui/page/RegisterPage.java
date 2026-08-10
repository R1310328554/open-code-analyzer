/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testframework.ui.page;

import java.util.Map;
import java.util.Map.Entry;

import org.keycloak.models.Constants;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 用户自助注册页面对象，对应 {@code login-register} 模板。
 * <p>
 * 支持填写姓名、邮箱、用户名、密码及自定义属性，并适配无密码注册表单变体。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RegisterPage extends AbstractLoginPage {

    @FindBy(name = "firstName")
    private WebElement firstNameInput;

    @FindBy(name = "lastName")
    private WebElement lastNameInput;

    @FindBy(name = "email")
    private WebElement emailInput;

    @FindBy(name = "username")
    private WebElement usernameInput;

    @FindBy(name = "password")
    private WebElement passwordInput;

    @FindBy(name = "password-confirm")
    private WebElement passwordConfirmInput;

    @FindBy(name = "department")
    private WebElement departmentInput;

    @FindBy(name = "termsAccepted")
    private WebElement termsAcceptedInput;

    @FindBy(css = "input[type=\"submit\"]")
    private WebElement submitButton;

    @FindBy(linkText = "« Back to Login")
    private WebElement backToLoginLink;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public RegisterPage(ManagedWebDriver driver) {
        super(driver);
    }

    // 适用于注册页不包含密码与确认密码字段的场景
    /**
     * 在无密码字段的注册页提交用户信息。
     *
     * @param firstName 名
     * @param lastName 姓
     * @param email 邮箱
     * @param username 用户名
     */
    public void registerWithoutPassword(String firstName, String lastName, String email, String username) {
        register(firstName, lastName, email, username, null, null, null, null, null);
    }

    /**
     * 填写并提交完整注册表单（密码与确认密码相同）。
     *
     * @param firstName 名
     * @param lastName 姓
     * @param email 邮箱
     * @param username 用户名
     * @param password 密码
     */
    public void register(String firstName, String lastName, String email, String username, String password) {
        register(firstName, lastName, email, username, password, password, null, null, null);
    }

    /**
     * 填写并提交注册表单，可分别指定密码与确认密码。
     *
     * @param firstName 名
     * @param lastName 姓
     * @param email 邮箱
     * @param username 用户名
     * @param password 密码
     * @param passwordConfirm 确认密码
     */
    public void register(String firstName, String lastName, String email, String username, String password, String passwordConfirm) {
        register(firstName, lastName, email, username, password, passwordConfirm, null, null, null);
    }

    /**
     * 填写并提交注册表单，支持部门、条款同意及自定义用户属性。
     *
     * @param firstName 名
     * @param lastName 姓
     * @param email 邮箱
     * @param username 用户名
     * @param password 密码
     * @param passwordConfirm 确认密码
     * @param department 部门（字段不存在时忽略）
     * @param termsAccepted 是否勾选条款同意
     * @param attributes 额外用户属性键值对
     */
    public void register(String firstName, String lastName, String email, String username, String password, String passwordConfirm, String department, Boolean termsAccepted, Map<String, String> attributes) {
        firstNameInput.clear();
        if (firstName != null) {
            firstNameInput.sendKeys(firstName);
        }

        lastNameInput.clear();
        if (lastName != null) {
            lastNameInput.sendKeys(lastName);
        }

        if (email != null) {
            if (isEmailPresent()) {
                emailInput.clear();
                emailInput.sendKeys(email);
            }
        }

        usernameInput.clear();
        if (username != null) {
            usernameInput.sendKeys(username);
        }

        if (!isPasswordPresent() && password != null) {
            Assertions.fail("Password expected to be filled, but password field not present on the registration page");
        }

        if (isPasswordPresent()) {
            passwordInput.clear();
            if (password != null) {
                passwordInput.sendKeys(password);
            }

            passwordConfirmInput.clear();
            if (passwordConfirm != null) {
                passwordConfirmInput.sendKeys(passwordConfirm);
            }
        }


        if (department != null) {
            if(isDepartmentPresent()) {
                departmentInput.clear();
                departmentInput.sendKeys(department);
            }
        }

        if (termsAccepted != null && termsAccepted) {
            termsAcceptedInput.click();
        }

        if (attributes != null) {
            for (Entry<String, String> attribute : attributes.entrySet()) {
                driver.findElement(By.name(Constants.USER_ATTRIBUTES_PREFIX + attribute.getKey())).sendKeys(attribute.getValue());
            }
        }

        submitButton.sendKeys(Keys.ENTER);
    }

    /** @return 名字段当前值 */
    public String getFirstName() {
        return firstNameInput.getAttribute("value");
    }

    /** @return 姓字段当前值 */
    public String getLastName() {
        return lastNameInput.getAttribute("value");
    }

    /** @return 邮箱字段当前值 */
    public String getEmail() {
        return emailInput.getAttribute("value");
    }

    /** @return 用户名字段当前值 */
    public String getUsername() {
        return usernameInput.getAttribute("value");
    }

    /** @return 密码字段当前值 */
    public String getPassword() {
        return passwordInput.getAttribute("value");
    }

    /** @return 部门输入框是否可见 */
    public boolean isDepartmentPresent() {
        try {
            return driver.findElement(By.name("department")).isDisplayed();
        } catch (NoSuchElementException nse) {
            return false;
        }
    }

    /** @return 邮箱输入框是否可见 */
    public boolean isEmailPresent() {
        try {
            return driver.findElement(By.name("email")).isDisplayed();
        } catch (NoSuchElementException nse) {
            return false;
        }
    }

    /** @return 密码输入框是否可见 */
    public boolean isPasswordPresent() {
        try {
            return driver.findElement(By.name("password")).isDisplayed();
        } catch (NoSuchElementException nse) {
            return false;
        }
    }

    /** {@inheritDoc} 返回 {@code login-register}。 */
    @Override
    public String getExpectedPageId() {
        return "login-register";
    }

    /** 点击“返回登录”链接。 */
    public void clickBackToLogin() {
        backToLoginLink.click();
    }
}
