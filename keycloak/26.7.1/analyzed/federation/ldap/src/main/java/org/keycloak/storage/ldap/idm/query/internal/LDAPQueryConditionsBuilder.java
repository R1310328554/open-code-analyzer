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

import java.util.Arrays;

import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.Sort;

/**
 * LDAP 查询条件与排序的流式构建器，提供等值、范围、子串等常用过滤器工厂方法。
 *
 * @author Pedro Igor
 */
public class LDAPQueryConditionsBuilder {

    /** 构造等值条件 {@code (attr=value)}。 */
    public Condition equal(String parameter, Object value) {
        return new EqualCondition(parameter, value);
    }

    /** 构造严格大于条件 {@code (attr>value)}。 */
    public Condition greaterThan(String paramName, Object x) {
        throwExceptionIfNotComparable(x);
        return new GreaterThanCondition(paramName, (Comparable) x, false);
    }

    /** 构造大于等于条件 {@code (attr>=value)}。 */
    public Condition greaterThanOrEqualTo(String paramName, Object x) {
        throwExceptionIfNotComparable(x);
        return new GreaterThanCondition(paramName, (Comparable) x, true);
    }

    /** 构造严格小于条件 {@code (attr<value)}。 */
    public Condition lessThan(String paramName, Comparable x) {
        return new LessThanCondition(paramName, x, false);
    }

    /** 构造小于等于条件 {@code (attr<=value)}。 */
    public Condition lessThanOrEqualTo(String paramName, Comparable x) {
        return new LessThanCondition(paramName, x, true);
    }

    /** 构造闭区间条件 {@code (lower<=attr<=upper)}。 */
    public Condition between(String paramName, Comparable x, Comparable y) {
        return new BetweenCondition(paramName, x, y);
    }

    /** 构造 OR 组合条件；至少需提供一个子条件。 */
    public Condition orCondition(Condition... conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new ModelException("At least one condition should be provided to OR query");
        }
        return new OrCondition(conditions);
    }

    /** 构造 AND 组合条件；至少需提供一个子条件。 */
    public Condition andCondition(Condition... conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new ModelException("At least one condition should be provided to AND query");
        }
        return new AndCondition(conditions);
    }

    /** 追加调用方提供的原始 LDAP 过滤器片段。 */
    public Condition addCustomLDAPFilter(String filter) {
        filter = filter.trim();
        return new CustomLDAPFilter(filter);
    }

    /** 构造 IN 语义条件（多个等值子条件的 AND 组合）。 */
    public Condition in(String paramName, Object... x) {
        return new InCondition(paramName, x);
    }

    /** 构造存在性条件 {@code (attr=*)}。 */
    public Condition present(String paramName) {
        return new PresentCondition(paramName);
    }

    /**
     * 构造子串匹配条件 {@code (attr=[start]*[middle]*[end])}。
     *
     * <p>{@code start}、{@code middle}、{@code end} 至少一项非空；{@code middle} 数组不得含空串。</p>
     */
    public Condition substring(String paramName, String start, String[] middle, String end) {
        if ((start == null || start.isEmpty())
                && (end == null || end.isEmpty())
                && (middle == null || middle.length == 0)) {
            throw new ModelException("Invalid substring filter with no start, middle or end");
        }
        if (middle != null && middle.length > 0 && Arrays.stream(middle).filter(s -> s == null || s.isEmpty()).findAny().isPresent()) {
            throw new ModelException("Invalid substring filter with an empty string in the middle array");
        }

        return new SubstringCondition(paramName, start, middle, end);
    }

    /** 构造升序排序字段。 */
    public Sort asc(String paramName) {
        return new Sort(paramName, true);
    }

    /** 构造降序排序字段。 */
    public Sort desc(String paramName) {
        return new Sort(paramName, false);
    }

    /** 校验比较值实现了 {@link Comparable}。 */
    private void throwExceptionIfNotComparable(Object x) {
        if (!Comparable.class.isInstance(x)) {
            throw new ModelException("Query parameter value [" + x + "] must be " + Comparable.class + ".");
        }
    }
}
