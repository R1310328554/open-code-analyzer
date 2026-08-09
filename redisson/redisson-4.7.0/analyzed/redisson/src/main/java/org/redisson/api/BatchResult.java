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
package org.redisson.api;

import java.util.List;

/**
 * 批量命令执行结果。
 * <p>包含各子命令的响应列表及成功同步的从节点数量。
 *
 * @author Nikita Koksharov
 * @param <E> 单条命令响应类型
 */
public final class BatchResult<E> {

    private final List<E> responses;
    private final int syncedSlaves;
    
    public BatchResult(List<E> responses, int syncedSlaves) {
        super();
        this.responses = responses;
        this.syncedSlaves = syncedSlaves;
    }
    
    /**
     * 返回各子命令的结果对象列表。
     *
     * @return 结果对象列表
     */
    public List<E> getResponses() {
        return responses;
    }

    /**
     * 返回批量执行期间成功同步的从节点数量。
     *
     * @return 从节点数量
     */
    public int getSyncedSlaves() {
        return syncedSlaves;
    }

}
