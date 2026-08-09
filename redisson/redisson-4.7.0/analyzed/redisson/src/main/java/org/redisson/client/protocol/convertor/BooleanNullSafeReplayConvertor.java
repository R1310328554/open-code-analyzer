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
 * 空安全布尔回复转换器：{@code null} 视为 {@code false}。
 * <p>
 * 成功条件为整数 {@code 1} 或字符串 {@code "OK"}，用于 {@code SET}、
 * {@code DEL}、{@code FCALL} 等需要明确成功/失败语义的命令。
 *
 * @author Nikita Koksharov
 *
 */
public class BooleanNullSafeReplayConvertor implements Convertor<Boolean> {

    /** {@code null} 返回 {@code false}；{@code 1} 或 {@code "OK"} 返回 {@code true}。 */
    @Override
    public Boolean convert(Object obj) {
        if (obj == null) {
            return false;
        }
        return Long.valueOf(1).equals(obj) || "OK".equals(obj);
    }


}
