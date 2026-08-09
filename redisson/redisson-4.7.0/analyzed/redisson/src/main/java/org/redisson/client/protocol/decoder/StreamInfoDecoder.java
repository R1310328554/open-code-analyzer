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

import org.redisson.api.stream.StreamInfo;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.convertor.StreamIdConvertor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Stream 元信息解码器（{@code XINFO STREAM} 响应）。
 * <p>
 * 将键值交替序列还原为 Map，填充长度、基数树统计、消费组数、
 * 最后生成 ID、首尾条目等 {@link StreamInfo} 字段。
 *
 * @author Nikita Koksharov, Fabian Witte
 *
 */
public class StreamInfoDecoder implements MultiDecoder<StreamInfo<Object, Object>> {
    /** {@code XINFO STREAM} 响应中 length 字段键名。 */
    private static final String LENGTH_KEY = "length";
    /** 基数树键数量字段键名。 */
    private static final String RADIX_TREE_KEYS_KEY = "radix-tree-keys";
    /** 基数树节点数量字段键名。 */
    private static final String RADIX_TREE_NODES_KEY = "radix-tree-nodes";
    /** 消费组数量字段键名。 */
    private static final String GROUPS_KEY = "groups";
    /** 最后生成消息 ID 字段键名。 */
    private static final String LAST_GENERATED_ID_KEY = "last-generated-id";
    /** 首条消息条目字段键名。 */
    private static final String FIRST_ENTRY_KEY = "first-entry";
    /** 末条消息条目字段键名。 */
    private static final String LAST_ENTRY_KEY = "last-entry";
    /** 最大已删除条目 ID 字段键名。 */
    private static final String MAX_DELETED_ENTRY_ID = "max-deleted-entry-id";
    /** 累计写入条目数字段键名。 */
    private static final String ENTRIES_ADDED = "entries-added";
    /** 记录的首条条目 ID 字段键名。 */
    private static final String RECORDED_FIRST_ENTRY_ID = "recorded-first-entry-id";

    /** 解析键值对序列并填充 {@link StreamInfo} 各统计字段。 */
    @Override
    public StreamInfo<Object, Object> decode(List<Object> parts, State state) {
        Map<String, Object> map = IntStream.range(0, parts.size())
                                    .filter(i -> i % 2 == 0)
                                    .mapToObj(i -> parts.subList(i, i+2))
                                    .filter(p -> p.get(1) != null)
                                    .collect(Collectors.toMap(e -> (String) e.get(0), e -> e.get(1)));

        StreamInfo<Object, Object> info = new StreamInfo<>();
        info.setLength(((Long) map.get(LENGTH_KEY)).intValue());
        info.setRadixTreeKeys(((Long) map.get(RADIX_TREE_KEYS_KEY)).intValue());
        info.setRadixTreeNodes(((Long) map.get(RADIX_TREE_NODES_KEY)).intValue());
        info.setGroups(((Long) map.get(GROUPS_KEY)).intValue());
        info.setLastGeneratedId(StreamIdConvertor.INSTANCE.convert(map.get(LAST_GENERATED_ID_KEY)));
        info.setMaxDeletedEntryId(StreamIdConvertor.INSTANCE.convert(map.getOrDefault(MAX_DELETED_ENTRY_ID, "0-0")));
        info.setRecordedFirstEntryId(StreamIdConvertor.INSTANCE.convert(map.getOrDefault(RECORDED_FIRST_ENTRY_ID, "0-0")));
        info.setEntriesAdded(((Long) map.getOrDefault(ENTRIES_ADDED, -1L)).intValue());

        List<?> firstEntry = (List<?>) map.get(FIRST_ENTRY_KEY);
        if (firstEntry != null) {
            StreamInfo.Entry<Object, Object> first = createStreamInfoEntry(firstEntry);
            info.setFirstEntry(first);
        }

        List<?> lastEntry = (List<?>) map.get(LAST_ENTRY_KEY);
        if (lastEntry != null) {
            StreamInfo.Entry<Object, Object> last = createStreamInfoEntry(lastEntry);
            info.setLastEntry(last);
        }
        return info;
    }

    /** 从 {@code [messageId, fieldMap]} 二元组构造 Stream 条目。 */
    private StreamInfo.Entry<Object, Object> createStreamInfoEntry(List<?> fieldValue) {
        StreamMessageId id = StreamIdConvertor.INSTANCE.convert(fieldValue.get(0));
        Map<Object, Object> data = (Map<Object, Object>) fieldValue.get(1);
        return new StreamInfo.Entry<>(id, data);
    }
}
