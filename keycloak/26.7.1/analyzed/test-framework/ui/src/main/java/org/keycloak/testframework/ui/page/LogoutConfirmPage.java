/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.testframework.ui.page;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 登出确认页面对象，对应 {@code login-logout-confirm} 模板。
 * <p>
 * 用于在 SSO 登出流程中确认是否结束当前会话。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutConfirmPage extends AbstractLoginPage {

    @FindBy(css = "input[type=\"submit\"]")
    private WebElement confirmLogoutButton;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LogoutConfirmPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** 通过回车键确认登出。 */
    public void confirmLogout() {
        confirmLogoutButton.sendKeys(Keys.ENTER);
    }

    /** {@inheritDoc} 返回 {@code login-logout-confirm}。 */
    @Override
    public String getExpectedPageId() {
        return "login-logout-confirm";
    }
}
