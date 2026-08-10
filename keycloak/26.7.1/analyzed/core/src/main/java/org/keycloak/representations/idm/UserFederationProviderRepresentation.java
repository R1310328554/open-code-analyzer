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

package org.keycloak.representations.idm;

import java.util.Map;

/**
 * 用户联合提供者（User Federation Provider）的 REST 表示，用于配置 LDAP 等外部用户存储及其同步策略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserFederationProviderRepresentation {

    /** 联合提供者持久化 ID。 */
    private String id;
    /** 管理界面显示名称。 */
    private String displayName;
    /** 提供者 SPI 名称（如 ldap）。 */
    private String providerName;
    /** 提供者配置键值对。 */
    private Map<String, String> config;
    /** 多提供者时的优先级（数值越小越优先）。 */
    private int priority;
    /** 全量同步周期（秒）；-1 表示禁用。 */
    private int fullSyncPeriod;
    /** 增量同步周期（秒）；-1 表示禁用。 */
    private int changedSyncPeriod;
    /** 上次同步完成时间（Unix 秒时间戳）。 */
    private int lastSync;

    /** @return 联合提供者 ID */
    public String getId() {
        return id;
    }

    /** @param id 联合提供者 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @return 提供者 SPI 名称 */
    public String getProviderName() {
        return providerName;
    }

    /** @param providerName 提供者 SPI 名称 */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }


    /** @return 提供者配置 */
    public Map<String, String> getConfig() {
        return config;
    }

    /** @param config 提供者配置 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /** @return 优先级 */
    public int getPriority() {
        return priority;
    }

    /** @param priority 优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /** @return 全量同步周期（秒） */
    public int getFullSyncPeriod() {
        return fullSyncPeriod;
    }

    /** @param fullSyncPeriod 全量同步周期（秒） */
    public void setFullSyncPeriod(int fullSyncPeriod) {
        this.fullSyncPeriod = fullSyncPeriod;
    }

    /** @return 增量同步周期（秒） */
    public int getChangedSyncPeriod() {
        return changedSyncPeriod;
    }

    /** @param changedSyncPeriod 增量同步周期（秒） */
    public void setChangedSyncPeriod(int changedSyncPeriod) {
        this.changedSyncPeriod = changedSyncPeriod;
    }

    /** @return 上次同步时间戳 */
    public int getLastSync() {
        return lastSync;
    }

    /** @param lastSync 上次同步时间戳 */
    public void setLastSync(int lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserFederationProviderRepresentation that = (UserFederationProviderRepresentation) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
