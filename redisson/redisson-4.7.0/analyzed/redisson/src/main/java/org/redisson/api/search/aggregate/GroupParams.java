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
package org.redisson.api.search.aggregate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link GroupBy} 的具体实现，持有分组字段与归约器列表。
 *
 * @author Nikita Koksharov
 *
 */
public final class GroupParams implements GroupBy {

    private List<String> fieldNames;
    private List<Reducer> reducers = Collections.emptyList();

    GroupParams(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    public GroupBy reducers(Reducer... reducers) {
        this.reducers = Arrays.asList(reducers);
        return this;
    }

    /** 返回分组字段名列表。 */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /** 返回挂载的归约器列表。 */
    public List<Reducer> getReducers() {
        return reducers;
    }
}
