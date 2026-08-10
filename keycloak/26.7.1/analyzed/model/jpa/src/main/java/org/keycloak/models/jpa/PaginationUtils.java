/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa;

import jakarta.persistence.TypedQuery;

/**
 * JPA 查询分页工具：统一处理 first/max 参数及 Hibernate 分页边界问题。
 */
public class PaginationUtils {

    /** 未指定 max 时的默认上限，规避 HHH-14295 分页缺陷。 */
    public static final int DEFAULT_MAX_RESULTS = Integer.MAX_VALUE >> 1;

    /** 对 TypedQuery 应用 offset/limit；first 有效且 max 缺失时使用 {@link #DEFAULT_MAX_RESULTS}。 */
    public static <T> TypedQuery<T> paginateQuery(TypedQuery<T> query, Integer first, Integer max) {
        if (first != null && first >= 0) {
            query = query.setFirstResult(first);

            // Hibernate 在 setFirstResult 后必须 setMaxResults，否则可能抛出异常
            if (max == null || max < 0) {
                max = DEFAULT_MAX_RESULTS;
            }
        }

        if (max != null && max >= 0) {
            query = query.setMaxResults(max);
        }

        return query;
    }

}
