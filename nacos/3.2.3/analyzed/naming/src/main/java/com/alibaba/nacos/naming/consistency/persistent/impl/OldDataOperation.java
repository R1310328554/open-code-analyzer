/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.consistency.persistent.impl;

/**
 * 旧版持久化数据操作类型枚举。
 *
 * <p>适配 {@link com.alibaba.nacos.naming.consistency.persistent.impl.BasePersistentServiceProcessor.Op}，用于兼容历史 Raft 日志中的操作描述。</p>
 *
 * @author xiweng.yy
 */
public enum OldDataOperation {
    
    /** 写入操作。 */
    Write("Write"),
    
    /** 读取操作。 */
    Read("Read"),
    
    /** 删除操作。 */
    Delete("Delete");
    
    /** 操作类型的可读描述字符串。 */
    private final String desc;
    
    OldDataOperation(String desc) {
        this.desc = desc;
    }
    
    /** 返回操作描述。 */
    public String getDesc() {
        return desc;
    }
}
