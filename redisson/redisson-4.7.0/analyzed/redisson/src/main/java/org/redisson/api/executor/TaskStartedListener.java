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
package org.redisson.api.executor;

/**
 * 分布式任务开始执行时触发的监听器。
 * <p>
 * 在远程执行器节点真正开始运行任务逻辑前回调，可用于记录启动时间或初始化监控上下文。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface TaskStartedListener extends TaskListener {

    /**
     * 任务开始执行时调用。
     *
     * @param taskId 任务唯一标识
     */
    void onStarted(String taskId);

}
