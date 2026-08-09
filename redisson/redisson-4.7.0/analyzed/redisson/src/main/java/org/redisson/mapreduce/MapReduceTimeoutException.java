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
package org.redisson.mapreduce;

import org.redisson.client.RedisException;

/**
 * MapReduce 作业整体或某阶段等待超时异常。
 * <p>
 * 继承 {@link org.redisson.client.RedisException}，
 * 由 {@link CoordinatorTask}、{@link SubTasksExecutor} 及
 * {@link MapReduceExecutor} 的超时调度抛出。
 *
 * @author Nikita Koksharov
 *
 */
public class MapReduceTimeoutException extends RedisException {

    private static final long serialVersionUID = -198991995396319360L;

}
