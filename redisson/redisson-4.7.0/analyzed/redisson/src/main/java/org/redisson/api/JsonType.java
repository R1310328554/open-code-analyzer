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
package org.redisson.api;

/**
 * RedisJSON 文档节点类型，对应 {@code JSON.TYPE} 返回值。
 *
 * @author Nikita Koksharov
 */
public enum JsonType {

    /** 布尔类型。 */
    BOOLEAN,

    /** 字符串类型。 */
    STRING,

    /** 浮点数值类型。 */
    NUMBER,

    /** 整数类型。 */
    INTEGER,

    /** JSON 对象类型。 */
    OBJECT,

    /** JSON 数组类型。 */
    ARRAY

}
