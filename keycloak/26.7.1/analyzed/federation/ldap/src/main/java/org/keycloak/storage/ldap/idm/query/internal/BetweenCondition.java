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
 * LDAP 区间查询条件，生成 {@code (lower<=attr<=upper)} 形式的过滤器片段。
 *
 * @author Pedro Igor
 */
class BetweenCondition extends NamedParameterCondition {

    /** 区间下界（含）。 */
    private final Comparable x;
    /** 区间上界（含）。 */
    private final Comparable y;

    /**
     * @param name LDAP 属性名
     * @param x 区间下界
     * @param y 区间上界
     */
    public BetweenCondition(String name, Comparable x, Comparable y) {
        super(name);
        this.x = x;
        this.y = y;
    }

    /** {@inheritDoc} 追加区间比较过滤器。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(").append(escapeValue(x)).append("<=").append(getParameterName()).append("<=").append(escapeValue(y)).append(")");
    }
}
