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

package org.keycloak.provider;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration property metadata.  Used to render generic configuration pages for Keycloak extensions in the admin console.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ProviderConfigProperty {
    /** 布尔类型。 */
    public static final String BOOLEAN_TYPE="boolean";

    /**
     * 整数值类型。
     * Integral Value
     */
    public static final String INTEGER_TYPE="Integer";

    /**
     * 任意数值类型（整数或浮点）。
     * Arbitrary number, e.g. integral, floating-point.
     */
    public static final String NUMBER_TYPE="Number";

    /** 字符串类型。 */
    public static final String STRING_TYPE="String";

    /**
     * 可配置多个任意字符串值（类似客户端 redirect_uris）。
     * Possibility to configure multiple String values of any value (something like "redirect_uris" for clients)
     */
    public static final String MULTIVALUED_STRING_TYPE="MultivaluedString";

    /** 脚本类型。 */
    public static final String SCRIPT_TYPE="Script";
    /** 文件类型。 */
    public static final String FILE_TYPE="File";
    /** 角色类型。 */
    public static final String ROLE_TYPE="Role";
    /** 组类型。 */
    public static final String GROUP_TYPE="Group";

    /**
     * 从预定义列表中选择单个字符串值（HTML select）。
     * Possibility to configure single String value, which needs to be chosen from the list of predefined values (HTML select)
     */
    public static final String LIST_TYPE="List";

    /**
     * 从预定义列表中选择多个字符串值（多选 select）。
     * Possibility to configure multiple String values, which needs to be chosen from the list of predefined values (HTML select with multiple)
     */
    public static final String MULTIVALUED_LIST_TYPE="MultivaluedList";

    /** 客户端列表类型。 */
    public static final String CLIENT_LIST_TYPE="ClientList";

    /**
     * 从用户 profile 属性中选择，也允许自定义值。
     * Possibility to select from user attributes defined in the user-profile, but also still have an option to configure custom value
     */
    public static final String USER_PROFILE_ATTRIBUTE_LIST_TYPE="UserProfileAttributeList";
    /** 密码类型。 */
    public static final String PASSWORD="Password";

    /**
     * 多行文本字段。
     * textarea field
     */
    public static final String TEXT_TYPE="Text";

    /**
     * 配置多个键值对。
     * Configure multiple (key, value) pairs
     */
    public static final String MAP_TYPE ="Map";

    /**
     * URL 字段。
     * URL field
     */
    public static final String URL_TYPE ="Url";

    /** IdP 多选列表类型（仅管理控制台）。 */
    public static final String IDENTITY_PROVIDER_MULTI_LIST_TYPE="IdentityProviderMultiList"; // only in admin console, not in themes

    /**
     * 钱包应用声明显示元数据（用户友好的 claim 名称）。
     * Display metadata for wallet applications to show user-friendly claim names
     */
    public static final String CLAIM_DISPLAY_TYPE="ClaimDisplay";

    protected String name;
    protected String label;
    protected String helpText;
    protected String type = STRING_TYPE;
    protected Object defaultValue;
    protected List<String> options;
    protected boolean secret;
    protected boolean required;
    private boolean readOnly;

    /** 默认构造。 */
    public ProviderConfigProperty() {
    }

    /** @param name 配置名
     * @param label 标签
     * @param helpText 帮助文本
     * @param type 类型
     * @param defaultValue 默认值 */
    public ProviderConfigProperty(String name, String label, String helpText, String type, Object defaultValue) {
        this.name = name;
        this.label = label;
        this.helpText = helpText;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public ProviderConfigProperty(String name, String label, String helpText, String type, Object defaultValue, String... options) {
        this.name = name;
        this.label = label;
        this.helpText = helpText;
        this.type = type;
        this.defaultValue = defaultValue;
        this.options = Arrays.asList(options);
    }

    public ProviderConfigProperty(String name, String label, String helpText, String type, Object defaultValue, boolean secret) {
        this(name, label, helpText, type, defaultValue);
        this.secret = secret;
    }

    public ProviderConfigProperty(String name, String label, String helpText, String type, Object defaultValue, boolean secret, boolean required) {
        this(name, label, helpText, type, defaultValue, secret);
        this.required = required;
    }

    /**
     * 存储在数据库中的配置变量名。
     * Name of the config variable stored in the database
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /** @param name 配置名 */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 管理控制台中显示的配置标签。
     * Label shown in the admin console when configuring the variable
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /** @param label 标签 */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 变量类型（boolean、string 等，见本类常量）。
     * Type of the variable.  i.e. boolean, string etc.  See the constants declared in this class for what your choices
     * are.
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /** @param type 类型 */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 变量默认值。
     * Default value for the variable
     *
     * @return
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /** @param defaultValue 默认值 */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * 列表类型的可选值列表。
     * For list types, this is a list of choices to choose from.
     *
     * @return
     */
    public List<String> getOptions() {
        return options;
    }

    /** @param options 可选值列表 */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    /**
     * 管理控制台 tooltip 中显示的帮助文本。
     * Help text that will be displayed in the admin console tooltip
     *
     * @return
     */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /**
     * 若为 true，该变量仅可写不可读（如密码字段）。
     * If true, this variable is only writeable.  It will never be viewable.  This is important for things like
     * passwords in which you never want to display them on the screen.
     *
     * @return
     */
    public boolean isSecret() {
        return secret;
    }

    /** @param secret 是否仅写 */
    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    /**
     * 若为 true，该配置属性为必填。
     * If true, the configuration property must be specified
     */
    public boolean isRequired() {
        return required;
    }

    /** @param required 是否必填 */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /** @param readOnly 是否只读 */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** @return 是否只读 */
    public boolean isReadOnly() {
        return readOnly;
    }
}
