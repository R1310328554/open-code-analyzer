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
 * Decoder for the {@code FT.PROFILE ... SEARCH ...} command response.
 * <p>
 * Handles both RESP2 and RESP3 wire formats:
 * <ul>
 *   <li>RESP2: a 2-element array {@code [<search-result>, <profile-info>]}
 *       where the search result has the positional shape
 *       {@code [total, doc_id, [attrs], ...]} returned by {@code FT.SEARCH}.</li>
 *   <li>RESP3: a Map with {@code "Results"} and {@code "Profile"} keys; the
 *       Results value is itself a Map keyed by {@code total_results},
 *       {@code results}, etc., and each result has {@code id} and
 *       {@code extra_attributes} entries.</li>
 * </ul>
 * Both shapes are flattened to nested {@link List}s by the surrounding
 * {@link UnboundedListMultiDecoder}; this decoder examines the shape at
 * runtime to decide how to interpret the data.
 *
 * @author Nikita Koksharov
 *
 */
public class SearchProfileResultDecoder implements MultiDecoder<Object> {

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

    private static boolean isResp3Shape(List<Object> parts) {
        // In RESP2 the outer is a 2-element positional array whose first
        // element is itself an array (the search result). In RESP3 the outer
        // is a Map flattened to alternating String keys and values, so the
        // first element is a String like "Results" or "Profile".
        return parts.get(0) instanceof String;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

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
