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

package com.alibaba.nacos.naming.pojo;

import java.io.Serializable;

/**
 * 命名集群内可传输、可持久化的记录接口。
 *
 * <p>实现类（如 {@link com.alibaba.nacos.naming.misc.SwitchDomain}）通过 {@link #getChecksum()} 提供内容摘要，用于 Raft 同步与变更比对。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public interface Record extends Serializable {
    
    /**
     * 获取记录内容校验和，通常用于记录版本比对。
     *
     * @return checksum of record
     */
    String getChecksum();
}
