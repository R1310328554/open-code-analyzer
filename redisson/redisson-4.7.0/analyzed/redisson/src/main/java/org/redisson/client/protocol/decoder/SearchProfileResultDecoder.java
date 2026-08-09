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

import org.redisson.api.search.profile.SearchProfileResult;
import org.redisson.api.search.query.Document;
import org.redisson.api.search.query.SearchResult;
import org.redisson.client.handler.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code FT.PROFILE ... SEARCH ...} 命令响应解码器。
 * <p>
 * 同时兼容 RESP2 与 RESP3 两种线格式：
 * <ul>
 *   <li>RESP2：二元数组 {@code [<search-result>, <profile-info>]}
 *       其中搜索结果为 {@code FT.SEARCH} 的位置式结构
 *       {@code [total, doc_id, [attrs], ...]}。</li>
 *   <li>RESP3：Map 结构，含 {@code "Results"} 与 {@code "Profile"} 键；
 *       Results 值本身为 Map（{@code total_results}、{@code results} 等），
 *       每条结果含 {@code id} 与 {@code extra_attributes}。</li>
 * </ul>
 * 外层 {@link UnboundedListMultiDecoder} 会将两种形状展平为嵌套 {@link List}；
 * 本解码器在运行时根据首元素类型判断协议版本并解析。
 *
 * @author Nikita Koksharov
 *
 */
public class SearchProfileResultDecoder implements MultiDecoder<Object> {

    /** 将搜索结果与性能剖析信息组装为 {@link SearchProfileResult}。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts == null || parts.isEmpty()) {
            return new SearchProfileResult(new SearchResult(0, Collections.emptyList()),
                    Collections.emptyMap());
        }

        SearchResult searchResult;
        Map<String, Object> info;

        if (isResp3Shape(parts)) {
            Map<String, Object> top = ProfileInfoDecoder.toMap(parts);
            searchResult = decodeSearchResultResp3(asList(top.get("Results")));
            info = ProfileInfoDecoder.toMap(asList(top.get("Profile")));
        } else {
            searchResult = decodeSearchResultResp2(asList(parts.get(0)));
            List<Object> profileParts;
            if (parts.size() > 1) {
                profileParts = asList(parts.get(1));
            } else {
                profileParts = Collections.emptyList();
            }
            info = ProfileInfoDecoder.toMap(profileParts);
        }

        return new SearchProfileResult(searchResult, info);
    }

    /**
     * 判断是否为 RESP3 形状。
     * <p>
     * RESP2 外层为二元位置数组，首元素本身是数组（搜索结果）；
     * RESP3 外层为 Map 展平后的键值交替序列，首元素为 {@code "Results"} 等字符串键。
     */
    private static boolean isResp3Shape(List<Object> parts) {
        // In RESP2 the outer is a 2-element positional array whose first
        // element is itself an array (the search result). In RESP3 the outer
        // is a Map flattened to alternating String keys and values, so the
        // first element is a String like "Results" or "Profile".
        return parts.get(0) instanceof String;
    }

    /** 安全地将对象转为 List，非 List 时返回空列表。 */
    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    /** 解析 RESP2 格式的 {@code FT.SEARCH} 搜索结果。 */
    @SuppressWarnings("unchecked")
    private static SearchResult decodeSearchResultResp2(List<Object> parts) {
        if (parts.isEmpty()) {
            return new SearchResult(0, Collections.emptyList());
        }

        long total = ((Number) parts.get(0)).longValue();
        List<Document> docs = new ArrayList<>();
        if (total > 0) {
            for (int i = 1; i < parts.size(); i++) {
                Object idObj = parts.get(i);
                if (idObj == null) {
                    continue;
                }
                String id = idObj.toString();
                if ((i + 1) < parts.size()) {
                    Object next = parts.get(i + 1);
                    if (next instanceof Map) {
                        Map<String, Object> attrs = (Map<String, Object>) next;
                        docs.add(new Document(id, attrs));
                        i++;
                        continue;
                    }
                    if (next instanceof List) {
                        Map<String, Object> attrs = kvListToMap((List<Object>) next);
                        docs.add(new Document(id, attrs));
                        i++;
                        continue;
                    }
                }
                docs.add(new Document(id));
            }
        }
        return new SearchResult(total, docs);
    }

    /** 解析 RESP3 格式的搜索结果 Map。 */
    @SuppressWarnings("unchecked")
    private static SearchResult decodeSearchResultResp3(List<Object> parts) {
        if (parts.isEmpty()) {
            return new SearchResult(0, Collections.emptyList());
        }

        Map<String, Object> top = ProfileInfoDecoder.toMap(parts);

        long total = 0;
        Object totalObj = top.get("total_results");
        if (totalObj instanceof Number) {
            total = ((Number) totalObj).longValue();
        }

        List<Document> docs = new ArrayList<>();
        Object resultsObj = top.get("results");
        if (resultsObj instanceof List) {
            for (Object resultObj : (List<Object>) resultsObj) {
                if (!(resultObj instanceof List)) {
                    continue;
                }
                Map<String, Object> resultMap = ProfileInfoDecoder.toMap((List<Object>) resultObj);
                Object idObj = resultMap.get("id");
                String id = null;
                if (idObj != null) {
                    id = idObj.toString();
                }
                Object attrsObj = resultMap.get("extra_attributes");
                Map<String, Object> attrs;
                if (attrsObj instanceof Map) {
                    attrs = (Map<String, Object>) attrsObj;
                } else if (attrsObj instanceof List) {
                    attrs = kvListToMap((List<Object>) attrsObj);
                } else {
                    attrs = new LinkedHashMap<>();
                }
                if (id != null) {
                    if (attrs.isEmpty()) {
                        docs.add(new Document(id));
                    } else {
                        docs.add(new Document(id, attrs));
                    }
                }
            }
        }
        return new SearchResult(total, docs);
    }

    /** 将键值交替 List 转为 {@link Map}。 */
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
