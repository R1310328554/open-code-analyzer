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
 * Decoder for the {@code FT.PROFILE ... AGGREGATE ...} command response.
 * <p>
 * Handles both RESP2 and RESP3 wire formats:
 * <ul>
 *   <li>RESP2: a 2-element array {@code [<aggregate-result>, <profile-info>]}
 *       where the aggregate result has the positional shape
 *       {@code [total, [row], [row], ...]} returned by {@code FT.AGGREGATE}.</li>
 *   <li>RESP3: a Map with {@code "Results"} and {@code "Profile"} keys; the
 *       Results value is itself a Map keyed by {@code total_results},
 *       {@code results}, etc., and each result has an
 *       {@code extra_attributes} entry holding the row data.</li>
 * </ul>
 * Both shapes are flattened to nested {@link List}s by the surrounding
 * {@link UnboundedListMultiDecoder}; this decoder examines the shape at
 * runtime to decide how to interpret the data.
 *
 * @author Nikita Koksharov
 *
 */
public class AggregateProfileResultDecoder implements MultiDecoder<Object> {

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

    private static boolean isResp3Shape(List<Object> parts) {
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
