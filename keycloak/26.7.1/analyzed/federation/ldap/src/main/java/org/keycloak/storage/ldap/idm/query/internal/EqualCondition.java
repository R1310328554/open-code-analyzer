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

import org.keycloak.models.LDAPConstants;

/**
 * LDAP 等值查询条件，生成 {@code (attr=value)} 形式的过滤器片段。
 *
 * @author Pedro Igor
 */
public class EqualCondition extends NamedParameterCondition {

    /** 待比较的属性值。 */
    private Object value;

    /**
     * @param name LDAP 属性名
     * @param value 比较值
     */
    public EqualCondition(String name, Object value) {
        super(name);
        this.value = value;
    }

    /** 返回当前比较值。 */
    public Object getValue() {
        return this.value;
    }

    /** 设置比较值。 */
    public void setValue(Object value) {
        this.value = value;
    }

    /** {@inheritDoc} 追加等值比较过滤器。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(").append(getParameterName()).append(LDAPConstants.EQUAL).append(escapeValue(value)).append(")");
    }

    @Override
    public String toString() {
        return "EqualCondition{" +
                "paramName=" + getParameterName() +
                ", value=" + value +
                '}';
    }
}
