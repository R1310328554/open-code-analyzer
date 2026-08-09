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
 * Helper used by {@link SearchProfileResultDecoder} and
 * {@link AggregateProfileResultDecoder} to convert a flat list of alternating
 * keys and values into a {@link Map}. This encoding is used both by the
 * {@code FT.PROFILE} profile information section and by RESP3 maps that have
 * been flattened by {@link ObjectListReplayDecoder}.
 *
 * @author Nikita Koksharov
 *
 */
final class ProfileInfoDecoder {

    private ProfileInfoDecoder() {
    }

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
