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

import java.util.concurrent.ScheduledFuture;

/**
 * 分布式调度任务的 Future 接口。
 * <p>扩展 {@link RExecutorFuture} 与 {@link java.util.concurrent.ScheduledFuture}，
 * 支持取消、延迟查询及 Redisson 任务监听器。
 * 
 * @author Nikita Koksharov
 *
 * @param <V> 任务返回值类型
 */
public interface RScheduledFuture<V> extends RExecutorFuture<V>, ScheduledFuture<V> {

}
