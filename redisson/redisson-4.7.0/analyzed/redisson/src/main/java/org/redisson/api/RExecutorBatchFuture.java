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
 * 批量提交任务后返回的 Future 对象。
 * <p>可通过 {@link #getTaskFutures()} 获取各子任务的 {@link RExecutorFuture}。
 *
 * @author Nikita Koksharov
 */
public interface RExecutorBatchFuture extends RFuture<Void> {

    /**
     * 返回各子任务对应的 Future 列表。
     * 
     * @return Future 列表
     */
    List<RExecutorFuture<?>> getTaskFutures();
    
}
