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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * TOTP（基于时间的一次性密码）配置页面对象。
 * <p>
 * 用于在 Required Action 流程中绑定或验证 TOTP 设备。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginConfigTotpPage extends AbstractLoginPage {

    /** 展示 TOTP 共享密钥的只读输入框。 */
    @FindBy(id = "totpSecret")
    private WebElement totpSecret;

    /** 用户输入 TOTP 验证码的输入框。 */
    @FindBy(id = "totp")
    private WebElement totpInput;

    /** 为 TOTP 设备设置可读标签的输入框。 */
    @FindBy(id = "userLabel")
    private WebElement totpLabelInput;

    /** 提交 TOTP 配置的按钮。 */
    @FindBy(css = "input[type=\"submit\"]")
    private WebElement submitButton;

    /** 取消应用内认证（AIA）操作的按钮。 */
    @FindBy(name = "cancel-aia")
    private WebElement cancelAIAButton;

    /** 切换到二维码扫描模式的链接。 */
    @FindBy(id = "mode-barcode")
    private WebElement barcodeLink;

    /** 切换到手动输入密钥模式的链接。 */
    @FindBy(id = "mode-manual")
    private WebElement manualLink;

    /** 页面级错误告警消息元素。 */
    @FindBy(css = "div[class^='pf-v5-c-alert'], div[class^='alert-error']")
    private WebElement loginAlertErrorMessage;

    /** TOTP 验证码字段的校验错误提示。 */
    @FindBy(id = "input-error-otp-code")
    private WebElement totpInputCodeError;

    /** TOTP 设备标签字段的校验错误提示。 */
    @FindBy(id = "input-error-otp-label")
    private WebElement totpInputLabelError;

    /**
     * 使用指定 WebDriver 构造 TOTP 配置页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginConfigTotpPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 读取页面上展示的 TOTP 共享密钥。
     *
     * @return 密钥字符串
     */
    public String getTotpSecret() {
        return totpSecret.getAttribute("value");
    }

    /**
     * 判断 AIA 取消按钮是否在页面上可见。
     *
     * @return 可见返回 {@code true}
     */
    public boolean isCancelDisplayed() {
        try {
            return cancelAIAButton.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** {@inheritDoc} TOTP 配置页面标识为 {@code login-login-config-totp}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-config-totp";
    }
}
