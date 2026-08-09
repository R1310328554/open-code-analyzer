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
 * 分布式执行器任务失败时的回调监听器。
 * <p>
 * 当远程任务执行抛出异常时触发 {@link #onFailed(String, Throwable)}。
 *
 * @author Nikita Koksharov
 *
 */
@FunctionalInterface
public interface TaskFailureListener extends TaskListener {

    /**
     * 任务执行失败时调用。
     *
     * @param taskId 任务 ID
     * @param exception 执行期间抛出的异常
     */
    void onFailed(String taskId, Throwable exception);

}
