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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.dom.saml.v1.assertion.SAML11AttributeDesignatorType;

/**
 * SAML 1.1 属性查询（AttributeQuery）：向断言颁发者请求指定主体的属性值。
 *
 * <complexType name="AttributeQueryType"> <complexContent> <extension base="samlp:SubjectQueryAbstractType">
 * <sequence>
 * <element ref="saml:AttributeDesignator" minOccurs="0" maxOccurs="unbounded"/> </sequence>
 *
 * <attribute name="Resource" type="anyURI" use="optional"/> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeQueryType extends SAML11SubjectQueryAbstractType {

    /** 待查询的属性指示符列表。 */
    protected List<SAML11AttributeDesignatorType> attributeDesignator = new ArrayList<>();

    /** 可选的资源 URI（属性所关联的资源）。 */
    protected URI resource;

    /** 返回资源 URI。 */
    public URI getResource() {
        return resource;
    }

    /** 设置资源 URI。 */
    public void setResource(URI resource) {
        this.resource = resource;
    }

    /** 添加属性指示符。 */
    public void add(SAML11AttributeDesignatorType sadt) {
        this.attributeDesignator.add(sadt);
    }

    /** 移除属性指示符。 */
    public boolean remove(SAML11AttributeDesignatorType sadt) {
        return this.attributeDesignator.remove(sadt);
    }

    /** 返回不可修改的属性指示符列表。 */
    public List<SAML11AttributeDesignatorType> get() {
        return Collections.unmodifiableList(attributeDesignator);
    }
}
