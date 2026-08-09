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
package org.redisson.api.search.index;

/**
 * 可排序字段值的归一化模式。
 *
 * @author Nikita Koksharov
 *
 */
public enum SortMode {

    /** 对字段值做归一化后再参与排序。 */
    NORMALIZED,

    /** 使用原始字段值排序，不做归一化。 */
    UNNORMALIZED

}
