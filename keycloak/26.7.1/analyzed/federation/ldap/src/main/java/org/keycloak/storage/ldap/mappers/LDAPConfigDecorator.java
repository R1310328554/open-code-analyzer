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
import org.keycloak.storage.ldap.LDAPConfig;

/**
 * LDAP 配置装饰器：映射器可在 LDAP 联邦提供者初始化时修改 {@link LDAPConfig}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface LDAPConfigDecorator {

    /**
     * 根据映射器组件配置更新 LDAP 连接/同步相关设置。
     *
     * @param ldapConfig 待修改的 LDAP 配置
     * @param mapperModel 映射器组件模型
     */
    void updateLDAPConfig(LDAPConfig ldapConfig, ComponentModel mapperModel);

}
