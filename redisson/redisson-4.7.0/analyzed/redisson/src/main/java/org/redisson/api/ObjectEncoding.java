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
 * Redis {@code OBJECT ENCODING} 返回值枚举。
 * <p>对应命令文档：https://redis.io/docs/latest/commands/object-encoding/
 *
 * @author seakider
 */
public enum ObjectEncoding {
    /** 普通字符串编码（raw）。 */

    RAW("raw"),

    /** 64 位有符号整数区间内的整数字符串编码。 */

    INT("int"),

    /** 长度不超过 OBJ_ENCODING_EMBSTR_SIZE_LIMIT（44 字节）的嵌入式字符串编码。 */

    EMBSTR("embstr"),

    /** 旧版 list 编码，已不再使用。 */

    LINKEDLIST("linkedlist"),

    /** 小 list 的空间优化编码（Redis &lt;= 6.2）。 */

    ZIPLIST("ziplist"),

    /** 小 list 的空间优化编码（Redis &gt;= 7.0）。 */

    LISTPACK("listpack"),

    /** quicklist：由 ziplist 或 listpack 组成的链表。 */

    QUICKLIST("quicklist"),

    /** 普通 set 哈希表编码。 */

    HASHTABLE("hashtable"),

    /** 仅含整数的小 set 专用编码。 */

    INTSET("intset"),

    /** 旧版 hash 编码，已不再使用。 */

    ZIPMAP("zipmap"),

    /** 普通 sorted set 跳表编码。 */

    SKIPLIST("skiplist"),

    /** stream：由 listpack 构成的 radix tree 编码。 */

    STREAM("stream"),

    /** 键不存在。 */

    NULL("nonexistence"),

    /** Redis 新增编码类型，当前枚举尚未定义。 */

    UNKNOWN("unknown");

    private final String type;

    ObjectEncoding(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ObjectEncoding valueOfEncoding(Object object) {
        if (object == null) {
            return NULL;
        }
        String value = (String) object;
        for (ObjectEncoding encoding : ObjectEncoding.values()) {
            if (value.equals(encoding.getType()))
                return encoding;
        }
        return UNKNOWN;
    }
}
