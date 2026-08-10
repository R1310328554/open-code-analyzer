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
package org.keycloak.storage;

import java.io.Serializable;
import java.util.Objects;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.UserModel;

/**
 * 存储标识符：封装 Keycloak 内部 ID 与外部存储 provider 的外部 ID。
 * <p>本地存储 ID 为纯字符串；联邦存储 ID 格式为 {@code f:<providerId>:<externalId>}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StorageId implements Serializable {
    /** 存储 provider 组件 ID；本地存储时为 {@code null}。 */
    private final String providerId;
    /** 外部存储系统中的实体 ID。 */
    private final String externalId;


    /** 从 Keycloak ID 字符串解析存储标识。
     * @param id Keycloak 内部 ID（本地或 {@code f:provider:external} 格式） */
    public StorageId(String id) {
        if (!id.startsWith("f:")) {
            providerId = null;
            externalId = id;
        } else {
            int providerIndex = id.indexOf(':', 2);
            providerId = id.substring(2, providerIndex);
            externalId = id.substring(providerIndex + 1);
        }
    }

    /** 由 provider ID 与外部 ID 构造存储标识。
     * @param providerId 存储 provider 组件 ID（不可含冒号）
     * @param externalId 外部存储中的 ID */
    public StorageId(String providerId, String externalId) {
        if (providerId != null && providerId.contains(":")) {
            throw new IllegalArgumentException("Provider must not contain a colon (:) character");
        }
        this.providerId = providerId;
        this.externalId = externalId;
    }

    /** 是否为本地存储（非联邦）。 */
    public boolean isLocal() {
        return getProviderId() == null;
    }

    /** 返回 Keycloak 内部 ID 字符串。 */
    public String getId() {
        return providerId == null ? externalId : ("f:" + providerId + ":" + externalId);
    }

    /** 返回存储 provider 组件 ID；本地存储时为 {@code null}。 */
    public String getProviderId() {
        return providerId;
    }

    /** 返回外部存储系统中的 ID。 */
    public String getExternalId() {
        return externalId;
    }

    /**
     * 生成 {@link UserModel#getId()} 应返回的 ID 字符串。
     * generate the id string that should be returned by UserModel.getId()
     *
     * @param model
     * @param externalId id used to resolve user in external storage
     * @return
     */
    public static String keycloakId(ComponentModel model, String externalId) {
        return new StorageId(model.getId(), externalId).getId();
    }

    /** 从 Keycloak ID 提取外部 ID。
     * @param keycloakId Keycloak 内部 ID */
    public static String externalId(String keycloakId) {
        return new StorageId(keycloakId).getExternalId();
    }
    /** 从 Keycloak ID 提取存储 provider 组件 ID。
     * @param keycloakId Keycloak 内部 ID */
    public static String providerId(String keycloakId) {
        return new StorageId(keycloakId).getProviderId();
    }

    /** 判断给定 ID 是否属于本地存储。
     * @param id Keycloak 内部 ID */
    public static boolean isLocalStorage(String id) {
        return new StorageId(id).getProviderId() == null;
    }

    /**
     * 已弃用，请改用 {@link #providerId(String)}。
     * @deprecated Use {@link #providerId(String)} instead.
     */
    public static String resolveProviderId(UserModel user) {
        return providerId(user.getId());
    }

    /**
     * 已弃用，请改用 {@link #isLocalStorage(String)}。
     * @deprecated Use {@link #isLocalStorage(String)} instead.
     */
    public static boolean isLocalStorage(UserModel user) {
        return isLocalStorage(user.getId());
    }

    /**
     * @deprecated Use {@link #providerId(String)} instead.
     */
    public static String resolveProviderId(ClientModel client) {
        return providerId(client.getId());
    }

    /**
     * @deprecated Use {@link #isLocalStorage(String)} instead.
     */
    public static boolean isLocalStorage(ClientModel client) {
        return isLocalStorage(client.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, externalId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StorageId other = (StorageId) obj;
        if ( ! Objects.equals(this.providerId, other.providerId)) {
            return false;
        }
        if ( ! Objects.equals(this.externalId, other.externalId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getId();
    }

}
