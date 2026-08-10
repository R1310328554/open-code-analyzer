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

/**
 * SAML 1.1 属性指示符，指定属性名称与命名空间 URI。
 *
 * <complexType name="AttributeDesignatorType"> <attribute name="AttributeName" type="string" use="required"/>
 * <attribute
 * name="AttributeNamespace" type="anyURI" use="required"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeDesignatorType {

    protected String attributeName;

    protected URI attributeNamespace;

    /**
     * 构造属性指示符。
     *
     * @param attributeName 属性名称
     * @param attributeNamespace 属性命名空间 URI
     */
    public SAML11AttributeDesignatorType(String attributeName, URI attributeNamespace) {
        this.attributeName = attributeName;
        this.attributeNamespace = attributeNamespace;
    }

    /** 获取属性名称。 */
    public String getAttributeName() {
        return attributeName;
    }

    /** 获取属性命名空间 URI。 */
    public URI getAttributeNamespace() {
        return attributeNamespace;
    }
}