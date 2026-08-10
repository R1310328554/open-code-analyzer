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

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 密码更新页面对象。
 * <p>
 * 用于 Required Action 或管理员强制改密流程中设置新密码。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginPasswordUpdatePage extends AbstractLoginPage {

    /** 新密码输入框。 */
    @FindBy(id = "password-new")
    private WebElement newPasswordInput;

    /** 确认新密码输入框。 */
    @FindBy(id = "password-confirm")
    private WebElement passwordConfirmInput;

    /** 提交新密码的按钮。 */
    @FindBy(css = "[type=\"submit\"]")
    private WebElement submitButton;

    /** 页面级错误告警消息元素。 */
    @FindBy(css = "div[class^='pf-v5-c-alert'], div[class^='alert-error']")
    private WebElement loginErrorMessage;

    /** 密码策略等反馈文本元素。 */
    @FindBy(className = "kc-feedback-text")
    private WebElement feedbackMessage;

    /** 取消应用内认证（AIA）操作的按钮。 */
    @FindBy(name = "cancel-aia")
    private WebElement cancelAIAButton;

    /**
     * 使用指定 WebDriver 构造密码更新页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginPasswordUpdatePage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 填写新密码与确认密码并提交表单。
     *
     * @param newPassword 新密码
     * @param passwordConfirm 确认密码
     */
    public void changePassword(String newPassword, String passwordConfirm) {
        newPasswordInput.sendKeys(newPassword);
        passwordConfirmInput.sendKeys(passwordConfirm);

        submitButton.click();
    }

    /** 点击 AIA 取消按钮。 */
    public void cancel() {
        cancelAIAButton.click();
    }

    /**
     * 读取页面级错误消息文本。
     *
     * @return 错误内容；元素不存在时返回 {@code null}
     */
    public String getError() {
        return loginErrorMessage != null ? loginErrorMessage.getText() : null;
    }

    /**
     * 读取密码策略反馈文本。
     *
     * @return 反馈消息内容
     */
    public String getFeedbackMessage() {
        return feedbackMessage.getText();
    }

    /**
     * 判断 AIA 取消按钮是否可见。
     *
     * @return 可见返回 {@code true}
     */
    public boolean isCancelDisplayed() {
        return cancelAIAButton.isDisplayed();
    }

    /** {@inheritDoc} 密码更新页面标识为 {@code login-login-update-password}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-update-password";
    }
}
