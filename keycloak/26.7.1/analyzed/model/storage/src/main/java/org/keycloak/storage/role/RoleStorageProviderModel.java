/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.role;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.CacheableStorageProviderModel;

/**
 * 角色存储 Provider 实例的持久化配置模型。
 */
public class RoleStorageProviderModel extends CacheableStorageProviderModel {

    /** 默认构造，Provider 类型设为 {@link RoleStorageProvider}。 */
    public RoleStorageProviderModel() {
        setProviderType(RoleStorageProvider.class.getName());
    }

    /** 从已有 ComponentModel 复制构造。 */
    public RoleStorageProviderModel(ComponentModel copy) {
        super(copy);
    }

    /** 是否启用（懒解析缓存）。 */
    private transient Boolean enabled;

    /** 设置角色存储 Provider 是否启用。 */
    @Override
    public void setEnabled(boolean flag) {
        enabled = flag;
        getConfig().putSingle(ENABLED, Boolean.toString(flag));
    }

    /** 角色存储 Provider 是否启用；未配置时默认为 true。 */
    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            String val = getConfig().getFirst(ENABLED);
            if (val == null) {
                enabled = true;
            } else {
                enabled = Boolean.valueOf(val);
            }
        }
        return enabled;

    }
}
