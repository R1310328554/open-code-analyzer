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
 * 将 Redis 回复转换为布尔值：{@code null} 为 {@code true}，非空为 {@code false}。
 * <p>
 * 用于检测 Redis 是否返回了空/nil 回复（例如键不存在场景）。
 *
 * @author Nikita Koksharov
 *
 */
public class BooleanNullReplayConvertor implements Convertor<Boolean> {

    /** 回复为 {@code null} 时返回 {@code true}。 */
    @Override
    public Boolean convert(Object obj) {
        return obj == null;
    }


}
