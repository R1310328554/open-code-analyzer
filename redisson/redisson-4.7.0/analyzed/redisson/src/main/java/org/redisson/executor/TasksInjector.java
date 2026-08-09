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
package org.redisson.executor;

/**
 * 远程任务实例的依赖注入扩展点。
 * <p>
 * Worker 端 {@link TasksRunnerService} 在反序列化任务后调用，
 * 默认实现为 {@link SpringTasksInjector}。
 *
 * @author Nikita Koksharov
 *
 */
public interface TasksInjector {

    /** 对反序列化后的任务对象注入外部依赖（如 Spring Bean）。 */
    void inject(Object task);

}
