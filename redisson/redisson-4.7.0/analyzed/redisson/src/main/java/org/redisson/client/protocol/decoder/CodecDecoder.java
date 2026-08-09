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

import org.redisson.client.handler.State;

import java.util.List;

/**
 * 透传型 {@link MultiDecoder}：不对子元素做二次转换。
 * <p>
 * 直接返回已解码的 {@code parts} 列表，供上层自行处理原始结构。
 *
 * @author Nikita Koksharov
 *
 */
public class CodecDecoder implements MultiDecoder<Object> {

    /** 原样返回 RESP 数组各元素，不做类型变换。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        return parts;
    }

}
