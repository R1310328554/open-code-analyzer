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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索性能剖析（Profile）信息的扁平列表转 Map 工具类。
 * <p>
 * 供 {@link SearchProfileResultDecoder} 与 {@link AggregateProfileResultDecoder}
 * 共用：将交替排列的 key/value 扁平列表转为 {@link Map}。
 * 该编码既用于 {@code FT.PROFILE} 的 profile 信息段，也用于经
 * {@link ObjectListReplayDecoder} 展平后的 RESP3 Map。
 *
 * @author Nikita Koksharov
 *
 */
final class ProfileInfoDecoder {

    /** 工具类禁止实例化。 */
    private ProfileInfoDecoder() {
    }

    /** 按步长 2 遍历 parts，将 [k0,v0,k1,v1,...] 聚合为 LinkedHashMap。 */
    static Map<String, Object> toMap(List<Object> parts) {
        if (parts == null || parts.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < parts.size(); i += 2) {
            Object key = parts.get(i);
            if (key == null) {
                continue;
            }
            result.put(key.toString(), parts.get(i + 1));
        }
        return result;
    }

}
