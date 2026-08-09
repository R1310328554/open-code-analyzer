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
 * 恒等转换器：将 Redis 回复原样转为目标类型，不做额外处理。
 * <p>
 * 适用于解码结果已与 {@code R} 兼容、仅需类型擦除的场景。
 *
 * @author Nikita Koksharov
 *
 * @param <R> type of value
 */
public class EmptyConvertor<R> implements Convertor<R> {

    /** 直接强转并返回输入对象。 */
    @Override
    public R convert(Object obj) {
        return (R) obj;
    }

}
