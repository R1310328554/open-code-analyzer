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

import java.util.Arrays;

/**
 * <p>LDAP 子串匹配条件，例如 {@code attrname=*some*thing*}。</p>
 *
 * <p>过滤器格式为 {@code attrname=[start]*[middle1]*[middle2]*[middleN]*[end]}；
 * {@code start}、{@code middle}、{@code end} 至少一项非空，且 {@code middle} 数组不得含空串。</p>
 *
 * @author rmartinc
 */
public class SubstringCondition extends NamedParameterCondition {

    /** 前缀子串。 */
    private final String start;
    /** 中间子串数组。 */
    private final String[] middle;
    /** 后缀子串。 */
    private final String end;

    /**
     * @param name LDAP 属性名
     * @param start 前缀子串，可为空
     * @param middle 中间子串数组，可为空
     * @param end 后缀子串，可为空
     */
    public SubstringCondition(String name, String start, String[] middle, String end) {
        super(name);
        this.start = start;
        this.middle = middle;
        this.end = end;
    }

    /** {@inheritDoc} 按 start/middle/end 拼接子串过滤器。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(").append(getParameterName()).append("=");
        if (start != null && !start.isEmpty()) {
            filter.append(escapeValue(start));
        }
        filter.append("*");
        if (middle != null && middle.length > 0) {
            Arrays.stream(middle).forEach(s -> filter.append(escapeValue(s)).append("*"));
        }
        if (end != null && !end.isEmpty()) {
            filter.append(escapeValue(end));
        }
        filter.append(")");
    }

    @Override
    public String toString() {
        return "PresentCondition{"
                + "paramName=" + getParameterName()
                + ", start=" + start
                + ", middle=" + (middle == null? "null" : Arrays.asList(middle))
                + ", end=" + end
                + '}';
    }
}
