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
 * Keycloak 登录错误页面对象。
 * <p>
 * 用于读取错误说明、追踪 ID 以及返回客户端应用的链接。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ErrorPage extends AbstractLoginPage {

    /** 面向用户的错误说明文本元素。 */
    @FindBy(className = "instruction")
    private WebElement errorMessage;

    /** 服务端生成的错误追踪 ID 元素。 */
    @FindBy(id = "traceId")
    private WebElement traceIdMessage;

    /** 返回客户端应用的链接元素。 */
    @FindBy(id = "backToApplication")
    private WebElement backToApplicationLink;

    /**
     * 使用指定 WebDriver 构造错误页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public ErrorPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 读取页面上显示的错误说明文本。
     *
     * @return 错误消息内容
     */
    public String getError() {
        return errorMessage.getText();
    }

    /**
     * 读取错误追踪 ID 文本。
     *
     * @return 追踪 ID 字符串
     */
    public String getTraceId() {
        return traceIdMessage.getText();
    }

    /**
     * 判断错误追踪 ID 是否在页面上可见。
     *
     * @return 可见返回 {@code true}，元素不存在时返回 {@code false}
     */
    public boolean isTraceIdPresent() {
        try {
            return traceIdMessage.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** 点击“返回应用”链接。 */
    public void clickBackToApplication() {
        backToApplicationLink.click();
    }

    /**
     * 获取“返回应用”链接的目标 URL。
     *
     * @return 链接 {@code href} 属性值；元素不存在时返回 {@code null}
     */
    public String getBackToApplicationLink() {
        if (backToApplicationLink == null) {
            return null;
        } else {
            return backToApplicationLink.getAttribute("href");
        }
    }

    /** {@inheritDoc} 错误页面标识为 {@code login-error}。 */
    @Override
    public String getExpectedPageId() {
        return "login-error";
    }
}
