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
package org.redisson.api.search.profile;

import org.redisson.api.search.aggregate.AggregationResult;

import java.util.Map;

/**
 * Result object returned by
 * {@link org.redisson.api.RSearch#profileAggregate(String, String, org.redisson.api.search.aggregate.AggregationOptions)} method
 * and its overloads. Wraps the {@link AggregationResult} produced by the underlying
 * {@code FT.AGGREGATE} call together with the performance information collected by
 * the {@code FT.PROFILE} command.
 *
 * @author Nikita Koksharov
 *
 */
public final class AggregateProfileResult {

    private final AggregationResult result;

    private final Map<String, Object> info;

    public AggregateProfileResult(AggregationResult result, Map<String, Object> info) {
        this.result = result;
        this.info = info;
    }

    /**
     * Returns the result of the underlying {@code FT.AGGREGATE} call.
     *
     * @return aggregation result
     */
    public AggregationResult getResult() {
        return result;
    }

    /**
     * Returns profile information collected by the {@code FT.PROFILE} command.
     * <p>
     * The map is keyed by the top-level section name as returned by the
     * server. For Redis Stack 8 and later this is typically {@code "Shards"}
     * (a list of per-shard profile data) and {@code "Coordinator"} (data
     * from the coordinator node, if any). Per-shard entries themselves
     * contain timings (e.g. {@code "Total profile time"}, {@code "Parsing
     * time"}, {@code "Pipeline creation time"}), the iterators tree under
     * {@code "Iterators profile"}, and the per-stage result processors data
     * under {@code "Result processors profile"}.
     * <p>
     * Values are returned as raw lists or primitive values; the nested
     * structure mirrors the array layout returned by the server. Concrete
     * keys and value shapes depend on the Redis Stack version and may
     * evolve over time.
     *
     * @return profile information
     */
    public Map<String, Object> getInfo() {
        return info;
    }

}
