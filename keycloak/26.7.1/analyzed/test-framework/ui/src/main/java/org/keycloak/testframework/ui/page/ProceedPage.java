/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
 * 信息提示页面对象，对应 {@code login-info} 模板。
 * <p>
 * 展示说明文字并提供“点击此处继续”链接以推进流程。
 *
 * @author hmlnarik
 */
public class ProceedPage extends AbstractPage {

    @FindBy(className = "instruction")
    private WebElement infoMessage;

    @FindBy(linkText = "» Click here to proceed")
    private WebElement proceedLink;

    /**
     * 绑定托管 WebDriver 并初始化页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public ProceedPage(ManagedWebDriver driver) {
        super(driver);
    }

    /** @return 页面说明文本 */
    public String getInfo() {
        return infoMessage.getText();
    }

    /** 点击继续链接以进入下一步。 */
    public void clickProceedLink() {
        proceedLink.click();
    }

    /** {@inheritDoc} 返回 {@code login-info}。 */
    @Override
    public String getExpectedPageId() {
        return "login-info";
    }
}
