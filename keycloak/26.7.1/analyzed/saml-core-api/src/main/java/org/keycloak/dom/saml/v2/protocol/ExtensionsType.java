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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for ExtensionsType complex type.
 * SAML 2.0 扩展元素容器，用于在协议消息中承载任意自定义扩展内容。
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

    /** 扩展元素列表，可包含任意类型的扩展对象。 */
    protected List<Object> any = new ArrayList<>();

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
     * 获取扩展元素列表（只读视图）。
     *
     * Gets the value of the any property.
     */
    public List<Object> getAny() {
        return Collections.unmodifiableList(this.any);
    }
}