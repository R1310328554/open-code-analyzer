/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * 组织选择页面对象，对应 {@code login-select-organization} 模板。
 * <p>
 * 在多组织场景下让用户选择要登录的目标组织。
 */
public class SelectOrganizationPage extends AbstractLoginPage {

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public SelectOrganizationPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 点击指定别名的组织按钮。
     *
     * @param alias 组织别名，对应元素 id {@code organization-<alias>}
     */
    public void selectOrganization(String alias) {
        WebElement button = driver.findElement(By.id("organization-" + alias));
        button.click();
    }

    /**
     * 判断指定别名的组织按钮是否存在于 DOM 中。
     *
     * @param alias 组织别名
     * @return 存在返回 {@code true}
     */
    public boolean isOrganizationButtonPresent(String alias) {
        return !driver.driver().findElements(By.id("organization-" + alias)).isEmpty();
    }

    /** {@inheritDoc} 返回 {@code login-select-organization}。 */
    @Override
    public String getExpectedPageId() {
        return "login-select-organization";
    }
}
