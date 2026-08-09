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
package org.redisson.client.protocol.convertor;

/**
 * 将 Redis 浮点或字符串回复转换为 {@link Double}。
 * <p>
 * 可配置 {@code null} 或空字符串时的默认返回值，用于 ZSET 分数等场景。
 *
 * @author Nikita Koksharov
 *
 */
public class DoubleReplayConvertor implements Convertor<Double> {

    /** {@code null} 或空字符串时返回的默认值。 */
    private Double nullValue;

    /** 默认构造，空值时返回 {@code null}。 */
    public DoubleReplayConvertor() {
    }

    /** @param nullValue 空回复时的替代值 */
    public DoubleReplayConvertor(Double nullValue) {
        this.nullValue = nullValue;
    }

    /** {@code null} 或空串返回 {@link #nullValue}，否则解析为 double。 */
    @Override
    public Double convert(Object obj) {
        if (obj == null || obj.toString().isEmpty()) {
            return nullValue;
        }
        return Double.valueOf(obj.toString());
    }


}
