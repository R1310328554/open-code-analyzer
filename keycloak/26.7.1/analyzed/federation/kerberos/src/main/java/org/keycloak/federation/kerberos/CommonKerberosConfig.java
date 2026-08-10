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

package org.keycloak.federation.kerberos;

import org.keycloak.common.constants.KerberosConstants;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.representations.idm.ComponentRepresentation;

/**
 * Kerberos 联邦提供器共用的配置抽象基类，封装 realm、服务主体、keytab 等通用项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class CommonKerberosConfig {

    /** 用户存储组件的多值配置映射。 */
    protected MultivaluedHashMap<String, String> userStorageConfig;

    /** 从 {@link ComponentModel} 加载 Kerberos 配置。 */
    public CommonKerberosConfig(ComponentModel componentModel) {
        this.userStorageConfig = componentModel.getConfig();
    }

    /** 从 {@link ComponentRepresentation} 加载 Kerberos 配置。 */
    public CommonKerberosConfig(ComponentRepresentation componentRep) {
        this.userStorageConfig = componentRep.getConfig();
    }

    protected MultivaluedHashMap<String, String> getConfig() {
        return userStorageConfig;
    }

    // 对 KerberosFederationProvider 应始终为 true
    /** 是否允许 Kerberos（SPNEGO）认证。 */
    public boolean isAllowKerberosAuthentication() {
        return Boolean.valueOf(getConfig().getFirst(KerberosConstants.ALLOW_KERBEROS_AUTHENTICATION));
    }

    /** 返回配置的 Kerberos realm 名称。 */
    public String getKerberosRealm() {
        return getConfig().getFirst(KerberosConstants.KERBEROS_REALM);
    }

    /** 返回 Keycloak 服务主体（SPN）。 */
    public String getServerPrincipal() {
        return getConfig().getFirst(KerberosConstants.SERVER_PRINCIPAL);
    }

    /** 返回 keytab 文件路径。 */
    public String getKeyTab() {
        return getConfig().getFirst(KerberosConstants.KEYTAB);
    }

    /** 是否启用 Kerberos 调试日志。 */
    public boolean isDebug() {
        return Boolean.valueOf(getConfig().getFirst(KerberosConstants.DEBUG));
    }


}
