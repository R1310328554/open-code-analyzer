/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store.index;

import java.util.List;

/**
 * 索引查询结果：包含物理偏移列表及索引最后更新时间信息。
 */
public class QueryOffsetResult {
    /** 查询命中的 CommitLog 物理偏移列表。 */
    private final List<Long> phyOffsets;
    /** 索引最后更新时间戳。 */
    private final long indexLastUpdateTimestamp;
    /** 索引最后更新时的物理偏移。 */
    private final long indexLastUpdatePhyoffset;

    /** 构造索引查询结果。 */
    public QueryOffsetResult(List<Long> phyOffsets, long indexLastUpdateTimestamp,
        long indexLastUpdatePhyoffset) {
        this.phyOffsets = phyOffsets;
        this.indexLastUpdateTimestamp = indexLastUpdateTimestamp;
        this.indexLastUpdatePhyoffset = indexLastUpdatePhyoffset;
    }

    /** 返回物理偏移列表。 */
    public List<Long> getPhyOffsets() {
        return phyOffsets;
    }

    /** 返回索引最后更新时间戳。 */
    public long getIndexLastUpdateTimestamp() {
        return indexLastUpdateTimestamp;
    }

    /** 返回索引最后更新的物理偏移。 */
    public long getIndexLastUpdatePhyoffset() {
        return indexLastUpdatePhyoffset;
    }
}
