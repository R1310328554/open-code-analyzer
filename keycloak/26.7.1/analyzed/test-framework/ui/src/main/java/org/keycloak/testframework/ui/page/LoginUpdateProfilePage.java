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

import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 用户资料更新页面对象。
 * <p>
 * 支持 Required Action 流程中修改用户名、姓名、邮箱及自定义属性，并提供流式构建器 {@link Update}。
 */
public class LoginUpdateProfilePage extends AbstractLoginPage {

    /** 用户名输入框。 */
    @FindBy(id = "username")
    private WebElement usernameInput;

    /** 名字输入框。 */
    @FindBy(name = "firstName")
    private WebElement firstNameInput;

    /** 姓氏输入框。 */
    @FindBy(name = "lastName")
    private WebElement lastNameInput;

    /** 邮箱输入框。 */
    @FindBy(name = "email")
    private WebElement emailInput;

    /** 部门自定义属性输入框。 */
    @FindBy(name = "department")
    private WebElement departmentInput;

    /** 提交资料更新表单的按钮。 */
    @FindBy(css = "input[type=\"submit\"]")
    private WebElement submitButton;

    /** 取消应用内认证（AIA）操作的按钮。 */
    @FindBy(name = "cancel-aia")
    private WebElement cancelAIAButton;

    /** 页面级错误告警消息元素。 */
    @FindBy(css = "div[class^='pf-v5-c-alert'], div[class^='alert-error']")
    private WebElement loginAlertErrorMessage;

    /** 各字段校验错误的访问器。 */
    private final UpdateProfileErrors errorsPage;

    /**
     * 使用指定 WebDriver 构造资料更新页面对象。
     *
     * @param driver 托管 WebDriver 实例
     */
    public LoginUpdateProfilePage(ManagedWebDriver driver) {
        super(driver);
        this.errorsPage = new UpdateProfileErrors(driver);
    }

    /**
     * 更新名字与姓氏并提交表单。
     *
     * @param firstName 名字
     * @param lastName 姓氏
     */
    public void update(String firstName, String lastName) {
        prepareUpdate().firstName(firstName).lastName(lastName).submit();
    }

    /**
     * 更新名字、姓氏与邮箱并提交表单。
     *
     * @param firstName 名字
     * @param lastName 姓氏
     * @param email 邮箱
     */
    public void update(String firstName, String lastName, String email) {
        prepareUpdate().firstName(firstName).lastName(lastName).email(email).submit();
    }

    /**
     * 按属性映射更新自定义字段并提交表单。
     *
     * @param attributes 字段 id 到值的映射
     */
    public void update(Map<String, String> attributes) {
        prepareUpdate().otherProfileAttribute(attributes).submit();
    }

    /**
     * 创建流式资料更新构建器。
     *
     * @return 新的 {@link Update} 实例
     */
    public Update prepareUpdate() {
        return new Update(this);
    }

    /** 点击 AIA 取消按钮。 */
    public void cancel() {
        cancelAIAButton.click();
    }

