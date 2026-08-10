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

import org.keycloak.models.UserModel;
import org.keycloak.models.utils.UserModelDelegate;

/**
 * 只读模式下的 Kerberos 用户模型委托，禁止通过 Keycloak 修改联邦用户属性。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ReadOnlyKerberosUserModelDelegate extends UserModelDelegate {

    /** 关联的 Kerberos 联邦提供器。 */
    protected KerberosFederationProvider provider;

    /**
     * @param delegate 被委托的本地用户模型
     * @param provider Kerberos 联邦提供器
     */
    public ReadOnlyKerberosUserModelDelegate(UserModel delegate, KerberosFederationProvider provider) {
        super(delegate);
        this.provider = provider;
    }

}
