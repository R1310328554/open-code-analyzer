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
package org.redisson.api.map;

import java.util.List;

/**
 * {@link MapsImportArgs} 的默认实现，保存共享字段名与批量 flush 阈值。
 *
 * @author Nikita Koksharov
 *
 * @param <K> field type
 */
public final class MapsImportParams<K> implements MapsImportArgs<K> {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final List<K> fields;

    private int batchSize = DEFAULT_BATCH_SIZE;

    public MapsImportParams(List<K> fields) {
        this.fields = fields;
    }

    /** 设置自动 flush 的缓冲 Map 数量阈值。 */
    @Override
    public MapsImportArgs<K> batchSize(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize should be greater than zero");
        }
        this.batchSize = batchSize;
        return this;
    }

    /** 返回所有导入 Map 共用的字段名列表。 */
    public List<K> getFields() {
        return fields;
    }

    /** 返回触发自动 flush 的缓冲 Map 数量阈值。 */
    public int getBatchSize() {
        return batchSize;
    }

}
