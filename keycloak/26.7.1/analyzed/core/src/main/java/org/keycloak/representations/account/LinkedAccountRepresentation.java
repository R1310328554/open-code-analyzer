/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 账户控制台中已关联或待关联的外部身份提供者账户信息，支持按 GUI 排序字段比较。
 *
 * @author Stan Silvert
 */
public class LinkedAccountRepresentation implements Comparable<LinkedAccountRepresentation> {
    /** 该身份提供者账户是否已与本地用户关联。 */
    private boolean connected;
    /** 是否为社交登录提供者（而非联合/企业 IdP）。 */
    private boolean isSocial;
    /** 身份提供者在 realm 内的别名。 */
    private String providerAlias;
    /** 身份提供者显示名称。 */
    private String providerName;
    /** 关联账户在 IdP 侧的显示名。 */
    private String displayName;
    /** 关联账户在 IdP 侧的用户名。 */
    private String linkedUsername;

    /** UI 排序权重，不参与 JSON 序列化。 */
    @JsonIgnore
    private String guiOrder;

    /** @return IdP 侧用户名 */
    public String getLinkedUsername() {
        return linkedUsername;
    }

    /** @param userName IdP 侧用户名 */
    public void setLinkedUsername(String userName) {
        this.linkedUsername = userName;
    }

    /** @return 是否已关联 */
    public boolean isConnected() {
        return connected;
    }

    /** @param connected 是否已关联 */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /** @return 是否为社交登录提供者 */
    public boolean isSocial() {
        return this.isSocial;
    }

    /** @param isSocial 是否为社交登录提供者 */
    public void setSocial(boolean isSocial) {
        this.isSocial = isSocial;
    }

    /** @return 提供者别名 */
    public String getProviderAlias() {
        return providerAlias;
    }

    /** @param providerAlias 提供者别名 */
    public void setProviderAlias(String providerAlias) {
        this.providerAlias = providerAlias;
    }

    /** @return 提供者名称 */
    public String getProviderName() {
        return providerName;
    }

    /** @param providerName 提供者名称 */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /** @return GUI 排序权重 */
    public String getGuiOrder() {
        return guiOrder;
    }

    /** @param guiOrder GUI 排序权重 */
    public void setGuiOrder(String guiOrder) {
        this.guiOrder = guiOrder;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** 按 {@link #guiOrder} 数值升序比较；null 值排在后面。 */
    @Override
    public int compareTo(LinkedAccountRepresentation rep) {
        if (this.getGuiOrder() == null) return 1;
        if (rep.getGuiOrder() == null) return -1;

        return Integer.valueOf(this.getGuiOrder()).compareTo(Integer.valueOf(rep.getGuiOrder()));
    }

}
