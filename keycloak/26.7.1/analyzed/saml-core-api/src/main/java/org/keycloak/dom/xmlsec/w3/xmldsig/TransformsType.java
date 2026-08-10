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

package org.keycloak.dom.xmlsec.w3.xmldsig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for TransformsType complex type.
 * W3C XML Signature 转换算法列表容器，按顺序应用多个 {@link TransformType}。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TransformsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transform" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class TransformsType {

    /** 转换算法列表。 */
    protected List<TransformType> transform = new ArrayList<>();

    /** 添加一条转换算法。 */
    public void addTransformsType(TransformType tt) {
        this.transform.add(tt);
    }

    /** 移除一条转换算法。 */
    public void removeTransformsType(TransformType tt) {
        this.transform.remove(tt);
    }

    /**
     * 获取转换算法列表（只读视图）。
     *
     * Gets the value of the transform property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link TransformType }
     */
    public List<TransformType> getTransform() {
        return Collections.unmodifiableList(this.transform);
    }

}
