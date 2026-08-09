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

import org.redisson.api.SortOrder;
import org.redisson.api.search.query.HighlightOptions;
import org.redisson.api.search.query.QueryFilter;
import org.redisson.api.search.query.QueryOptions;
import org.redisson.api.search.query.ReturnAttribute;
import org.redisson.api.search.query.SummarizeOptions;

import java.util.List;
import java.util.Map;

/**
 * Search query options for {@code FT.PROFILE ... SEARCH ...}.
 * <p>
 * Inherits all the regular {@link QueryOptions} settings and adds
 * profile-specific flags (such as {@code LIMITED}) that control behavior
 * of the profiling itself.
 *
 * @author Nikita Koksharov
 *
 */
public class ProfileQueryOptions extends QueryOptions {

    private boolean limited;

    protected ProfileQueryOptions() {
    }

    /**
     * Creates a new {@link ProfileQueryOptions} with default settings
     * (no flags set, no query options).
     *
     * @return new options instance
     */
    public static ProfileQueryOptions defaults() {
        return new ProfileQueryOptions();
    }

    /**
     * Enables the {@code LIMITED} flag, which removes details of reader
     * iterators from the returned profile information.
     *
     * @return this
     */
    public ProfileQueryOptions limited() {
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
    public ProfileQueryOptions limited(boolean limited) {
        this.limited = limited;
        return this;
    }

    public boolean isLimited() {
        return limited;
    }

    @Override
    public ProfileQueryOptions filters(QueryFilter... filters) {
        super.filters(filters);
        return this;
    }

    @Override
    public ProfileQueryOptions noContent(boolean noContent) {
        super.noContent(noContent);
        return this;
    }

    @Override
    public ProfileQueryOptions verbatim(boolean verbatim) {
        super.verbatim(verbatim);
        return this;
    }

    @Override
    public ProfileQueryOptions noStopwords(boolean noStopwords) {
        super.noStopwords(noStopwords);
        return this;
    }

    @Override
    public ProfileQueryOptions withScores(boolean withScores) {
        super.withScores(withScores);
        return this;
    }

    @Override
    public ProfileQueryOptions withSortKeys(boolean withSortKeys) {
        super.withSortKeys(withSortKeys);
        return this;
    }

    @Override
    public ProfileQueryOptions slop(Integer slop) {
        super.slop(slop);
        return this;
    }

    @Override
    public ProfileQueryOptions timeout(Long timeout) {
        super.timeout(timeout);
        return this;
    }

    @Override
    public ProfileQueryOptions inOrder(boolean inOrder) {
        super.inOrder(inOrder);
        return this;
    }

    @Override
    public ProfileQueryOptions language(String language) {
        super.language(language);
        return this;
    }

    @Override
    public ProfileQueryOptions expander(String expander) {
        super.expander(expander);
        return this;
    }

    @Override
    public ProfileQueryOptions scorer(String scorer) {
        super.scorer(scorer);
        return this;
    }

    @Override
    public ProfileQueryOptions explainScore(boolean explainScore) {
        super.explainScore(explainScore);
        return this;
    }

    @Override
    public ProfileQueryOptions sortBy(String sortBy) {
        super.sortBy(sortBy);
        return this;
    }

    @Override
    public ProfileQueryOptions sortOrder(SortOrder sortOrder) {
        super.sortOrder(sortOrder);
        return this;
    }

    @Override
    public ProfileQueryOptions withCount(boolean withCount) {
        super.withCount(withCount);
        return this;
    }

    @Override
    public ProfileQueryOptions limit(int offset, int count) {
        super.limit(offset, count);
        return this;
    }

    @Override
    public ProfileQueryOptions params(Map<String, Object> params) {
        super.params(params);
        return this;
    }

    @Override
    public ProfileQueryOptions dialect(Integer dialect) {
        super.dialect(dialect);
        return this;
    }

    @Override
    public ProfileQueryOptions summarize(SummarizeOptions summarize) {
        super.summarize(summarize);
        return this;
    }

    @Override
    public ProfileQueryOptions highlight(HighlightOptions highlight) {
        super.highlight(highlight);
        return this;
    }

    @Override
    public ProfileQueryOptions inKeys(List<String> inKeys) {
        super.inKeys(inKeys);
        return this;
    }

    @Override
    public ProfileQueryOptions inFields(List<String> inFields) {
        super.inFields(inFields);
        return this;
    }

    @Override
    public ProfileQueryOptions returnAttributes(ReturnAttribute... returnAttributes) {
        super.returnAttributes(returnAttributes);
        return this;
    }

    @Override
    public ProfileQueryOptions returnAttributes(List<ReturnAttribute> returnAttributes) {
        super.returnAttributes(returnAttributes);
        return this;
    }

}
