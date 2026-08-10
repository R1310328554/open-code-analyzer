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

import org.keycloak.component.ComponentModel;

import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户存储 Provider 实例的持久化配置模型。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
@ProtoTypeId(65539) //see org.keycloak.Marshalling
public class UserStorageProviderModel extends CacheableStorageProviderModel {

    /** 是否将联邦用户导入本地库。 */
    public static final String IMPORT_ENABLED = "importEnabled";
    /** 全量同步周期（秒）；-1 表示禁用。 */
    public static final String FULL_SYNC_PERIOD = "fullSyncPeriod";
    /** 增量同步周期（秒）；-1 表示禁用。 */
    public static final String CHANGED_SYNC_PERIOD = "changedSyncPeriod";
    /** 上次同步时间戳配置键前缀。 */
    public static final String LAST_SYNC = "lastSync";
    /** 是否移除无效导入用户。 */
    public static final String REMOVE_INVALID_USERS_ENABLED = "removeInvalidUsersEnabled";

    /** 同步模式：全量或增量。 */
    public enum SyncMode {
        /** 全量同步。 */
        FULL,
        /** 仅同步变更用户。 */
        CHANGED;
    }

    /** 默认构造，Provider 类型设为 {@link UserStorageProvider}。 */
    public UserStorageProviderModel() {
        setProviderType(UserStorageProvider.class.getName());
    }

    /** 从已有 ComponentModel 复制构造。 */
    public UserStorageProviderModel(ComponentModel copy) {
        super(copy);
    }

    /** 全量同步周期（懒解析）。 */
    private transient Integer fullSyncPeriod;
    /** 增量同步周期（懒解析）。 */
    private transient Integer changedSyncPeriod;
    /** 是否启用导入（懒解析）。 */
    private transient Boolean importEnabled;
    /** 是否移除无效用户（懒解析）。 */
    private transient Boolean removeInvalidUsersEnabled;

    /** 是否启用将联邦用户导入 Keycloak 本地存储。 */
    public boolean isImportEnabled() {
        if (importEnabled == null) {
            importEnabled = Boolean.parseBoolean(getConfig().getFirstOrDefault(IMPORT_ENABLED, Boolean.TRUE.toString()));
        }
        return importEnabled;
    }

    /** 设置是否启用用户导入。 */
    public void setImportEnabled(boolean flag) {
        importEnabled = flag;
        getConfig().putSingle(IMPORT_ENABLED, Boolean.toString(flag));
    }


    /** 获取全量同步周期（秒）；-1 表示未配置/禁用。 */
    public int getFullSyncPeriod() {
        if (fullSyncPeriod == null) {
            String val = getConfig().getFirst(FULL_SYNC_PERIOD);
            if (val == null) {
                fullSyncPeriod = -1;
            } else {
                fullSyncPeriod = Integer.valueOf(val);
            }
        }
        return fullSyncPeriod;
    }

    /** 设置全量同步周期（秒）。 */
    public void setFullSyncPeriod(int fullSyncPeriod) {
        this.fullSyncPeriod = fullSyncPeriod;
        getConfig().putSingle(FULL_SYNC_PERIOD, Integer.toString(fullSyncPeriod));
    }

    /** 获取增量同步周期（秒）；-1 表示未配置/禁用。 */
    public int getChangedSyncPeriod() {
        if (changedSyncPeriod == null) {
            String val = getConfig().getFirst(CHANGED_SYNC_PERIOD);
            if (val == null) {
                changedSyncPeriod = -1;
            } else {
                changedSyncPeriod = Integer.valueOf(val);
            }
        }
        return changedSyncPeriod;
    }

    /** 设置增量同步周期（秒）。 */
    public void setChangedSyncPeriod(int changedSyncPeriod) {
        this.changedSyncPeriod = changedSyncPeriod;
        getConfig().putSingle(CHANGED_SYNC_PERIOD, Integer.toString(changedSyncPeriod));
    }

    /** 返回全量与增量同步中较新的上次同步时间戳。 */
    public int getLastSync() {
        return Math.max(getLastSync(SyncMode.FULL), getLastSync(SyncMode.CHANGED));
    }

    /** 按同步模式获取上次同步时间戳。 */
    public int getLastSync(SyncMode syncMode) {
        String val = getConfig().getFirst(LAST_SYNC + "_" + syncMode.name());
        if (val == null) {
            return 0;
        }
        return Integer.parseInt(val);
    }

    /** 按同步模式记录上次同步时间戳。 */
    public void setLastSync(int lastSync, SyncMode syncMode) {
        getConfig().putSingle(LAST_SYNC + "_" + syncMode.name(), Integer.toString(lastSync));
    }

    /** 同时更新全量与增量两种模式的上次同步时间。 */
    public void setLastSync(int lastSync) {
        setLastSync(lastSync, SyncMode.FULL);
        setLastSync(lastSync, SyncMode.CHANGED);
    }

    /** 是否在同步时移除已失效的导入用户；默认 true。 */
    public boolean isRemoveInvalidUsersEnabled() {
        if (removeInvalidUsersEnabled == null) {
            String val = getConfig().getFirst(REMOVE_INVALID_USERS_ENABLED);
            if (val == null) {
                removeInvalidUsersEnabled = true;
            } else {
                removeInvalidUsersEnabled = Boolean.valueOf(val);
            }
        }
        return removeInvalidUsersEnabled;
    }
}
