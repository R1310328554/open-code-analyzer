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
package org.redisson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 延迟队列/优先级队列的跨节点 {@link QueueTransferTask} 注册表。
 * <p>同名任务共享实例并通过引用计数复用；计数归零时停止任务。
 *
 * @author Nikita Koksharov
 */
public class QueueTransferService {

    private final Map<String, QueueTransferTask> tasks = new ConcurrentHashMap<>();
    
    /** 注册或复用名为 {@code name} 的转移任务；首次调用时 {@link QueueTransferTask#start()}。 */
    public void schedule(String name, QueueTransferTask task) {
        tasks.compute(name, (k, t) -> {
            if (t == null) {
                task.start();
                return task;
            }
            t.incUsage();
            return t;
        });
    }
    
    /** 递减引用计数；归零时 {@link QueueTransferTask#stop()} 并移除。 */
    public void remove(String name) {
        AtomicReference<QueueTransferTask> ref = new AtomicReference<>();
        tasks.compute(name, (k, task) -> {
            if (task == null) {
                return null;
            }

            if (task.decUsage() == 0) {
                ref.set(task);
                return null;
            }
            return task;
        });

        if (ref.get() != null) {
            ref.get().stop();
        }
    }
    
    

}
