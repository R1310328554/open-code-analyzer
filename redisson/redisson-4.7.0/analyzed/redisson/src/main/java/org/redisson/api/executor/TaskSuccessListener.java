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
 * 分布式任务成功完成时触发的监听器。
 * <p>
 * 仅在任务正常返回结果时回调；若任务抛异常或超时，应通过其他失败监听器处理。
 *
 * @author Nikita Koksharov
 */
@FunctionalInterface
public interface TaskSuccessListener extends TaskListener {

    /**
     * 任务成功完成时调用。
     *
     * @param taskId 任务唯一标识
     * @param result 任务执行返回的结果，类型由提交时的泛型决定
     */
    <T> void onSucceeded(String taskId, T result);

}
