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

import java.util.Map;

/**
 * 分布式 ID 生成器接口：提供初始化、当前 ID、workerId 及下一 ID 分配。
 * Id generator.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface IdGenerator {
    
    /**
     * 执行生成器初始化（如 workerId 分配、序列号恢复等）。
     * Perform the corresponding initialization operation.
     */
    void init();
    
    /**
     * 返回当前已分配的最大/最新 ID。
     * current id info.
     *
     * @return current id
     */
    long currentId();
    
    /**
     * 返回当前 worker 节点 ID。
     * worker id info.
     *
     * @return worker id
     */
    long workerId();
    
    /**
     * 分配并返回下一个全局唯一 ID。
     * Get next id.
     *
     * @return next id
     */
    long nextId();
    
    /**
     * 返回生成器运行时信息（如 workerId、datacenterId 等）。
     * Returns information for the current IDGenerator.
     *
     * @return {@link Map}
     */
    Map<Object, Object> info();
    
}
