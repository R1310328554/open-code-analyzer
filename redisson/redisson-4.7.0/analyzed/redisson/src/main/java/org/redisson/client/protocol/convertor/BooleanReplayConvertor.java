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
 * 标准布尔回复转换器：整数 {@code 1} 或字符串 {@code "OK"} 视为成功。
 * <p>
 * 与 {@link BooleanNullSafeReplayConvertor} 不同，{@code null} 输入返回 {@code null}，
 * 保留 Redis nil 回复的三态语义。
 *
 * @author Nikita Koksharov
 *
 */
public class BooleanReplayConvertor implements Convertor<Boolean> {

    /** {@code null} 原样返回；{@code 1} 或 {@code "OK"} 返回 {@code true}，否则 {@code false}。 */
    @Override
    public Boolean convert(Object obj) {
        if (obj == null) {
            return null;
        }
        return Long.valueOf(1).equals(obj) || "OK".equals(obj);
    }


}
