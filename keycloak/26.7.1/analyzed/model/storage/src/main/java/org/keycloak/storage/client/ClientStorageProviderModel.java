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

package org.keycloak.storage.client;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.CacheableStorageProviderModel;

/**
 * 客户端存储 Provider 实例的配置模型，封装启用状态及缓存相关设置。
 *
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
public class ClientStorageProviderModel extends CacheableStorageProviderModel {

    /** 配置项键：Provider 是否启用。 */
    public static final String ENABLED = "enabled";

    /** 创建默认客户端存储 Provider 配置，并设置 Provider 类型。 */
    public ClientStorageProviderModel() {
        setProviderType(ClientStorageProvider.class.getName());
    }

    /** 从已有 {@link ComponentModel} 拷贝构造配置模型。 */
    public ClientStorageProviderModel(ComponentModel copy) {
        super(copy);
    }

    /** 启用状态的内存缓存，避免重复解析配置。 */
    private transient Boolean enabled;

    /**
     * 设置 Provider 是否启用，并同步写入组件配置。
     *
     * @param flag 启用为 {@code true}，禁用为 {@code false}
     */
     public void setEnabled(boolean flag) {
        enabled = flag;
        getConfig().putSingle(ENABLED, Boolean.toString(flag));
    }

    /**
     * 判断 Provider 是否启用；配置未设置时默认为启用。
     *
     * @return 启用返回 {@code true}
     */
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
