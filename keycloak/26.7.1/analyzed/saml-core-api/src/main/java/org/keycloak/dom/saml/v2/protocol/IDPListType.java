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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for IDPListType complex type.
 * SAML 2.0 IdP 列表，供 {@link ScopingType} 指定可咨询或代理的身份提供者集合。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="IDPListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}IDPEntry" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}GetComplete" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class IDPListType {

    /** IdP 条目列表。 */
    protected List<IDPEntryType> idpEntry = new ArrayList<>();
    /** 获取完整 IdP 列表的 URI（GetComplete）。 */
    protected URI getComplete;

    /**
     * 添加 IdP 条目。
     *
     * Add an idp entry
     *
     * @param entry
     */
    public void addIDPEntry(IDPEntryType entry) {
        this.idpEntry.add(entry);
    }

    /**
     * 移除 IdP 条目。
     *
     * Remove an idp entry
     *
     * @param entry
     */
    public void removeIDPEntry(IDPEntryType entry) {
        this.idpEntry.remove(entry);
    }

    /**
     * 获取 IdP 条目列表（只读视图）。
     *
     * Gets the value of the idpEntry property.
     */
    public List<IDPEntryType> getIDPEntry() {
        return Collections.unmodifiableList(this.idpEntry);
    }

    /**
     * 获取 GetComplete URI 属性的值。
     *
     * Gets the value of the getComplete property.
     *
     * @return possible object is {@link String }
     */
    public URI getGetComplete() {
        return getComplete;
    }

    /**
     * 设置 GetComplete URI 属性的值。
     *
     * Sets the value of the getComplete property.
     *
     * @param value allowed object is {@link String }
     */
    public void setGetComplete(URI value) {
        this.getComplete = value;
    }

}