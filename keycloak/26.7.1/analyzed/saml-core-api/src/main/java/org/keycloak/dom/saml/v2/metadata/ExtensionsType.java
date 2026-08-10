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
package org.keycloak.dom.saml.v2.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.dom.saml.v2.mdattr.EntityAttributes;
import org.keycloak.dom.saml.v2.mdui.UIInfoType;

import org.w3c.dom.Element;

/**
 * <p>
 * Java class for ExtensionsType complex type.
 * SAML 2.0 扩展容器：承载任意命名空间的扩展元素。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtensionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ExtensionsType {

    protected List<Object> any = new ArrayList<>();

    /**
     * 已废弃，请使用 getAny。
     *
     * Function is obsoleted with getAny
     * @return
     */
    @Deprecated
    public Element getElement() {
        return (any.isEmpty()) ? null : (Element) any.get(0);
    }

    /**
     * 已废弃，请使用 addExtension。
     *
     * Function is obsoleted with addExtension
     * @return
     */
    @Deprecated
    public void setElement(Element element) {
        any.clear();
        any.add(element);
    }

    /**
     * 添加扩展元素。
     *
     * Add an extension
     *
     * @param extension
     */
    public void addExtension(Object extension) {
        any.add(extension);
    }

    /**
     * 移除扩展元素。
     *
     * Remove an extension
     *
     * @param extension
     */
    public void removeExtension(Object extension) {
        any.remove(extension);
    }

    /**
     * 获取只读扩展元素列表。
     *
     * Gets the value of the any property.
     */
    public List<Object> getAny() {
        return Collections.unmodifiableList(this.any);
    }

    /** 从扩展列表中筛选 DOM Element 元素。 */
    public List<Element> getDomElements() {
        List<Element> output = new ArrayList<Element>();

        for (Object o : this.any) {
            if (o instanceof Element) {
                output.add((Element) o);
            }
        }

        return Collections.unmodifiableList(output);
    }

    /** 查找并返回 EntityAttributes 扩展（若存在）。 */
    public EntityAttributes getEntityAttributes() {
        for (Object o : this.any) {
            if (o instanceof EntityAttributes) {
                return (EntityAttributes) o;
            }
        }
        return null;
    }

    /** 查找并返回 UIInfo 扩展（若存在）。 */
    public UIInfoType getUIInfo() {
        for (Object o : this.any) {
            if (o instanceof UIInfoType) {
                return (UIInfoType) o;
            }
        }
        return null;
    }

}
