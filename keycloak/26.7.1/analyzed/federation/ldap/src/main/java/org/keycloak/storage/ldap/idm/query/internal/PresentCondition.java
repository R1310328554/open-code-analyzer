/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.ldap.idm.query.internal;

/**
 * <p>LDAP 存在性条件，生成 {@code attrname=*} 过滤器片段。</p>
 *
 * @author rmartinc
 */
public class PresentCondition extends NamedParameterCondition {

    /**
     * @param name LDAP 属性名
     */
    public PresentCondition(String name) {
        super(name);
    }

    /** {@inheritDoc} 追加存在性过滤器 {@code (attr=*)}。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(").append(getParameterName()).append("=*)");
    }

    @Override
    public String toString() {
        return "PresentCondition{"
                + "paramName=" + getParameterName()
                + '}';
    }
}
