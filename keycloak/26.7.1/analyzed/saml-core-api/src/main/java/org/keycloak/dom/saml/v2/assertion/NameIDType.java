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
package org.keycloak.dom.saml.v2.assertion;

import java.net.URI;

import org.keycloak.common.util.StringSerialization;
import org.keycloak.common.util.StringSerialization.Deserializer;

/**
 * Represents a NameIDType
 * SAML 2.0 名称标识符：含格式 URI、SP 提供 ID 及 ID 名称限定属性。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class NameIDType extends BaseIDAbstractType {
    /*
     * <complexType name="NameIDType"> <simpleContent> <extension base="string"> <attributeGroup ref="saml:IDNameQualifiers"/>
     * <attribute name="Format" type="anyURI" use="optional"/> <attribute name="SPProvidedID" type="string" use="optional"/>
     * </extension> </simpleContent> </complexType>
     *
     * <attributeGroup name="IDNameQualifiers"> <attribute name="NameQualifier" type="string" use="optional"/> <attribute
     * name="SPNameQualifier" type="string" use="optional"/> </attributeGroup>
     */

    private String value;
    private URI format;
    private String sPProvidedID;

    /** 获取名称标识符字符串值。 */
    public String getValue() {
        return value;
    }

    /** 设置名称标识符字符串值。 */
    public void setValue(String value) {
        this.value = value;
    }

    public String getsPProvidedID() {
        return sPProvidedID;
    }

    public void setsPProvidedID(String sPProvidedID) {
        this.sPProvidedID = sPProvidedID;
    }

    /** 获取名称标识符格式 URI。 */
    public URI getFormat() {
        return format;
    }

    /** 设置名称标识符格式 URI。 */
    public void setFormat(URI format) {
        this.format = format;
    }

    public String getSPProvidedID() {
        return sPProvidedID;
    }

    public void setSPProvidedID(String sPProvidedID) {
        this.sPProvidedID = sPProvidedID;
    }

    /** 序列化为字符串以便存储或传输。 */
    public String serializeAsString() {
        return StringSerialization.serialize(
          getNameQualifier(),
          getSPNameQualifier(),
          value,
          format,
          sPProvidedID
        );
    }

    /** 从字符串反序列化为 {@link NameIDType}。 */
    public static NameIDType deserializeFromString(String s) {
        NameIDType res = new NameIDType();
        Deserializer d = StringSerialization.deserialize(s);
        res.setNameQualifier(d.next(String.class));
        res.setSPNameQualifier(d.next(String.class));
        res.setValue(d.next(String.class));
        res.setFormat(d.next(URI.class));
        res.setSPProvidedID(d.next(String.class));
        return res;
    }
}