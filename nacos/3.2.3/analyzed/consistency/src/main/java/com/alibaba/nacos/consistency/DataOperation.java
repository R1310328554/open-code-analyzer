/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.consistency;

/**
 * 一致性数据操作类型：描述状态机对 Datum 的增删改查及快照、校验动作。
 * Apply action.
 *
 * @author nkorange
 */
public enum DataOperation {
    /**
     * 新增数据。
     * Data add.
     */
    ADD,
    /**
     * 变更已有数据。
     * Data changed.
     */
    CHANGE,
    /**
     * 删除数据。
     * Data deleted.
     */
    DELETE,
    /**
     * 数据校验（对账）。
     * Data verify.
     */
    VERIFY,
    /**
     * 快照操作。
     * Data Snapshot.
     */
    SNAPSHOT,
    /**
     * 只读查询。
     * Data query.
     */
    QUERY;
}
