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

package org.keycloak.storage.ldap.idm.query.internal;

/**
 * LDAP 小于（或小于等于）查询条件，生成 {@code (attr<=value)} 或 {@code (attr<value)} 过滤器。
 *
 * @author Pedro Igor
 */
class LessThanCondition extends NamedParameterCondition {

    /** 是否包含等于（{@code <=}）。 */
    private final boolean orEqual;

    /** 比较阈值。 */
    private final Comparable value;

    /**
     * @param name LDAP 属性名
     * @param value 比较阈值
     * @param orEqual 为 {@code true} 时使用 {@code <=}
     */
    public LessThanCondition(String name, Comparable value, boolean orEqual) {
        super(name);
        this.value = value;
        this.orEqual = orEqual;
    }

    /** {@inheritDoc} 追加小于/小于等于比较过滤器。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(").append(getParameterName()).append(orEqual? "<=" : "<").append(escapeValue(value)).append(")");
    }
}
