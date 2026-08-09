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

import org.redisson.api.CuckooFilterInfo;
import org.redisson.client.handler.State;

import java.util.List;

/**
 * {@code CF.INFO} 命令响应解码器。
 * <p>
 * 将 Redis 返回的键值交替列表解析为 {@link CuckooFilterInfo} 实例。
 *
 * @author Nikita Koksharov
 *
 */
public class CuckooFilterInfoDecoder implements MultiDecoder<CuckooFilterInfo> {

    /** 解码布谷鸟过滤器元信息响应。 */
    @Override
    public CuckooFilterInfo decode(List<Object> parts, State state) {
        return new CuckooFilterInfo(parts);
    }

}
