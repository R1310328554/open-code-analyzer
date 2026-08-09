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
package org.redisson.client.protocol.decoder;

import org.redisson.api.array.ArrayInfo;
import org.redisson.client.handler.State;

import java.util.List;
import java.util.Map;

/**
 * Redis Array 类型元信息解码器。
 * <p>
 * 将 {@code ARRAY INFO} 等命令返回的键值对列表解析为 {@link ArrayInfo}，
 * 基础字段由父类 {@link AbstractArrayInfoDecoder#populateBase} 填充。
 *
 * @author lamnt2008
 *
 */
public class ArrayInfoDecoder extends AbstractArrayInfoDecoder implements MultiDecoder<ArrayInfo> {

    /** 将 RESP 数组段转为键值 Map 并填充 {@link ArrayInfo}。 */
    @Override
    public ArrayInfo decode(List<Object> parts, State state) {
        Map<String, Object> map = toMap(parts);

        ArrayInfo info = new ArrayInfo();
        populateBase(map, info);
        return info;
    }

}
