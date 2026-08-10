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
 * OAuth 授权同意（Consent）页面对象。
 * <p>
 * 封装用户确认或拒绝客户端权限请求的交互元素。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ConsentPage extends AbstractPage {

    /** 同意授权并继续的提交按钮。 */
    @FindBy(id = "kc-login")
    private WebElement submitButton;

    /** 拒绝授权并取消流程的按钮。 */
    @FindBy(id = "kc-cancel")
    private WebElement cancelButton;

    /**
     * 使用指定 WebDriver 构造同意页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public ConsentPage(ManagedWebDriver driver) { super(driver); }

    /** 点击同意按钮以批准客户端请求的权限。 */
    public void confirm() {
        submitButton.click();
    }

    /** 点击取消按钮以拒绝授权请求。 */
    public void cancel() {
        cancelButton.click();
    }

    /** {@inheritDoc} OAuth 同意页面标识为 {@code login-login-oauth-grant}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-oauth-grant";
    }
}
