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
 * Keycloak 登录信息提示页面对象。
 * <p>
 * 用于读取中性提示消息及返回客户端应用的链接。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InfoPage extends AbstractLoginPage {

    /** 面向用户的信息说明文本元素。 */
    @FindBy(className = "instruction")
    private WebElement infoMessage;

    /** 返回客户端应用的链接元素。 */
    @FindBy(linkText = "« Back to Application")
    private WebElement backToApplicationLink;

    /**
     * 使用指定 WebDriver 构造信息页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public InfoPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 读取页面上显示的信息说明文本。
     *
     * @return 信息消息内容
     */
    public String getInfo() {
        return infoMessage.getText();
    }

    /** {@inheritDoc} 信息页面标识为 {@code login-info}。 */
    @Override
    public String getExpectedPageId() {
        return "login-info";
    }

    /** 点击“返回应用”链接。 */
    public void clickBackToApplicationLink() {
        backToApplicationLink.click();
    }
}
