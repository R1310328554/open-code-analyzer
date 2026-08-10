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
package org.keycloak.dom.saml.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for StatusDetailType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="StatusDetailType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * SAML 状态详情类型，可包含任意扩展元素以补充状态信息。
 */
public class CommonStatusDetailType implements Serializable {

    protected List<Object> any = new ArrayList<>();

    /**
     * 添加状态详情元素。
     *
     * @param obj 详情对象
     */
    public void addStatusDetail(Object obj) {
        this.any.add(obj);
    }

    /**
     * 移除状态详情元素。
     *
     * @param obj 待移除对象
     */
    public void removeStatusDetail(Object obj) {
        this.any.remove(obj);
    }

    /**
     * 获取状态详情元素列表（只读）。
     */
    public List<Object> getAny() {
        return Collections.unmodifiableList(this.any);
    }
}