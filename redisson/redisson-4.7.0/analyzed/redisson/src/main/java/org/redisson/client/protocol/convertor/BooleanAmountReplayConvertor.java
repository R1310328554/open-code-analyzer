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
 * 将 Redis 整数回复转换为布尔值：大于 0 为 {@code true}，否则为 {@code false}。
 * <p>
 * 适用于 {@code ZADD}、{@code SADD}、{@code DEL} 等返回受影响元素数量的命令，
 * 表示是否至少修改了一个元素。
 *
 * @author Nikita Koksharov
 *
 */
public class BooleanAmountReplayConvertor implements Convertor<Boolean> {

    /** 受影响数量大于 0 时返回 {@code true}。 */
    @Override
    public Boolean convert(Object obj) {
        return (Long) obj > 0;
    }


}
