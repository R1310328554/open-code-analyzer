/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.account;

/**
 * 同意界面中单个 OAuth/OIDC 作用域的展示信息，含协议、描述及本地化显示文本。
 */
public class ConsentScopeRepresentation {

    /** 作用域内部 ID。 */
    private String id;
    /** 作用域名称（如 {@code profile}、{@code email}）。 */
    private String name;
    /** 作用域详细描述。 */
    private String description;
    /** 所属协议（如 openid-connect）。 */
    private String protocol;
    /** 面向用户展示的本地化文本。 */
    private String displayText;

    /** 默认无参构造器。 */
    public ConsentScopeRepresentation() {
    }

    /**
     * 构造含 ID、名称与显示文本的作用域表示。
     *
     * @param id 作用域 ID
     * @param name 作用域名称
     * @param displayText 显示文本
     */
    public ConsentScopeRepresentation(String id, String name, String displayText) {
        this.id = id;
        this.name = name;
        this.displayText = displayText;
    }

    /**
     * 构造包含全部字段的作用域表示。
     *
     * @param id 作用域 ID
     * @param name 作用域名称
     * @param description 描述
     * @param protocol 协议
     * @param displayText 显示文本
     */
    public ConsentScopeRepresentation(String id, String name, String description, String protocol, String displayText) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.protocol = protocol;
        this.displayText = displayText;
    }

    /** @return 作用域 ID */
    public String getId() {
        return id;
    }

    /** @param id 作用域 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 作用域名称 */
    public String getName() {
        return name;
    }

    /** @param name 作用域名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 作用域描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 作用域描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 所属协议 */
    public String getProtocol() {
        return protocol;
    }

    /** @param protocol 所属协议 */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** @return 本地化显示文本 */
    public String getDisplayText() {
        return displayText;
    }

    /** @param displayText 本地化显示文本 */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * @deprecated Use {@link #getDisplayText()} instead. This method will be removed in KC 27.0.
     * @return 显示文本（拼写错误的遗留 getter）
     */
    @Deprecated
    public String getDisplayTest() {
        return displayText;
    }

    /**
     * @deprecated Use {@link #setDisplayText(String)} instead. This method will be removed in KC 27.0.
     * @param displayTest 显示文本（拼写错误的遗留 setter）
     */
    @Deprecated
    public void setDisplayTest(String displayTest) {
        this.displayText = displayTest;
    }
}
