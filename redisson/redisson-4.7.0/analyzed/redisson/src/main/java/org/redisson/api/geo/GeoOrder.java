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
package org.redisson.api.geo;

/**
 * 地理空间搜索结果的距离排序方式。
 * <p>
 * 配合 {@link OptionalGeoSearch#order(GeoOrder)} 使用，
 * {@link #ASC} 表示按距离升序（近到远），{@link #DESC} 表示降序。
 *
 * @author Nikita Koksharov
 */
public enum GeoOrder {
    /** 按距离升序排列（由近到远）。 */
    ASC,
    /** 按距离降序排列（由远到近）。 */
    DESC
}
