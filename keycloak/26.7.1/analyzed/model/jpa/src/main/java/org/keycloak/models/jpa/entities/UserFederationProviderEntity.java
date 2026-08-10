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

package org.keycloak.models.jpa.entities;

import java.util.Map;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

/**
 * User Federation Provider JPA 实体，映射 USER_FEDERATION_PROVIDER 表。
 * <p>
 * 表示 realm 级外部用户存储连接（LDAP、Kerberos 等）；{@link #priority} 决定多 provider 时的查找顺序，
 * 同步周期字段控制全量/增量 LDAP 同步调度。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
@Entity
@Table(name="USER_FEDERATION_PROVIDER")
public class UserFederationProviderEntity {

    /** Provider UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // 关联常只取 id 不加载实体，PROPERTY 访问可避免额外 SQL
    protected String id;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    /** SPI provider 实现 ID（如 ldap、kerberos）。 */
    @Column(name="PROVIDER_NAME")
    private String providerName;
    /** 多 provider 并存时的优先级（数值越小越优先）。 */
    @Column(name="PRIORITY")
    private int priority;

    /** Provider 运行时配置项（连接 URL、bind DN 等）。 */
    @ElementCollection
    @MapKeyColumn(name="NAME")
    @Column(name="VALUE")
    @CollectionTable(name="USER_FEDERATION_CONFIG", joinColumns={ @JoinColumn(name="USER_FEDERATION_PROVIDER_ID") })
    private Map<String, String> config;

    /** Admin Console 展示名称。 */
    @Column(name="DISPLAY_NAME")
    private String displayName;

    /** 全量同步间隔（秒）；-1 表示禁用。 */
    @Column(name="FULL_SYNC_PERIOD")
    private int fullSyncPeriod;
    /** 增量同步间隔（秒）；-1 表示禁用。 */
    @Column(name="CHANGED_SYNC_PERIOD")
    private int changedSyncPeriod;
    /** 上次同步完成时间戳（秒）。 */
    @Column(name="LAST_SYNC")
    private int lastSync;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getFullSyncPeriod() {
        return fullSyncPeriod;
    }

    public void setFullSyncPeriod(int fullSyncPeriod) {
        this.fullSyncPeriod = fullSyncPeriod;
    }

    public int getChangedSyncPeriod() {
        return changedSyncPeriod;
    }

    public void setChangedSyncPeriod(int changedSyncPeriod) {
        this.changedSyncPeriod = changedSyncPeriod;
    }

    public int getLastSync() {
        return lastSync;
    }

    public void setLastSync(int lastSync) {
        this.lastSync = lastSync;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof UserFederationProviderEntity)) return false;

        UserFederationProviderEntity that = (UserFederationProviderEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
