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

package org.keycloak.storage.ldap.mappers;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPStorageProvider;

/**
 * LDAP 存储映射器管理器：根据组件模型从会话中解析并获取 {@link LDAPStorageMapper} 实例。
 * <p>
 * TODO: {@link LDAPStorageMapper} 应拆分为更多接口，由本管理器按映射器实现检查各操作（特性）是否受支持。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPStorageMapperManager {

    private final LDAPStorageProvider ldapProvider;

    /** 绑定所属的 LDAP 存储提供者。 */
    public LDAPStorageMapperManager(LDAPStorageProvider ldapProvider) {
        this.ldapProvider = ldapProvider;
    }

    /**
     * 按组件模型查找并返回对应的 LDAP 映射器。
     *
     * @param mapperModel 映射器组件配置
     * @return 已注册的映射器实例
     * @throws ModelException 找不到指定 providerId 的映射器类型时
     */
    public LDAPStorageMapper getMapper(ComponentModel mapperModel) {
        LDAPStorageMapper ldapMapper = ldapProvider.getSession().getProvider(LDAPStorageMapper.class, mapperModel);
        if (ldapMapper == null) {
            throw new ModelException("Can't find mapper type with ID: " + mapperModel.getProviderId());
        }

        return ldapMapper;
    }
}
