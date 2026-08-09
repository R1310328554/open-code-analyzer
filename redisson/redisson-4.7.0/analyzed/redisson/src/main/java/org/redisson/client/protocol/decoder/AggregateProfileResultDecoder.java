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

import org.redisson.api.search.aggregate.AggregationResult;
import org.redisson.api.search.profile.AggregateProfileResult;
import org.redisson.client.handler.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code FT.PROFILE ... AGGREGATE ...} 命令回复解码器。
 * <p>
 * 同时支持 RESP2 与 RESP3 两种线格式：
 * <ul>
 *   <li>RESP2：二元数组 {@code [<聚合结果>, <性能剖析信息>]，聚合部分为
 *       {@code FT.AGGREGATE} 的位置式结构 {@code [total, [row], [row], ...]}。</li>
 *   <li>RESP3：含 {@code "Results"} 与 {@code "Profile"} 键的 Map；Results 内嵌
 *       {@code total_results}、{@code results} 等字段，每行数据位于
 *       {@code extra_attributes} 中。</li>
 * </ul>
 * 外层 {@link UnboundedListMultiDecoder} 会将两种格式展平为嵌套 {@link List}；
 * 本解码器在运行时根据首元素类型判断具体格式。
 *
 * @author Nikita Koksharov
 *
 */
public class AggregateProfileResultDecoder implements MultiDecoder<Object> {

    /** 解析聚合结果与性能剖析信息，组装为 {@link AggregateProfileResult}。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts == null || parts.isEmpty()) {
            return new AggregateProfileResult(
                    new AggregationResult(0, Collections.emptyList()),
                    Collections.emptyMap());
        }

        AggregationResult aggregationResult;
        Map<String, Object> info;

        if (isResp3Shape(parts)) {
            Map<String, Object> top = ProfileInfoDecoder.toMap(parts);
            aggregationResult = decodeAggregationResultResp3(asList(top.get("Results")));
            info = ProfileInfoDecoder.toMap(asList(top.get("Profile")));
        } else {
            aggregationResult = decodeAggregationResultResp2(asList(parts.get(0)));
            List<Object> profileParts;
            if (parts.size() > 1) {
                profileParts = asList(parts.get(1));
            } else {
                profileParts = Collections.emptyList();
            }
            info = ProfileInfoDecoder.toMap(profileParts);
        }

        return new AggregateProfileResult(aggregationResult, info);
    }

    /** RESP3 格式下首元素为键名字符串，据此与 RESP2 区分。 */
    private static boolean isResp3Shape(List<Object> parts) {
        return parts.get(0) instanceof String;
    }

    /** 安全地将对象转为 {@link List}，非列表时返回空列表。 */
    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    /** 解析 RESP2 位置式聚合结果：首元素为总数，后续为各行属性 Map 或键值列表。 */
    @SuppressWarnings("unchecked")
    private static AggregationResult decodeAggregationResultResp2(List<Object> parts) {
        if (parts.isEmpty()) {
            return new AggregationResult(0, Collections.emptyList());
        }

        long total = ((Number) parts.get(0)).longValue();
        List<Map<String, Object>> attributes = new ArrayList<>();
        if (total > 0) {
            for (int i = 1; i < parts.size(); i++) {
                Object item = parts.get(i);
                if (item instanceof Map) {
                    attributes.add((Map<String, Object>) item);
                } else if (item instanceof List) {
                    attributes.add(kvListToMap((List<Object>) item));
                }
            }
        }
        return new AggregationResult(total, attributes);
    }

    /** 解析 RESP3 映射式聚合结果，从 {@code total_results} 与 {@code results} 提取行数据。 */
    @SuppressWarnings("unchecked")
    private static AggregationResult decodeAggregationResultResp3(List<Object> parts) {
        if (parts.isEmpty()) {
            return new AggregationResult(0, Collections.emptyList());
        }

        Map<String, Object> top = ProfileInfoDecoder.toMap(parts);

        long total = 0;
        Object totalObj = top.get("total_results");
        if (totalObj instanceof Number) {
            total = ((Number) totalObj).longValue();
        }

        List<Map<String, Object>> attributes = new ArrayList<>();
        Object resultsObj = top.get("results");
        if (resultsObj instanceof List) {
            for (Object resultObj : (List<Object>) resultsObj) {
                if (!(resultObj instanceof List)) {
                    continue;
                }
                Map<String, Object> resultMap = ProfileInfoDecoder.toMap((List<Object>) resultObj);
                Object attrsObj = resultMap.get("extra_attributes");
                if (attrsObj instanceof Map) {
                    attributes.add((Map<String, Object>) attrsObj);
                } else if (attrsObj instanceof List) {
                    attributes.add(kvListToMap((List<Object>) attrsObj));
                }
            }
        }
        return new AggregationResult(total, attributes);
    }

    /** 将交替排列的键值列表转为 {@link Map}，跳过键为 {@code null} 的项。 */
    private static Map<String, Object> kvListToMap(List<Object> kvs) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        for (int j = 0; j + 1 < kvs.size(); j += 2) {
            Object k = kvs.get(j);
            if (k != null) {
                attrs.put(k.toString(), kvs.get(j + 1));
            }
        }
        return attrs;
    }

}
