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

import java.io.Serializable;
import java.net.URI;

/**
 * <complexType name="NameIdentifierType">
 * SAML 1.1 名称标识符（NameIdentifier）DOM 类型：携带主体名称值及可选限定符与格式 URI。
 <simpleContent> <extension base="string"> <attribute name="NameQualifier"
 * type="string" use="optional"/> <attribute name="Format" type="anyURI" use="optional"/> </extension> </simpleContent>
 * </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11NameIdentifierType implements Serializable {

    /** 名称限定符，用于区分同名标识。 */
    protected String nameQualifier;

    /** 名称格式 URI（如 email、unspecified）。 */
    protected URI format;

    /** 名称标识字符串值。 */
    protected String value;

    /** 以给定字符串构造名称标识符。 */
    public SAML11NameIdentifierType(String val) {
        this.value = val;
    }

    /** 返回名称限定符。 */
    public String getNameQualifier() {
        return nameQualifier;
    }

    /** 设置名称限定符。 */
    public void setNameQualifier(String nameQualifier) {
        this.nameQualifier = nameQualifier;
    }

    /** 返回名称格式 URI。 */
    public URI getFormat() {
        return format;
    }

    /** 设置名称格式 URI。 */
    public void setFormat(URI format) {
        this.format = format;
    }

    /** 返回名称标识值。 */
    public String getValue() {
        return value;
    }
}