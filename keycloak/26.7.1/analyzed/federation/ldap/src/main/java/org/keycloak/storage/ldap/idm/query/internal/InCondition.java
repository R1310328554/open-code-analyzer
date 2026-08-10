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
 * LDAP IN 查询条件，将多个等值条件以 AND 组合，模拟 SQL {@code IN} 语义。
 *
 * @author Pedro Igor
 */
class InCondition extends NamedParameterCondition {

    /** 候选值集合。 */
    private final Object[] valuesToCompare;

    /**
     * @param name LDAP 属性名
     * @param valuesToCompare 待匹配的候选值数组
     */
    public InCondition(String name, Object[] valuesToCompare) {
        super(name);
        this.valuesToCompare = valuesToCompare;
    }

    /** {@inheritDoc} 为每个候选值生成等值子条件并以 AND 连接。 */
    @Override
    public void applyCondition(StringBuilder filter) {

        filter.append("(&(");

        for (Object value : valuesToCompare) {
            filter.append("(").append(getParameterName()).append(LDAPConstants.EQUAL).append(escapeValue(value)).append(")");
        }

        filter.append("))");
    }
}
