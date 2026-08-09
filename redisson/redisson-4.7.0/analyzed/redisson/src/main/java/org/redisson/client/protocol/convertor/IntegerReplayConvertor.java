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
 * 整数回复转换器：将协议层 {@link Long} 窄化为 {@link Integer}。
 * <p>
 * 可通过构造函数指定 {@code null} 输入时的替代值；默认无参构造时 {@code null} 映射为 {@code null}。
 *
 * @author Nikita Koksharov
 *
 */
public class IntegerReplayConvertor implements Convertor<Integer> {

    /** 输入为 {@code null} 时返回的默认值。 */
    private Integer nullValue;
    
    /** 使用默认 {@code null} 映射。 */
    public IntegerReplayConvertor() {
    }
    
    /**
     * 指定 {@code null} 输入时的返回值。
     *
     * @param nullValue 空回复对应的整数
     */
    public IntegerReplayConvertor(Integer nullValue) {
        this.nullValue = nullValue;
    }

    /** {@code null} 返回 {@link #nullValue}，否则取 {@link Long#intValue()}。 */
    @Override
    public Integer convert(Object obj) {
        if (obj == null) {
            return nullValue;
        }
        return ((Long) obj).intValue();
    }

}
