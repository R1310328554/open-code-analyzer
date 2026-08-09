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
package org.redisson.command;

/**
 * 标记接口，表示实现类支持 Redis 批量/Pipeline 命令聚合。
 * <p>由 {@link CommandBatchService} 实现，供 {@link CommandAsyncExecutor}
 * 在创建批量上下文时识别。
 *
 * @author Nikita Koksharov
 *
 */
public interface BatchService {
}
