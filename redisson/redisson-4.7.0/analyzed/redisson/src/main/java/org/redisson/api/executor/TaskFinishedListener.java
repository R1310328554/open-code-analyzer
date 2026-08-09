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
 * 分布式任务执行完成时触发的监听器。
 * <p>
 * 无论任务成功或失败，只要执行结束都会回调 {@link #onFinished(String)}。
 * 可与 {@link TaskStartedListener}、{@link TaskSuccessListener} 等组合使用以跟踪任务全生命周期。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface TaskFinishedListener extends TaskListener {

    /**
     * 任务执行结束时调用。
     *
     * @param taskId 任务唯一标识
     */
    void onFinished(String taskId);

}
