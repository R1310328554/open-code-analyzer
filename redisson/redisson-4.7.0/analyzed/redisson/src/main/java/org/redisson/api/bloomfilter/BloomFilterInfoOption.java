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
package org.redisson.api.bloomfilter;

/**
 * {@code BF.INFO} 命令可选返回字段枚举；{@link #getOptionString()} 为 Redis 协议字段名。
 *
 * @author Su Ko
 */
public enum BloomFilterInfoOption {
    /** 设计容量字段。 */
    CAPACITY("CAPACITY"),
    /** 位数组大小字段。 */
    SIZE("SIZE"),
    /** 子过滤器数量字段。 */
    FILTERS("FILTERS"),
    /** 已插入元素数字段。 */
    ITEMS("ITEMS"),
    /** 扩展倍率字段。 */
    EXPANSION("EXPANSION");

    private final String option;

    BloomFilterInfoOption(String option) {
        this.option = option;
    }

    /** 返回 Redis 协议中的选项字符串。 */
    public String getOptionString() {
        return option;
    }
}
