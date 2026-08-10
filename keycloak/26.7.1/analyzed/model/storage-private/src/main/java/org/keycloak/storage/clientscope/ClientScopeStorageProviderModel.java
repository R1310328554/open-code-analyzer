/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.clientscope;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.CacheableStorageProviderModel;

/**
 * 客户端作用域存储 Provider 实例的配置模型：封装组件属性并继承 {@link CacheableStorageProviderModel} 的缓存策略。
 */
public class ClientScopeStorageProviderModel extends CacheableStorageProviderModel {

    /** 默认构造：将 Provider 类型设为 {@link ClientScopeStorageProvider}。 */
    public ClientScopeStorageProviderModel() {
        setProviderType(ClientScopeStorageProvider.class.getName());
    }

    /** 从已有 {@link ComponentModel} 复制构造。 */
    public ClientScopeStorageProviderModel(ComponentModel copy) {
        super(copy);
    }

    /** 启用状态缓存，避免重复解析配置。 */
    private transient Boolean enabled;

    /** 设置 Provider 是否启用，并同步写入组件配置。 */
    @Override
    public void setEnabled(boolean flag) {
        enabled = flag;
        getConfig().putSingle(ENABLED, Boolean.toString(flag));
    }

    /** 读取启用状态；配置缺失时默认为启用。 */
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
