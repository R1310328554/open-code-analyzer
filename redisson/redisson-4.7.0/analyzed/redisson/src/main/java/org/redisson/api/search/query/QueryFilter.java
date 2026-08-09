/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.search.query;

/**
 * {@link org.redisson.api.RSearch#search(String, String, QueryOptions)} 的查询过滤器工厂。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueryFilter {

    /**
     * 按字段名创建数值范围过滤器。
     *
     * @param fieldName 字段名
     * @return 数值过滤器
     */
    static NumericFilter numeric(String fieldName) {
        return new NumericFilterParams(fieldName);
    }

    /**
     * 按字段名创建地理坐标过滤器。
     *
     * @param fieldName 字段名
     * @return 地理过滤器
     */
    static GeoFilter geo(String fieldName) {
        return new GeoFilterParams(fieldName);
    }

}
