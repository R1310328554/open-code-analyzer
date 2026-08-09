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
 * 全文索引可选的语音匹配算法及语言。
 * <p>
 * 基于 Double Metaphone 编码，用于近似发音的检索匹配。
 *
 * @author Nikita Koksharov
 *
 */
public enum PhoneticMatcher {

    /** 英语 Double Metaphone 匹配器。 */
    DM_EN("dm:en"),

    /** 法语 Double Metaphone 匹配器。 */
    DM_FR("dm:fr"),

    /** 葡萄牙语 Double Metaphone 匹配器。 */
    DM_PT("dm:pt"),

    /** 西班牙语 Double Metaphone 匹配器。 */
    DM_ES("dm:es");

    private final String value;

    PhoneticMatcher(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
