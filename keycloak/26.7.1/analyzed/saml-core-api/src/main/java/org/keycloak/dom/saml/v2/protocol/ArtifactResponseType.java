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
package org.keycloak.dom.saml.v2.protocol;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for ArtifactResponseType complex type.
 * SAML 2.0 工件响应：返回与工件关联的 SAML 消息（任意元素或对象）。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArtifactResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ArtifactResponseType extends StatusResponseType {

    /** 解析得到的 SAML 消息内容。 */
    protected Object any;

    /** 构造工件响应。 */
    public ArtifactResponseType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /** 从已有状态响应复制构造工件响应。 */
    public ArtifactResponseType(StatusResponseType srt) {
        super(srt);
    }

    /**
     * 获取 any 属性的值。
     *
     * Gets the value of the any property.
     *
     * @return possible object is {@link org.w3c.dom.Element } {@link Object }
     */
    public Object getAny() {
        return any;
    }

    /**
     * 设置 any 属性的值。
     *
     * Sets the value of the any property.
     *
     * @param value allowed object is {@link org.w3c.dom.Element } {@link Object }
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
