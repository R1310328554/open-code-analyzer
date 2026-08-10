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

/**
 * SAML 操作（Action）类型，表示授权决策所针对的操作及其命名空间。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonActionType implements Serializable {

    protected String namespace;

    protected String value;

    /**
     * 获取命名空间 URI。
     *
     * @return 可能的值为 {@link String }
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 设置命名空间 URI。
     *
     * @param value 允许的值为 {@link String }
     */
    public void setNamespace(String value) {
        this.namespace = value;
    }

    /** 获取操作名称或标识字符串。 */
    public String getValue() {
        return value;
    }

    /** 设置操作名称或标识字符串。 */
    public void setValue(String value) {
        this.value = value;
    }
}