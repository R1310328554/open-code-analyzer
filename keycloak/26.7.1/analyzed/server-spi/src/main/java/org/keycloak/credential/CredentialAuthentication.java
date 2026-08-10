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
package org.keycloak.credential;

import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.RealmModel;

/**
 * 凭据认证 SPI：在用户未知时从凭据中提取身份并完成认证（如 Kerberos）。
 * Single purpose method that knows how to authenticate a user based on a credential type.  This is used when the user
 * is not known but the provider knows how to extract this information from the credential.  Examples are Kerberos.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CredentialAuthentication {
    /** 是否支持指定类型的凭据认证。 */
    boolean supportsCredentialAuthenticationFor(String type);
    /** 在 realm 中根据 {@link CredentialInput} 执行认证。 */
    CredentialValidationOutput authenticate(RealmModel realm, CredentialInput input);
}
