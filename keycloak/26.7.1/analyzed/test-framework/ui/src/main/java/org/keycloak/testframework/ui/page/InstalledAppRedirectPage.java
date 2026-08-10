/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.testframework.ui.page;

import java.net.URI;
import java.net.URISyntaxException;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.services.Urls;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 由 {@code code.ftl} 模板渲染的已安装应用（Out-of-Band）回调页面对象。
 * <p>
 * 供 {@code KeycloakInstalled} 等原生客户端在授权码或错误回调场景下使用。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InstalledAppRedirectPage extends AbstractPage {

    /** 成功回调时展示授权码的输入元素。 */
    @FindBy(id = "code")
    private WebElement code;

    /** 页面标题元素。 */
    @FindBy(id = "kc-page-title")
    private WebElement pageTitle;

    /** 错误回调时展示错误详情的告警框元素。 */
    @FindBy(css = "div[class^='pf-v5-c-alert'], div[class^='alert-error']")
    private WebElement errorBox;

    /**
     * 使用指定 WebDriver 构造已安装应用回调页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public InstalledAppRedirectPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 构造并导航至 realm 已安装应用 OOB 回调 URL。
     *
     * @param kcUrl Keycloak 服务器基础 URL
     * @param realmName 目标 realm 名称
     * @param code 授权码；成功场景传入，否则为 {@code null}
     * @param error OAuth 错误码；失败场景传入，否则为 {@code null}
     * @param errorDescription 错误描述；可选
     */
    public void open(String kcUrl, String realmName, String code, String error, String errorDescription) {
        try {
            KeycloakUriBuilder kcUriBuilder = KeycloakUriBuilder.fromUri(Urls.realmInstalledAppUrnCallback(new URI(kcUrl), realmName));
            if (code != null) {
                kcUriBuilder.queryParam(OAuth2Constants.CODE, code);
            }
            if (error != null) {
                kcUriBuilder.queryParam(OAuth2Constants.ERROR, error);
            }
            if (errorDescription != null) {
                kcUriBuilder.queryParam(OAuth2Constants.ERROR_DESCRIPTION, errorDescription);
            }
            String oobEndpointUri = kcUriBuilder.build().toString();
            driver.driver().navigate().to(oobEndpointUri);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    /**
     * 断言页面标题为成功状态并返回授权码值。
     *
     * @return 授权码字符串
     */
    public String getSuccessCode() {
        Assertions.assertEquals("Success code", getPageTitleText());
        return code.getAttribute("value");
    }

    /**
     * 读取页面标题文本。
     *
     * @return 标题内容
     */
    public String getPageTitleText() {
        return pageTitle.getText();
    }

    // 断言标题与错误框内均不存在返回应用的链接
    public void assertLinkBackToApplicationNotPresent() {
        try {
            pageTitle.findElement(By.tagName("a"));
            throw new AssertionError("Link was present inside title");
        } catch (NoSuchElementException nsee) {
            // 预期无链接，忽略异常
        }

        try {
            errorBox.findElement(By.tagName("a"));
            throw new AssertionError("Link was present inside error box");
        } catch (NoSuchElementException nsee) {
            // Ignore
        }
    }

    /** {@inheritDoc} 已安装应用回调页面标识为 {@code login-code}。 */
    @Override
    public String getExpectedPageId() {
        return "login-code";
    }
}
