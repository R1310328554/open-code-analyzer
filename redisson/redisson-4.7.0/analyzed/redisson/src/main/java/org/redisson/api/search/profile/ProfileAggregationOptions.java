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

import org.redisson.api.search.aggregate.AggregationOptions;
import org.redisson.api.search.aggregate.Expression;
import org.redisson.api.search.aggregate.GroupBy;
import org.redisson.api.search.aggregate.SortedField;

import java.util.Map;

/**
 * Aggregation options for {@code FT.PROFILE ... AGGREGATE ...}.
 * <p>
 * Inherits all the regular {@link AggregationOptions} settings and adds
 * profile-specific flags (such as {@code LIMITED}) that control behavior
 * of the profiling itself.
 *
 * @author Nikita Koksharov
 *
 */
public class ProfileAggregationOptions extends AggregationOptions {

    private boolean limited;

    protected ProfileAggregationOptions() {
    }

    /**
     * Creates a new {@link ProfileAggregationOptions} with default settings
     * (no flags set, no aggregation options).
     *
     * @return new options instance
     */
    public static ProfileAggregationOptions defaults() {
        return new ProfileAggregationOptions();
    }

    /**
     * Enables the {@code LIMITED} flag, which removes details of reader
     * iterators from the returned profile information.
     *
     * @return this
     */
    public ProfileAggregationOptions limited() {
        this.limited = true;
        return this;
    }

    /**
     * Sets the {@code LIMITED} flag, which removes details of reader
     * iterators from the returned profile information.
     *
     * @param limited whether to enable the {@code LIMITED} flag
     * @return this
     */
    public ProfileAggregationOptions limited(boolean limited) {
        this.limited = limited;
        return this;
    }

    public boolean isLimited() {
        return limited;
    }

    @Override
    public ProfileAggregationOptions withCursor() {
        super.withCursor();
        return this;
    }

    @Override
    public ProfileAggregationOptions withCursor(int count) {
        super.withCursor(count);
        return this;
    }

    @Override
    public ProfileAggregationOptions withCursor(int count, int maxIdle) {
        super.withCursor(count, maxIdle);
        return this;
    }

    @Override
    public ProfileAggregationOptions verbatim(boolean verbatim) {
        super.verbatim(verbatim);
        return this;
    }

    @Override
    public ProfileAggregationOptions load(String... attributes) {
        super.load(attributes);
        return this;
    }

    @Override
    public ProfileAggregationOptions timeout(Long timeout) {
        super.timeout(timeout);
        return this;
    }

    @Override
    public ProfileAggregationOptions loadAll() {
        super.loadAll();
        return this;
    }

    @Override
    public ProfileAggregationOptions groupBy(GroupBy... groups) {
        super.groupBy(groups);
        return this;
    }

    @Override
    public ProfileAggregationOptions sortBy(SortedField... fields) {
        super.sortBy(fields);
        return this;
    }

    @Override
    public ProfileAggregationOptions sortBy(int max, SortedField... fields) {
        super.sortBy(max, fields);
        return this;
    }

    @Override
    public ProfileAggregationOptions sortBy(boolean withCount, SortedField... fields) {
        super.sortBy(withCount, fields);
        return this;
    }

    @Override
    public ProfileAggregationOptions sortBy(int max, boolean withCount, SortedField... fields) {
        super.sortBy(max, withCount, fields);
        return this;
    }

    @Override
    public ProfileAggregationOptions apply(Expression... expressions) {
        super.apply(expressions);
        return this;
    }

    @Override
    public ProfileAggregationOptions limit(int offset, int count) {
        super.limit(offset, count);
        return this;
    }

    @Override
    public ProfileAggregationOptions filter(String filter) {
        super.filter(filter);
        return this;
    }

    @Override
    public ProfileAggregationOptions params(Map<String, Object> params) {
        super.params(params);
        return this;
    }

    @Override
    public ProfileAggregationOptions dialect(Integer dialect) {
        super.dialect(dialect);
        return this;
    }

}
