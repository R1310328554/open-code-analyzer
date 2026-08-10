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
package org.keycloak.dom.saml.v1.protocol;

import java.net.URI;

/**
 * SAML 1.1 认证查询（AuthenticationQuery）：请求指定主体在特定认证方法下的断言。
 *
 * <complexType name="AuthenticationQueryType"> <complexContent> <extension base="samlp:SubjectQueryAbstractType">
 * <attribute
 * name="AuthenticationMethod" type="anyURI"/> </extension>
 *
 * </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthenticationQueryType extends SAML11SubjectQueryAbstractType {

    /** 请求的认证方法 URI。 */
    protected URI authenticationMethod;

    /** 返回认证方法 URI。 */
    public URI getAuthenticationMethod() {
        return authenticationMethod;
    }

    /** 设置认证方法 URI。 */
    public void setAuthenticationMethod(URI authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
