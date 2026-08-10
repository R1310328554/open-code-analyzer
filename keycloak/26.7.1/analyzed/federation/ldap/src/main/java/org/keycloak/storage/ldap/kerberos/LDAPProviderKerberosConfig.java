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

package org.keycloak.storage.ldap.kerberos;

import org.keycloak.common.constants.KerberosConstants;
import org.keycloak.component.ComponentModel;
import org.keycloak.federation.kerberos.CommonKerberosConfig;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.storage.ldap.LDAPStorageProvider;

/**
 * {@link LDAPStorageProvider} 专用的 Kerberos 配置封装。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPProviderKerberosConfig extends CommonKerberosConfig {

    public LDAPProviderKerberosConfig(ComponentModel componentModel) {
        super(componentModel);
    }

    public LDAPProviderKerberosConfig(ComponentRepresentation componentRep) {
        super(componentRep);
    }

    /** 是否使用 Kerberos 进行密码认证。 */
    public boolean isUseKerberosForPasswordAuthentication() {
        return Boolean.valueOf(getConfig().getFirst(KerberosConstants.USE_KERBEROS_FOR_PASSWORD_AUTHENTICATION));
    }

    /** 返回 LDAP 中存储 Kerberos 主体名的属性名。 */
    public String getKerberosPrincipalAttribute() {
        return getConfig().getFirst(KerberosConstants.KERBEROS_PRINCIPAL_ATTRIBUTE);
    }
}
