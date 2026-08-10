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

import java.util.Optional;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 登录主题页面对象基类，封装语言切换、提示与错误消息等通用元素。
 * <p>
 * 兼容 Login v1 与 v2 主题的不同 DOM 结构，在元素缺失时自动回退。
 */
public abstract class AbstractLoginPage extends AbstractPage {

    /** Login v2 主题下当前选中的语言选项。 */
    @FindBy(xpath = "//select[@aria-label='languages']/option[@selected]")
    private WebElement selectedLanguage;

    /** Login v2 主题下的语言下拉选择器。 */
    @FindBy(xpath = "//select[@aria-label='languages']")
    private WebElement languages;

    /** Login v1（base 主题）下显示当前语言的链接元素。 */
    @FindBy(id = "kc-current-locale-link")
    private WebElement languageTextBase;    // base 主题

    /** Login v1（base 主题）下的语言区域下拉容器。 */
    @FindBy(id = "kc-locale-dropdown")
    private WebElement localeDropdownBase;  // base 主题

    /** 重新认证流程中展示已尝试用户名的标签元素。 */
    @FindBy(id = "kc-attempted-username") // 重新认证时显示的用户名
    private WebElement attemptedUsernameLabel;

    /** PatternFly 信息级别登录提示消息元素。 */
    @FindBy(className = "pf-m-info")
    private WebElement loginInfoMessage;

    /** PatternFly 危险级别登录错误消息元素。 */
    @FindBy(className = "pf-m-danger")
    private WebElement loginErrorMessage;

    /**
     * 使用指定 WebDriver 构造登录页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public AbstractLoginPage(ManagedWebDriver driver) {
        super(driver);
    }

    /**
     * 读取当前选中的界面语言文本。
     *
     * @return 语言显示名称；元素不存在时回退到 v1 主题元素
     */
    public String getSelectedLanguage() {
        try {
            final String text = selectedLanguage.getText();
            return text == null ? text : text.trim();
        } catch (NoSuchElementException ex) {
            // Login v1 主题回退
            return languageTextBase.getText();
        }
    }

    /**
     * 在语言选择器中切换到指定语言。
     *
     * @param language 目标语言的显示文本（部分匹配）
     */
    public void selectLanguage(String language){
        try {
            WebElement langLink = languages.findElement(By.xpath("//option[text()[contains(.,'" + language + "')]]"));
            langLink.click();
        } catch (NoSuchElementException ex) {
            // Fallback for Login v1
            WebElement langLink = localeDropdownBase.findElement(By.xpath("//a[text()[contains(.,'" + language + "')]]"));
            langLink.click();
        }
    }

    /**
     * 获取重新认证页面上展示的用户名。
     *
     * @return 用户名文本；元素不存在时返回 {@code null}
     */
    public String getAttemptedUsername() {
        try {
            String text = attemptedUsernameLabel.getAttribute("value");
            if (text == null) return attemptedUsernameLabel.getText();
            return text;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * 读取登录页信息提示文本。
     *
     * @return 包含提示内容的 {@link Optional}，无提示时为空
     */
    public Optional<String> getInfoMessage() {
        try {
            return Optional.of(loginInfoMessage.getText());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    /**
     * 读取登录页错误提示文本。
     *
     * @return 包含错误内容的 {@link Optional}，无错误时为空
     */
    public Optional<String> getErrorMessage() {
        try {
            return Optional.of(loginErrorMessage.getText());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
