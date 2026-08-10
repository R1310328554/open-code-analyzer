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
package org.keycloak.dom.saml.v1.assertion;

import java.net.URI;
import javax.xml.namespace.QName;

/**
 * <complexType name="AuthorityBindingType">
 * SAML 1.1 权威绑定（AuthorityBinding）DOM 类型：标识某类 SAML 权威的服务位置与协议绑定 URI。
 <attribute name="AuthorityKind" type="QName" use="required"/> <attribute
 * name="Location" type="anyURI" use="required"/>
 *
 * <attribute name="Binding" type="anyURI" use="required"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthorityBindingType {

    /** 权威种类（AuthorityKind）QName。 */
    protected QName authorityKind;

    /** 权威服务端点位置 URI。 */
    protected URI location;

    /** 与该权威通信所用的 SAML 绑定 URI。 */
    protected URI binding;

    /** 构造权威绑定，指定种类、位置与绑定。 */
    public SAML11AuthorityBindingType(QName authorityKind, URI location, URI binding) {
        super();
        this.authorityKind = authorityKind;
        this.location = location;
        this.binding = binding;
    }

    /** 返回权威种类。 */
    public QName getAuthorityKind() {
        return authorityKind;
    }

    /** 返回权威服务位置 URI。 */
    public URI getLocation() {
        return location;
    }

    /** 返回 SAML 绑定 URI。 */
    public URI getBinding() {
        return binding;
    }
}