    /**
     * 读取页面级错误告警文本。
     *
     * @return 错误内容；无错误时返回 {@code null}
     */
    public String getAlertError() {
        try {
            return loginAlertErrorMessage.getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * 读取用户名输入框当前值。
     *
     * @return 用户名
     */
    public String getUsername() {
        return readValue(By.id("username"));
    }

    /** @return 名字字段当前值 */
    public String getFirstName() {
        return readValue(By.name("firstName"));
    }

    /** @return 姓氏字段当前值 */
    public String getLastName() {
        return readValue(By.name("lastName"));
    }

    /** @return 邮箱字段当前值 */
    public String getEmail() {
        return readValue(By.name("email"));
    }

    /** @return 部门属性字段当前值 */
    public String getDepartment() {
        return readValue(By.name("department"));
    }

    /**
     * 等待元素稳定后读取其 {@code value} 属性。
     *
     * @param locator 元素定位器
     * @return 输入框值
     */
    private String readValue(By locator) {
        return driver.waiting().until(d -> {
            try {
                return d.findElement(locator).getAttribute("value");
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
    }

    /**
     * 判断部门字段是否处于可编辑状态。
     *
     * @return 可编辑返回 {@code true}
     */
    public boolean isDepartmentEnabled() {
        return departmentInput.isEnabled();
    }

    /**
     * 返回各输入字段校验错误的访问器。
     *
     * @return {@link UpdateProfileErrors} 实例
     */
    public UpdateProfileErrors getInputErrors() {
        return errorsPage;
    }

    /**
     * 读取指定字段关联标签的可见文本（去除必填星号）。
     *
     * @param fieldId 字段 id
     * @return 标签文本
     */
    public String getLabelForField(String fieldId) {
        return driver.findElement(By.cssSelector("label[for=" + fieldId + "]")).getText().replaceAll("\\s\\*$", "");
    }

    /**
     * 按 id 查找页面元素。
     *
     * @param fieldId 元素 id
     * @return 对应元素；不存在时返回 {@code null}
     */
    public WebElement getElementById(String fieldId) {
        try {
            return driver.findElement(By.id(fieldId));
        } catch (NoSuchElementException ignore) {
            return null;
        }
    }

    /** @return 用户名字段是否可见 */
    public boolean isUsernamePresent() {
        try {
            return usernameInput.isDisplayed();
        } catch (NoSuchElementException nse) {
            return false;
        }
    }

    /** @return 邮箱字段是否可见 */
    public boolean isEmailInputPresent() {
        try {
            return emailInput.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** @return 部门字段是否可见 */
    public boolean isDepartmentPresent() {
        try {
            return departmentInput.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** @return AIA 取消按钮是否可见 */
    public boolean isCancelDisplayed() {
        try {
            return cancelAIAButton.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * 向指定 id 的输入元素写入值。
     *
     * @param elementId 元素 id
     * @param value 要填入的文本
     */
    public void setAttribute(String elementId, String value) {
        WebElement element = getElementById(elementId);

        if (element != null) {
            element.clear();
            element.sendKeys(value);
        }
    }

    /**
     * 点击多值属性的“添加”按钮。
     *
     * @param elementId 属性基础 id（不含 {@code kc-add-} 前缀）
     */
    public void clickAddAttributeValue(String elementId) {
        WebElement element = getElementById("kc-add-" + elementId);

        if (element != null) {
            element.click();
        }
    }

    /**
     * 点击多值属性的“移除”按钮。
     *
     * @param elementId 属性基础 id（不含 {@code kc-remove-} 前缀）
     */
    public void clickRemoveAttributeValue(String elementId) {
        WebElement element = getElementById("kc-remove-" + elementId);

        if (element != null) {
            element.click();
        }
    }

    /**
     * 读取指定 id 输入元素的当前值。
     *
     * @param elementId 元素 id
     * @return 属性值；元素不存在时返回 {@code null}
     */
    public String getAttribute(String elementId) {
        WebElement element = getElementById(elementId);

        if (element != null) {
            return element.getAttribute("value");
        }

        return null;
    }

    /** {@inheritDoc} 资料更新页面标识为 {@code login-login-update-profile}。 */
    @Override
    public String getExpectedPageId() {
        return "login-login-update-profile";
    }

    /** 流式构建并提交资料更新表单的辅助类。 */
    public static class Update {
        /** 关联的父页面对象。 */
        private final LoginUpdateProfilePage page;
        private String username;
        private String firstName;
        private String lastName;
        private String department;
        private String email;
        private final Map<String, String> other = new LinkedHashMap<>();

        /**
         * 创建与指定页面绑定的更新构建器。
         *
         * @param page 父页面对象
         */
        protected Update(LoginUpdateProfilePage page) {
            this.page = page;
        }

        /** @param username 待写入的用户名 @return 当前构建器 */
        public Update username(String username) {
            this.username = username;
            return this;
        }

        /** @param firstName 待写入的名字 @return 当前构建器 */
        public Update firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /** @param lastName 待写入的姓氏 @return 当前构建器 */
        public Update lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        /** @param department 待写入的部门 @return 当前构建器 */
        public Update department(String department) {
            this.department = department;
            return this;
        }

        /** @param email 待写入的邮箱 @return 当前构建器 */
        public Update email(String email) {
            this.email = email;
            return this;
        }

        /**
         * 合并额外的自定义资料属性。
         *
         * @param attributes 字段 id 到值的映射
         * @return 当前构建器
         */
        public Update otherProfileAttribute(Map<String, String> attributes) {
            other.putAll(attributes);
            return this;
        }

        /** 将已设置的字段值写入表单并提交。 */
        public void submit() {
            if (username != null) {
                page.usernameInput.clear();
                page.usernameInput.sendKeys(username);
            }
            if (firstName != null) {
                page.firstNameInput.clear();
                page.firstNameInput.sendKeys(firstName);
            }
            if (lastName != null) {
                page.lastNameInput.clear();
                page.lastNameInput.sendKeys(lastName);
            }

            if (department != null) {
                page.departmentInput.clear();
                page.departmentInput.sendKeys(department);
            }

            if (email != null) {
                page.emailInput.clear();
                page.emailInput.sendKeys(email);
            }

            for (Map.Entry<String, String> entry : other.entrySet()) {
                WebElement el = page.driver.findElement(By.id(entry.getKey()));
                if (el != null) {
                    el.clear();
                    el.sendKeys(entry.getValue());
                }
            }

            page.submitButton.submit();
        }
    }

    // 管理各输入字段校验错误
    /** 读取资料更新页各字段校验错误文本的辅助类。 */
    public static class UpdateProfileErrors {

        /** 用于定位错误元素的 WebDriver。 */
        private final ManagedWebDriver driver;

        /**
         * 创建错误访问器。
         *
         * @param driver 托管 WebDriver 实例
         */
        public UpdateProfileErrors(ManagedWebDriver driver) {
            this.driver = driver;
        }

        /**
         * 按元素 id 读取错误提示文本。
         *
         * @param id 错误元素 id
         * @return 错误文本；元素不存在时返回 {@code null}
         */
        private String getTextById(String id) {
            try {
                return driver.findElement(By.id(id)).getText();
            } catch (NoSuchElementException e) {
                return null;
            }
        }

        /** @return 名字字段校验错误文本（兼容多种 id 命名） */
        public String getFirstNameError() {
            String text = getTextById("input-error-firstname");
            if (text != null) {
                return text;
            }
            return getTextById("input-error-firstName");
        }

        /** @return 姓氏字段校验错误文本（兼容多种 id 命名） */
        public String getLastNameError() {
            String text = getTextById("input-error-lastname");
            if (text != null) {
                return text;
            }
            return getTextById("input-error-lastName");
        }

        /** @return 邮箱字段校验错误文本 */
        public String getEmailError() {
            return getTextById("input-error-email");
        }

        /** @return 用户名字段校验错误文本 */
        public String getUsernameError() {
            return getTextById("input-error-username");
        }
    }
}
